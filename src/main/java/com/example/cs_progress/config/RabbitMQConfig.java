package com.example.cs_progress.config;

import com.example.cs_common.config.RabbitMQCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Slf4j
@Configuration
@Import(RabbitMQCommonConfig.class)
public class RabbitMQConfig {

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            RetryOperationsInterceptor retryInterceptor,
            MessageConverter messageConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(retryInterceptor);

        // 3. КРИТИЧЕСКИ ВАЖНО: установите MessageConverter!
        factory.setMessageConverter(messageConverter);

        log.info("RabbitListenerContainerFactory configured with MessageConverter: {}",
                messageConverter.getClass().getSimpleName());

        return factory;
    }

    @Bean
    public RetryOperationsInterceptor retryOperationsInterceptor(RabbitTemplate rabbitTemplate) {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(2000, 2.0, 10000)
                .recoverer(new MessageRecoverer() {
                    @Override
                    public void recover(Message message, Throwable cause) {
                        // Определяем правильную DLQ
                        String receivedRoutingKey = message.getMessageProperties().getReceivedRoutingKey();
                        String dlqRoutingKey = "test.item.resolved.dlq"; // по умолчанию

                        if ("test.item.resolved".equals(receivedRoutingKey)) {
                            dlqRoutingKey = "test.item.resolved.dlq";
                        } else if ("lesson.viewed".equals(receivedRoutingKey)) {
                            dlqRoutingKey = "lesson.viewed.dlq";
                        } else if ("progress".equals(receivedRoutingKey)) {
                            dlqRoutingKey = "progress.dlq";
                        }

                        log.warn("Republishing to {} (original routing key: {})",
                                dlqRoutingKey, receivedRoutingKey);

                        rabbitTemplate.send(
                                com.example.cs_common.constants.RabbitMQConstants.DLX_EXCHANGE,
                                dlqRoutingKey,
                                message
                        );
                    }
                })
                .build();
    }
}