package cabo.backend.trip.service.impl;

import cabo.backend.trip.dto.ResponseRecentTripFromCustomer;
import cabo.backend.trip.dto.ResponseTotalTrip;
import cabo.backend.trip.entity.Trip;
import cabo.backend.trip.service.TripService;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class TripServiceImpl implements TripService {

    private static final String COLLECTION_NAME = "trips";
    private static final String COLLECTION_NAME_CUSTOMER = "customers";

    @Override
    public ResponseRecentTripFromCustomer getRecentTripFromCustomer(String customerId) {

        //String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        Firestore dbFirestore = FirestoreClient.getFirestore();

        CollectionReference collectionReference = dbFirestore.collection(COLLECTION_NAME);

        CollectionReference collectionReferenceCustomer = dbFirestore.collection(COLLECTION_NAME_CUSTOMER);

        DocumentReference customerRef = collectionReferenceCustomer.document(customerId);

        Query query = collectionReference.whereEqualTo("customerId", customerRef)
                .orderBy("startTime", Query.Direction.DESCENDING)
                .limit(1);

        try {
            QuerySnapshot querySnapshot = query.get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            for (QueryDocumentSnapshot document : documents) {
                Trip trip = document.toObject(Trip.class);

                ResponseRecentTripFromCustomer responseRecentTripFromCustomer = ResponseRecentTripFromCustomer.builder()
                        .cost(trip.getCost())
                        .distance(trip.getDistance())
                        .startTime(trip.getStartTime())
                        .endTime(trip.getEndTime())
                        .customerOrderLocation(trip.getCustomerOrderLocation())
                        .toLocation(trip.getToLocation())
                        .paymentType(trip.getPaymentType())
                        .build();

                log.info("Successfully");
                return  responseRecentTripFromCustomer;
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        log.info("Failed");
        return null;
    }

    @Override
    public ResponseTotalTrip getTotalTrip(String collection, String id) {

        //String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        Firestore dbFirestore = FirestoreClient.getFirestore();

        CollectionReference collectionReference = dbFirestore.collection(COLLECTION_NAME);

        CollectionReference collectionReferenceUser = dbFirestore.collection(collection);

        DocumentReference userRef = collectionReferenceUser.document(id);

        String field = "driverId";
        if (collection.equals("customers")) {
            field = "customerId";
        }

        Query query = collectionReference.whereEqualTo(field, userRef);

        AggregateQuerySnapshot snapshot = null;
        try {
            snapshot = query.count().get().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        // Lấy số chuyến đi
        long numberOfTrips = snapshot.getCount();
        ResponseTotalTrip responseTotalTrip = new ResponseTotalTrip(numberOfTrips);

        return responseTotalTrip;
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
