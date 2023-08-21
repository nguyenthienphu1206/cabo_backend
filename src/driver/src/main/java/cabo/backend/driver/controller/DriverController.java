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
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/driver")
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
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

    @GetMapping("/{driverId}/trip")
    public ResponseEntity<List<TripDto>> getAllTripsById(@RequestHeader("Authorization") String bearerToken,
                                                         @PathVariable("driverId") String driverId) {

        List<TripDto> tripDtos = driverService.getAllTripsById(bearerToken, driverId);

        return new ResponseEntity<>(tripDtos, HttpStatus.OK);
    }

    @GetMapping("/get-driver-status")
    public ResponseEntity<String> getDriverStatusIntByUid(@RequestParam String uid) {

        String status = driverService.getDriverStatusIntByUid(uid);

        return new ResponseEntity<>(status, HttpStatus.OK);
    }

    @PostMapping("/auth/register")
    public ResponseEntity<ResponseDriverId> registerDriverInfo(@RequestHeader("Authorization") String bearerToken,
                                                               @Valid @RequestBody RequestRegistryInfo requestRegistryInfo) throws ExecutionException, InterruptedException {

        String driverId = driverService.registerInfo(bearerToken, requestRegistryInfo);

        ResponseDriverId responseCustomerId = new ResponseDriverId(new Date(), driverId);

        return new ResponseEntity<>(responseCustomerId, HttpStatus.CREATED);
    }

    @GetMapping("/document/{driverId}")
    public  ResponseEntity<DocumentRef> getDocumentById(@RequestHeader("Authorization") String bearerToken,
                                                        @PathVariable("driverId") String driverId) {

        DocumentRef documentRef = driverService.getDocumentById(bearerToken, driverId);

        return new ResponseEntity<>(documentRef, HttpStatus.OK);
    }

    @GetMapping("/{driverId}/getName")
    public ResponseEntity<String> getNameByDriverId(@RequestHeader("Authorization") String bearerToken,
                                                      @PathVariable("driverId") String driverId) {

        String fullName = driverService.getNameByDriverId(bearerToken, driverId);

        return new ResponseEntity<>(fullName, HttpStatus.OK);
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
    public ResponseEntity<ResponseStatus> checkIn(@RequestHeader("Authorization") String bearerToken,
                                                      @Valid @RequestBody RequestCheckIn requestCheckIn) {

        ResponseStatus responseStatus = driverService.checkIn(bearerToken, requestCheckIn);

        return new ResponseEntity<>(responseStatus, HttpStatus.CREATED);
    }

    @PostMapping("/check-out")
    public ResponseEntity<ResponseStatus> checkOut(@RequestHeader("Authorization") String bearerToken,
                                                      @Valid @RequestBody RequestCheckOut requestCheckOut) {

        ResponseStatus responseStatus;

        responseStatus = driverService.checkOut(bearerToken, requestCheckOut);

        return new ResponseEntity<>(responseStatus, HttpStatus.CREATED);

    }

    @PutMapping("/{driverId}")
    public ResponseEntity<ResponseStatus> updateDriverStatus(@RequestHeader("Authorization") String bearerToken,
                                                             @PathVariable("driverId") String driverId,
                                                             @RequestParam String status) {

        ResponseStatus responseStatus = driverService.updateDriverStatus(bearerToken, driverId, status);

        return new ResponseEntity<>(responseStatus, HttpStatus.CREATED);

    }

    @GetMapping("/{id}/overview")
    public ResponseEntity<ResponseOverview> getOverview(@RequestHeader("Authorization") String bearerToken,
                                                        @PathVariable("id") String driverId) {

        ResponseOverview responseOverview = driverService.getOverview(bearerToken, driverId);

        return new ResponseEntity<>(responseOverview, HttpStatus.OK);
    }

    @GetMapping("/notification/subscribe/{fcmToken}")
    public ResponseEntity<ResponseStatus> subscribeNotification(@RequestHeader("Authorization") String bearerToken,
                                                                               @PathVariable("fcmToken") String fcmToken) {

        ResponseStatus responseStatus;

        try {
            driverService.subscribeNotification(bearerToken, fcmToken);

            responseStatus = new ResponseStatus(new Date(), "Successfully");

            return new ResponseEntity<>(responseStatus, HttpStatus.OK);
        } catch (Exception ex) {
            responseStatus = new ResponseStatus(new Date(), ex.getMessage());

            return new ResponseEntity<>(responseStatus, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/drive-booking/accept-drive")
    public ResponseEntity<ResponseStatus> sendReceivedDriverInfo(@RequestHeader("Authorization") String bearerToken,
                                                                 @RequestBody RequestReceivedDriverInfo requestReceivedDriverInfo) {

        ResponseStatus responseStatus = driverService.sendReceivedDriverInfo(bearerToken, requestReceivedDriverInfo);

        return new ResponseEntity<>(responseStatus, HttpStatus.CREATED);
    }

    @PostMapping("/drive-booking/current-gps")
    public ResponseEntity<ResponseStatus> sendGPS(@RequestHeader("Authorization") String bearerToken,
                                                  @RequestBody RequestGPS requestGPS) {

        ResponseStatus responseStatus = driverService.sendGPS(bearerToken, requestGPS);

        return new ResponseEntity<>(responseStatus, HttpStatus.OK);
    }
}
