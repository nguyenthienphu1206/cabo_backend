package cabo.backend.bingmap.controller;

import cabo.backend.bingmap.dto.ResponseListAddresses;
import cabo.backend.bingmap.service.BingMapService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class BingMapController {

    private BingMapService bingMapService;

    public BingMapController(BingMapService bingMapService) {
        this.bingMapService = bingMapService;
    }

    @GetMapping("/bing-map/address")
    public ResponseEntity<String> getAddress(@RequestParam double latitude, @RequestParam double longitude) {
        try {
            String address = bingMapService.getAddress(latitude, longitude);
            return ResponseEntity.ok(address);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/bing-map/get-list-places")

    public ResponseEntity<List<ResponseListAddresses>> getRealPlacesBySearchQuery(@RequestParam String searchLocation) {

        return new ResponseEntity<>(bingMapService.getRealPlacesBySearchQuery(searchLocation), HttpStatus.OK);
    }

    @GetMapping("/bing-map/address/location")

    public ResponseEntity<List<ResponseListAddresses>> getAddressByQuery(@RequestParam String query) {

        return new ResponseEntity<>(bingMapService.getAddressByQuery(query), HttpStatus.OK);
    }

    @GetMapping("/bing-map/get-distance")
    public ResponseEntity<Double> calculateDistance(@RequestParam double latitude_1, @RequestParam double longitude_1,
                                                    @RequestParam double latitude_2, @RequestParam double longitude_2) {
        try {
            Double distance = bingMapService.calculateDistance(latitude_1, longitude_1, latitude_2, longitude_2);

            return ResponseEntity.ok(distance);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

