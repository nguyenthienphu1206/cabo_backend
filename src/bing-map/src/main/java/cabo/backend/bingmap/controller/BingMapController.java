package cabo.backend.bingmap.controller;

import cabo.backend.bingmap.dto.RequestOriginsAndDestinationsLocation;
import cabo.backend.bingmap.dto.ResponseEstimateCostAndDistance;
import cabo.backend.bingmap.dto.ResponseListAddresses;
import cabo.backend.bingmap.dto.TravelInfor;
import cabo.backend.bingmap.service.BingMapService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
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

        HttpHeaders headers = new HttpHeaders();
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        headers.set("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization");
        return ResponseEntity.ok().headers(headers).body(bingMapService.getRealPlacesBySearchQuery(searchLocation));
    }

    @GetMapping("/bing-map/address/location")
    public ResponseEntity<List<ResponseListAddresses>> getAddressByQuery(@RequestParam String query) {

        return new ResponseEntity<>(bingMapService.getAddressByQuery(query), HttpStatus.OK);
    }

    @GetMapping("/bing-map/get-distance")
    public ResponseEntity<Double> calculateDistance(@RequestParam double latitude_1, @RequestParam double longitude_1,
                                                    @RequestParam double latitude_2, @RequestParam double longitude_2) {
        try {
            double distance = bingMapService.calculateDistance(latitude_1, longitude_1, latitude_2, longitude_2);

            return ResponseEntity.ok(distance);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/bing-map/get-distance-and-time")
    public ResponseEntity<TravelInfor> getDistanceAndTime(@RequestParam double latitude_1, @RequestParam double longitude_1,
                                                          @RequestParam double latitude_2, @RequestParam double longitude_2) {
        try {
            TravelInfor travelInfor = bingMapService.getDistanceAndTime(latitude_1, longitude_1, latitude_2, longitude_2);

            return ResponseEntity.ok(travelInfor);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/bing-map/drive-booking/estimate-cost")
    public ResponseEntity<ResponseEstimateCostAndDistance> getEstimateCostAndDistance(@RequestBody RequestOriginsAndDestinationsLocation requestOriginsAndDestinationsLocation) {

        ResponseEstimateCostAndDistance responseEstimateCostAndDistance = bingMapService.getEstimateCostAndDistance(requestOriginsAndDestinationsLocation);

        return new ResponseEntity<>(responseEstimateCostAndDistance, HttpStatus.OK);
    }
}

