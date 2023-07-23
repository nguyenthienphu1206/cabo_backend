package cabo.backend.customer.controller;

import cabo.backend.customer.dto.*;
import cabo.backend.customer.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/customer")
@Slf4j
public class CustomerController {

    private CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<ResponseCustomerId> getCustomerId(@RequestHeader("Authorization") String bearerToken,
                                                            @RequestBody RequestRegisterCustomer requestRegisterCustomer) throws ExecutionException, InterruptedException {

        String customerId = customerService.registerCustomer(bearerToken, requestRegisterCustomer);

        ResponseCustomerId responseCustomerId = new ResponseCustomerId(new Date(), customerId);

        return new ResponseEntity<>(responseCustomerId, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<String> saveCustomer(@RequestHeader("Authorization") String bearerToken,
                                               @RequestBody CustomerDto customerDto) throws ExecutionException, InterruptedException {

        String response = customerService.saveCustomer(bearerToken, customerDto);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomerDetails(@RequestHeader("Authorization") String bearerToken,
                                                          @PathVariable("id") String customerId) throws ExecutionException, InterruptedException {

        log.info("Bearer Token ----> " + bearerToken);

        CustomerDto customerDto = customerService.getCustomerDetails(bearerToken, customerId);

        return new ResponseEntity<>(customerDto, HttpStatus.OK);
    }

    @PostMapping("/check-phone-existence")
    public ResponseEntity<ResponseCheckPhoneExistence> checkPhoneExistence(@RequestHeader("Authorization") String bearerToken,
                                                                           @RequestBody RequestPhoneNumberDto requestPhoneNumberDto,
                                                                           HttpServletRequest request) {

        log.info(requestPhoneNumberDto.toString());
        ResponseCheckPhoneExistence responseCheckPhoneExistence = new ResponseCheckPhoneExistence();
        responseCheckPhoneExistence.setTimestamp(new Date());
        responseCheckPhoneExistence.setIsExisted(customerService.checkPhoneExistence(bearerToken, requestPhoneNumberDto.getPhoneNumber()));

//        // Get the incoming request headers
//        Enumeration<String> headerNames = request.getHeaderNames();
//        while (headerNames.hasMoreElements()) {
//            String headerName = headerNames.nextElement();
//            String headerValue = request.getHeader(headerName);
//            log.info(headerName + ": " + headerValue);
//        }

        return new ResponseEntity<>(responseCheckPhoneExistence, HttpStatus.OK);
    }

    @GetMapping("/{id}/overview")
    public ResponseEntity<ResponseOverview> getOverview(@RequestHeader("Authorization") String bearerToken,
                                                        @PathVariable("id") String customerId) {

        ResponseOverview responseOverview = customerService.getOverview(bearerToken, customerId);

        return new ResponseEntity<>(responseOverview, HttpStatus.OK);
    }

    @PostMapping("/drive-booking/estimate-cost")
    public  ResponseEntity<ResponseEstimateCostAndDistance> getEstimateCostAndDistance(@RequestHeader("Authorization") String bearerToken,
                                                                                       @RequestBody RequestOriginsAndDestinationsLocation requestOriginsAndDestinationsLocation) {

        ResponseEstimateCostAndDistance responseEstimateCostAndDistance = customerService.getEstimateCostAndDistance(bearerToken, requestOriginsAndDestinationsLocation);

        return new ResponseEntity<>(responseEstimateCostAndDistance, HttpStatus.OK);
    }
}
