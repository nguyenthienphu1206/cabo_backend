package cabo.backend.driver.service;

import cabo.backend.driver.dto.RequestRegisterVehicle;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(url = "${VEHICLE_SERVICE_URL}", value = "VEHICLE-SERVICE")
public interface VehicleServiceClient {

    @PostMapping("/api/v1/vehicle/register")
    String registerVehicle(@RequestBody RequestRegisterVehicle requestRegisterVehicle);
}
