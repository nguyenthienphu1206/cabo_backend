package cabo.backend.vehicle.controller;

import cabo.backend.vehicle.dto.DocumentRef;
import cabo.backend.vehicle.dto.VehicleDto;
import cabo.backend.vehicle.service.VehicleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vehicle")
//@CrossOrigin(origins = "*")
public class VehicleController {

    private VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping("/document/{vehicleId}")
    public  ResponseEntity<DocumentRef> getDocumentById(@RequestHeader("Authorization") String bearerToken,
                                                        @PathVariable("vehicleId") String vehicleId) {

        DocumentRef documentRef = vehicleService.getDocumentById(bearerToken, vehicleId);

        return new ResponseEntity<>(documentRef, HttpStatus.OK);
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<VehicleDto> getVehicle(@RequestHeader("Authorization") String bearerToken,
                                                 @PathVariable("vehicleId") String vehicleId) {

        VehicleDto vehicleDto = vehicleService.getVehicle(bearerToken, vehicleId);

        return new ResponseEntity<>(vehicleDto, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerVehicle(@RequestHeader("Authorization") String bearerToken,
                                                  @RequestBody VehicleDto vehicleDto) {

        String vehicleId = vehicleService.registerVehicle(bearerToken, vehicleDto);

        return new ResponseEntity<>(vehicleId, HttpStatus.CREATED);
    }
}
