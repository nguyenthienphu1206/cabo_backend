package cabo.backend.driver.controller;

import cabo.backend.driver.dto.*;
import cabo.backend.driver.dto.ResponseStatus;
import cabo.backend.driver.service.DriverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/driver")
@Slf4j
public class DriverController {

    private DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping("/get-driver-info")
    public ResponseEntity<DriverInfo> getDriverInfoById(@RequestHeader("Authorization") String bearerToken,
                                                        @RequestParam String tripId,
                                                        @RequestParam String driverId) {

        DriverInfo driverInfo = driverService.getDriverInfoByDriverIdAndTripId(bearerToken, driverId, tripId);

        return new ResponseEntity<>(driverInfo, HttpStatus.OK);
    }

    @PostMapping("/auth/register")
    public ResponseEntity<ResponseDriverId> registerDriverInfo(@RequestHeader("Authorization") String bearerToken,
                                                               @Valid @RequestBody RequestRegistryInfo requestRegistryInfo) throws ExecutionException, InterruptedException {

        String driverId = driverService.registerInfo(bearerToken, requestRegistryInfo);

        ResponseDriverId responseCustomerId = new ResponseDriverId(new Date(), driverId);

        return new ResponseEntity<>(responseCustomerId, HttpStatus.CREATED);
    }

    @PostMapping("")
    public ResponseEntity<String> saveDriver(@RequestHeader("Authorization") String bearerToken,
                                             @RequestBody DriverDto driverDto) throws ExecutionException, InterruptedException {

        String response = driverService.saveDriver(bearerToken, driverDto);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDriverDetails> getDriverDetails(@RequestHeader("Authorization") String bearerToken,
                                                                  @PathVariable("id") String driverId) throws ExecutionException, InterruptedException {

        ResponseDriverDetails responseDriverDetails = driverService.getDriverDetails(bearerToken, driverId);

        //log.info("driverDto ----> " + driverDto);

        return new ResponseEntity<>(responseDriverDetails, HttpStatus.OK);
    }

    @PostMapping("/auth/phone-verify")
    public ResponseEntity<ResponseCheckPhoneExistence> checkPhoneExistence(@RequestHeader("Authorization") String bearerToken,
                                                                           @RequestBody RequestPhoneNumberDto requestPhoneNumberDto) {

        log.info(requestPhoneNumberDto.toString());
        ResponseCheckPhoneExistence responseCheckPhoneExistence = new ResponseCheckPhoneExistence();
        responseCheckPhoneExistence.setTimestamp(new Date());
        responseCheckPhoneExistence.setIsExisted(driverService.checkPhoneExistence(bearerToken, requestPhoneNumberDto.getPhoneNumber()));

        return new ResponseEntity<>(responseCheckPhoneExistence, HttpStatus.OK);
    }

    @PostMapping("/{id}/vehicle/register")
    public ResponseEntity<ResponseVehicleId> registerDriverVehicle(@RequestHeader("Authorization") String bearerToken,
                                                                   @PathVariable("id") String driverId,
                                                                   @Valid @RequestBody RequestRegisterVehicle requestRegisterVehicle) {

        String vehicleId = driverService.registerDriverVehicle(bearerToken, driverId, requestRegisterVehicle);

        ResponseVehicleId responseVehicleId = new ResponseVehicleId(new Date(), vehicleId);

        return new ResponseEntity<>(responseVehicleId, HttpStatus.CREATED);
    }

    @PostMapping("/check-in")
    public ResponseEntity<ResponseCheckInOut> checkIn(@RequestHeader("Authorization") String bearerToken,
                                                      @Valid @RequestBody RequestCheckIn requestCheckIn) {

        ResponseCheckInOut responseCheckIn = driverService.checkIn(bearerToken, requestCheckIn);

        return new ResponseEntity<>(responseCheckIn, HttpStatus.CREATED);
    }

    @PostMapping("/check-out")
    public ResponseEntity<ResponseCheckInOut> checkOut(@RequestHeader("Authorization") String bearerToken,
                                                      @Valid @RequestBody RequestCheckOut requestCheckOut) {

        ResponseCheckInOut responseCheckOut;

        responseCheckOut = driverService.checkOut(bearerToken, requestCheckOut);

        return new ResponseEntity<>(responseCheckOut, HttpStatus.CREATED);

    }

    @GetMapping("/{id}/overview")
    public ResponseEntity<ResponseOverview> getOverview(@RequestHeader("Authorization") String bearerToken,
                                                        @PathVariable("id") String driverId) {

        ResponseOverview responseOverview = driverService.getOverview(bearerToken, driverId);

        return new ResponseEntity<>(responseOverview, HttpStatus.OK);
    }

    @GetMapping("/notification/subscribe/{fcmToken}")
    public ResponseEntity<ResponseSubscribeNotification> subscribeNotification(@RequestHeader("Authorization") String bearerToken,
                                                                               @PathVariable("fcmToken") String fcmToken) {

        ResponseSubscribeNotification responseSubscribeNotification;

        try {
            driverService.subscribeNotification(bearerToken, fcmToken);

            responseSubscribeNotification = new ResponseSubscribeNotification(new Date(), "Successfully");

            return new ResponseEntity<>(responseSubscribeNotification, HttpStatus.OK);
        } catch (Exception ex) {
            responseSubscribeNotification = new ResponseSubscribeNotification(new Date(), ex.getMessage());

            return new ResponseEntity<>(responseSubscribeNotification, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/drive-booking/accept-drive")
    public ResponseEntity<ResponseStatus> sendReceivedDriverInfo(@RequestHeader("Authorization") String bearerToken,
                                                                 @RequestBody RequestReceivedDriverInfo requestReceivedDriverInfo) {

        ResponseStatus responseStatus = driverService.sendReceivedDriverInfo(bearerToken, requestReceivedDriverInfo);

        return new ResponseEntity<>(responseStatus, HttpStatus.CREATED);
    }
}
