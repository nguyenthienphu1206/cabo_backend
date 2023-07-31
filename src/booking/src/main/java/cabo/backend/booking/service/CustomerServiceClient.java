package cabo.backend.booking.service;

import cabo.backend.booking.dto.CustomerDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;



@FeignClient(url = "${CUSTOMER_SERVICE_URL}", value = "CUSTOMER-SERVICE")
public interface CustomerServiceClient {

    @GetMapping("/api/v1/customer/{id}")
    CustomerDto getCustomerDetails(@RequestHeader("Authorization") String bearerToken,
                                   @PathVariable("id") String customerId);
}
