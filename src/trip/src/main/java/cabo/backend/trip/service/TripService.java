package cabo.backend.trip.service;

import cabo.backend.trip.dto.ResponseRecentTripFromCustomer;
import cabo.backend.trip.dto.ResponseTotalTrip;
import org.apache.coyote.Response;

public interface TripService {

    ResponseRecentTripFromCustomer getRecentTripFromCustomer(String customerId);

    ResponseTotalTrip getTotalTrip(String collection, String id);
}
