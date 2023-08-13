package cabo.backend.receiverservice.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "${CUSTOMER_SERVICE_URL}", value = "CUSTOMER-SERVICE")
public interface CustomerServiceClient {

    @PostMapping("/api/v1/customer/call-center/register")
    String createCustomerIfPhoneNumberNotRegistered(@RequestHeader("Authorization") String bearerToken,
                                                                           @PathVariable("phoneNumber") String phoneNumber);
}
