package cabo.backend.booking.config;

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

    @Value("${rabbitmq.queue.status_customer.name}")
    private String statusCustomerQueue;

    @Value("${rabbitmq.exchange.status.name}")
    private String statusExchange;

    @Value("${rabbitmq.binding.status.routing.key}")
    private String statusRoutingKey;

    @Value("${rabbitmq.binding.status_customer.routing.key}")
    private String statusCustomerRoutingKey;

    // spring bean for queue
    @Bean
    public Queue statusQueue() {
        return new Queue(statusQueue);
    }

    @Bean
    public Queue statusCustomerQueue() {
        return new Queue(statusCustomerQueue);
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
    public Binding statusCustomerBinding() {
        return BindingBuilder
                .bind(statusCustomerQueue())
                .to(statusExchange())
                .with(statusCustomerRoutingKey);
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
