package cabo.backend.booking.service.impl;

import cabo.backend.booking.dto.*;
import cabo.backend.booking.entity.GPS;
import cabo.backend.booking.entity.GeoPoint;
import cabo.backend.booking.service.BingMapServiceClient;
import cabo.backend.booking.service.BookingService;
import cabo.backend.booking.service.DriverServiceClient;
import cabo.backend.booking.service.TripServiceClient;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.messaging.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BingMapServiceClient bingMapServiceClient;

    @Autowired
    private DriverServiceClient driverServiceClient;

    @Autowired
    private TripServiceClient tripServiceClient;

    private static final String COLLECTION_NAME_CUSTOMER = "customers";

    private final CollectionReference collectionRefCustomer;

    private static final String COLLECTION_NAME_TRIP = "trips";

    private final CollectionReference collectionRefTrip;

    private static final String COLLECTION_NAME_FCMTOKEN = "fcmTokens";

    private final CollectionReference collectionRefFcmToken;

    private static final String COLLECTION_NAME_GPS = "gps";

    private final CollectionReference collectionRefGPS;

    private Firestore dbFirestore;

    public BookingServiceImpl(Firestore firestore) {

        this.dbFirestore = firestore;

        this.collectionRefCustomer = dbFirestore.collection(COLLECTION_NAME_CUSTOMER);

        this.collectionRefFcmToken = dbFirestore.collection(COLLECTION_NAME_FCMTOKEN);

        this.collectionRefGPS = dbFirestore.collection(COLLECTION_NAME_GPS);

        this.collectionRefTrip = dbFirestore.collection(COLLECTION_NAME_TRIP);
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

    @Override
    public void collectGPSFromDriver(String bearerToken, RequestGPS requestGPS) {

        String idToken = bearerToken.substring(7);

        //log.info("idToken -----> " + idToken);

        //FirebaseToken decodedToken = decodeToken(idToken);

        GPS gps = GPS.builder()
                .uid(requestGPS.getUid())
                .currentLocation(requestGPS.getCurrentLocation())
                .build();

        collectionRefGPS.document().set(gps);
    }

    @Override
    public ResponseDriverInformation getDriverInformation(String bearerToken, String customerId, RequestBookADrive requestBooking) {

        String idToken = bearerToken.substring(7);

        log.info("idToken");

        //FirebaseToken decodedToken = decodeToken(idToken);

        ResponseTripId responseTripId = createTrip(customerId, requestBooking);

        log.info("ResponseTripId: " + responseTripId);

        String tripId = responseTripId.getTripId();

        log.info("tripId: " + tripId);

        CompletableFuture<ResponseDriverInformation> future = CompletableFuture.supplyAsync(() -> {
            // Gửi thông báo
//            NotificationDto notificationDto = NotificationDto.builder()
//                    .title("GPS")
//                    .body("GPS")
//                    .build();
//
//            sendNotificationToSuitableDriver(getAllDeviceTokens(), notificationDto);
//
//            try {
//                Thread.sleep(5000); // Tạm dừng thực thi trong 5 giây
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }

            // Tìm ra tài xế phù hợp
            String driverId = searchSuitableDriver(tripId, requestBooking.getCustomerOrderLocation());
            ResponseDriverInformation responseDriverInformation = null;

            if (driverId != null) {
                responseDriverInformation = getDriverInformationFromDB(bearerToken, driverId, tripId);
            }

            // ---

            return responseDriverInformation;
        });

        ResponseDriverInformation responseDriverInformation = future.join();

        log.info("Test: " + responseDriverInformation);

        return responseDriverInformation;
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

    private ResponseDriverInformation getDriverInformationFromDB(String bearerToken, String driverId, String tripId) {

        DriverInfo driverInfo = driverServiceClient.getDriverInfoById(bearerToken, tripId, driverId);

        ResponseDriverInformation responseDriverInformation = ResponseDriverInformation.builder()
                .tripId(tripId)
                .driverInfo(driverInfo)
                .build();

        return  responseDriverInformation;
    }

    private ResponseTripId createTrip(String customerId, RequestBookADrive requestBooking) {

        DocumentReference documentReference = collectionRefCustomer.document(customerId);

        GeoPoint geoPoint =new GeoPoint(0.0, 0.0);

        log.info("documentReference: " + documentReference);

        CreateTripDto createTripDto = CreateTripDto.builder()
                .cost(requestBooking.getCost())
                .customerId(documentReference)
                .driverId(null)
                .distance(requestBooking.getDistance())
                .startTime(0)
                .pickUpTime(0)
                .endTime(0)
                .customerOrderLocation(requestBooking.getCustomerOrderLocation())
                .driverStartLocation(geoPoint)
                .toLocation(requestBooking.getToLocation())
                .paymentType(requestBooking.getPaymentType())
                .build();

        log.info("test: ");

        ResponseTripId responseTripId = tripServiceClient.createTrip(createTripDto);

        log.info("responseTripId: " + responseTripId);

        return responseTripId;
    }

    private String searchSuitableDriver(String tripId, GeoPoint customerOrderLocation) {

        NotificationDto notificationDto = NotificationDto.builder()
                .title("BOOKING_OPEN")
                .body("BOOKING_OPEN")
                .build();

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {

            List<String> suitableDriverUid;
            List<String> listOfFcmTokens;

            double x = 0.0;

            for (int i = 0; i < 2; i++) {

                suitableDriverUid = getListOfSuitableDriverUid(customerOrderLocation, x, x + 2.0);

                listOfFcmTokens = getListOfFcmToken(suitableDriverUid);

                //sendNotificationToSuitableDriver(listOfFcmTokens, notificationDto);

                long endTime = System.currentTimeMillis() + 5000;

                while (System.currentTimeMillis() < endTime) {

                    String driverId = tripServiceClient.getDriverIdByTripId(tripId);

                    if (driverId != null) {
                        return driverId;
                    }
                }

                x += 2;
            }

            return null;
        });

        return future.join();
    }

    private void sendNotificationToSuitableDriver(List<String> listOfFcmTokens, NotificationDto notificationDto) {

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(notificationDto.getTitle())
                        .setBody(notificationDto.getBody())
                        .build())
                .addAllTokens(listOfFcmTokens)
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

    private List<String> getListOfSuitableDriverUid(GeoPoint customerOrderLocation, double from, double to) {

        List<String> suitableDriverUid = new ArrayList<>();

        ApiFuture<QuerySnapshot> future = collectionRefGPS.get();

        List<QueryDocumentSnapshot> documents = null;

        try {
            documents = future.get().getDocuments();

            for (QueryDocumentSnapshot document : documents) {

                com.google.cloud.firestore.GeoPoint currentLocation = document.getGeoPoint("currentLocation");

                if (currentLocation != null) {
                    double distance = bingMapServiceClient.calculateDistance(customerOrderLocation.getLatitude(), customerOrderLocation.getLongitude(),
                            currentLocation.getLatitude(), currentLocation.getLongitude());

                    if (distance >= from && distance <= to) {
                        String uid = document.getString("uid");

                        suitableDriverUid.add(uid);
                    }
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return suitableDriverUid;
    }

    private List<String> getListOfFcmToken(List<String> suitableDriverUid) {

        List<String> listOfFcmTokens = new ArrayList<>();

        if (suitableDriverUid.size() > 0) {

            for (String uid : suitableDriverUid) {
                Query query = collectionRefFcmToken.whereEqualTo("uid", uid);

                try {
                    QuerySnapshot querySnapshot = query.get().get();

                    List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

                    String fcmToken = documents.get(0).getString("fcmToken");

                    listOfFcmTokens.add(fcmToken);

                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return listOfFcmTokens;
    }
}
