package cabo.backend.booking.service.impl;

import cabo.backend.booking.dto.*;
import cabo.backend.booking.entity.GPS;
import cabo.backend.booking.entity.GeoPoint;
import cabo.backend.booking.service.*;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    @Autowired
    private CustomerServiceClient customerServiceClient;

    @Autowired
    private BingMapServiceClient bingMapServiceClient;

    @Autowired
    private DriverServiceClient driverServiceClient;

    @Autowired
    private TripServiceClient tripServiceClient;

    private static final String COLLECTION_NAME_FCMTOKEN = "fcmTokens";

    private final CollectionReference collectionRefFcmToken;

    private static final String COLLECTION_NAME_GPS = "gps";

    private final CollectionReference collectionRefGPS;

    private Firestore dbFirestore;

    public BookingServiceImpl(Firestore firestore) {

        this.dbFirestore = firestore;

        this.collectionRefFcmToken = dbFirestore.collection(COLLECTION_NAME_FCMTOKEN);

        this.collectionRefGPS = dbFirestore.collection(COLLECTION_NAME_GPS);
    }

    @Override
    public void sendNotification(NotificationDto notificationDto) {

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

    @Override
    public void sendNotificationToDesignatedDriver(String bearerToken, String uid, NotificationDto notificationDto) {

        String idToken = bearerToken.substring(7);

        log.info("idToken");

        //FirebaseToken decodedToken = decodeToken(idToken);

        List<String> fcmTokens = getAllDeviceTokens();

        Query query = collectionRefFcmToken.whereEqualTo("uid", uid);

        try {
            QuerySnapshot querySnapshot = query.get().get();

            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            String fcmToken = documents.get(0).getString("fcmToken");

            fcmTokens.remove(fcmToken);

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        Map<String, String> data = new HashMap<>();
        data.put("data", "null");

        sendNotificationToSuitableDriver(fcmTokens, notificationDto, data);
    }

    @Override
    public void removeAllGPS(String bearerToken) {

        String idToken = bearerToken.substring(7);

        log.info("idToken");

        //FirebaseToken decodedToken = decodeToken(idToken);

        ApiFuture<QuerySnapshot> future = collectionRefGPS.get();

        try {

            QuerySnapshot querySnapshot = future.get();

            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            for (QueryDocumentSnapshot document : documents) {
                String documentId = document.getId();

                DocumentReference documentReference = collectionRefGPS.document(documentId);

                documentReference.delete();
            }

        } catch (InterruptedException | ExecutionException e) {
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

        log.info("idToken -----> " + idToken);

        //FirebaseToken decodedToken = decodeToken(idToken);

        com.google.cloud.firestore.GeoPoint currentLocation = new com.google.cloud.firestore.GeoPoint(requestGPS.getCurrentLocation().getLatitude(),
                requestGPS.getCurrentLocation().getLongitude());

        GPS gps = GPS.builder()
                .uid(requestGPS.getUid())
                .currentLocation(currentLocation)
                .build();

        collectionRefGPS.document().set(gps);
    }

    @Override
    public ResponseDriverInformation getDriverInformation(String bearerToken, String customerId, RequestBookADrive requestBooking) {

        String idToken = bearerToken.substring(7);

        log.info("idToken");

        //FirebaseToken decodedToken = decodeToken(idToken);

        ResponseTripId responseTripId = createTrip(bearerToken, customerId, requestBooking);

        log.info("ResponseTripId: " + responseTripId);

        String tripId = responseTripId.getTripId();

        log.info("tripId: " + tripId);

        CompletableFuture<ResponseDriverInformation> future = CompletableFuture.supplyAsync(() -> {
            // Gửi thông báo
            NotificationDto notificationDto = NotificationDto.builder()
                    .title("GPS")
                    .body("GPS")
                    .build();

            Map<String, String> dataNull = new HashMap<>();
            dataNull.put("data", "null");

            log.info("Test: dataNull");

            sendNotificationToSuitableDriver(getAllDeviceTokens(), notificationDto, dataNull);

            log.info("Test: sendNotificationToSuitableDriver");

            try {
                Thread.sleep(5000); // Tạm dừng thực thi trong 5 giây
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // Tìm ra tài xế phù hợp
            Map<String, String> data = getCustomerInfo(bearerToken, customerId, requestBooking);

            log.info("CustomerInfo: " + data);

            String driverId = searchSuitableDriver(bearerToken, tripId, requestBooking.getCustomerOrderLocation(), data);
            ResponseDriverInformation responseDriverInformation = null;

            log.info("Test: searchSuitableDriver");

            if (driverId != null) {
                responseDriverInformation = getDriverInformationFromDB(bearerToken, driverId, tripId);

                log.info("Test: getDriverInformationFromDB");
            }

            // ---

            return responseDriverInformation;
        });

        ResponseDriverInformation responseDriverInformation = future.join();

        log.info("Test: " + responseDriverInformation);

        if (responseDriverInformation == null) {
            responseDriverInformation = new ResponseDriverInformation(null, new DriverInfo());

            tripServiceClient.deleteTrip(bearerToken, tripId);
        }

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

    private ResponseTripId createTrip(String bearerToken, String customerId, RequestBookADrive requestBooking) {

        DocumentRef documentRef = customerServiceClient.getDocumentById(bearerToken, customerId);

        DocumentReference documentReference = documentRef.getDocumentReference();

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

        ResponseTripId responseTripId = tripServiceClient.createTrip(bearerToken, createTripDto);

        log.info("responseTripId: " + responseTripId);

        return responseTripId;
    }

    private String searchSuitableDriver(String bearerToken, String tripId, GeoPoint customerOrderLocation, Map<String, String> data) {

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

                log.info("searchSuitableDriver ---> suitableDriverUid: " + suitableDriverUid);

                listOfFcmTokens = getListOfFcmToken(suitableDriverUid);

                log.info("searchSuitableDriver ---> listOfFcmTokens: " + listOfFcmTokens);

                if (listOfFcmTokens.size() > 0) {
                    sendNotificationToSuitableDriver(listOfFcmTokens, notificationDto, data);
                } else {
                    continue;
                }

                long endTime = System.currentTimeMillis() + 5000;

                while (System.currentTimeMillis() < endTime) {

                    String driverId = tripServiceClient.getDriverIdByTripId(bearerToken, tripId);

                    if (driverId != null) {
                        return driverId;
                    }
                }

                x += 2;
            }

            removeAllGPS(bearerToken);

            return null;
        });

        return future.join();
    }

    private void sendNotificationToSuitableDriver(List<String> listOfFcmTokens, NotificationDto notificationDto, Map<String, String> data) {

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(notificationDto.getTitle())
                        .setBody(notificationDto.getBody())
                        .build())
                .addAllTokens(listOfFcmTokens)
                .putAllData(data)
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

    private Map<String, String> getCustomerInfo(String bearerToken, String customerId, RequestBookADrive requestBookADrive) {

        Map<String, String> dataCustomerInfo = new HashMap<>();

        double profit = getProfit(requestBookADrive.getCost());

        CustomerDto customerDto = customerServiceClient.getCustomerDetails(bearerToken, customerId);
        CustomerInfo customerInfo = CustomerInfo.builder()
                .fullName(customerDto.getFullName())
                .phoneNumber(customerDto.getPhoneNumber())
                .avatar(customerDto.getAvatar())
                .vip(customerDto.getVip())
                .build();

        dataCustomerInfo.put("customerOrderLocation", requestBookADrive.getCustomerOrderLocation().toString());
        dataCustomerInfo.put("toLocation", requestBookADrive.getToLocation().toString());
        dataCustomerInfo.put("distance", String.valueOf(requestBookADrive.getDistance()));
        dataCustomerInfo.put("profit", String.valueOf(profit));
        dataCustomerInfo.put("customerInfo", customerInfo.toString());

        return dataCustomerInfo;
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

    private double getProfit(long cost) {

       double profit = cost * 80.0/100.0;

        return profit;
    }
}
