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

    @Value("${rabbitmq.queue.status_customer.name}")
    private String statusCustomerQueue;

    @Value("${rabbitmq.queue.gps.name}")
    private String gpsQueue;

    @Value("${rabbitmq.exchange.status.name}")
    private String statusExchange;

    @Value("${rabbitmq.exchange.gps.name}")
    private String gpsExchange;

    @Value("${rabbitmq.binding.status_customer.routing.key}")
    private String statusCustomerRoutingKey;

    @Value("${rabbitmq.binding.gps.routing.key}")
    private String gpsRoutingKey;

    // spring bean for queue
    @Bean
    public Queue statusCustomerQueue() {
        return new Queue(statusCustomerQueue);
    }

    @Bean
    public Queue gpsQueue() {
        return new Queue(gpsQueue);
    }

    // spring bean for exchange
    @Bean
    public TopicExchange statusExchange() {
        return new TopicExchange(statusExchange);
    }

    @Bean
    public TopicExchange gpsExchange() {
        return new TopicExchange(gpsExchange);
    }

    // spring bean for binding between exchange and queue using routing key

    @Bean
    public Binding statusCustomerBinding() {
        return BindingBuilder
                .bind(statusCustomerQueue())
                .to(statusExchange())
                .with(statusCustomerRoutingKey);
    }

    @Bean
    public Binding gpsBinding() {
        return BindingBuilder
                .bind(gpsQueue())
                .to(gpsExchange())
                .with(gpsRoutingKey);
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
