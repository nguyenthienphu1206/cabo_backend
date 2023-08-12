package cabo.backend.receiverservice.controller;

import cabo.backend.receiverservice.dto.RequestBookADrive;
import cabo.backend.receiverservice.dto.RequestBookADriveEvent;
import cabo.backend.receiverservice.dto.ResponseStatus;
import cabo.backend.receiverservice.publisher.ReceiverProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/api/v1")
public class ReceiverController {

    private ReceiverProducer receiverProducer;

    public ReceiverController(ReceiverProducer receiverProducer) {
        this.receiverProducer = receiverProducer;
    }

    @PostMapping("/call-center/drive-booking/confirm")
    public ResponseEntity<ResponseStatus> placeLocation(@RequestBody RequestBookADrive requestBookADrive) {

        ResponseStatus responseStatus;
        try {
            responseStatus = new ResponseStatus(new Date(), "Successfully");

        } catch (Exception e) {
             responseStatus = new ResponseStatus(new Date(), e.getMessage());
        }

        return new ResponseEntity<>(responseStatus, HttpStatus.OK);
    }
}
