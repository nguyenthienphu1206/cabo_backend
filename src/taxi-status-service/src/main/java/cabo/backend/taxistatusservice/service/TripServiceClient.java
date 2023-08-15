package cabo.backend.taxistatusservice.service;

import cabo.backend.taxistatusservice.dto.TripDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(url = "${TRIP_SERVICE_URL}", value = "TRIP-SERVICE")
public interface TripServiceClient {

    @PutMapping("/api/v1/trip/{tripId}")
    TripDto updateTripStatus(@RequestHeader("Authorization") String bearerToken,
                             @PathVariable("tripId") String tripId,
                             @RequestParam("status") String status);

}
