package com.example.cs_progress.service

import com.example.cs_common.exception.AIInsightGenerationException
import com.example.cs_progress.model.PromptData
import com.example.cs_progress.service.impl.AIInsightGeneratorServiceImpl
import org.springframework.ai.chat.client.ChatClient
import spock.lang.Specification
import spock.lang.Subject

class AIInsightGeneratorServiceImplSpec extends Specification {

    PromptDataCollectorService promptDataCollector = Mock()
    PromptGeneratorService promptGenerator = Mock()
    ChatClient chatClient = Mock()
    ChatClient.ChatClientRequestSpec requestSpec = Mock()
    ChatClient.CallResponseSpec callResponseSpec = Mock()

    @Subject
    AIInsightGeneratorServiceImpl service = new AIInsightGeneratorServiceImpl(promptDataCollector, promptGenerator, chatClient)

    def "generate throws NullPointerException for null userId without calling collaborators"() {
        when:
        service.generate(null)

        then:
        thrown(NullPointerException)
        0 * promptDataCollector.collectData(_)
        0 * promptGenerator.generatePrompt(_)
        0 * chatClient.prompt()
    }

    def "generate parses a valid AI JSON response into AIInsightResponse"() {
        given:
        def promptData = PromptData.builder().userId("user-1").build()
        def aiResponse = '''
            {
              "summary": "Great progress overall",
              "strengths": ["Loops", "Arrays"],
              "recommendations": [
                {"title": "Practice OOP", "description": "Work on inheritance", "priority": "high"}
              ]
            }
            '''

        promptDataCollector.collectData("user-1") >> promptData
        promptGenerator.generatePrompt(promptData) >> "generated prompt"
        chatClient.prompt() >> requestSpec
        requestSpec.user("generated prompt") >> requestSpec
        requestSpec.call() >> callResponseSpec
        callResponseSpec.content() >> aiResponse

        when:
        def result = service.generate("user-1")

        then:
        result.summary == "Great progress overall"
        result.strengths == ["Loops", "Arrays"]
        result.recommendations.size() == 1
        result.recommendations[0].title == "Practice OOP"
        result.recommendations[0].description == "Work on inheritance"
        result.recommendations[0].priority == "high"
        result.generatedAt != null
    }

    def "generate strips markdown code fences before parsing"() {
        given:
        def promptData = PromptData.builder().userId("user-1").build()
        def aiResponse = '''```json
            {"summary": "Solid work", "strengths": [], "recommendations": []}
            ```'''

        promptDataCollector.collectData("user-1") >> promptData
        promptGenerator.generatePrompt(promptData) >> "generated prompt"
        chatClient.prompt() >> requestSpec
        requestSpec.user(_ as String) >> requestSpec
        requestSpec.call() >> callResponseSpec
        callResponseSpec.content() >> aiResponse

        when:
        def result = service.generate("user-1")

        then:
        result.summary == "Solid work"
        result.strengths == []
        result.recommendations == []
    }

    def "generate strips plain code fences without json marker before parsing"() {
        given:
        def promptData = PromptData.builder().userId("user-1").build()
        def aiResponse = '''```
            {"summary": "Plain fence", "strengths": [], "recommendations": []}
            ```'''

        promptDataCollector.collectData("user-1") >> promptData
        promptGenerator.generatePrompt(promptData) >> "generated prompt"
        chatClient.prompt() >> requestSpec
        requestSpec.user(_ as String) >> requestSpec
        requestSpec.call() >> callResponseSpec
        callResponseSpec.content() >> aiResponse

        when:
        def result = service.generate("user-1")

        then:
        result.summary == "Plain fence"
        result.strengths == []
        result.recommendations == []
    }

    def "generate returns empty lists when strengths and recommendations are missing"() {
        given:
        def promptData = PromptData.builder().userId("user-1").build()
        def aiResponse = '{"summary": "No data yet"}'

        promptDataCollector.collectData("user-1") >> promptData
        promptGenerator.generatePrompt(promptData) >> "generated prompt"
        chatClient.prompt() >> requestSpec
        requestSpec.user(_ as String) >> requestSpec
        requestSpec.call() >> callResponseSpec
        callResponseSpec.content() >> aiResponse

        when:
        def result = service.generate("user-1")

        then:
        result.summary == "No data yet"
        result.strengths == []
        result.recommendations == []
    }

    def "generate wraps invalid JSON response in AIInsightGenerationException"() {
        given:
        def promptData = PromptData.builder().userId("user-1").build()

        promptDataCollector.collectData("user-1") >> promptData
        promptGenerator.generatePrompt(promptData) >> "generated prompt"
        chatClient.prompt() >> requestSpec
        requestSpec.user(_ as String) >> requestSpec
        requestSpec.call() >> callResponseSpec
        callResponseSpec.content() >> "not a json"

        when:
        service.generate("user-1")

        then:
        def ex = thrown(AIInsightGenerationException)
        ex.message == "Failed to generate AI insight"
    }

    def "generate wraps failure from PromptDataCollectorService in AIInsightGenerationException"() {
        given:
        promptDataCollector.collectData("user-1") >> { throw new RuntimeException("data collection failed") }

        when:
        service.generate("user-1")

        then:
        def ex = thrown(AIInsightGenerationException)
        ex.message == "Failed to generate AI insight"
        ex.cause.message == "data collection failed"
    }

    def "generate wraps failure from ChatClient call in AIInsightGenerationException"() {
        given:
        def promptData = PromptData.builder().userId("user-1").build()

        promptDataCollector.collectData("user-1") >> promptData
        promptGenerator.generatePrompt(promptData) >> "generated prompt"
        chatClient.prompt() >> requestSpec
        requestSpec.user(_ as String) >> requestSpec
        requestSpec.call() >> { throw new RuntimeException("AI service unavailable") }

        when:
        service.generate("user-1")

        then:
        def ex = thrown(AIInsightGenerationException)
        ex.message == "Failed to generate AI insight"
        ex.cause.message == "AI service unavailable"
    }

    def "generate passes collected data to prompt generator and generated prompt to ChatClient"() {
        given:
        def promptData = PromptData.builder().userId("user-1").build()
        def aiResponse = '{"summary": "ok", "strengths": [], "recommendations": []}'

        chatClient.prompt() >> requestSpec
        requestSpec.call() >> callResponseSpec
        callResponseSpec.content() >> aiResponse

        when:
        service.generate("user-1")

        then:
        1 * promptDataCollector.collectData("user-1") >> promptData
        1 * promptGenerator.generatePrompt(promptData) >> "specific prompt"
        1 * requestSpec.user("specific prompt") >> requestSpec
    }
}