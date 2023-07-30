package cabo.backend.driver.service;

import cabo.backend.driver.dto.RequestRegisterVehicle;
import cabo.backend.driver.dto.VehicleDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(url = "${VEHICLE_SERVICE_URL}", value = "VEHICLE-SERVICE")
public interface VehicleServiceClient {

    @GetMapping("/api/v1/vehicle/{vehicleId}")
    VehicleDto getVehicle(@PathVariable("vehicleId") String vehicleId);

    @PostMapping("/api/v1/vehicle/register")
    String registerVehicle(@RequestBody RequestRegisterVehicle requestRegisterVehicle);
}
