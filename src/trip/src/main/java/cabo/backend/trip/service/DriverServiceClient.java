package cabo.backend.trip.service;

import cabo.backend.trip.dto.ResponseStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@FeignClient(url = "${DRIVER_SERVICE_URL}", value = "DRIVER-SERVICE")
public interface DriverServiceClient {

    @GetMapping("/api/v1/driver/{driverId}/getName")
    String getNameByDriverId(@RequestHeader("Authorization") String bearerToken,
                             @PathVariable("driverId") String driverId);


    @PutMapping("/api/v1/driver/{driverId}")
    ResponseStatus updateDriverStatus(@RequestHeader("Authorization") String bearerToken,
                                      @PathVariable("driverId") String driverId,
                                      @RequestParam String status);
}
