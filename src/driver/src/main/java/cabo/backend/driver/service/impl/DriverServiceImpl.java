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
import cabo.backend.driver.utils.FcmClient;
import cabo.backend.driver.utils.StatusDriver;
import cabo.backend.driver.utils.VehicleType;
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

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        DocumentReference documentReference = collectionRefDrvier.document(driverId);

        DocumentRef documentRef = DocumentRef.builder()
                .documentReference(documentReference)
                .build();

        return documentRef;
    }

    @Override
    public String getNameByDriverId(String bearerToken, String driverId) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        DocumentReference documentReference = collectionRefDrvier.document(driverId);

        ApiFuture<DocumentSnapshot> future = documentReference.get();

        String fullName;

        try {
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                fullName = document.getString("fullName");
            }
            else {
                throw new ResourceNotFoundException("Driver", "DriverId", driverId);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return fullName;
    }

    @Override
    public List<TripDto> getAllTripsById(String bearerToken, String driverId) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        return tripServiceClient.getTripByDriverId(bearerToken, driverId);
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
                    String vehicleId = "";

                    if (vehicleIdRef != null) {
                        vehicleId = vehicleIdRef.getId();
                    }

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
    public String getDriverStatusIntByUid(String uid) {

        Query query = collectionRefDrvier.whereEqualTo("uid", uid);

        ApiFuture<QuerySnapshot> querySnapshotFuture = query.get();

        String status = null;

        try {
            QuerySnapshot querySnapshot = querySnapshotFuture.get();

            if (!querySnapshot.isEmpty()) {
                QueryDocumentSnapshot queryDocumentSnapshot = querySnapshot.getDocuments().get(0);

                Driver driver = queryDocumentSnapshot.toObject(Driver.class);

                status = driver.getDriverStatus();
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return status;
    }

    @Override
    public String registerInfo(String bearerToken, RequestRegistryInfo requestRegistryInfo) {

        String idToken = bearerToken.substring(7);

        FirebaseToken decodedToken = decodeToken(idToken);

        String uid = decodedToken.getUid();
        //log.info("UID -----> " + uid);

        Query query = collectionRefDrvier.whereEqualTo("phoneNumber", requestRegistryInfo.getPhoneNumber());
        ApiFuture<QuerySnapshot> querySnapshotFuture = query.get();

        try {
            QuerySnapshot querySnapshot = querySnapshotFuture.get();

            if (querySnapshot.isEmpty()) {

                Driver driver = Driver.builder()
                        .uid(uid)
                        .fullName(requestRegistryInfo.getFullName())
                        .phoneNumber(requestRegistryInfo.getPhoneNumber())
                        .avatar("")
                        .vehicleId(null)
                        .driverStatus(StatusDriver.OFFLINE.name())
                        .build();

                DocumentReference documentReference = collectionRefDrvier.document(uid);

                documentReference.set(driver);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return uid;
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

        String vehicleId;

        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                // Đăng Kí vehicle và trả về vehicleId
                vehicleId = vehicleServiceClient.registerVehicle(bearerToken, requestRegisterVehicle);

                DocumentRef documentRef = vehicleServiceClient.getDocumentById(bearerToken, vehicleId);

                DocumentReference vehicleDocumentReference = documentRef.getDocumentReference();

                Driver driver = document.toObject(Driver.class);

                if (driver != null) {
                    driver.setVehicleId(vehicleDocumentReference);

                    documentReference.set(driver);
                }
            }
            else {
                throw new ResourceNotFoundException("Document", "DriverId", driverId);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return vehicleId;
    }

//    private void registerFcmClient(String driverId, String carType) {
//
//        DocumentReference documentReference = collectionRefFcmToken.document(driverId);
//
//        ApiFuture<DocumentSnapshot> future = documentReference.get();
//
//        try {
//            DocumentSnapshot document = future.get();
//            if (document.exists()) {
//
//                FcmToken fcmToken = document.toObject(FcmToken.class);
//
//                if (fcmToken != null) {
//                    if (carType.equals(VehicleType.VEHICLE_TYPE_CAR_4.name())) {
//                        fcmToken.setFcmClient(FcmClient.DRIVER_CAR_4.name());
//                    }
//                    else if (carType.equals(VehicleType.VEHICLE_TYPE_CAR_7.name())) {
//                        fcmToken.setFcmClient(FcmClient.DRIVER_CAR_7.name());
//                    }
//
//                    documentReference.set(fcmToken);
//                }
//            }
//        } catch (InterruptedException | ExecutionException e) {
//            throw new RuntimeException(e);
//        }
//    }

    @Override
    public ResponseStatus checkIn(String bearerToken, RequestCheckIn requestCheckIn) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);


        DocumentReference documentReference = collectionRefDrvier.document(requestCheckIn.getDriverId());

        ApiFuture<DocumentSnapshot> future = documentReference.get();

        DocumentSnapshot document = null;
        Date timestamp = null;

        try {
            document = future.get();

            if (document.exists()) {
                if (document.getString("driverStatus").equals(StatusDriver.OFFLINE.name())) { // 1: OFFLINE

                    Attendance attendance = Attendance.builder()
                            .checkInAt(requestCheckIn.getCheckInAt())
                            .checkOutAt(requestCheckIn.getCheckOutAt())
                            .driverId(requestCheckIn.getDriverId())
                            .build();

                    Driver driver = document.toObject(Driver.class);

                    driver.setDriverStatus(StatusDriver.ONLINE.name());

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

        ResponseStatus responseStatus = new ResponseStatus(timestamp, "Successfully");

        return responseStatus;
    }

    @Override
    public ResponseStatus checkOut(String bearerToken, RequestCheckOut requestCheckOut) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);


        DocumentReference documentReference = collectionRefDrvier.document(requestCheckOut.getDriverId());

        ApiFuture<DocumentSnapshot> future = documentReference.get();

        Query query = collectionAttendance.whereEqualTo("driverId", requestCheckOut.getDriverId())
                .orderBy("checkInAt", Query.Direction.DESCENDING)
                .limit(1);

        ResponseStatus responseStatus;

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

                DocumentSnapshot document = future.get();

                Driver driver = document.toObject(Driver.class);

                driver.setDriverStatus(StatusDriver.OFFLINE.name());

                ApiFuture<WriteResult> collectionApiFutureDriver = collectionRefDrvier.document(document.getId())
                        .set(driver);
                log.info("Test4");
                responseStatus = new ResponseStatus(timestamp, "Successfully");
            }
            else {
                throw new CheckOutException(HttpStatus.INTERNAL_SERVER_ERROR, "Driver has checked out");
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return responseStatus;
    }

    @Override
    public ResponseStatus updateDriverStatus(String bearerToken, String driverId, String status) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        DocumentReference documentReference = collectionRefDrvier.document(driverId);

        ApiFuture<DocumentSnapshot> future = documentReference.get();

        try {
            DocumentSnapshot document = future.get();

            if (document.exists()) {

                Driver driver = document.toObject(Driver.class);

                if (driver != null) {

                    StatusDriver statusDriver = StatusDriver.valueOf(status);

                    driver.setDriverStatus(statusDriver.name());

                    collectionRefDrvier.document(driverId).set(driver);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return new ResponseStatus(new Date(), "Successfully");
    }

    @Override
    public ResponseOverview getOverview(String bearerToken, String driverId) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);
        log.info("Test");
        RecentTrip recentTrip = tripServiceClient.getRecentTripFromDriver(bearerToken, driverId);

        ResponseRecentTrip responseRecentTrip = null;

        if (recentTrip != null) {

            responseRecentTrip = ResponseRecentTrip.builder()
                    .cost(recentTrip.getCost())
                    .distance(recentTrip.getDistance())
                    .startTime(recentTrip.getStartTime())
                    .pickUpTime(recentTrip.getPickUpTime())
                    .endTime(recentTrip.getEndTime())
                    .driverStartLocation(recentTrip.getDriverStartLocation())
                    .toLocation(recentTrip.getToLocation())
                    .build();
        }

        //log.info("Test1 " + recentTrip);

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
    public void subscribeNotification(String bearerToken, String fcmToken, String carType) {

        String idToken = bearerToken.substring(7);

        FirebaseToken decodedToken = decodeToken(idToken);

        String uid = decodedToken.getUid();

        String fcmClient = FcmClient.DRIVER_CAR_4.name();

        if (carType.equals(VehicleType.VEHICLE_TYPE_CAR_7.name())) {
            fcmClient = FcmClient.DRIVER_CAR_7.name();
        }

        FcmToken savedFcmToken = FcmToken.builder()
                .fcmToken(fcmToken)
                .fcmClient(fcmClient)
                .uid(uid)
                .build();

        collectionRefFcmToken.document(uid).set(savedFcmToken);
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
}
