package cabo.backend.callcenter.controller;

import cabo.backend.callcenter.dto.RequestBookADrive;
import cabo.backend.callcenter.dto.ResponseStatus;
import cabo.backend.callcenter.dto.TripDto;
import cabo.backend.callcenter.service.CallCenterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/call-center")
public class CallCenterController {

    private CallCenterService callCenterService;

    public CallCenterController(CallCenterService callCenterService) {
        this.callCenterService = callCenterService;
    }

    @GetMapping("/notification/subscribe/{fcmToken}")
    public ResponseEntity<ResponseStatus> subscribeNotification(@RequestHeader("Authorization") String bearerToken,
                                                                @PathVariable("fcmToken") String fcmToken) {

        ResponseStatus responseStatus;

        try {
            callCenterService.subscribeNotification(bearerToken, fcmToken);

            responseStatus = new ResponseStatus(new Date(), "Successfully");

            return new ResponseEntity<>(responseStatus, HttpStatus.OK);
        } catch (Exception ex) {
            responseStatus = new ResponseStatus(new Date(), ex.getMessage());

            return new ResponseEntity<>(responseStatus, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/drive-booking/confirm")
    public ResponseEntity<ResponseStatus> sendInfoCustomerFromCallCenter(@RequestHeader("Authorization") String bearerToken,
                                                                         @RequestBody RequestBookADrive requestBookADrive) {

        ResponseStatus responseStatus;
        try {
            responseStatus = callCenterService.sendInfoCustomerFromCallCenter(bearerToken, requestBookADrive);

            return new ResponseEntity<>(responseStatus, HttpStatus.OK);

        } catch (Exception e) {
            responseStatus = new ResponseStatus(new Date(), e.getMessage());

            return new ResponseEntity<>(responseStatus, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/trip")
    public ResponseEntity<List<TripDto>> getAllTrips(@RequestHeader("Authorization") String bearerToken) {

        List<TripDto> tripDtos = callCenterService.getAllTrips(bearerToken);

        return new ResponseEntity<>(tripDtos, HttpStatus.OK);
    }
}
