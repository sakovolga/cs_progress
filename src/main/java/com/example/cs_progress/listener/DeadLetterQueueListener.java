//package com.example.cs_progress.listener;
//
//import com.example.cs_common.dto.event.LessonViewedEvent;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//public class DeadLetterQueueListener {
//
//    /**
//     * Обработка сообщений, которые не удалось обработать после всех retry
//     */
//    @RabbitListener(queues = "${rabbitmq.queue.dlx:dlx.progress.lesson.viewed}")
//    public void handleDeadLetterMessage(@Payload LessonViewedEvent event) {
//        log.error("Message moved to DLQ: userId={}, courseId={}, topicId={}",
//                event.getLastTopicId().getUserId(), event.getLastTopicId().getCourseId(), event.getTopicId());
//
//        // Здесь можно:
//        // 1. Отправить алерт в систему мониторинга
//        // 2. Сохранить в отдельную таблицу для ручной обработки
//        // 3. Отправить уведомление администратору
//
//        // Пример: сохранение в таблицу failed_events
//        // failedEventRepository.save(event);
//    }
//}
