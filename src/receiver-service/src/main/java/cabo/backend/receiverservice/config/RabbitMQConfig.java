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

    @Value("${rabbitmq.queue.booking.name}")
    private String bookingQueue;

    @Value("${rabbitmq.exchange.booking.name}")
    private String bookingExchange;

    @Value("${rabbitmq.binding.booking.routing.key}")
    private String bookingRoutingKey;

    // spring bean for queue
    @Bean
    public Queue bookingQueue() {
        return new Queue(bookingQueue);
    }

    // spring bean for exchange
    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(bookingExchange);
    }

    // spring bean for binding between exchange and queue using routing key
    @Bean
    public Binding locationBinding() {
        return BindingBuilder
                .bind(bookingQueue())
                .to(bookingExchange())
                .with(bookingRoutingKey);
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
