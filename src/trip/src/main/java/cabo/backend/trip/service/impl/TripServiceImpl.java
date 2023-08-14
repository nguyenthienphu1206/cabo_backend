package cabo.backend.trip.service.impl;

import cabo.backend.trip.dto.*;
import cabo.backend.trip.entity.Trip;
import cabo.backend.trip.exception.ResourceNotFoundException;
import cabo.backend.trip.service.CustomerServiceClient;
import cabo.backend.trip.service.DriverServiceClient;
import cabo.backend.trip.service.TripService;
import cabo.backend.trip.utils.AppConstants;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class TripServiceImpl implements TripService {

    @Autowired
    private CustomerServiceClient customerServiceClient;

    @Autowired
    private DriverServiceClient driverServiceClient;

    private static final String COLLECTION_NAME_TRIP = "trips";

    private final CollectionReference collectionRefTrip;

    private Firestore dbFirestore;

    public  TripServiceImpl(Firestore dbFirestore) {

        this.dbFirestore = dbFirestore;

        this.collectionRefTrip = dbFirestore.collection(COLLECTION_NAME_TRIP);
    }

    @Override
    public GeoPoint getDriverLocation(String bearerToken, String tripId) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        DocumentReference documentReference = collectionRefTrip.document(tripId);

        ApiFuture<DocumentSnapshot> future = documentReference.get();

        GeoPoint driverLocation = null;

        try {
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                Trip trip = document.toObject(Trip.class);

                if (trip != null) {
                    driverLocation = trip.getDriverStartLocation();
                }
            }
            else {
                throw new ResourceNotFoundException("Document", "TripId", tripId);
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return driverLocation;
    }

    @Override
    public ResponseTripId createTrip(String bearerToken, CreateTripDto createTripDto) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        GeoPoint customerOrderLocation = new GeoPoint(createTripDto.getCustomerOrderLocation().getLatitude(),
                createTripDto.getCustomerOrderLocation().getLongitude());

        GeoPoint driverStartLocation = new GeoPoint(createTripDto.getDriverStartLocation().getLatitude(),
                createTripDto.getDriverStartLocation().getLongitude());

        GeoPoint toLocation = new GeoPoint(createTripDto.getToLocation().getLatitude(),
                createTripDto.getToLocation().getLongitude());

        Trip trip = Trip.builder()
                .cost(createTripDto.getCost())
                .customerId(createTripDto.getCustomerId())
                .driverId(createTripDto.getDriverId())
                .distance(createTripDto.getDistance())
                .startTime(createTripDto.getStartTime())
                .pickUpTime(createTripDto.getPickUpTime())
                .endTime(createTripDto.getEndTime())
                .customerOrderLocation(customerOrderLocation)
                .driverStartLocation(driverStartLocation)
                .toLocation(toLocation)
                .paymentType(createTripDto.getPaymentType())
                .updatedAt(Instant.now().getEpochSecond())
                .build();

        DocumentReference documentReference = collectionRefTrip.document();
        documentReference.set(trip);

        ResponseTripId responseTripId = new ResponseTripId(new Date(), documentReference.getId());

        return responseTripId;
    }

    @Override
    public TripDto getTripById(String bearerToken, String tripId) {

        //String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        DocumentReference documentReference = collectionRefTrip.document(tripId);

        ApiFuture<DocumentSnapshot> future = documentReference.get();

        try {
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                Trip trip = document.toObject(Trip.class);

                if (trip != null) {

                    return convertTripToTripDto(bearerToken, trip, tripId);
                }
            }
            else {
                throw new ResourceNotFoundException("Document", "TripId", tripId);
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public List<TripDto> getAllTrip(String bearerToken) {

        //String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        List<TripDto> tripDtos = new ArrayList<>();

        collectionRefTrip.listDocuments().forEach(documentReference -> {
            try {
                DocumentSnapshot document = documentReference.get().get();

                Trip trip = document.toObject(Trip.class);

                if (trip != null) {

                    TripDto tripDto = convertTripToTripDto(bearerToken, trip, document.getId());

                    tripDtos.add(tripDto);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return tripDtos;
    }

    @Override
    public List<TripDto> getTripByCustomerId(String bearerToken, String customerId) {

        //String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        List<TripDto> tripDtos = new ArrayList<>();

        DocumentRef documentRef = customerServiceClient.getDocumentById(bearerToken, customerId);

        Query query = collectionRefTrip.whereEqualTo("customerId", documentRef);

        try {
            QuerySnapshot querySnapshot = query.get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            for (QueryDocumentSnapshot document : documents) {
                Trip trip = document.toObject(Trip.class);

                TripDto tripDto = convertTripToTripDto(bearerToken, trip, document.getId());

                tripDtos.add(tripDto);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return tripDtos;
    }

    @Override
    public List<TripDto> getTripByDriverId(String bearerToken, String driverId) {

        //String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        List<TripDto> tripDtos = new ArrayList<>();

        DocumentRef documentRef = driverServiceClient.getDocumentById(bearerToken, driverId);

        Query query = collectionRefTrip.whereEqualTo("driverId", documentRef);

        try {
            QuerySnapshot querySnapshot = query.get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            for (QueryDocumentSnapshot document : documents) {
                Trip trip = document.toObject(Trip.class);

                TripDto tripDto = convertTripToTripDto(bearerToken, trip, document.getId());

                tripDtos.add(tripDto);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return tripDtos;
    }

    @Override
    public ResponseRecentTripFromCustomer getRecentTripFromCustomer(String bearerToken, String customerId) {

        //String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        DocumentRef documentRef = customerServiceClient.getDocumentById(bearerToken, customerId);

        DocumentReference customerRef = documentRef.getDocumentReference();

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
    public ResponseRecentTripFromDriver getRecentTripFromDriver(String bearerToken, String driverId) {

        //String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        DocumentRef documentRef = driverServiceClient.getDocumentById(bearerToken, driverId);

        DocumentReference driverRef = documentRef.getDocumentReference();

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
    public ResponseTotalTrip getTotalTrip(String bearerToken, String userType, String id) {

        //String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        DocumentRef documentRef = driverServiceClient.getDocumentById(bearerToken, id);
        String field = "driverId";

        if (userType.equals("customer")) {
            documentRef = customerServiceClient.getDocumentById(bearerToken, id);
            field = "customerId";
        }

        DocumentReference userRef = documentRef.getDocumentReference();

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
    public ResponseAverageIncomePerDrive getAverageIncomePerDrive(String bearerToken, String driverId) {

        //String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        DocumentRef documentRef = driverServiceClient.getDocumentById(bearerToken, driverId);

        DocumentReference driverRef = documentRef.getDocumentReference();

        Query query = collectionRefTrip.whereEqualTo("driverId", driverRef);

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

    @Override
    public String getDriverIdByTripId(String bearerToken, String tripId) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        String driverId  = null;

        DocumentReference documentReference = collectionRefTrip.document(tripId);

        ApiFuture<DocumentSnapshot> future = documentReference.get();

        try {
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                Trip trip = document.toObject(Trip.class);

                if (trip != null) {

                    DocumentReference driverIdRef = trip.getDriverId();

                    if (driverIdRef != null) {
                        driverId = driverIdRef.getId();

                        return driverId;
                    }
                }
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public ResponseStatus sendReceivedDriverInfo(String bearerToken, RequestReceivedDriverInfo requestReceivedDriverInfo) {

//        String idToken = bearerToken.substring(7);
//
//        FirebaseToken decodedToken = decodeToken(idToken);

        DocumentReference documentReference = collectionRefTrip.document(requestReceivedDriverInfo.getTripId());

        ApiFuture<DocumentSnapshot> future = documentReference.get();

        try {
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                Trip trip = document.toObject(Trip.class);

                if (trip != null && trip.getDriverId() == null) {

                    GeoPoint driverStartLocation = new GeoPoint(requestReceivedDriverInfo.getCurrentLocation().getLatitude(),
                            requestReceivedDriverInfo.getCurrentLocation().getLongitude());
                    trip.setDriverId(requestReceivedDriverInfo.getDriverId());
                    trip.setDriverStartLocation(driverStartLocation);

                    documentReference.set(trip);

                    ResponseStatus responseStatus = new ResponseStatus(new Date(), "Successfully");

                    return responseStatus;
                }
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return new ResponseStatus(new Date(), "Failed");
    }

    @Override
    public TripDto updateTripStatus(String bearerToken, String tripId, String status) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        DocumentReference documentReference = collectionRefTrip.document(tripId);

        ApiFuture<DocumentSnapshot> future = documentReference.get();

        DocumentSnapshot document;

        TripDto tripDto;

        try {
            document = future.get();

            if (document.exists()) {
                Trip trip = document.toObject(Trip.class);

                if (trip != null) {

                    AppConstants.StatusTrip statusTrip = AppConstants.StatusTrip.valueOf(status);

                    trip.setStatus(statusTrip.toString());
                    trip.setUpdatedAt(Instant.now().getEpochSecond());

                    documentReference.set(trip);

                    tripDto = convertTripToTripDto(bearerToken, trip, tripId);

                    return tripDto;
                }
            }
            else {
                throw new ResourceNotFoundException("Trip", "tripId", tripId);
            }

        } catch (InterruptedException | ExecutionException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public void deleteTrip(String bearerToken, String tripId) {

        String idToken = bearerToken.substring(7);

        log.info("idToken");

        //FirebaseToken decodedToken = decodeToken(idToken);

        DocumentReference documentReference = collectionRefTrip.document(tripId);

        documentReference.delete();
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

    private TripDto convertTripToTripDto(String bearerToken, Trip trip, String tripId) {

        String customerName = customerServiceClient.getNameByCustomerId(bearerToken, tripId);
        String driverName = driverServiceClient.getNameByDriverId(bearerToken, tripId);

        TripDto tripDto = TripDto.builder()
                .cost(trip.getCost())
                .customerName(customerName)
                .driverName(driverName)
                .distance(trip.getDistance())
                .startTime(trip.getStartTime())
                .pickUpTime(trip.getPickUpTime())
                .endTime(trip.getEndTime())
                .customerOrderLocation(trip.getCustomerOrderLocation())
                .driverStartLocation(trip.getDriverStartLocation())
                .toLocation(trip.getToLocation())
                .paymentType(trip.getPaymentType())
                .status(trip.getStatus())
                .updatedAt(trip.getUpdatedAt())
                .build();

        return tripDto;
    }
}
