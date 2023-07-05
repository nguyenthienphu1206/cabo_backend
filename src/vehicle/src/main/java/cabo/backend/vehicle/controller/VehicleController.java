package cabo.backend.vehicle.controller;

import cabo.backend.vehicle.dto.VehicleDto;
import cabo.backend.vehicle.service.VehicleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class VehicleController {

    private VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping("/vehicle/register")
    public ResponseEntity<String> registerVehicle(@RequestHeader("Authorization") String bearerToken,
                                                  @RequestBody VehicleDto vehicleDto) {

        String vehicleId = vehicleService.registerVehicle(bearerToken, vehicleDto);

        return new ResponseEntity<>(vehicleId, HttpStatus.CREATED);
    }
}
