package cabo.backend.trip.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;


@FeignClient(url = "${DRIVER_SERVICE_URL}", value = "DRIVER-SERVICE")
public interface DriverServiceClient {

    @GetMapping("/api/v1/driver/{driverId}/getName")
    String getNameByDriverId(@RequestHeader("Authorization") String bearerToken,
                             @PathVariable("driverId") String driverId);
}
