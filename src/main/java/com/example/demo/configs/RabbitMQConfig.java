package com.example.demo.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PRODUCT_EXCHANGE = "product.exchange";
    public static final String PRODUCT_QUEUE = "product.queue";
    public static final String PRODUCT_CREATED_ROUTING_KEY = "product.created";
    public static final String PRODUCT_UPDATED_ROUTING_KEY = "product.updated";
    public static final String PRODUCT_DELETED_ROUTING_KEY = "product.deleted";

    @Bean
    public TopicExchange productExchange() {
        return new TopicExchange(PRODUCT_EXCHANGE);
    }

    @Bean
    public Queue productQueue() {
        return QueueBuilder.durable(PRODUCT_QUEUE)
                .withArgument("x-dead-letter-exchange", "product.dlx")
                .build();
    }

    @Bean
    public Binding productCreatedBinding(Queue productQueue, TopicExchange productExchange) {
        return BindingBuilder
                .bind(productQueue)
                .to(productExchange)
                .with(PRODUCT_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding productUpdatedBinding(Queue productQueue, TopicExchange productExchange) {
        return BindingBuilder
                .bind(productQueue)
                .to(productExchange)
                .with(PRODUCT_UPDATED_ROUTING_KEY);
    }

    @Bean
    public Binding productDeletedBinding(Queue productQueue, TopicExchange productExchange) {
        return BindingBuilder
                .bind(productQueue)
                .to(productExchange)
                .with(PRODUCT_DELETED_ROUTING_KEY);
    }

    /**
     * Configure ObjectMapper for Jackson to handle BigDecimal, LocalDateTime, and Enums
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Register JavaTimeModule for LocalDateTime support
        mapper.registerModule(new JavaTimeModule());

        // Don't write dates as timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Handle BigDecimal as plain numbers (not scientific notation)
        mapper.enable(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN);

        // Ignore null values
        mapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);

        return mapper;
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);

        // Enable publisher confirms
        template.setMandatory(true);

        return template;
    }
}