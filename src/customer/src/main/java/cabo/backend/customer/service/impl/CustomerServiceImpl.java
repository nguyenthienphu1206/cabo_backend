package cabo.backend.customer.service.impl;

import cabo.backend.customer.dto.*;
import cabo.backend.customer.entity.Customer;
import cabo.backend.customer.entity.FcmToken;
import cabo.backend.customer.exception.ResourceNotFoundException;
import cabo.backend.customer.service.BingMapServiceClient;
import cabo.backend.customer.service.BookingServiceClient;
import cabo.backend.customer.service.CustomerService;
import cabo.backend.customer.service.TripServiceClient;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private TripServiceClient tripServiceClient;

    @Autowired
    private BingMapServiceClient bingMapServiceClient;

    @Autowired
    private BookingServiceClient bookingServiceClient;

    private static final String COLLECTION_NAME_CUSTOMER = "customers";

    private static final String COLLECTION_NAME_FCMTOKEN = "fcmTokens";

    private final CollectionReference collectionRefFcmToken;

    private final CollectionReference collectionRefCustomer;

    public CustomerServiceImpl(Firestore dbFirestore) {

        this.collectionRefCustomer = dbFirestore.collection(COLLECTION_NAME_CUSTOMER);
        this.collectionRefFcmToken = dbFirestore.collection(COLLECTION_NAME_FCMTOKEN);
    }


    @Override
    public DocumentRef getDocumentById(String bearerToken, String customerId) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        DocumentReference documentReference = collectionRefCustomer.document(customerId);

        DocumentRef documentRef = DocumentRef.builder()
                .documentReference(documentReference)
                .build();

        return documentRef;
    }

    @Override
    public String getNameByCustomerId(String bearerToken, String customerId) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        DocumentReference documentReference = collectionRefCustomer.document(customerId);

        ApiFuture<DocumentSnapshot> future = documentReference.get();

        String fullName;

        try {
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                fullName = document.getString("fullName");
            }
            else {
                throw new ResourceNotFoundException("Customer", "CustomerId", customerId);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return fullName;
    }

    @Override
    public String getUidByCustomerId(String customerId) {

        DocumentReference documentReference = collectionRefCustomer.document(customerId);

        ApiFuture<DocumentSnapshot> future = documentReference.get();

        String uid;

        try {
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                uid = document.getString("uid");
            }
            else {
                throw new ResourceNotFoundException("Customer", "CustomerId", customerId);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return uid;
    }

    @Override
    public ResponseFullNameAndPhone getNameAndPhoneByCustomerId(String bearerToken, String customerId) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        DocumentReference documentReference = collectionRefCustomer.document(customerId);

        ApiFuture<DocumentSnapshot> future = documentReference.get();

        try {
            DocumentSnapshot document = future.get();

            if (document.exists()) {

                return ResponseFullNameAndPhone.builder()
                        .fullName(document.getString("fullName"))
                        .phoneNumber(document.getString("phoneNumber"))
                        .build();
            }
            else {
                throw new ResourceNotFoundException("Customer", "CustomerId", customerId);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String registerCustomer(String bearerToken, RequestRegisterCustomer requestRegisterCustomer) {

        String idToken = bearerToken.substring(7);

        FirebaseToken decodedToken = decodeToken(idToken);

        String uid = decodedToken.getUid();
        //log.info("UID -----> " + uid);

        Query query = collectionRefCustomer.whereEqualTo("phoneNumber", requestRegisterCustomer.getPhoneNumber());
        ApiFuture<QuerySnapshot> querySnapshotFuture = query.get();

        String customerId;

        try {
            QuerySnapshot querySnapshot = querySnapshotFuture.get();

            if (querySnapshot.isEmpty()) {

                log.info("Empty");

                Customer customer = Customer.builder()
                        .uid(uid)
                        .fullName(requestRegisterCustomer.getFullName())
                        .phoneNumber(requestRegisterCustomer.getPhoneNumber())
                        .avatar("")
                        .vip(false)
                        .isRegisteredOnApp(true)
                        .build();

                DocumentReference documentReference = collectionRefCustomer.document();

                documentReference.set(customer);

                customerId = documentReference.getId();

            } else {
                QueryDocumentSnapshot queryDocumentSnapshot = querySnapshot.getDocuments().get(0);
                Boolean isRegisteredOnApp = queryDocumentSnapshot.getBoolean("isRegisteredOnApp");

                if (Boolean.FALSE.equals(isRegisteredOnApp)) {

                    Customer customerInDB = Customer.builder()
                            .uid(uid)
                            .fullName(requestRegisterCustomer.getFullName())
                            .phoneNumber(requestRegisterCustomer.getPhoneNumber())
                            .avatar("")
                            .vip(false)
                            .isRegisteredOnApp(true)
                            .build();

                    DocumentReference documentReference = collectionRefCustomer.document(queryDocumentSnapshot.getId());

                    documentReference.set(customerInDB);
                }
                customerId = querySnapshot.getDocuments().get(0).getId();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return customerId;
    }

    @Override
    public String createCustomerIfPhoneNumberNotRegistered(String bearerToken, String phoneNumber) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        Query query = collectionRefCustomer.whereEqualTo("phoneNumber", phoneNumber);
        ApiFuture<QuerySnapshot> querySnapshotFuture = query.get();

        QuerySnapshot querySnapshot;

        try {
            querySnapshot = querySnapshotFuture.get();

            if (querySnapshot.isEmpty()) {

                Customer customer = Customer.builder()
                        .uid("")
                        .fullName("")
                        .phoneNumber(phoneNumber)
                        .avatar("")
                        .vip(false)
                        .isRegisteredOnApp(false)
                        .build();

                DocumentReference documentReference = collectionRefCustomer.document();

                documentReference.set(customer);

                return documentReference.getId();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return querySnapshot.getDocuments().get(0).getId();
    }

    @Override
    public String saveCustomer(String bearerToken, CustomerDto customerDto) {

        String idToken = bearerToken.substring(7);

        FirebaseToken decodedToken = decodeToken(idToken);

        ApiFuture<WriteResult> collectionApiFuture = collectionRefCustomer.document().set(customerDto);

        String timestamp = null;
        try {
            timestamp = collectionApiFuture.get().getUpdateTime().toString();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return timestamp;
    }

    @Override
    public CustomerDto getCustomerDetails(String bearerToken, String customerId) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        DocumentReference documentReference = collectionRefCustomer.document(customerId);

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
    public Boolean checkPhoneExistence(String bearerToken, String phoneNumber) {

        String idToken = bearerToken.substring(7);

        FirebaseToken decodedToken = decodeToken(idToken);

        // Lấy tất cả các tài liệu trong collection
        ApiFuture<QuerySnapshot> future = collectionRefCustomer.get();

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
    public ResponseOverview getOverview(String bearerToken, String customerId) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);
        //log.info("Test");
        RecentTrip recentTrip = tripServiceClient.getRecentTripFromCustomer(bearerToken, customerId);

        ResponseRecentTrip responseRecentTrip = null;

        if (recentTrip != null) {
            String customerOrderLocation = bingMapServiceClient.getAddress(recentTrip.getCustomerOrderLocation().getLatitude(),
                    recentTrip.getCustomerOrderLocation().getLongitude());

            String toLocation = bingMapServiceClient.getAddress(recentTrip.getToLocation().getLatitude(),
                    recentTrip.getToLocation().getLongitude());

            responseRecentTrip = ResponseRecentTrip.builder()
                    .cost(recentTrip.getCost())
                    .distance(recentTrip.getDistance())
                    .startTime(recentTrip.getStartTime())
                    .endTime(recentTrip.getEndTime())
                    .customerOrderLocation(customerOrderLocation)
                    .toLocation(toLocation)
                    .paymentType(recentTrip.getPaymentType())
                    .build();
        }



        //log.info("Test1 " + recentTrip);
        ResponseTotalTrip responseTotalTrip = tripServiceClient.getTotalTrip(bearerToken, "customer", customerId);

        //log.info("Test2 " + responseTotalTrip);
        ResponseOverview responseOverview = ResponseOverview.builder()
                .totalTrip(responseTotalTrip.getTotalTrip())
                .recentTrip(responseRecentTrip)
                .build();
        //log.info("Test3");

        return responseOverview;
    }

    @Override
    public ResponseDriverInformation bookADrive(String bearerToken, String customerId, RequestBookADrive requestBookADrive) {

        ResponseDriverInformation responseDriverInformation = bookingServiceClient.getDriverInformation(bearerToken, customerId, requestBookADrive);

        if (responseDriverInformation.getTripId() == null) {
            throw new ResourceNotFoundException("Not Found The Driver");
        }

        return responseDriverInformation;
    }

    @Override
    public void subscribeNotification(String bearerToken, String fcmToken) {

        String idToken = bearerToken.substring(7);

        FirebaseToken decodedToken = decodeToken(idToken);

        String uid = decodedToken.getUid();

        FcmToken savedFcmToken = FcmToken.builder()
                .fcmToken(fcmToken)
                .fcmClient("CUSTOMER")
                .uid(uid)
                .build();

        collectionRefFcmToken.document(uid).set(savedFcmToken);
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
