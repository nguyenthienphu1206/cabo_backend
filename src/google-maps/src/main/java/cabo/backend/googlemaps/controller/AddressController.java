package cabo.backend.googlemaps.controller;

import cabo.backend.googlemaps.service.GeocodingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AddressController {

    private final GeocodingService geocodingService;

    public AddressController(GeocodingService geocodingService) {
        this.geocodingService = geocodingService;
    }

    @GetMapping("/google-maps/address")
    public ResponseEntity<String> getAddress(@RequestParam double latitude, @RequestParam double longitude) {
        try {
            String address = geocodingService.getAddress(latitude, longitude);
            return ResponseEntity.ok(address);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

