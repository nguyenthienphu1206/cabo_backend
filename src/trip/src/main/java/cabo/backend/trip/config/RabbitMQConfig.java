package cabo.backend.trip.config;

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

    @Value("${rabbitmq.queue.status.name}")
    private String statusQueue;
    @Value("${rabbitmq.queue.status_done.name}")
    private String statusDoneQueue;

    @Value("${rabbitmq.exchange.status.name}")
    private String statusExchange;

    @Value("${rabbitmq.binding.status.routing.key}")
    private String statusRoutingKey;

    @Value("${rabbitmq.binding.status_done.routing.key}")
    private String statusDoneRoutingKey;

    // spring bean for queue
    @Bean
    public Queue statusQueue() {
        return new Queue(statusQueue);
    }

    @Bean
    public Queue statusDoneQueue() {
        return new Queue(statusDoneQueue);
    }

    // spring bean for exchange
    @Bean
    public TopicExchange statusExchange() {
        return new TopicExchange(statusExchange);
    }

    // spring bean for binding between exchange and queue using routing key
    @Bean
    public Binding statusBinding() {
        return BindingBuilder
                .bind(statusQueue())
                .to(statusExchange())
                .with(statusRoutingKey);
    }

    @Bean
    public Binding statusDoneBinding() {
        return BindingBuilder
                .bind(statusDoneQueue())
                .to(statusExchange())
                .with(statusDoneRoutingKey);
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
