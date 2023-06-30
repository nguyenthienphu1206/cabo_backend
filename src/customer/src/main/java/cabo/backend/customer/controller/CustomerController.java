package cabo.backend.customer.controller;

import cabo.backend.customer.dto.*;
import cabo.backend.customer.entity.Customer;
import cabo.backend.customer.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1")
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
    public ResponseEntity<Customer> getCustomerDetails(@PathVariable("id") String customerId) throws ExecutionException, InterruptedException {

        Customer customer = customerService.getCustomerDetails(customerId);

        return new ResponseEntity<>(customer, HttpStatus.OK);
    }

    @PostMapping("/customer/check-phone-existence")
    public ResponseEntity<ResponseCheckPhoneExistence> checkPhoneExistence(@RequestBody RequestPhoneNumberDto requestPhoneNumberDto) {

        ResponseCheckPhoneExistence responseCheckPhoneExistence = new ResponseCheckPhoneExistence();
        responseCheckPhoneExistence.setTimestamp(new Date());
        responseCheckPhoneExistence.setIsExisted(customerService.checkPhoneExistence(requestPhoneNumberDto.getPhoneNumber()));

        return new ResponseEntity<>(responseCheckPhoneExistence, HttpStatus.OK);
    }
}
