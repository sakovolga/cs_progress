package com.example.cs_progress.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PROGRESS_EXCHANGE = "progress.exchange";

    // Lesson viewed
    public static final String LESSON_VIEWED_QUEUE = "progress.lesson.viewed";
    public static final String LESSON_VIEWED_ROUTING_KEY = "lesson.viewed";

    // Test item resolved
    public static final String TEST_ITEM_RESOLVED_QUEUE = "progress.test.item.resolved";
    public static final String TEST_ITEM_RESOLVED_ROUTING_KEY = "test.item.resolved";

    // DLX (оставляем закомментированным)
//    public static final String DLX_EXCHANGE = "dlx.exchange";
//    public static final String DLX_QUEUE = "dlx.progress";

    @Bean
    public TopicExchange progressExchange() {
        return new TopicExchange(PROGRESS_EXCHANGE, true, false);
    }

    // --------------------------
    // LESSON VIEWED
    // --------------------------

    @Bean
    public Queue lessonViewedQueue() {
        return QueueBuilder
                .durable(LESSON_VIEWED_QUEUE)
//                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
//                .withArgument("x-dead-letter-routing-key", "dlx.lesson.viewed")
                .build();
    }

    @Bean
    public Binding lessonViewedBinding(Queue lessonViewedQueue, TopicExchange progressExchange) {
        return BindingBuilder
                .bind(lessonViewedQueue)
                .to(progressExchange)
                .with(LESSON_VIEWED_ROUTING_KEY);
    }

    // --------------------------
    // TEST ITEM RESOLVED (NEW)
    // --------------------------

    @Bean
    public Queue testItemResolvedQueue() {
        return QueueBuilder
                .durable(TEST_ITEM_RESOLVED_QUEUE)
//                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
//                .withArgument("x-dead-letter-routing-key", "dlx.test.item.resolved")
                .build();
    }

    @Bean
    public Binding testItemResolvedBinding(Queue testItemResolvedQueue, TopicExchange progressExchange) {
        return BindingBuilder
                .bind(testItemResolvedQueue)
                .to(progressExchange)
                .with(TEST_ITEM_RESOLVED_ROUTING_KEY);
    }

    // --------------------------
    // Message Converter & Listener Factory
    // --------------------------

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(10);
        return factory;
    }
}
