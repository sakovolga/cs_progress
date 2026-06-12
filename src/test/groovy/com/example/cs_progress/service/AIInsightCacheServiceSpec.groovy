package com.example.cs_progress.service

import com.example.cs_common.dto.analitics.AIInsightResponse
import com.example.cs_progress.service.impl.AIInsightCacheServiceImpl
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

class AIInsightCacheServiceSpec extends Specification {

    AIInsightGeneratorService generatorService = Mock()
    CacheManager cacheManager = Mock()
    Cache cache = Mock()

    @Subject
    AIInsightCacheServiceImpl service = new AIInsightCacheServiceImpl(generatorService, cacheManager)

    // ── getCachedInsight ──────────────────────────────────────────────────────

    def "getCachedInsight throws NullPointerException for null userId"() {
        when:
        service.getCachedInsight(null)

        then:
        thrown(NullPointerException)
        0 * cacheManager.getCache(_)
    }

    def "getCachedInsight returns null when cache is not registered in CacheManager"() {
        given:
        cacheManager.getCache("ai-insights") >> null

        when:
        def result = service.getCachedInsight("user-1")

        then:
        result == null
    }

    def "getCachedInsight returns null on cache miss"() {
        given:
        cacheManager.getCache("ai-insights") >> cache
        cache.get("user-1") >> null

        when:
        def result = service.getCachedInsight("user-1")

        then:
        result == null
    }

    def "getCachedInsight returns cached AIInsightResponse on cache hit"() {
        given:
        def insight = AIInsightResponse.builder()
                .summary("Great progress")
                .strengths(["Arrays", "Loops"])
                .recommendations([])
                .generatedAt(LocalDateTime.now())
                .build()

        def wrapper = Mock(Cache.ValueWrapper)
        wrapper.get() >> insight

        cacheManager.getCache("ai-insights") >> cache
        cache.get("user-1") >> wrapper

        when:
        def result = service.getCachedInsight("user-1")

        then:
        result == insight
    }

    // ── generateAndCache ──────────────────────────────────────────────────────

    def "generateAndCache throws NullPointerException for null userId"() {
        when:
        service.generateAndCache(null)

        then:
        thrown(NullPointerException)
        0 * generatorService.generate(_)
    }

    def "generateAndCache delegates to generator service and returns its result"() {
        given:
        def insight = AIInsightResponse.builder()
                .summary("Good work")
                .strengths(["Sorting"])
                .recommendations([])
                .generatedAt(LocalDateTime.now())
                .build()

        when:
        def result = service.generateAndCache("user-1")

        then:
        1 * generatorService.generate("user-1") >> insight
        result == insight
    }

    // ── evictInsight ──────────────────────────────────────────────────────────

    def "evictInsight throws NullPointerException for null userId"() {
        when:
        service.evictInsight(null)

        then:
        thrown(NullPointerException)
    }

    def "evictInsight completes without error"() {
        when:
        service.evictInsight("user-1")

        then:
        noExceptionThrown()
    }
}