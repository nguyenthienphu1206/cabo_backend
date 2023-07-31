package cabo.backend.trip.service;

import cabo.backend.trip.dto.*;
import com.google.cloud.firestore.GeoPoint;

public interface TripService {

    GeoPoint getDriverLocation(String tripId);

    ResponseTripId createTrip(CreateTripDto createTripDto);

    ResponseRecentTripFromCustomer getRecentTripFromCustomer(String customerId);

    ResponseRecentTripFromDriver getRecentTripFromDriver(String driverId);

    ResponseTotalTrip getTotalTrip(String collection, String id);

    ResponseAverageIncomePerDrive getAverageIncomePerDrive(String driverId);

    String getDriverIdByTripId(String bearerToken, String tripId);

    ResponseStatus sendReceivedDriverInfo(RequestReceivedDriverInfo requestReceivedDriverInfo);
}
