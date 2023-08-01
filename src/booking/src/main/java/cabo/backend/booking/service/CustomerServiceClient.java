package cabo.backend.booking.service;

import cabo.backend.booking.dto.CustomerDto;
import cabo.backend.booking.dto.DocumentRef;
import com.google.cloud.firestore.DocumentReference;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;



@FeignClient(url = "${CUSTOMER_SERVICE_URL}", value = "CUSTOMER-SERVICE")
public interface CustomerServiceClient {

    @GetMapping("/api/v1/customer/document/{customerId}")
    DocumentRef getDocumentById(@RequestHeader("Authorization") String bearerToken,
                                @PathVariable("customerId") String customerId);

    @GetMapping("/api/v1/customer/{id}")
    CustomerDto getCustomerDetails(@RequestHeader("Authorization") String bearerToken,
                                   @PathVariable("id") String customerId);
}
