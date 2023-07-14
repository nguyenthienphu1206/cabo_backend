package cabo.backend.driver.service;

import cabo.backend.driver.dto.ResponseAverageIncomePerDrive;
import cabo.backend.driver.dto.ResponseTotalTrip;
import cabo.backend.driver.dto.TripDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(url = "${TRIP_SERVICE_URL}", value = "TRIP-SERVICE")
public interface TripServiceClient {

    @GetMapping("/api/v1/trip/recent-trip/driver/{driverId}")
    TripDto getRecentTripFromDriver(@PathVariable("driverId") String driverId);

    @GetMapping("/api/v1/trip/total-trip/{collection}/{id}")
    ResponseTotalTrip getTotalTrip(@PathVariable("collection") String collection,
                                   @PathVariable("id") String id);


    @GetMapping("/api/v1/trip/average-income/driver/{driverId}")
    ResponseAverageIncomePerDrive getAverageIncomePerDrive(@PathVariable("driverId") String driverId);
}
