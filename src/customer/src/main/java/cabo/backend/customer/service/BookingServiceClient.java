package cabo.backend.customer.service;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(url = "${BOOKING_SERVICE_URL}", value = "BOOKING-SERVICE")
public interface BookingServiceClient {
}
