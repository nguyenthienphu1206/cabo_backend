package cabo.backend.booking.service;

import cabo.backend.booking.dto.TravelInfor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(url = "${BING_MAP_SERVICE_URL}", value = "BING-MAP-SERVICE")
public interface BingMapServiceClient {

    @GetMapping("/api/v1/bing-map/address")
    String getAddress(@RequestParam double latitude, @RequestParam double longitude);

    @GetMapping("/api/v1/bing-map/get-distance")
    Double calculateDistance(@RequestParam double latitude_1, @RequestParam double longitude_1,
                             @RequestParam double latitude_2, @RequestParam double longitude_2);

    @GetMapping("/api/v1/bing-map/get-distance-and-time")
    TravelInfor getDistanceAndTime(@RequestParam double latitude_1, @RequestParam double longitude_1,
                                   @RequestParam double latitude_2, @RequestParam double longitude_2);
}
