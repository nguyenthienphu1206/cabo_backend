package cabo.backend.booking.controller;

import cabo.backend.booking.dto.NotificationDto;
import cabo.backend.booking.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/booking")
public class BookingController {

    private BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("send-notification")
    public ResponseEntity<String> sendNotificationToAllDevices(@RequestBody NotificationDto notificationDto) {

        bookingService.sendNotificationToAllDevices(notificationDto);

        return new ResponseEntity<>("Successfully", HttpStatus.OK);
    }
}
