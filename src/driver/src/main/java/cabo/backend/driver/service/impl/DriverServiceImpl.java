package cabo.backend.driver.service.impl;

import cabo.backend.driver.dto.*;
import cabo.backend.driver.entity.Attendance;
import cabo.backend.driver.entity.Driver;
import cabo.backend.driver.exception.CheckInException;
import cabo.backend.driver.exception.CheckOutException;
import cabo.backend.driver.exception.ResourceNotFoundException;
import cabo.backend.driver.service.BingMapServiceClient;
import cabo.backend.driver.service.DriverService;
import cabo.backend.driver.service.TripServiceClient;
import cabo.backend.driver.service.VehicleServiceClient;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.cloud.FirestoreClient;
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

    private ModelMapper modelMapper;

    private static final String COLLECTION_NAME = "drivers";

    private static final String COLLECTION_NAME_VEHICLE = "vehicles";

    private static final String COLLECTION_NAME_ATTENDANCE = "attendance";

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

        Firestore dbFirestore = FirestoreClient.getFirestore();

        CollectionReference collectionRef = dbFirestore.collection(COLLECTION_NAME);

        Query query = collectionRef.whereEqualTo("phoneNumber", requestRegistryInfo.getPhoneNumber());
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

                DocumentReference documentReference = collectionRef.document();

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

        Firestore dbFirestore = FirestoreClient.getFirestore();

        Driver driver = modelMapper.map(driverDto, Driver.class);

        ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection(COLLECTION_NAME).document().set(driver);

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

        Firestore dbFirestore = FirestoreClient.getFirestore();

        DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document(driverId);

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

        Firestore dbFirestore = FirestoreClient.getFirestore();

        CollectionReference collectionReference = dbFirestore.collection(COLLECTION_NAME);

        // Lấy tất cả các tài liệu trong collection
        ApiFuture<QuerySnapshot> future = collectionReference.get();

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

        Firestore dbFirestore = FirestoreClient.getFirestore();

        DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document(driverId);

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
            vehicleId = vehicleServiceClient.registerVehicle(requestRegisterVehicle);
            DocumentReference vehicleDocumentReference = dbFirestore.collection(COLLECTION_NAME_VEHICLE).document(vehicleId);

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

        Firestore dbFirestore = FirestoreClient.getFirestore();

        CollectionReference collectionReference = dbFirestore.collection(COLLECTION_NAME_ATTENDANCE);

        CollectionReference collectionReferenceDriver = dbFirestore.collection(COLLECTION_NAME);

        DocumentReference documentReference = collectionReferenceDriver.document(requestCheckIn.getDriverId());

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

                    ApiFuture<WriteResult> collectionApiFutureDriver = collectionReferenceDriver.document(document.getId())
                            .set(driver);

                    ApiFuture<WriteResult> collectionApiFuture = collectionReference.document().set(attendance);

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

        Firestore dbFirestore = FirestoreClient.getFirestore();

        CollectionReference collectionReference = dbFirestore.collection(COLLECTION_NAME_ATTENDANCE);

        CollectionReference collectionReferenceDriver = dbFirestore.collection(COLLECTION_NAME);

        DocumentReference documentReference = collectionReferenceDriver.document(requestCheckOut.getDriverId());

        ApiFuture<DocumentSnapshot> future = documentReference.get();

        Query query = collectionReference.whereEqualTo("driverId", requestCheckOut.getDriverId())
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

                ApiFuture<WriteResult> collectionApiFuture = collectionReference.document(documentId).set(attendance);

                Date timestamp = collectionApiFuture.get().getUpdateTime().toDate();

                // Set isWorking = false
                DocumentSnapshot document = future.get();

                Driver driver = document.toObject(Driver.class);

                driver.setIsWorking(false);

                ApiFuture<WriteResult> collectionApiFutureDriver = collectionReferenceDriver.document(document.getId())
                        .set(driver);

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
        TripDto tripDto = tripServiceClient.getRecentTripFromDriver(driverId);

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

        ResponseTotalTrip responseTotalTrip = tripServiceClient.getTotalTrip("drivers", driverId);


        ResponseAverageIncomePerDrive responseAverageIncomePerDrive = tripServiceClient.getAverageIncomePerDrive(driverId);

        //log.info("Test2 " + responseTotalTrip);
        ResponseOverview responseOverview = ResponseOverview.builder()
                .averageIncomePerDrive(responseAverageIncomePerDrive.getAverageIncomePerDrive())
                .totalTrip(responseTotalTrip.getTotalTrip())
                .recentTrip(responseRecentTrip)
                .build();
        //log.info("Test3");

        return responseOverview;
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
