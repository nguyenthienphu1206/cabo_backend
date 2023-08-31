package cabo.backend.customer.controller;

import cabo.backend.customer.dto.*;
import cabo.backend.customer.dto.ResponseStatus;
import cabo.backend.customer.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/customer")
@Slf4j
@CrossOrigin(origins = "*")
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

        return new ResponseEntity<>(responseCustomerId, HttpStatus.CREATED);
    }

    @PostMapping("/call-center/register")
    public ResponseEntity<String> createCustomerIfPhoneNumberNotRegistered(@RequestHeader("Authorization") String bearerToken,
                                                                           @RequestParam String phoneNumber) {

        String customerId = customerService.createCustomerIfPhoneNumberNotRegistered(bearerToken, phoneNumber);

        return new ResponseEntity<>(customerId, HttpStatus.CREATED);
    }

    @PostMapping("")
    public ResponseEntity<String> saveCustomer(@RequestHeader("Authorization") String bearerToken,
                                               @RequestBody CustomerDto customerDto) throws ExecutionException, InterruptedException {

        String response = customerService.saveCustomer(bearerToken, customerDto);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/document/{customerId}")
    public  ResponseEntity<DocumentRef> getDocumentById(@RequestHeader("Authorization") String bearerToken,
                                                        @PathVariable("customerId") String customerId) {

        DocumentRef documentRef = customerService.getDocumentById(bearerToken, customerId);

        return new ResponseEntity<>(documentRef, HttpStatus.OK);
    }

    @GetMapping("/{customerId}/getName")
    public ResponseEntity<String> getNameByCustomerId(@RequestHeader("Authorization") String bearerToken,
                                                      @PathVariable("customerId") String customerId) {

        String fullName = customerService.getNameByCustomerId(bearerToken, customerId);

        return new ResponseEntity<>(fullName, HttpStatus.OK);
    }

    @GetMapping("/{customerId}/uid")
    public ResponseEntity<String> getUidByCustomerId(@PathVariable("customerId") String customerId) {

        String uid = customerService.getUidByCustomerId(customerId);

        return new ResponseEntity<>(uid, HttpStatus.OK);
    }

    @GetMapping("/{customerId}/get-name-and-phone")
    public ResponseEntity<ResponseFullNameAndPhone> getNameAndPhoneByCustomerId(@RequestHeader("Authorization") String bearerToken,
                                                                                @PathVariable("customerId") String customerId) {

        ResponseFullNameAndPhone responseFullNameAndPhone = customerService.getNameAndPhoneByCustomerId(bearerToken, customerId);

        return new ResponseEntity<>(responseFullNameAndPhone, HttpStatus.OK);
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

    @PostMapping("/drive-booking/confirm/{customerId}")
    public ResponseEntity<ResponseDriverInformation> bookADrive(@RequestHeader("Authorization") String bearerToken,
                                                                @PathVariable("customerId") String customerId,
                                                                @RequestBody RequestBookADrive requestBookADrive) {

        ResponseDriverInformation responseDriverInformation = customerService.bookADrive(bearerToken, customerId, requestBookADrive);

        return new ResponseEntity<>(responseDriverInformation, HttpStatus.OK);
    }

    @GetMapping("/notification/subscribe/{fcmToken}")
    public ResponseEntity<ResponseStatus> subscribeNotification(@RequestHeader("Authorization") String bearerToken,
                                                                @PathVariable("fcmToken") String fcmToken) {

        ResponseStatus responseStatus;

        try {
            customerService.subscribeNotification(bearerToken, fcmToken);

            responseStatus = new ResponseStatus(new Date(), "Successfully");

            return new ResponseEntity<>(responseStatus, HttpStatus.OK);
        } catch (Exception ex) {
            responseStatus = new ResponseStatus(new Date(), ex.getMessage());

            return new ResponseEntity<>(responseStatus, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
