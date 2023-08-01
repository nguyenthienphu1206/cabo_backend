package cabo.backend.driver.service.impl;

import cabo.backend.driver.dto.*;
import cabo.backend.driver.entity.Attendance;
import cabo.backend.driver.entity.Driver;
import cabo.backend.driver.entity.FcmToken;
import cabo.backend.driver.entity.GeoPoint;
import cabo.backend.driver.exception.CheckInException;
import cabo.backend.driver.exception.CheckOutException;
import cabo.backend.driver.exception.ResourceNotFoundException;
import cabo.backend.driver.service.*;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class DriverServiceImpl implements DriverService {

    @Autowired
    private VehicleServiceClient vehicleServiceClient;

    @Autowired
    private TripServiceClient tripServiceClient;

    @Autowired
    private BingMapServiceClient bingMapServiceClient;

    @Autowired
    private BookingServiceClient bookingServiceClient;

    private ModelMapper modelMapper;

    private static final String COLLECTION_NAM_DRIVER = "drivers";

    private final CollectionReference collectionRefDrvier;

    private static final String COLLECTION_NAME_ATTENDANCE = "attendance";

    private final CollectionReference collectionAttendance;

    private static final String COLLECTION_NAME_FCMTOKEN = "fcmTokens";

    private final CollectionReference collectionRefFcmToken;

    private Firestore dbFirestore;

    public DriverServiceImpl(Firestore firestore) {

        this.dbFirestore = firestore;

        this.collectionRefDrvier = dbFirestore.collection(COLLECTION_NAM_DRIVER);
        this.collectionAttendance = dbFirestore.collection(COLLECTION_NAME_ATTENDANCE);
        this.collectionRefFcmToken = dbFirestore.collection(COLLECTION_NAME_FCMTOKEN);
    }

    @Override
    public DocumentRef getDocumentById(String bearerToken, String driverId) {

        DocumentReference documentReference = collectionRefDrvier.document(driverId);

        DocumentRef documentRef = DocumentRef.builder()
                .documentReference(documentReference)
                .build();

        return documentRef;
    }

    @Override
    public DriverInfo getDriverInfoByDriverIdAndTripId(String bearerToken, String driverId, String tripId) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        DocumentReference documentReference = collectionRefDrvier.document(driverId);

        ApiFuture<DocumentSnapshot> future = documentReference.get();

        DriverInfo driverInfo = null;

        try {
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                Driver driver = document.toObject(Driver.class);

                if (driver != null) {
                    DocumentReference vehicleIdRef = driver.getVehicleId();

                    String vehicleId = vehicleIdRef.getId();

                    VehicleDto vehicleDto = vehicleServiceClient.getVehicle(bearerToken, vehicleId);

                    GeoPoint driverLocation = tripServiceClient.getDriverLocation(bearerToken, tripId);

                    driverInfo = DriverInfo.builder()
                            .fullName(driver.getFullName())
                            .phoneNumber(driver.getPhoneNumber())
                            .avatar(driver.getAvatar())
                            .brand(vehicleDto.getBrand())
                            .regNo(vehicleDto.getRegNo())
                            .driverCurrentLocation(driverLocation)
                            .build();

                }

            }
            else {
                throw new ResourceNotFoundException("Document", "DriverId", driverId);
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return driverInfo;
    }

    @Override
    public String registerInfo(String bearerToken, RequestRegistryInfo requestRegistryInfo) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        //String uid = decodedToken.getUid();
        //log.info("UID -----> " + uid);

//        try {
//            UserRecord userRecord = FirebaseAuth.getInstance().getUser(uid);
//            phoneNumber = userRecord.getPhoneNumber();
//
//        } catch (FirebaseAuthException e) {
//            throw new RuntimeException(e);
//        }

        Query query = collectionRefDrvier.whereEqualTo("phoneNumber", requestRegistryInfo.getPhoneNumber());
        ApiFuture<QuerySnapshot> querySnapshotFuture = query.get();

        String driverId;

        try {
            QuerySnapshot querySnapshot = querySnapshotFuture.get();

            if (querySnapshot.isEmpty()) {

                Driver driver = Driver.builder()
                        .uid("")
                        .fullName(requestRegistryInfo.getFullName())
                        .phoneNumber(requestRegistryInfo.getPhoneNumber())
                        .avatar("")
                        .vehicleId(null)
                        .isWorking(false)
                        .build();

                DocumentReference documentReference = collectionRefDrvier.document();

                ApiFuture<WriteResult> collectionApiFuture = documentReference.set(driver);

                driverId = documentReference.getId();
            }
            else {
                driverId = querySnapshot.getDocuments().get(0).getId();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return driverId;
    }

    @Override
    public String saveDriver(String bearerToken, DriverDto driverDto) {

        String idToken = bearerToken.substring(7);

        FirebaseToken decodedToken = decodeToken(idToken);


        Driver driver = modelMapper.map(driverDto, Driver.class);

        ApiFuture<WriteResult> collectionApiFuture = collectionRefDrvier.document().set(driver);

        String timestamp = null;
        try {
            timestamp = collectionApiFuture.get().getUpdateTime().toString();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return timestamp;
    }

    @Override
    public ResponseDriverDetails getDriverDetails(String bearerToken, String driverId) {

        String idToken = bearerToken.substring(7);

        FirebaseToken decodedToken = decodeToken(idToken);

        DocumentReference documentReference = collectionRefDrvier.document(driverId);

        ApiFuture<DocumentSnapshot> future = documentReference.get();

        DocumentSnapshot document = null;
        try {
            document = future.get();
            log.info("Document ----> " + document);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        Driver driver = null;

        if (document.exists()) {
            driver = document.toObject(Driver.class);
            log.info("driver ----> " + driver);
        }

        ResponseDriverDetails responseDriverDetails = ResponseDriverDetails.builder()
                .uid(driver.getUid())
                .fullName(driver.getFullName())
                .phoneNumber(driver.getPhoneNumber())
                .avatar(driver.getAvatar())
                .carId(driver.getVehicleId())
                .build();

        return responseDriverDetails;
    }

    @Override
    public Boolean checkPhoneExistence(String bearerToken, String phoneNumber) {

        String idToken = bearerToken.substring(7);

        FirebaseToken decodedToken = decodeToken(idToken);

        // Lấy tất cả các tài liệu trong collection
        ApiFuture<QuerySnapshot> future = collectionRefDrvier.get();

        List<QueryDocumentSnapshot> documents = null;

        try {
            documents = future.get().getDocuments();

            for (QueryDocumentSnapshot document : documents) {

                String phoneNumberInDB = document.getString("phoneNumber");

                if (phoneNumber.equals(phoneNumberInDB)) {
                    log.info(phoneNumber);
                    return true;
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    @Override
    public String registerDriverVehicle(String bearerToken, String driverId, RequestRegisterVehicle requestRegisterVehicle) {

        String idToken = bearerToken.substring(7);

        log.info("idToken -----> " + idToken);

        //FirebaseToken decodedToken = decodeToken(idToken);

        DocumentReference documentReference = collectionRefDrvier.document(driverId);

        ApiFuture<DocumentSnapshot> future = documentReference.get();

        DocumentSnapshot document;

        try {
            document = future.get();
            log.info("Document ----> " + document);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        String vehicleId = "";
        if (document.exists()) {
            // Đăng Kí vehicle và trả về vehicleId
            vehicleId = vehicleServiceClient.registerVehicle(bearerToken, requestRegisterVehicle);

            DocumentRef documentRef = vehicleServiceClient.getDocumentById(bearerToken, vehicleId);

            DocumentReference vehicleDocumentReference = documentRef.getDocumentReference();

            Driver driver = document.toObject(Driver.class);

            if (driver != null) {
                driver.setVehicleId(vehicleDocumentReference);

                ApiFuture<WriteResult> writeResult = documentReference.set(driver);
            }
        }
        else {
            throw new ResourceNotFoundException("Document", "DriverId", driverId);
        }

        return vehicleId;
    }

    @Override
    public ResponseCheckInOut checkIn(String bearerToken, RequestCheckIn requestCheckIn) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);


        DocumentReference documentReference = collectionRefDrvier.document(requestCheckIn.getDriverId());

        ApiFuture<DocumentSnapshot> future = documentReference.get();

        DocumentSnapshot document = null;
        Date timestamp = null;

        try {
            document = future.get();

            if (document.exists()) {
                if (Objects.equals(document.get("isWorking"), false)) {

                    Attendance attendance = Attendance.builder()
                            .checkInAt(requestCheckIn.getCheckInAt())
                            .checkOutAt(requestCheckIn.getCheckOutAt())
                            .driverId(requestCheckIn.getDriverId())
                            .build();

                    Driver driver = document.toObject(Driver.class);

                    driver.setIsWorking(true);

                    ApiFuture<WriteResult> collectionApiFutureDriver = collectionRefDrvier.document(document.getId())
                            .set(driver);

                    ApiFuture<WriteResult> collectionApiFuture = collectionAttendance.document().set(attendance);

                    timestamp = collectionApiFuture.get().getUpdateTime().toDate();
                }
                else {
                    throw new CheckInException(HttpStatus.BAD_REQUEST, "Checking in ...");
                }

            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        ResponseCheckInOut responseCheckIn = new ResponseCheckInOut(timestamp, "Successfully");

        return responseCheckIn;
    }

    @Override
    public ResponseCheckInOut checkOut(String bearerToken, RequestCheckOut requestCheckOut) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);


        DocumentReference documentReference = collectionRefDrvier.document(requestCheckOut.getDriverId());

        ApiFuture<DocumentSnapshot> future = documentReference.get();

        Query query = collectionAttendance.whereEqualTo("driverId", requestCheckOut.getDriverId())
                .orderBy("checkInAt", Query.Direction.DESCENDING)
                .limit(1);

        ResponseCheckInOut responseCheckOut;

        try {
            // Set checkOutAt
            QuerySnapshot querySnapshot = query.get().get();

            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();


            String documentId = documents.get(0).getId();

            Attendance attendance = documents.get(0).toObject(Attendance.class);

            if (documents.get(0).get("checkOutAt") == null) {
                attendance.setCheckOutAt(requestCheckOut.getCheckOutAt());

                ApiFuture<WriteResult> collectionApiFuture = collectionAttendance.document(documentId).set(attendance);

                Date timestamp = collectionApiFuture.get().getUpdateTime().toDate();

                // Set isWorking = false
                DocumentSnapshot document = future.get();

                Driver driver = document.toObject(Driver.class);

                driver.setIsWorking(false);

                ApiFuture<WriteResult> collectionApiFutureDriver = collectionRefDrvier.document(document.getId())
                        .set(driver);
                log.info("Test4");
                responseCheckOut = new ResponseCheckInOut(timestamp, "Successfully");
            }
            else {
                throw new CheckOutException(HttpStatus.INTERNAL_SERVER_ERROR, "Driver has checked out");
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return responseCheckOut;
    }

    @Override
    public ResponseOverview getOverview(String bearerToken, String driverId) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);
        log.info("Test");
        TripDto tripDto = tripServiceClient.getRecentTripFromDriver(bearerToken, driverId);

        ResponseRecentTrip responseRecentTrip = null;

        if (tripDto != null) {
            String driverStartLocation = bingMapServiceClient.getAddress(tripDto.getDriverStartLocation().getLatitude(),
                    tripDto.getDriverStartLocation().getLongitude());

            String toLocation = bingMapServiceClient.getAddress(tripDto.getToLocation().getLatitude(),
                    tripDto.getToLocation().getLongitude());

            responseRecentTrip = ResponseRecentTrip.builder()
                    .cost(tripDto.getCost())
                    .distance(tripDto.getDistance())
                    .startTime(tripDto.getStartTime())
                    .pickUpTime(tripDto.getPickUpTime())
                    .endTime(tripDto.getEndTime())
                    .driverStartLocation(driverStartLocation)
                    .toLocation(toLocation)
                    .build();
        }

        //log.info("Test1 " + tripDto);

        ResponseTotalTrip responseTotalTrip = tripServiceClient.getTotalTrip(bearerToken, "driver", driverId);


        ResponseAverageIncomePerDrive responseAverageIncomePerDrive = tripServiceClient.getAverageIncomePerDrive(bearerToken, driverId);

        //log.info("Test2 " + responseTotalTrip);
        ResponseOverview responseOverview = ResponseOverview.builder()
                .averageIncomePerDrive(responseAverageIncomePerDrive.getAverageIncomePerDrive())
                .totalTrip(responseTotalTrip.getTotalTrip())
                .recentTrip(responseRecentTrip)
                .build();
        //log.info("Test3");

        return responseOverview;
    }

    @Override
    public void subscribeNotification(String bearerToken, String fcmToken) {

        String idToken = bearerToken.substring(7);

        FirebaseToken decodedToken = decodeToken(idToken);

        String uid = decodedToken.getUid();

        FcmToken savedFcmToken = FcmToken.builder()
                .fcmToken(fcmToken)
                .uid(uid)
                .build();

        ApiFuture<WriteResult> collectionApiFuture = collectionRefFcmToken.document().set(savedFcmToken);
    }

    @Override
    public ResponseStatus sendReceivedDriverInfo(String bearerToken, RequestReceivedDriverInfo requestReceivedDriverInfo) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        DocumentReference documentReference = collectionRefDrvier.document(requestReceivedDriverInfo.getDriverId());

        RequestReceivedDriverRefInfo requestReceivedDriverRefInfo = RequestReceivedDriverRefInfo.builder()
                .tripId(requestReceivedDriverInfo.getTripId())
                .driverId(documentReference)
                .currentLocation(requestReceivedDriverInfo.getCurrentLocation())
                .build();

        ResponseStatus responseStatus = tripServiceClient.sendReceivedDriverInfo(bearerToken, requestReceivedDriverRefInfo);

        sendNotification(bearerToken, requestReceivedDriverInfo.getDriverId(), responseStatus.getMessage());

        return responseStatus;
    }

    @Override
    public ResponseStatus sendGPS(String bearerToken, RequestGPS requestGPS) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        ResponseStatus responseStatus = bookingServiceClient.collectGPS(bearerToken, requestGPS);

        return responseStatus;
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

    private void sendNotification(String bearerToken, String driverId, String message) {

        if (message.equals("Successfully")) {

            DocumentReference documentReference = collectionRefDrvier.document(driverId);

            ApiFuture<DocumentSnapshot> future = documentReference.get();

            try {
                DocumentSnapshot document = future.get();

                if (document.exists()) {

                    Driver driver = document.toObject(Driver.class);

                    if (driver != null) {
                        String uid = driver.getUid();

                        NotificationDto notificationDto = NotificationDto.builder()
                                .title("BOOKING_CLOSE")
                                .body("BOOKING_CLOSE")
                                .build();

                        RequestUidAndNotification requestUidAndNotification = RequestUidAndNotification.builder()
                                .uid(uid)
                                .notificationDto(notificationDto)
                                .build();

                        ResponseStatus rsNotify = bookingServiceClient.sendNotificationToDesignatedDriver(bearerToken, requestUidAndNotification);

                        ResponseStatus rsRemoveAllGPS = bookingServiceClient.removeAllGPS(bearerToken);
                    }
                }
                else {
                    throw new ResourceNotFoundException("Document", "DriverId", driverId);
                }

            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
