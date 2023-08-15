package cabo.backend.callcenter.service;

import cabo.backend.callcenter.dto.TripDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(url = "${TRIP_SERVICE_URL}", value = "TRIP-SERVICE")
public interface TripServiceClient {

    @GetMapping("/api/v1/trip")
    List<TripDto> getAllTrip(@RequestHeader("Authorization") String bearerToken);
}
