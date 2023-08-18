package cabo.backend.customer.service;

import cabo.backend.customer.dto.RecentTrip;
import cabo.backend.customer.dto.ResponseTotalTrip;
import cabo.backend.customer.dto.TripDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(url = "${TRIP_SERVICE_URL}", value = "TRIP-SERVICE")
public interface TripServiceClient {

    @GetMapping("/api/v1/trip/recent-trip/customer/{customerId}")
    RecentTrip getRecentTripFromCustomer(@RequestHeader("Authorization") String bearerToken,
                                         @PathVariable("customerId") String customerId);

    @GetMapping("/api/v1/trip/total-trip/{user}/{id}")
    ResponseTotalTrip getTotalTrip(@RequestHeader("Authorization") String bearerToken,
                                   @PathVariable("user") String userType,
                                   @PathVariable("id") String id);

    @GetMapping("/api/v1/trip/customer/{customerId}")
    List<TripDto> getTripByCustomerId(@RequestHeader("Authorization") String bearerToken,
                                      @PathVariable("customerId") String customerId);
}
