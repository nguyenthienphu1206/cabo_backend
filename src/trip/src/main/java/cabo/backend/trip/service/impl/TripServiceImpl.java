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

    private static final String COLLECTION_NAM_DRIVER = "drivers";

    private final CollectionReference collectionRefDrvier;

    private static final String COLLECTION_NAME_CUSTOMER = "customers";

    private final CollectionReference collectionRefCustomer;

    private static final String COLLECTION_NAME_TRIP = "trips";

    private final CollectionReference collectionRefTrip;

    private Firestore dbFirestore;

    public  TripServiceImpl(Firestore dbFirestore) {

        this.dbFirestore = dbFirestore;

        this.collectionRefDrvier = dbFirestore.collection(COLLECTION_NAM_DRIVER);
        this.collectionRefCustomer = dbFirestore.collection(COLLECTION_NAME_CUSTOMER);
        this.collectionRefTrip = dbFirestore.collection(COLLECTION_NAME_TRIP);
    }

    @Override
    public ResponseRecentTripFromCustomer getRecentTripFromCustomer(String customerId) {

        //String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        DocumentReference customerRef = collectionRefCustomer.document(customerId);

        Query query = collectionRefTrip.whereEqualTo("customerId", customerRef)
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

        DocumentReference driverRef = collectionRefDrvier.document(driverId);

        log.info("DriverRef: " + driverRef);

        Query query = collectionRefTrip.whereEqualTo("driverId", driverRef)
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

        CollectionReference collectionReferenceUser = dbFirestore.collection(collection);

        DocumentReference userRef = collectionReferenceUser.document(id);

        String field = "driverId";
        if (collection.equals("customers")) {
            field = "customerId";
        }

        Query query = collectionRefTrip.whereEqualTo(field, userRef);

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

        DocumentReference userRef = collectionRefDrvier.document(driverId);

        Query query = collectionRefTrip.whereEqualTo("driverId", userRef);

        double totalIncome = 0.0;
        long count = 1;

        try {
            QuerySnapshot querySnapshot = query.get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            count = documents.size();

            if (count == 0) {
                count = 1;
            }

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
