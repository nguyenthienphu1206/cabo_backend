package cabo.backend.customer.service;

import cabo.backend.customer.dto.ResponseTotalTrip;
import cabo.backend.customer.dto.TripDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(url = "${TRIP_SERVICE_URL}", value = "TRIP-SERVICE")
public interface TripServiceClient {

    @GetMapping("/api/v1/trip/recent-trip/customer/{customerId}")
    TripDto getRecentTripFromCustomer(@PathVariable("customerId") String customerId);

    @GetMapping("/api/v1/trip/total-trip/{collection}/{id}")
    ResponseTotalTrip getTotalTrip(@PathVariable("collection") String collection,
                                   @PathVariable("id") String id);
}
