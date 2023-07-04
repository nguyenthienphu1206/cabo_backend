package cabo.backend.driver.service.impl;

import cabo.backend.driver.dto.DriverDto;
import cabo.backend.driver.dto.ResponseDriverDetails;
import cabo.backend.driver.entity.Driver;
import cabo.backend.driver.service.DriverService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class DriverServiceImpl implements DriverService {

    private ModelMapper modelMapper;

    private static final String COLLECTION_NAME = "drivers";

    @Override
    public String getDriverId(String idToken, String fullName) {
        String driverId = "";
        String phoneNumber = "";

        FirebaseToken decodedToken = null;
        try {
            decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }

        String uid = decodedToken.getUid();
        log.info("UID -----> " + uid);

        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUser(uid);
            phoneNumber = userRecord.getPhoneNumber();

        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }

        Firestore dbFirestore = FirestoreClient.getFirestore();

        Driver driver = new Driver(uid, fullName, phoneNumber, "", null);

        DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document();

        ApiFuture<WriteResult> collectionApiFuture = documentReference.set(driver);

        // Lấy document ID
        driverId = documentReference.getId();

        return driverId;
    }

    @Override
    public String saveDriver(DriverDto driverDto) {
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
    public ResponseDriverDetails getDriverDetails(String driverId) {
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
                .carId(driver.getCarId())
                .build();

        return responseDriverDetails;
    }

    @Override
    public Boolean checkPhoneExistence(String phoneNumber) {
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
}
