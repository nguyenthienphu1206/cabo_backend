package cabo.backend.driver.service;

import cabo.backend.driver.dto.*;
import cabo.backend.driver.entity.GeoPoint;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(url = "${TRIP_SERVICE_URL}", value = "TRIP-SERVICE")
public interface TripServiceClient {

    @GetMapping("/api/v1/trip/get-driver-location/{tripId}")
    GeoPoint getDriverLocation(@RequestHeader("Authorization") String bearerToken,
                               @PathVariable("tripId") String tripId);

    @GetMapping("/api/v1/trip/recent-trip/driver/{driverId}")
    RecentTrip getRecentTripFromDriver(@RequestHeader("Authorization") String bearerToken,
                                       @PathVariable("driverId") String driverId);

    @GetMapping("/api/v1/trip/total-trip/{user}/{id}")
    ResponseTotalTrip getTotalTrip(@RequestHeader("Authorization") String bearerToken,
                                   @PathVariable("user") String userType,
                                   @PathVariable("id") String id);

    @GetMapping("/api/v1/trip/driver/{driverId}")
    List<TripDto> getTripByDriverId(@RequestHeader("Authorization") String bearerToken,
                                    @PathVariable("driverId") String driverId);


    @GetMapping("/api/v1/trip/average-income/driver/{driverId}")
    ResponseAverageIncomePerDrive getAverageIncomePerDrive(@RequestHeader("Authorization") String bearerToken,
                                                           @PathVariable("driverId") String driverId);

}
