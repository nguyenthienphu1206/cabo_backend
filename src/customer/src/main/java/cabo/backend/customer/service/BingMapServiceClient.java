package cabo.backend.customer.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(url = "${BING_MAP_SERVICE_URL}", value = "BING-MAP-SERVICE")
public interface BingMapServiceClient {

    @GetMapping("/api/v1/bing-map/address")
    String getAddress(@RequestParam double latitude, @RequestParam double longitude);
}
