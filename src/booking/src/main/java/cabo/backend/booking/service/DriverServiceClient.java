package cabo.backend.booking.service;

import cabo.backend.booking.dto.DocumentRef;
import cabo.backend.booking.dto.DriverInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(url = "${DRIVER_SERVICE_URL}", value = "DRIVER-SERVICE")
public interface DriverServiceClient {

    @GetMapping("/api/v1/driver/document/{driverId}")
    DocumentRef getDocumentById(@RequestHeader("Authorization") String bearerToken,
                                @PathVariable("driverId") String driverId);

    @GetMapping("/api/v1/driver/get-driver-info")
    DriverInfo getDriverInfoById(@RequestHeader("Authorization") String bearerToken,
                                 @RequestParam String tripId,
                                 @RequestParam String driverId);

    @GetMapping("/api/v1/driver/{driverId}/get-uid")
    String getUidByDriverId(@RequestHeader("Authorization") String bearerToken,
                            @PathVariable("driverId") String driverId);

    @GetMapping("/api/v1/driver/get-driver-status")
    Integer getDriverStatusIntByUid(@RequestParam String uid);

    @PutMapping("/api/v1/driver/{driverId}")
    ResponseStatus updateDriverStatus(@RequestHeader("Authorization") String bearerToken,
                                      @PathVariable("driverId") String driverId,
                                      @RequestParam int status);
}
