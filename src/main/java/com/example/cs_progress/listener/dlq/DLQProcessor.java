package com.example.cs_progress.listener.dlq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class DLQProcessor {

    @RabbitListener(queues = "test.item.resolved.dlq") // Точное имя!
    public void handleTestItemResolvedDLQ(Message message) {
        processDLQMessage(message, "test.item.resolved");
    }

    @RabbitListener(queues = "lesson.viewed.dlq") // Точное имя!
    public void handleLessonViewedDLQ(Message message) {
        processDLQMessage(message, "lesson.viewed");
    }

    private void processDLQMessage(Message message, String queueName) {
        try {
            Map<String, Object> headers = message.getMessageProperties().getHeaders();

            log.error("""
                ==========================================
                🚨 DLQ ALERT: Message in {} DLQ 🚨
                ==========================================
                Original Queue: {}
                Death Reason: {}
                Death Count: {}
                Exception Message: {}
                Stack Trace: {}
                Message Body: {}
                ==========================================
                """,
                    queueName,
                    headers.getOrDefault("x-first-death-queue", "unknown"),
                    headers.getOrDefault("x-first-death-reason", "unknown"),
                    getDeathCount(headers),
                    headers.get("x-exception-message"),
                    headers.get("x-exception-stacktrace"),
                    new String(message.getBody())
            );

        } catch (Exception e) {
            log.error("Failed to process DLQ message for {}", queueName, e);
        }
    }

    private int getDeathCount(Map<String, Object> headers) {
        if (headers.containsKey("x-death")) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> death = (Map<String, Object>)
                        ((java.util.List<?>) headers.get("x-death")).get(0);
                return ((Number) death.get("count")).intValue();
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }
}
