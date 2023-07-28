package cabo.backend.booking.service;

import cabo.backend.booking.dto.CreateTripDto;
import cabo.backend.booking.dto.ResponseCreatedTrip;
import cabo.backend.booking.dto.ResponseTripId;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(url = "${TRIP_SERVICE_URL}", value = "TRIP-SERVICE")
public interface TripServiceClient {

    @PostMapping("/api/v1/trip/create-trip")
    ResponseTripId createTrip(@RequestBody CreateTripDto createTripDto);

    @GetMapping("/api/v1/trip/check-received-trip")
    Boolean checkReceivedTrip(@RequestParam String tripId);
}
