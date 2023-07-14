package cabo.backend.trip.service;

import cabo.backend.trip.dto.ResponseAverageIncomePerDrive;
import cabo.backend.trip.dto.ResponseRecentTripFromCustomer;
import cabo.backend.trip.dto.ResponseRecentTripFromDriver;
import cabo.backend.trip.dto.ResponseTotalTrip;
import org.apache.coyote.Response;

public interface TripService {

    ResponseRecentTripFromCustomer getRecentTripFromCustomer(String customerId);

    ResponseRecentTripFromDriver getRecentTripFromDriver(String driverId);

    ResponseTotalTrip getTotalTrip(String collection, String id);

    ResponseAverageIncomePerDrive getAverageIncomePerDrive(String driverId);
}
