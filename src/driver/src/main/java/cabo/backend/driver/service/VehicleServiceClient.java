package cabo.backend.driver.service;

import cabo.backend.driver.dto.DocumentRef;
import cabo.backend.driver.dto.RequestRegisterVehicle;
import cabo.backend.driver.dto.VehicleDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(url = "${VEHICLE_SERVICE_URL}", value = "VEHICLE-SERVICE")
public interface VehicleServiceClient {

    @GetMapping("/api/v1/vehicle/document/{vehicleId}")
    DocumentRef getDocumentById(@RequestHeader("Authorization") String bearerToken,
                                @PathVariable("vehicleId") String vehicleId);

    @GetMapping("/api/v1/vehicle/{vehicleId}")
    VehicleDto getVehicle(@RequestHeader("Authorization") String bearerToken,
                          @PathVariable("vehicleId") String vehicleId);

    @PostMapping("/api/v1/vehicle/register")
    String registerVehicle(@RequestHeader("Authorization") String bearerToken,
                           @RequestBody RequestRegisterVehicle requestRegisterVehicle);
}
