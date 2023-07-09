package cabo.backend.driver.controller;

import cabo.backend.driver.dto.*;
import cabo.backend.driver.service.DriverService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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

    @PostMapping("/driver/auth/register")
    public ResponseEntity<ResponseDriverId> registerDriverInfo(@RequestHeader("Authorization") String bearerToken,
                                                               @RequestBody RequestRegistryInfo requestRegistryInfo) throws ExecutionException, InterruptedException {

        String driverId = driverService.registerInfo(bearerToken, requestRegistryInfo);

        ResponseDriverId responseCustomerId = new ResponseDriverId(new Date(), driverId);

        return new ResponseEntity<>(responseCustomerId, HttpStatus.CREATED);
    }

    @PostMapping("/driver")
    public ResponseEntity<String> saveDriver(@RequestHeader("Authorization") String bearerToken,
                                             @RequestBody DriverDto driverDto) throws ExecutionException, InterruptedException {

        String response = driverService.saveDriver(bearerToken, driverDto);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/driver/{id}")
    public ResponseEntity<ResponseDriverDetails> getDriverDetails(@RequestHeader("Authorization") String bearerToken,
                                                                  @PathVariable("id") String driverId) throws ExecutionException, InterruptedException {

        ResponseDriverDetails responseDriverDetails = driverService.getDriverDetails(bearerToken, driverId);

        //log.info("driverDto ----> " + driverDto);

        return new ResponseEntity<>(responseDriverDetails, HttpStatus.OK);
    }

    @PostMapping("/driver/auth/phone-verify")
    public ResponseEntity<ResponseCheckPhoneExistence> checkPhoneExistence(@RequestHeader("Authorization") String bearerToken,
                                                                           @RequestBody RequestPhoneNumberDto requestPhoneNumberDto) {

        log.info(requestPhoneNumberDto.toString());
        ResponseCheckPhoneExistence responseCheckPhoneExistence = new ResponseCheckPhoneExistence();
        responseCheckPhoneExistence.setTimestamp(new Date());
        responseCheckPhoneExistence.setIsExisted(driverService.checkPhoneExistence(bearerToken, requestPhoneNumberDto.getPhoneNumber()));

        return new ResponseEntity<>(responseCheckPhoneExistence, HttpStatus.OK);
    }

    @PostMapping("/driver/{id}/vehicle/register")
    public ResponseEntity<ResponseVehicleId> registerDriverVehicle(@RequestHeader("Authorization") String bearerToken,
                                                                   @PathVariable("id") String driverId,
                                                                   @RequestBody RequestRegisterVehicle requestRegisterVehicle) {

        String vehicleId = driverService.registerDriverVehicle(bearerToken, driverId, requestRegisterVehicle);

        ResponseVehicleId responseVehicleId = new ResponseVehicleId(new Date(), vehicleId);

        return new ResponseEntity<>(responseVehicleId, HttpStatus.CREATED);
    }
}
