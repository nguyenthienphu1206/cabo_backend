package cabo.backend.booking.service.impl;

import cabo.backend.booking.dto.*;
import cabo.backend.booking.entity.GPS;
import cabo.backend.booking.entity.GeoPoint;
import cabo.backend.booking.exception.ResourceNotFoundException;
import cabo.backend.booking.service.*;
import cabo.backend.booking.utils.AppConstants;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.messaging.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
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

    @Value("${rabbitmq.exchange.status.name}")
    private String statusExchange;

    @Value("${rabbitmq.binding.status.routing.key}")
    private String statusRoutingKey;

    @Value("${rabbitmq.binding.status_customer.routing.key}")
    private String statusCustomerRoutingKey;

    private final RabbitTemplate rabbitTemplate;

    public BookingServiceImpl(RabbitTemplate rabbitTemplate, Firestore firestore) {

        this.rabbitTemplate = rabbitTemplate;

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
                .addAllTokens(getAllDeviceTokensDriver())// Lấy danh sách các token của tất cả mobile client
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

    private void sendTripReceivedNotification(String driverId) {

        NotificationDto notificationDto = NotificationDto.builder()
                .title("BOOKING_CLOSED")
                .body("BOOKING_CLOSED")
                .build();

        List<String> fcmTokens = getAllDeviceTokensDriver();

        DocumentReference documentReference = collectionRefFcmToken.document(driverId);

        try {
            ApiFuture<DocumentSnapshot> future = documentReference.get();

            DocumentSnapshot document = future.get();

            String fcmToken = document.getString("fcmToken");

            // Bỏ đi tài xế đang nhận cuốc
            fcmTokens.remove(fcmToken);

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        // Gửi thông báo nhận cuốc thất bại đến các driver khác
        Map<String, String> data = new HashMap<>();
        data.put("data", "null");

        sendNotificationToSuitableDriver(fcmTokens, notificationDto, data);
    }

    private void removeAllGPS(String bearerToken) {

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

    @Override
    public void collectGPSFromDriver(String bearerToken, RequestGPS requestGPS) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        com.google.cloud.firestore.GeoPoint currentLocation = new com.google.cloud.firestore.GeoPoint(requestGPS.getCurrentLocation().getLatitude(),
                requestGPS.getCurrentLocation().getLongitude());

        GPS gps = GPS.builder()
                .currentLocation(currentLocation)
                .time(Instant.now().getEpochSecond())
                .build();

        collectionRefGPS.document(requestGPS.getUid()).set(gps);
    }

    @Override
    public ResponseDriverInformation getDriverInformation(String bearerToken, String customerId, RequestBookADrive requestBooking) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        // Tạo 1 chuyến đi
        ResponseTripId responseTripId = createTrip(bearerToken, customerId, requestBooking);

        String tripId = responseTripId.getTripId();

        log.info("tripId: " + tripId);

        // Update trip status
        sendStatusEventToStatusQueue(bearerToken, tripId, AppConstants.StatusTrip.TRIP_STATUS_SEARCHING.name());

        CompletableFuture<ResponseDriverInformation> future = CompletableFuture.supplyAsync(() -> {

            ResponseDriverInformation responseDriverInformation = null;

            // Gửi thông báo lấy GPS nếu thời gian lấy lần trước lâu hơn 60s
            if (sendNotificationIfTimeExceedsThreshold(bearerToken, tripId)) {
                // Tìm ra tài xế phù hợp
                Map<String, String> data = getCustomerInfo(bearerToken, customerId, requestBooking);

                log.info("CustomerInfo: " + data);

                String driverId = searchSuitableDriver(bearerToken, tripId, requestBooking.getCustomerOrderLocation(), data);

                log.info("Test: searchSuitableDriver");

                if (driverId != null) {
                    responseDriverInformation = getDriverInformationFromDB(bearerToken, driverId, tripId);

                    // Gửi thông báo đã có người nhận cuốc đến tất cả người còn lại
                    sendTripReceivedNotification(driverId);

                    // Update trip status
                    sendStatusEventToStatusQueue(bearerToken, tripId, AppConstants.StatusTrip.TRIP_STATUS_PICKING.name());

                    // send distance and time to customer
                    sendDistanceAndTimeToCustomer(bearerToken, tripId, driverId, customerId, requestBooking.getCustomerOrderLocation());

                    // Update driver status
                    driverServiceClient.updateDriverStatus(bearerToken, driverId, AppConstants.StatusDriver.BUSY.name());

                    log.info("Test: getDriverInformationFromDB");
                }
            }
            return responseDriverInformation;
        });

        ResponseDriverInformation responseDriverInformation = future.join();

        log.info("Test: " + responseDriverInformation);

        if (responseDriverInformation == null) {
            responseDriverInformation = new ResponseDriverInformation(null, new DriverInfo());

            // Update trip status
            sendStatusEventToStatusQueue(bearerToken, tripId, AppConstants.StatusTrip.TRIP_STATUS_NO_DRIVER.name());

            //tripServiceClient.deleteTrip(bearerToken, tripId);
        }

        return responseDriverInformation;
    }

    // Đặt xe từ phía Call-Center
    @RabbitListener(queues = "${rabbitmq.queue.booking.name}")
    public void bookDriveFromCallCenter(RequestBookADriveEvent requestBookADriveEvent) {
        String bearerToken = requestBookADriveEvent.getBearerToken();

        //sendStatusEventToStatusQueue(bearerToken, "WkgMkVceg8VVnS44VtlA", "TRIP_STATUS_CLOSE");
        getDriverInformation(bearerToken, requestBookADriveEvent.getCustomerId(), requestBookADriveEvent.getRequestBookADrive());
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

    private String getTripStatus(String bearerToken, String tripId) {

        return tripServiceClient.getTripStatusById(bearerToken, tripId);
    }

    private void sendDistanceAndTimeToCustomer(String bearerToken, String tripId, String driverId, String customerId, GeoPoint customerOrderLocation) {

        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {

            DocumentReference documentReference = collectionRefGPS.document(driverId);

            ApiFuture<DocumentSnapshot> future = documentReference.get();

            com.google.cloud.firestore.GeoPoint driverLocation;

            try {
                DocumentSnapshot document = future.get();

                if (document.exists()) {
                    driverLocation = document.getGeoPoint("currentLocation");

                    if (driverLocation != null) {
                        while (getTripStatus(bearerToken, tripId).equals(AppConstants.StatusTrip.TRIP_STATUS_PICKING.name())) {

                            TravelInfor travelInfor = bingMapServiceClient.getDistanceAndTime(
                                    customerOrderLocation.getLatitude(),
                                    customerOrderLocation.getLongitude(),
                                    driverLocation.getLatitude(),
                                    driverLocation.getLongitude()
                            );

                            sendDistanceAndTimeToQueue(customerId, travelInfor);

                            Thread.sleep(3000);

                        }
                    }
                }
                else {
                    throw new ResourceNotFoundException("Driver", "DriverId", driverId);
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
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

        GeoPoint geoPoint =new GeoPoint(0.0, 0.0);

        CreateTripDto createTripDto = CreateTripDto.builder()
                .cost(requestBooking.getCost())
                .customerId(customerId)
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

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    String driverId = tripServiceClient.getDriverIdByTripId(bearerToken, tripId);

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

                        suitableDriverUid.add(document.getId());
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
                Query query = collectionRefFcmToken.whereEqualTo("uid", uid)
                        .whereEqualTo("fcmClient", "DRIVER");

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

        return cost * 80.0/100.0;
    }

    private void sendStatusEventToStatusQueue(String bearerToken, String tripId, String status) {

        String fcmToken = getFcmTokenCallCenter();

        DriveStatus driveStatus = DriveStatus.builder()
                .bearerToken(bearerToken)
                .fcmToken(fcmToken)
                .tripId(tripId)
                .status(status)
                .build();

        // Send event to status queue
        rabbitTemplate.convertAndSend(statusExchange, statusRoutingKey, driveStatus);
    }

    private void sendDistanceAndTimeToQueue(String customerId, TravelInfor travelInfor) {

        String fcmToken = getFcmTokenCustomer(customerId);

        String driverRemainingDistance;
        double distance = travelInfor.getTravelDistance();

        if (distance < 1) {
            distance *= 1000;
            driverRemainingDistance = (int)convertDoubleToDoubleFormat(distance, 0) + " m";
        } else {
            driverRemainingDistance = convertDoubleToDoubleFormat(distance, 1) + " km";
        }

        String driverRemainingTime = (int)convertDoubleToDoubleFormat(travelInfor.getTravelDuration(), 0) + " minutes";

        TravelInfoToCustomer travelInfoToCustomer = TravelInfoToCustomer.builder()
                .fcmToken(fcmToken)
                .driverRemainingDistance(driverRemainingDistance)
                .driverRemainingTime(driverRemainingTime)
                .build();


        // Send event to status queue
        rabbitTemplate.convertAndSend(statusExchange, statusCustomerRoutingKey, travelInfoToCustomer);
    }

    private double convertDoubleToDoubleFormat(double value, int decimalPlaces) {

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(decimalPlaces, RoundingMode.HALF_UP);

        return bd.doubleValue();
    }

    private String getFcmTokenCallCenter() {

        Query query = collectionRefFcmToken.whereEqualTo("fcmClient", "CALL_CENTER");

        ApiFuture<QuerySnapshot> querySnapshotFuture = query.get();

        QuerySnapshot querySnapshot;

        try {
            querySnapshot = querySnapshotFuture.get();
            if (!querySnapshot.isEmpty()) {
                QueryDocumentSnapshot queryDocumentSnapshot = querySnapshot.getDocuments().get(0);

                return queryDocumentSnapshot.getString("fcmToken");
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return "";
    }

    private String getFcmTokenCustomer(String customerId) {

        Query query = collectionRefFcmToken.whereEqualTo("fcmClient", "CUSTOMER")
                .whereEqualTo("uid", customerId);

        ApiFuture<QuerySnapshot> querySnapshotFuture = query.get();

        QuerySnapshot querySnapshot;

        try {
            querySnapshot = querySnapshotFuture.get();
            if (!querySnapshot.isEmpty()) {
                QueryDocumentSnapshot queryDocumentSnapshot = querySnapshot.getDocuments().get(0);

                return queryDocumentSnapshot.getString("fcmToken");
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return "";
    }

    private Boolean sendNotificationIfTimeExceedsThreshold(String beaerToken, String tripId) {

        Query query = collectionRefGPS.orderBy("time", Query.Direction.DESCENDING).limit(1);

        try {
            QuerySnapshot querySnapshot = query.get().get();

            if (!querySnapshot.isEmpty()) {
                Long time = querySnapshot.getDocuments().get(0).getLong("time");

                Long currentTime = Instant.now().getEpochSecond();

                if (time != null && currentTime - time >= 60) {

                    NotificationDto notificationDto = NotificationDto.builder()
                            .title("GPS")
                            .body("GPS")
                            .build();

                    Map<String, String> dataNull = new HashMap<>();
                    dataNull.put("data", "null");

                    log.info("Test: dataNull");

                    // Lấy danh sach driver đang online
                    List<String> fcmTokenOnlineDrivers = getAllFcmTokenOfOnlineDriver();

                    if (fcmTokenOnlineDrivers.size() == 0) {
                        return false;
                    }
                    sendNotificationToSuitableDriver(getAllFcmTokenOfOnlineDriver(), notificationDto, dataNull);


                    log.info("Test: sendNotificationToSuitableDriver");

                    Thread.sleep(5000); // Tạm dừng thực thi trong 5 giây
                }
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    // Phương thức này để lấy danh sách các token của tất cả mobile client
    private List<String> getAllDeviceTokensDriver() {

        List<String> fcmTokens = new ArrayList<>();

        // Lấy tất cả các tài liệu trong collection
        ApiFuture<QuerySnapshot> future = collectionRefFcmToken.get();

        List<QueryDocumentSnapshot> documents = null;

        try {
            documents = future.get().getDocuments();

            for (QueryDocumentSnapshot document : documents) {

                if (Objects.equals(document.getString("fcmClient"), "DRIVER")) {
                    String fcmToken = document.getString("fcmToken");

                    fcmTokens.add(fcmToken);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        log.info("FcmTokens: " + fcmTokens);

        return fcmTokens;
    }

    private List<String> getAllFcmTokenOfOnlineDriver() {

        List<String> fcmTokens = new ArrayList<>();

        // Lấy tất cả các tài liệu trong collection
        ApiFuture<QuerySnapshot> future = collectionRefFcmToken.get();

        List<QueryDocumentSnapshot> documents = null;

        try {
            documents = future.get().getDocuments();

            for (QueryDocumentSnapshot document : documents) {

                String status = driverServiceClient.getDriverStatusIntByUid(document.getString("uid"));

                if (Objects.equals(document.getString("fcmClient"), "DRIVER") &&
                        status != null &&
                        status.equals(AppConstants.StatusDriver.ONLINE.name())) {

                    String fcmToken = document.getString("fcmToken");

                    fcmTokens.add(fcmToken);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        log.info("FcmTokens: " + fcmTokens);

        return fcmTokens;
    }
}
