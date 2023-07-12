package cabo.backend.customer.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(url = "${GOOGLE_MAPS_SERVICE_URL}", value = "GOOGLE-MAPS-SERVICE")
public interface GoogleMapsServiceClient {

    @GetMapping("/api/v1/google-maps/address")
    String getAddress(@RequestParam double latitude, @RequestParam double longitude);
}
