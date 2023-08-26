package cabo.backend.trip.service.impl;

import cabo.backend.trip.dto.*;
import cabo.backend.trip.entity.Trip;
import cabo.backend.trip.exception.ResourceNotFoundException;
import cabo.backend.trip.service.BingMapServiceClient;
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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired
    private BingMapServiceClient bingMapServiceClient;

    private static final String COLLECTION_NAME_TRIP = "trips";

    private final CollectionReference collectionRefTrip;

    private static final String COLLECTION_NAME_FCMTOKEN = "fcmTokens";

    private final CollectionReference collectionRefFcmToken;

    private Firestore dbFirestore;

    @Value("${rabbitmq.exchange.status.name}")
    private String statusExchange;

    @Value("${rabbitmq.binding.status_done.routing.key}")
    private String statusDoneRoutingKey;

    private final RabbitTemplate rabbitTemplate;

    public  TripServiceImpl(Firestore dbFirestore, RabbitTemplate rabbitTemplate) {

        this.dbFirestore = dbFirestore;

        this.collectionRefTrip = dbFirestore.collection(COLLECTION_NAME_TRIP);

        this.collectionRefFcmToken = dbFirestore.collection(COLLECTION_NAME_FCMTOKEN);

        this.rabbitTemplate = rabbitTemplate;
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

                    return convertTripToTripDto(bearerToken, trip);
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

                    TripDto tripDto = convertTripToTripDto(bearerToken, trip);

                    tripDtos.add(tripDto);
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        return tripDtos;
    }

    @Override
    public List<TripDto> getTripByCustomerId(String bearerToken, String customerId) {

        //String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        List<TripDto> tripDtos = new ArrayList<>();

        Query query = collectionRefTrip.whereEqualTo("customerId", customerId);

        try {
            QuerySnapshot querySnapshot = query.get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            for (QueryDocumentSnapshot document : documents) {
                Trip trip = document.toObject(Trip.class);

                TripDto tripDto = convertTripToTripDto(bearerToken, trip);

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

        Query query = collectionRefTrip.whereEqualTo("driverId", driverId);

        try {
            QuerySnapshot querySnapshot = query.get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            for (QueryDocumentSnapshot document : documents) {
                Trip trip = document.toObject(Trip.class);

                TripDto tripDto = convertTripToTripDto(bearerToken, trip);

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

        Query query = collectionRefTrip.whereEqualTo("customerId", customerId)
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

        Query query = collectionRefTrip.whereEqualTo("driverId", driverId)
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
        String field = "driverId";

        if (userType.equals("customer")) {
            field = "customerId";
        }

        Query query = collectionRefTrip.whereEqualTo(field, id);

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

        Query query = collectionRefTrip.whereEqualTo("driverId", driverId);

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
                    return trip.getDriverId();
                }
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public String getTripStatusById(String bearerToken, String tripId) {

        String idToken = bearerToken.substring(7);
//
//        FirebaseToken decodedToken = decodeToken(idToken);

        DocumentReference documentReference = collectionRefTrip.document(tripId);

        ApiFuture<DocumentSnapshot> future = documentReference.get();

        try {
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                Trip trip = document.toObject(Trip.class);

                if (trip != null) {

                   return trip.getStatus();
                }
            }
            else {
                throw new ResourceNotFoundException("Trip", "tripId", tripId);
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public ResponseStatus acceptDrive(String bearerToken, RequestReceivedDriverInfo requestReceivedDriverInfo) {

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

                    ResponseStatus responseStatus = new ResponseStatus(new Date(), "Successful");

                    return responseStatus;
                }
            }
            else {
                throw new ResourceNotFoundException("Trip", "tripId", requestReceivedDriverInfo.getTripId());
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return new ResponseStatus(new Date(), "Drive is received");
    }

    @Override
    public ResponseStatus confirmPickupLocationArrival(String bearerToken, PickUpAndCompletionLocation pickUpLocation) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        DocumentReference documentReference = collectionRefTrip.document(pickUpLocation.getTripId());

        ApiFuture<DocumentSnapshot> future = documentReference.get();

        ResponseStatus responseStatus = new ResponseStatus();

        try {
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                Trip trip = document.toObject(Trip.class);

                if (trip != null) {
                    GeoPoint customerOrderLocation = trip.getCustomerOrderLocation();

                    Double distance = bingMapServiceClient.calculateDistance(pickUpLocation.getCurrentLocation().getLatitude(),
                            pickUpLocation.getCurrentLocation().getLongitude(),
                            customerOrderLocation.getLatitude(),
                            customerOrderLocation.getLongitude());

                    if (distance > 0.1) {
                        responseStatus.setTimestamp(new Date());
                        responseStatus.setMessage("Failed, too far from pick up location");
                    }
                    else {
                        trip.setPickUpTime(Instant.now().getEpochSecond());
                        trip.setStatus(AppConstants.StatusTrip.TRIP_STATUS_INPROGRESS.name());
                        trip.setUpdatedAt(Instant.now().getEpochSecond());
                        documentReference.set(trip);

                        responseStatus.setTimestamp(new Date());
                        responseStatus.setMessage("Successful");
                    }
                }
            }
            else {
                throw new ResourceNotFoundException("Trip", "tripId", pickUpLocation.getTripId());
            }

        } catch (InterruptedException | ExecutionException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        }

        return responseStatus;
    }

    @Override
    public ResponseStatus confirmDriverTripCompletion(String bearerToken, PickUpAndCompletionLocation completionLocation) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        DocumentReference documentReference = collectionRefTrip.document(completionLocation.getTripId());

        ApiFuture<DocumentSnapshot> future = documentReference.get();

        ResponseStatus responseStatus = new ResponseStatus();

        try {
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                Trip trip = document.toObject(Trip.class);

                if (trip != null) {
                    GeoPoint customerOrderLocation = trip.getCustomerOrderLocation();

                    Double distance = bingMapServiceClient.calculateDistance(completionLocation.getCurrentLocation().getLatitude(),
                            completionLocation.getCurrentLocation().getLongitude(),
                            customerOrderLocation.getLatitude(),
                            customerOrderLocation.getLongitude());

                    if (distance > 0.05) {
                        responseStatus.setTimestamp(new Date());
                        responseStatus.setMessage("Failed, too far from completed location");
                    }
                    else {
                        trip.setPickUpTime(Instant.now().getEpochSecond());
                        trip.setStatus(AppConstants.StatusTrip.TRIP_STATUS_DONE.name());
                        trip.setUpdatedAt(Instant.now().getEpochSecond());
                        documentReference.set(trip);

                        NotificationDriveDone notificationDriveDone = NotificationDriveDone.builder()
                                .fcmToken(getFcmTokenCustomer(trip.getCustomerId()))
                                .tripId(document.getId())
                                .build();

                        // send to drive-status,then send to customer
                        rabbitTemplate.convertAndSend(statusExchange, statusDoneRoutingKey, notificationDriveDone);

                        responseStatus.setTimestamp(new Date());
                        responseStatus.setMessage("Successful");
                    }
                }
            }
            else {
                throw new ResourceNotFoundException("Trip", "tripId", completionLocation.getTripId());
            }

        } catch (InterruptedException | ExecutionException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        }

        return responseStatus;
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

                    tripDto = convertTripToTripDto(bearerToken, trip);

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

    @Override
    public void deleteAllTrips() {
        collectionRefTrip.listDocuments().forEach(DocumentReference::delete);
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

    private TripDto convertTripToTripDto(String bearerToken, Trip trip) {

        String customerName = customerServiceClient.getNameByCustomerId(bearerToken, trip.getCustomerId());
        String driverName = driverServiceClient.getNameByDriverId(bearerToken, trip.getDriverId());

        return TripDto.builder()
                .cost(trip.getCost())
                .customerName(customerName)
                .driverName(driverName)
                .distance(trip.getDistance())
                .startTime(trip.getStartTime())
                .pickUpTime(trip.getPickUpTime())
                .endTime(trip.getEndTime())
                .customerOrderLocation(convertToGeoPointEntity(trip.getCustomerOrderLocation()))
                .driverStartLocation(convertToGeoPointEntity(trip.getDriverStartLocation()))
                .toLocation(convertToGeoPointEntity(trip.getToLocation()))
                .paymentType(trip.getPaymentType())
                .status(trip.getStatus())
                .updatedAt(trip.getUpdatedAt())
                .build();
    }

    private cabo.backend.trip.entity.GeoPoint convertToGeoPointEntity(GeoPoint geoPointFirestore) {

        cabo.backend.trip.entity.GeoPoint geoPoint = new cabo.backend.trip.entity.GeoPoint();

        geoPoint.setLatitude(geoPointFirestore.getLatitude());
        geoPoint.setLongitude(geoPointFirestore.getLongitude());

        return geoPoint;
    }

    private String getFcmTokenCustomer(String customerId) {

        Query query = collectionRefFcmToken.whereEqualTo("fcmClient", "CUSTOMER")
                .whereEqualTo("uid", customerId);

        ApiFuture<QuerySnapshot> querySnapshotFuture = query.get();

        QuerySnapshot querySnapshot;

        try {
            querySnapshot = querySnapshotFuture.get();
            if (!querySnapshot.isEmpty()) {
                QueryDocumentSnapshot queryDocumentSnapshot = querySnapshot.getDocuments().get(0);

                return queryDocumentSnapshot.getString("fcmToken");
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return "";
    }
}
