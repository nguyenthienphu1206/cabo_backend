package cabo.backend.receiverservice.publisher;

import cabo.backend.receiverservice.dto.RequestBookADrive;
import cabo.backend.receiverservice.dto.RequestBookADriveEvent;
import cabo.backend.receiverservice.entity.Customer;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class ReceiverProducer {

    @Value("${rabbitmq.exchange.location.name}")
    private String locationExchange;

    @Value("${rabbitmq.exchange.status.name}")
    private String statusExchange;

    @Value("${rabbitmq.binding.location.routing.key}")
    private String locationRoutingKey;

    @Value("${rabbitmq.binding.status.routing.key}")
    private String statusRoutingKey;

    private final RabbitTemplate rabbitTemplate;

    private static final String COLLECTION_NAME_CUSTOMER = "customers";

    private final CollectionReference collectionRefCustomer;

    private final Firestore dbFirestore;

    private ReceiverProducer(RabbitTemplate rabbitTemplate, Firestore dbFirestore) {
        this.rabbitTemplate = rabbitTemplate;
        this.dbFirestore = dbFirestore;
        collectionRefCustomer = this.dbFirestore.collection(COLLECTION_NAME_CUSTOMER);
    }

    public void sendMessage(RequestBookADriveEvent requestBookADriveEvent) {
        log.info(String.format("Location event sent to RabbitMQ => %s", requestBookADriveEvent.toString()));

        rabbitTemplate.convertAndSend(locationExchange, locationRoutingKey, requestBookADriveEvent);
    }

    public void sendInforToQueue(RequestBookADrive requestBookADrive) {

        createCustomerIfPhoneNumberNotRegistered(requestBookADrive.getCustomerPhoneNumber());

        // Booking Drive


        // Send to status queue
        //rabbitTemplate.convertAndSend(statusExchange, statusRoutingKey, requestBookADrive);
    }

    private void createCustomerIfPhoneNumberNotRegistered(String phoneNumber) {
        Query query = collectionRefCustomer.whereEqualTo("phoneNumber", phoneNumber);
        ApiFuture<QuerySnapshot> querySnapshotFuture = query.get();

        try {
            QuerySnapshot querySnapshot = querySnapshotFuture.get();

            if (querySnapshot.isEmpty()) {

                Customer customer = Customer.builder()
                        .uid("")
                        .fullName("")
                        .phoneNumber(phoneNumber)
                        .avatar("")
                        .vip(false)
                        .isRegisteredOnApp(false)
                        .build();

                DocumentReference documentReference = collectionRefCustomer.document();

                documentReference.set(customer);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
