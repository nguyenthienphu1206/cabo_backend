package cabo.backend.driver.controller;

import cabo.backend.driver.dto.*;
import cabo.backend.driver.service.DriverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class DriverController {

    private DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping("/driver/get-id")
    public ResponseEntity<ResponseDriverId> getDriverId(@RequestBody RequestIdTokenDto requestIdTokenDto) throws ExecutionException, InterruptedException {

        String driverId = driverService.getDriverId(requestIdTokenDto.getIdToken(), requestIdTokenDto.getFullName());

        ResponseDriverId responseCustomerId = new ResponseDriverId(new Date(), driverId);

        return new ResponseEntity<>(responseCustomerId, HttpStatus.OK);
    }

    @PostMapping("/driver")
    public ResponseEntity<String> saveDriver(@RequestBody DriverDto driverDto) throws ExecutionException, InterruptedException {

        String response = driverService.saveDriver(driverDto);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/driver/{id}")
    public ResponseEntity<ResponseDriverDetails> getDriverDetails(@PathVariable("id") String driverId) throws ExecutionException, InterruptedException {

        ResponseDriverDetails responseDriverDetails = driverService.getDriverDetails(driverId);

        //log.info("driverDto ----> " + driverDto);

        return new ResponseEntity<>(responseDriverDetails, HttpStatus.OK);
    }

    @PostMapping("/driver/auth/phone-verify")
    public ResponseEntity<ResponseCheckPhoneExistence> checkPhoneExistence(@RequestBody RequestPhoneNumberDto requestPhoneNumberDto, HttpServletRequest request) {

        log.info(requestPhoneNumberDto.toString());
        ResponseCheckPhoneExistence responseCheckPhoneExistence = new ResponseCheckPhoneExistence();
        responseCheckPhoneExistence.setTimestamp(new Date());
        responseCheckPhoneExistence.setIsExisted(driverService.checkPhoneExistence(requestPhoneNumberDto.getPhoneNumber()));

        return new ResponseEntity<>(responseCheckPhoneExistence, HttpStatus.OK);
    }
}
