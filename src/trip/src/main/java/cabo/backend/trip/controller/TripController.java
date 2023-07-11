package cabo.backend.trip.controller;

import cabo.backend.trip.dto.ResponseRecentTripFromCustomer;
import cabo.backend.trip.dto.ResponseTotalTrip;
import cabo.backend.trip.service.TripService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class TripController {

    private TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping("/trip/recent-trip/customer/{customerId}")
    public ResponseEntity<ResponseRecentTripFromCustomer> getRecentTripFromCustomer(@PathVariable("customerId") String customerId) {

        ResponseRecentTripFromCustomer responseRecentTripFromCustomer = tripService.getRecentTripFromCustomer(customerId);

        return new ResponseEntity<>(responseRecentTripFromCustomer, HttpStatus.OK);
    }

    @GetMapping("/trip/total-trip/{collection}/{id}")
    public ResponseEntity<ResponseTotalTrip> getTotalTrip(@PathVariable("collection") String collection,
                                                          @PathVariable("id") String id) {

        ResponseTotalTrip responseTotalTrip = tripService.getTotalTrip(collection, id);

        return new ResponseEntity<>(responseTotalTrip, HttpStatus.OK);
    }
}
