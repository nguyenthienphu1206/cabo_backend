package cabo.backend.trip.service;

import cabo.backend.trip.dto.DocumentRef;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;



@FeignClient(url = "${CUSTOMER_SERVICE_URL}", value = "CUSTOMER-SERVICE")
public interface CustomerServiceClient {

    @GetMapping("/api/v1/customer/document/{customerId}")
    DocumentRef getDocumentById(@RequestHeader("Authorization") String bearerToken,
                                @PathVariable("customerId") String customerId);

    @GetMapping("/api/v1/customer/{customerId}/getName")
    String getNameByCustomerId(@RequestHeader("Authorization") String bearerToken,
                               @PathVariable("customerId") String customerId);

}
