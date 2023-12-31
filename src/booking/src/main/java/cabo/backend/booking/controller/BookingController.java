package cabo.backend.booking.controller;

import cabo.backend.booking.dto.*;
import cabo.backend.booking.dto.ResponseStatus;
import cabo.backend.booking.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/v1/booking")
@CrossOrigin(origins = "*")
public class BookingController {

    private BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/send-notification")
    public ResponseEntity<String> sendNotificationToAllDevices(@RequestBody NotificationDto notificationDto) {

        bookingService.sendNotification(notificationDto);

        return new ResponseEntity<>("Successfully", HttpStatus.OK);
    }


    @PostMapping("/drive-booking/current-gps")
    public ResponseEntity<ResponseStatus> collectGPS(@RequestHeader("Authorization") String bearerToken,
                                                     @RequestBody RequestGPS requestGPS) {

        ResponseStatus responseStatus;

        try {
            bookingService.collectGPSFromDriver(bearerToken, requestGPS);

            responseStatus = new ResponseStatus(new Date(), "Successfully");

            return new ResponseEntity<>(responseStatus, HttpStatus.CREATED);

        } catch (Exception e) {

            responseStatus = new ResponseStatus(new Date(), e.getMessage());

            return new ResponseEntity<>(responseStatus, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/current-gps")
    public ResponseEntity<ResponseStatus> sendGPS(@RequestBody RequestUpdateGPSInDrive requestUpdateGPSInDrive) {

        ResponseStatus responseStatus;

        try {
            bookingService.sendGPSToServer(requestUpdateGPSInDrive);

            responseStatus = new ResponseStatus(new Date(), "Successfully");

            return new ResponseEntity<>(responseStatus, HttpStatus.CREATED);

        } catch (Exception e) {

            responseStatus = new ResponseStatus(new Date(), e.getMessage());

            return new ResponseEntity<>(responseStatus, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/drive-booking/confirm/{customerId}")
    public ResponseEntity<ResponseDriverInformation> getDriverInformation(@RequestHeader("Authorization") String bearerToken,
                                                                          @PathVariable("customerId") String customerId,
                                                                          @RequestBody RequestBookADrive requestBooking) {

        ResponseDriverInformation responseDriverInformation = bookingService.getDriverInformation(bearerToken, customerId, requestBooking);

        return new ResponseEntity<>(responseDriverInformation, HttpStatus.OK);
    }
}
