package cabo.backend.customer.service.impl;

import cabo.backend.customer.dto.CustomerDto;
import cabo.backend.customer.entity.Customer;
import cabo.backend.customer.service.CustomerService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {
    private static final String COLLECTION_NAME = "customers";

    @Override
    public String getCustomerId(String idToken, String fullName) {

        String customerId = "";
        String phoneNumber = "";

        FirebaseToken decodedToken = decodeToken(idToken);

        String uid = decodedToken.getUid();
        log.info("UID -----> " + uid);

        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUser(uid);
            phoneNumber = userRecord.getPhoneNumber();

        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }

        Firestore dbFirestore = FirestoreClient.getFirestore();

        Customer customer = new Customer(uid, fullName, phoneNumber, "", false);

        DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document();

        ApiFuture<WriteResult> collectionApiFuture = documentReference.set(customer);

        // Lấy document ID
        customerId = documentReference.getId();

//        Firestore dbFirestore = FirestoreClient.getFirestore();
//
//        CollectionReference collectionReference = dbFirestore.collection(COLLECTION_NAME);
//
//        // Lấy tất cả các tài liệu trong collection
//        ApiFuture<QuerySnapshot> future = collectionReference.get();
//
//        List<QueryDocumentSnapshot> documents = null;
//
//        try {
//            documents = future.get().getDocuments();
//
//            for (QueryDocumentSnapshot document : documents) {
//
//                String uidInDB = document.getString("uid");
//
//                if (uid.equals(uidInDB)) {
//
//                    //customerId = document.getId();
//                    numberPhone = Objects.requireNonNull(document.get("phoneNumber")).toString();
//                    log.info(numberPhone);
//
//                    return numberPhone;
//                }
//            }
//        } catch (InterruptedException | ExecutionException e) {
//            throw new RuntimeException(e);
//        }

        return customerId;
    }

    @Override
    public String saveCustomer(String idToken, CustomerDto customerDto) {

        FirebaseToken decodedToken = decodeToken(idToken);

        Firestore dbFirestore = FirestoreClient.getFirestore();

        ApiFuture<WriteResult> collectionApiFuture = dbFirestore.collection(COLLECTION_NAME).document().set(customerDto);

        String timestamp = null;
        try {
            timestamp = collectionApiFuture.get().getUpdateTime().toString();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return timestamp;
    }

    @Override
    public CustomerDto getCustomerDetails(String idToken, String customerId) {

        FirebaseToken decodedToken = decodeToken(idToken);

        Firestore dbFirestore = FirestoreClient.getFirestore();

        DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document(customerId);

        ApiFuture<DocumentSnapshot> future = documentReference.get();

        DocumentSnapshot document = null;
        try {
            document = future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        CustomerDto customerDto = null;

        if (document.exists()) {
            customerDto = document.toObject(CustomerDto.class);
        }

        return customerDto;
    }

    @Override
    public Boolean checkPhoneExistence(String idToken, String phoneNumber) {

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
