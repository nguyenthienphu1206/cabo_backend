package cabo.backend.driver.service;

import cabo.backend.driver.dto.RequestRegisterVehicle;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "http://localhost:8082", value = "VEHICLE-SERVICE")
public interface VehicleServiceClient {

    @PostMapping("/api/v1/vehicle/register")
    String registerVehicle(@RequestHeader("Authentication") String bearerToken, @RequestBody RequestRegisterVehicle requestRegisterVehicle);
}
