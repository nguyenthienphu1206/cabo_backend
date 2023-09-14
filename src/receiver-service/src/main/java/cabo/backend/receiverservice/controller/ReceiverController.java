package cabo.backend.receiverservice.controller;

import cabo.backend.receiverservice.dto.RequestBookADrive;
import cabo.backend.receiverservice.dto.ResponseStatus;
import cabo.backend.receiverservice.publisher.ReceiverProducer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class ReceiverController {

    private final ReceiverProducer receiverProducer;

    public ReceiverController(ReceiverProducer receiverProducer) {
        this.receiverProducer = receiverProducer;
    }

    @PostMapping("/receiver-service")
    public ResponseEntity<ResponseStatus> receiverAndBookDriverFromCallCenter(@RequestHeader("Authorization") String bearerToken,
                                                                              @RequestBody RequestBookADrive requestBookADrive) {

        ResponseStatus responseStatus;
        try {
            receiverProducer.sendInforToBookingQueue(bearerToken, requestBookADrive);

            responseStatus = new ResponseStatus(new Date(), "Successfully");

            return new ResponseEntity<>(responseStatus, HttpStatus.OK);

        } catch (Exception e) {
             responseStatus = new ResponseStatus(new Date(), e.getMessage());

            return new ResponseEntity<>(responseStatus, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
