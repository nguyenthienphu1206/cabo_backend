package cabo.backend.customer.controller;

import cabo.backend.customer.dto.CustomerDto;
import cabo.backend.customer.entity.Customer;
import cabo.backend.customer.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1")
public class CustomerController {

    private CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/customer/get-id")
    public ResponseEntity<String> getCustomerId(@RequestBody String idToken) throws ExecutionException, InterruptedException {

        String customerId = customerService.getCustomerId(idToken);

        return new ResponseEntity<>(customerId, HttpStatus.OK);
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
    public ResponseEntity<Boolean> checkPhoneExistence(@RequestBody String phoneNumber) {

        Boolean isExisted = customerService.checkPhoneExistence(phoneNumber);

        return new ResponseEntity<>(isExisted, HttpStatus.OK);
    }
}
