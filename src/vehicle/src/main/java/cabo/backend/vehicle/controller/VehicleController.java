package cabo.backend.vehicle.controller;

import cabo.backend.vehicle.dto.VehicleDto;
import cabo.backend.vehicle.service.VehicleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vehicle")
public class VehicleController {

    private VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<VehicleDto> getVehicle(@PathVariable("vehicleId") String vehicleId) {

        VehicleDto vehicleDto = vehicleService.getVehicle(vehicleId);

        return new ResponseEntity<>(vehicleDto, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerVehicle(@RequestBody VehicleDto vehicleDto) {

        String vehicleId = vehicleService.registerVehicle(vehicleDto);

        return new ResponseEntity<>(vehicleId, HttpStatus.CREATED);
    }
}
