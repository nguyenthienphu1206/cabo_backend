package cabo.backend.trip.service;

import cabo.backend.trip.dto.*;
import com.google.cloud.firestore.GeoPoint;

public interface TripService {

    GeoPoint getDriverLocation(String bearerToken, String tripId);

    ResponseTripId createTrip(String bearerToken, CreateTripDto createTripDto);

    ResponseRecentTripFromCustomer getRecentTripFromCustomer(String bearerToken, String customerId);

    ResponseRecentTripFromDriver getRecentTripFromDriver(String bearerToken, String driverId);

    ResponseTotalTrip getTotalTrip(String bearerToken, String userType, String id);

    ResponseAverageIncomePerDrive getAverageIncomePerDrive(String bearerToken, String driverId);

    String getDriverIdByTripId(String bearerToken, String tripId);

    ResponseStatus sendReceivedDriverInfo(String bearerToken, RequestReceivedDriverInfo requestReceivedDriverInfo);

    void deleteTrip(String bearerToken, String tripId);
}
