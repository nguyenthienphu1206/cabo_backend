package cabo.backend.booking.service.impl;

import cabo.backend.booking.dto.NotificationDto;
import cabo.backend.booking.service.BookingService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.messaging.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    private static final String COLLECTION_NAME_FCMTOKEN = "fcmTokens";

    private final CollectionReference collectionRefFcmToken;

    private Firestore dbFirestore;

    public BookingServiceImpl(Firestore firestore) {

        this.dbFirestore = firestore;

        this.collectionRefFcmToken = dbFirestore.collection(COLLECTION_NAME_FCMTOKEN);
    }

    @Override
    public void sendNotificationToAllDevices(NotificationDto notificationDto) {

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(notificationDto.getTitle())
                        .setBody(notificationDto.getBody())
                        .build())
                .addAllTokens(getAllDeviceTokens())// Lấy danh sách các token của tất cả mobile client
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);

            List<SendResponse> successResponses = response.getResponses();

            log.info("---> successResponses: " + successResponses);
            // Xử lý kết quả (nếu cần)
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException(e);
        }
    }

    // Phương thức này để lấy danh sách các token của tất cả mobile client
    private List<String> getAllDeviceTokens() {

        List<String> fcmTokens = new ArrayList<>();

        // Lấy tất cả các tài liệu trong collection
        ApiFuture<QuerySnapshot> future = collectionRefFcmToken.get();

        List<QueryDocumentSnapshot> documents = null;

        try {
            documents = future.get().getDocuments();

            for (QueryDocumentSnapshot document : documents) {

                String fcmToken = document.getString("fcmToken");

                fcmTokens.add(fcmToken);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        log.info("FcmTokens: " + fcmTokens);

        return fcmTokens;
    }
}
