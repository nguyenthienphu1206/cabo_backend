package cabo.backend.receiverservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.location.name}")
    private String locationQueue;

    @Value("${rabbitmq.queue.status.name}")
    private String statusQueue;

    @Value("${rabbitmq.exchange.location.name}")
    private String locationExchange;

    @Value("${rabbitmq.exchange.status.name}")
    private String statusExchange;

    @Value("${rabbitmq.binding.location.routing.key}")
    private String locationRoutingKey;

    @Value("${rabbitmq.binding.status.routing.key}")
    private String statusRoutingKey;

    // spring bean for queue
    @Bean
    public Queue locationQueue() {
        return new Queue(locationQueue);
    }

    @Bean
    public Queue statusQueue() {
        return new Queue(statusQueue);
    }

    // spring bean for exchange
    @Bean
    public TopicExchange locationExchange() {
        return new TopicExchange(locationExchange);
    }

    @Bean
    public TopicExchange statusExchange() {
        return new TopicExchange(statusExchange);
    }

    // spring bean for binding between exchange and queue using routing key
    @Bean
    public Binding locationBinding() {
        return BindingBuilder
                .bind(locationQueue())
                .to(locationExchange())
                .with(locationRoutingKey);
    }

    @Bean
    public Binding statusBinding() {
        return BindingBuilder
                .bind(statusQueue())
                .to(statusExchange())
                .with(statusRoutingKey);
    }

    // message converter
    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    // configure RabbitTemplate
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}
