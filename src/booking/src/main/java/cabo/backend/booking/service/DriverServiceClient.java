package cabo.backend.booking.service;

import cabo.backend.booking.dto.DriverInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(url = "${DRIVER_SERVICE_URL}", value = "DRIVER-SERVICE")
public interface DriverServiceClient {

    @GetMapping("/api/v1/driver/get-driver-info")
    DriverInfo getDriverInfoById(@RequestHeader("Authorization") String bearerToken,
                                 @RequestParam String tripId,
                                 @RequestParam String driverId);
}
