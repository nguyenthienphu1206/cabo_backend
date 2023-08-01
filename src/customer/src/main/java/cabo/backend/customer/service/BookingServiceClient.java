package cabo.backend.customer.service;

import cabo.backend.customer.dto.RequestBookADrive;
import cabo.backend.customer.dto.ResponseDriverInformation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "${BOOKING_SERVICE_URL}", value = "BOOKING-SERVICE")
public interface BookingServiceClient {

    @PostMapping("/api/v1/booking/drive-booking/confirm/{customerId}")
    ResponseDriverInformation getDriverInformation(@RequestHeader("Authorization") String bearerToken,
                                                   @PathVariable("customerId") String customerId,
                                                   @RequestBody RequestBookADrive requestBooking);
}
