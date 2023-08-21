package cabo.backend.taxistatusservice.consumer;

import cabo.backend.taxistatusservice.dto.DriveStatus;
import cabo.backend.taxistatusservice.dto.NotificationDto;
import cabo.backend.taxistatusservice.dto.TripDto;
import cabo.backend.taxistatusservice.service.TripServiceClient;
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

        String bearerToken = driveStatus.getBearerToken();

        log.info("Status");

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        // Cập nhật trạng thái chuyến đi
        TripDto tripDto = tripServiceClient.updateTripStatus(bearerToken, driveStatus.getTripId(), driveStatus.getStatus());

        // Tạo notification
        NotificationDto notificationDto = NotificationDto.builder()
                .title("UPDATE_DRIVE_STATE")
                .body("UPDATE_DRIVE_STATE")
                .build();

        // Tạo data từ chuyến đi
        Map<String, String> data = createDataFromTrip(tripDto, driveStatus.getTripId());

        // Gửi notification về phía tổng đài
        if (!driveStatus.getFcmToken().equals("")) {
            sendNotification(notificationDto, driveStatus.getFcmToken(), data);
        }
    }

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

        data.put("tripId", tripId);
        data.put("tripInfo", tripDto.toString());

        return data;
    }
}
