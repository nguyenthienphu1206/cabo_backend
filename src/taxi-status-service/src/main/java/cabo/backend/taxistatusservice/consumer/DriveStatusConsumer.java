package cabo.backend.taxistatusservice.consumer;

import cabo.backend.taxistatusservice.dto.*;
import cabo.backend.taxistatusservice.service.TripServiceClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class DriveStatusConsumer {

    @Autowired
    private TripServiceClient tripServiceClient;

    @RabbitListener(queues = "${rabbitmq.queue.status.name}")
    public void consumeDriveStatus(DriveStatus driveStatus) {

        log.info("Status");

        // Tạo notification
        NotificationDto notificationDto = new NotificationDto("", "");

        // Tạo data từ chuyến đi
        Map<String, String> data = createDataFromTrip(driveStatus.getTripDto(), driveStatus.getTripId());

        // Gửi notification về phía tổng đài
        if (!driveStatus.getFcmToken().equals("")) {
            sendNotification(notificationDto, driveStatus.getFcmToken(), data);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.status_customer.name}")
    public void consumeDriveStatusToCustomer(TravelInfoToCustomer travelInfoToCustomer) {

        // Tạo notification
        NotificationDto notificationDto = new NotificationDto("", "");

        // Lấy data
        Map<String, String> data = createDataFromDistanceAndTime(travelInfoToCustomer);

        log.info("FCmtoken: " + travelInfoToCustomer.getFcmToken());

        // Gửi notification về phía customer
        if (!travelInfoToCustomer.getFcmToken().equals("") && travelInfoToCustomer.getFcmToken() != null) {
            sendNotification(notificationDto, travelInfoToCustomer.getFcmToken(), data);
        }
    }

//    @RabbitListener(queues = "${rabbitmq.queue.status_done.name}")
//    public void consumeDriveStatusDoneToCustomer(NotificationDriveDone notificationDriveDone) {
//
//        // Tạo notification
//        NotificationDto notificationDto = NotificationDto.builder()
//                .title("Your trip has ended")
//                .body("Thank you for trusting us.")
//                .build();
//
//        // Lấy data
//        Map<String, String> data = createDataStatusDone(notificationDriveDone);
//
//        // Gửi notification về phía tổng đài
//        if (!notificationDriveDone.getFcmToken().equals("")) {
//            sendNotification(notificationDto, notificationDriveDone.getFcmToken(), data);
//        }
//    }

    private FirebaseToken decodeToken(String idToken) {

        FirebaseToken decodedToken;

        try {
            decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }

        return decodedToken;
    }

    private void sendNotification(NotificationDto notificationDto, String fcmToken, Map<String, String> data) {

        Message message = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(notificationDto.getTitle())
                        .setBody(notificationDto.getBody())
                        .build())
                .setToken(fcmToken)
                .putAllData(data)
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);

            // Xử lý kết quả (nếu cần)
            log.info("Send notification: " + response);
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> createDataFromTrip(TripDto tripDto, String tripId) {

        Map<String, String> data = new HashMap<>();

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonData = null;
        try {
            jsonData = objectMapper.writeValueAsString(tripDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        data.put("category", "UPDATE_DRIVE_STATE");
        data.put("tripId", tripId);
        data.put("tripInfo", jsonData);

        return data;
    }

    private Map<String, String> createDataFromDistanceAndTime(TravelInfoToCustomer travelInfoToCustomer) {

        Map<String, String> data = new HashMap<>();

        data.put("category", "UPDATE_DRIVER_DISTANCE_AND_TIME");
        data.put("driverRemainingDistance", travelInfoToCustomer.getDriverRemainingDistance());
        data.put("driverRemainingTime", travelInfoToCustomer.getDriverRemainingTime());

        return data;
    }

    private Map<String, String> createDataStatusDone(NotificationDriveDone notificationDriveDone) {

        Map<String, String> data = new HashMap<>();

        data.put("category", "INFORM_TRIP_DONE");
        data.put("tripId", notificationDriveDone.getTripId());

        return data;
    }
}
