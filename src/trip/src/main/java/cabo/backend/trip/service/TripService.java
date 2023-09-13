package cabo.backend.trip.service;

import cabo.backend.trip.dto.*;
import com.google.cloud.firestore.GeoPoint;

import java.util.List;

public interface TripService {

    GeoPoint getDriverLocation(String bearerToken, String tripId);

    ResponseTripId createTrip(String bearerToken, CreateTripDto createTripDto);

    TripDto getTripById(String bearerToken, String tripId);

    List<TripDto> getAllTrip(String bearerToken);

    List<TripDto> getTripByCustomerId(String bearerToken, String customerId, int pageNumber);

    List<TripDto> getTripByDriverId(String bearerToken, String driverId);

    ResponseRecentTripFromCustomer getRecentTripFromCustomer(String bearerToken, String customerId);

    ResponseRecentTripFromDriver getRecentTripFromDriver(String bearerToken, String driverId);

    ResponseTotalTrip getTotalTrip(String bearerToken, String userType, String id);

    ResponseAverageIncomePerDrive getAverageIncomePerDrive(String bearerToken, String driverId);

    String getDriverIdByTripId(String bearerToken, String tripId);

    String getTripStatusById(String bearerToken, String tripId);

    IncomeDto getTotalIncome(String bearerToken, String driverId);

    IncomeDto getIncomeByTimeRange(String bearerToken, String driverId, long startDate, long endDate);

    ResponseStatus acceptDrive(String bearerToken, RequestReceivedDriverInfo requestReceivedDriverInfo);

    ResponseStatus confirmPickupLocationArrival(String bearerToken, PickUpAndCompletionLocation pickUpLocation);

    ResponseStatus confirmDriverTripCompletion(String bearerToken, PickUpAndCompletionLocation completionLocation);

    ResponseStatus updateTripStatus(String bearerToken, String tripId, String status);

    void deleteTrip(String bearerToken, String tripId);

    void deleteAllTrips();
}
