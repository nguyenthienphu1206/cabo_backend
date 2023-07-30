package cabo.backend.booking.service;

import cabo.backend.booking.dto.CreateTripDto;
import cabo.backend.booking.dto.ResponseTripId;
import cabo.backend.booking.entity.GeoPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(url = "${TRIP_SERVICE_URL}", value = "TRIP-SERVICE")
public interface TripServiceClient {

    @GetMapping("/api/v1/trip/get-driver-location/{tripId}")
    GeoPoint getDriverLocation(@PathVariable("tripId") String tripId);

    @PostMapping("/api/v1/trip/create-trip")
    ResponseTripId createTrip(@RequestBody CreateTripDto createTripDto);

    @GetMapping("/api/v1/trip/{tripId}/get-driverId")
    String getDriverIdByTripId(@PathVariable("tripId") String tripId);
}
