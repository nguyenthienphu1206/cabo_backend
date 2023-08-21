package cabo.backend.receiverservice.publisher;

import cabo.backend.receiverservice.dto.RequestBookADrive;
import cabo.backend.receiverservice.dto.RequestBookADriveEvent;
import cabo.backend.receiverservice.service.CustomerServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ReceiverProducer {

    @Value("${rabbitmq.exchange.booking.name}")
    private String bookingExchange;

    @Value("${rabbitmq.binding.booking.routing.key}")
    private String bookingRoutingKey;

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    private CustomerServiceClient customerServiceClient;

    private ReceiverProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(RequestBookADriveEvent requestBookADriveEvent) {
        log.info(String.format("Location event sent to RabbitMQ => %s", requestBookADriveEvent.toString()));

        //rabbitTemplate.convertAndSend(locationExchange, locationRoutingKey, requestBookADriveEvent);
    }

    public void sendInforToBookingQueue(String bearerToken, RequestBookADrive requestBookADrive) {

        String customerId = customerServiceClient.createCustomerIfPhoneNumberNotRegistered(bearerToken, requestBookADrive.getCustomerPhoneNumber());
        log.info("customerId: " + customerId);

        RequestBookADriveEvent requestBookADriveEvent = RequestBookADriveEvent.builder()
                .bearerToken(bearerToken)
                .customerId(customerId)
                .requestBookADrive(requestBookADrive)
                .build();

        // Booking Drive
        rabbitTemplate.convertAndSend(bookingExchange, bookingRoutingKey, requestBookADriveEvent);
    }

}
