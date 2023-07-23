package cabo.backend.customer.service.impl;

import cabo.backend.customer.dto.*;
import cabo.backend.customer.entity.Customer;
import cabo.backend.customer.service.BingMapServiceClient;
import cabo.backend.customer.service.CustomerService;
import cabo.backend.customer.service.TripServiceClient;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private TripServiceClient tripServiceClient;

    @Autowired
    private BingMapServiceClient bingMapServiceClient;

    private static final String COLLECTION_NAME_CUSTOMER = "customers";

    private final CollectionReference collectionRefCustomer;

    private Firestore dbFirestore;

    public CustomerServiceImpl(Firestore dbFirestore) {

        this.dbFirestore = dbFirestore;

        this.collectionRefCustomer = dbFirestore.collection(COLLECTION_NAME_CUSTOMER);
    }


    @Override
    public String registerCustomer(String bearerToken, RequestRegisterCustomer requestRegisterCustomer) {

        String idToken = bearerToken.substring(7);

        FirebaseToken decodedToken = decodeToken(idToken);

        String uid = decodedToken.getUid();
        log.info("UID -----> " + uid);

        Query query = collectionRefCustomer.whereEqualTo("phoneNumber", requestRegisterCustomer.getPhoneNumber());
        ApiFuture<QuerySnapshot> querySnapshotFuture = query.get();

        String customerId;

        try {
            QuerySnapshot querySnapshot = querySnapshotFuture.get();

            if (querySnapshot.isEmpty()) {

                Customer customer = Customer.builder()
                        .uid(uid)
                        .fullName(requestRegisterCustomer.getFullName())
                        .phoneNumber(requestRegisterCustomer.getPhoneNumber())
                        .avatar("")
                        .vip(false)
                        .build();

                DocumentReference documentReference = collectionRefCustomer.document();

                ApiFuture<WriteResult> collectionApiFuture = documentReference.set(customer);

                customerId = documentReference.getId();
            }
            else {
                customerId = querySnapshot.getDocuments().get(0).getId();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return customerId;
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

        FirebaseToken decodedToken = decodeToken(idToken);

        Firestore dbFirestore = FirestoreClient.getFirestore();

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
        TripDto tripDto = tripServiceClient.getRecentTripFromCustomer(customerId);

        ResponseRecentTrip responseRecentTrip = null;

        if (tripDto != null) {
            String customerOrderLocation = bingMapServiceClient.getAddress(tripDto.getCustomerOrderLocation().getLatitude(),
                    tripDto.getCustomerOrderLocation().getLongitude());

            String toLocation = bingMapServiceClient.getAddress(tripDto.getToLocation().getLatitude(),
                    tripDto.getToLocation().getLongitude());

            responseRecentTrip = ResponseRecentTrip.builder()
                    .cost(tripDto.getCost())
                    .distance(tripDto.getDistance())
                    .startTime(tripDto.getStartTime())
                    .endTime(tripDto.getEndTime())
                    .customerOrderLocation(customerOrderLocation)
                    .toLocation(toLocation)
                    .paymentType(tripDto.getPaymentType())
                    .build();
        }



        //log.info("Test1 " + tripDto);
        ResponseTotalTrip responseTotalTrip = tripServiceClient.getTotalTrip("customers", customerId);

        //log.info("Test2 " + responseTotalTrip);
        ResponseOverview responseOverview = ResponseOverview.builder()
                .totalTrip(responseTotalTrip.getTotalTrip())
                .recentTrip(responseRecentTrip)
                .build();
        //log.info("Test3");

        return responseOverview;
    }

    @Override
    public ResponseEstimateCostAndDistance getEstimateCostAndDistance(String bearerToken,
                                                                      RequestOriginsAndDestinationsLocation requestOriginsAndDestinationsLocation) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        double fromLat = requestOriginsAndDestinationsLocation.getFromLocation().getLatitude();
        double fromLon = requestOriginsAndDestinationsLocation.getFromLocation().getLongitude();
        double toLat = requestOriginsAndDestinationsLocation.getToLocation().getLatitude();
        double toLon = requestOriginsAndDestinationsLocation.getToLocation().getLongitude();

        Double distance = bingMapServiceClient.calculateDistance(fromLat, fromLon, toLat, toLon);

        Double estimateCost = getEstimateCost(distance);

        ResponseEstimateCostAndDistance responseEstimateCostAndDistance = ResponseEstimateCostAndDistance.builder()
                .cost(estimateCost)
                .distance(distance)
                .build();

        return responseEstimateCostAndDistance;
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

    private Double getEstimateCost(Double distance) {

        double baseCostPerKm = 4300.0;

        double cost = 12500.0;

        if (distance > 2.0) {
            cost += baseCostPerKm * (distance - 2.0);
        }

        return cost;
    }
}
