package cabo.backend.booking.service;

import cabo.backend.booking.dto.CreateTripDto;
import cabo.backend.booking.dto.ResponseStatus;
import cabo.backend.booking.dto.ResponseTripId;
import cabo.backend.booking.entity.GeoPoint;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(url = "${TRIP_SERVICE_URL}", value = "TRIP-SERVICE")
public interface TripServiceClient {

    @GetMapping("/api/v1/trip/get-driver-location/{tripId}")
    GeoPoint getDriverLocation(@RequestHeader("Authorization") String bearerToken,
                               @PathVariable("tripId") String tripId);

    @PostMapping("/api/v1/trip/create-trip")
    ResponseTripId createTrip(@RequestHeader("Authorization") String bearerToken,
                              @RequestBody CreateTripDto createTripDto);

    @GetMapping("/api/v1/trip/{tripId}/get-driverId")
    String getDriverIdByTripId(@RequestHeader("Authorization") String bearerToken,
                               @PathVariable("tripId") String tripId);

    @PutMapping("/api/v1/trip/{tripId}")
    ResponseStatus updateTripStatus(@RequestHeader("Authorization") String bearerToken,
                                                           @PathVariable("tripId") String tripId,
                                                           @RequestParam("status") String status);

    @DeleteMapping("/api/v1/trip/{tripId}")
    ResponseStatus deleteTrip(@RequestHeader("Authorization") String bearerToken,
                              @PathVariable("tripId") String tripId);
}
