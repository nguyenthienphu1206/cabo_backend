package cabo.backend.customer.controller;

import cabo.backend.customer.dto.*;
import cabo.backend.customer.entity.Customer;
import cabo.backend.customer.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class CustomerController {

    private CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/customer/get-id")
    public ResponseEntity<ResponseCustomerId> getCustomerId(@RequestBody RequestIdTokenDto requestIdTokenDto) throws ExecutionException, InterruptedException {

        String customerId = customerService.getCustomerId(requestIdTokenDto.getIdToken());

        ResponseCustomerId responseCustomerId = new ResponseCustomerId(new Date(), customerId);

        return new ResponseEntity<>(responseCustomerId, HttpStatus.OK);
    }

    @PostMapping("/customer")
    public ResponseEntity<String> saveCustomer(@RequestBody CustomerDto customerDto) throws ExecutionException, InterruptedException {

        String response = customerService.saveCustomer(customerDto);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/customer/{id}")
    public ResponseEntity<CustomerDto> getCustomerDetails(@PathVariable("id") String customerId) throws ExecutionException, InterruptedException {

        CustomerDto customerDto = customerService.getCustomerDetails(customerId);

        return new ResponseEntity<>(customerDto, HttpStatus.OK);
    }

    @PostMapping("/customer/check-phone-existence")
    public ResponseEntity<ResponseCheckPhoneExistence> checkPhoneExistence(@RequestBody RequestPhoneNumberDto requestPhoneNumberDto, HttpServletRequest request) {

        log.info(requestPhoneNumberDto.toString());
        ResponseCheckPhoneExistence responseCheckPhoneExistence = new ResponseCheckPhoneExistence();
        responseCheckPhoneExistence.setTimestamp(new Date());
        responseCheckPhoneExistence.setIsExisted(customerService.checkPhoneExistence(requestPhoneNumberDto.getPhoneNumber()));

//        // Get the incoming request headers
//        Enumeration<String> headerNames = request.getHeaderNames();
//        while (headerNames.hasMoreElements()) {
//            String headerName = headerNames.nextElement();
//            String headerValue = request.getHeader(headerName);
//            log.info(headerName + ": " + headerValue);
//        }

        return new ResponseEntity<>(responseCheckPhoneExistence, HttpStatus.OK);
    }
}
