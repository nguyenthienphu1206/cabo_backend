package cabo.backend.trip.service;

import cabo.backend.trip.dto.*;

public interface TripService {

    ResponseTripId createTrip(CreateTripDto createTripDto);

    ResponseRecentTripFromCustomer getRecentTripFromCustomer(String customerId);

    ResponseRecentTripFromDriver getRecentTripFromDriver(String driverId);

    ResponseTotalTrip getTotalTrip(String collection, String id);

    ResponseAverageIncomePerDrive getAverageIncomePerDrive(String driverId);

    Boolean checkReceivedTrip(String tripId);
}
