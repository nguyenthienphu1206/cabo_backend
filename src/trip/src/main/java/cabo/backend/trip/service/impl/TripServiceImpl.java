package cabo.backend.trip.service.impl;

import cabo.backend.trip.dto.ResponseAverageIncomePerDrive;
import cabo.backend.trip.dto.ResponseRecentTripFromCustomer;
import cabo.backend.trip.dto.ResponseRecentTripFromDriver;
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

    private static final String COLLECTION_NAME_DRIVER = "drivers";

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
    public ResponseRecentTripFromDriver getRecentTripFromDriver(String driverId) {

        //String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        Firestore dbFirestore = FirestoreClient.getFirestore();

        CollectionReference collectionReference = dbFirestore.collection(COLLECTION_NAME);

        CollectionReference collectionReferenceDriver = dbFirestore.collection(COLLECTION_NAME_DRIVER);

        DocumentReference driverRef = collectionReferenceDriver.document(driverId);

        Query query = collectionReference.whereEqualTo("driverId", driverRef)
                .orderBy("startTime", Query.Direction.DESCENDING)
                .limit(1);

        try {
            QuerySnapshot querySnapshot = query.get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            for (QueryDocumentSnapshot document : documents) {
                Trip trip = document.toObject(Trip.class);

                ResponseRecentTripFromDriver responseRecentTripFromDriver = ResponseRecentTripFromDriver.builder()
                        .cost(trip.getCost())
                        .distance(trip.getDistance())
                        .startTime(trip.getStartTime())
                        .pickUpTime(trip.getPickUpTime())
                        .endTime(trip.getEndTime())
                        .driverStartLocation(trip.getDriverStartLocation())
                        .toLocation(trip.getToLocation())
                        .build();

                log.info("Successfully");
                return  responseRecentTripFromDriver;
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

    @Override
    public ResponseAverageIncomePerDrive getAverageIncomePerDrive(String driverId) {

        //String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        Firestore dbFirestore = FirestoreClient.getFirestore();

        CollectionReference collectionReference = dbFirestore.collection(COLLECTION_NAME);

        CollectionReference collectionReferenceUser = dbFirestore.collection(COLLECTION_NAME_DRIVER);

        DocumentReference userRef = collectionReferenceUser.document(driverId);

        Query query = collectionReference.whereEqualTo("driverId", userRef);

        double totalIncome = 0.0;
        long count = 1;

        try {
            QuerySnapshot querySnapshot = query.get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            count = documents.size();

            for (QueryDocumentSnapshot document : documents) {
                Trip trip = document.toObject(Trip.class);

                totalIncome += trip.getCost() * 70d / 100;

            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        ResponseAverageIncomePerDrive responseAverageIncomePerDrive = new ResponseAverageIncomePerDrive(totalIncome / count);

        log.info("Successfully");
        return responseAverageIncomePerDrive;
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
