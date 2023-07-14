package cabo.backend.trip.controller;

import cabo.backend.trip.dto.ResponseAverageIncomePerDrive;
import cabo.backend.trip.dto.ResponseRecentTripFromCustomer;
import cabo.backend.trip.dto.ResponseRecentTripFromDriver;
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

    @GetMapping("/trip/recent-trip/driver/{driverId}")
    public ResponseEntity<ResponseRecentTripFromDriver> getRecentTripFromDriver(@PathVariable("driverId") String driverId) {

        ResponseRecentTripFromDriver responseRecentTripFromDriver = tripService.getRecentTripFromDriver(driverId);

        return new ResponseEntity<>(responseRecentTripFromDriver, HttpStatus.OK);
    }

    @GetMapping("/trip/total-trip/{collection}/{id}")
    public ResponseEntity<ResponseTotalTrip> getTotalTrip(@PathVariable("collection") String collection,
                                                          @PathVariable("id") String id) {

        ResponseTotalTrip responseTotalTrip = tripService.getTotalTrip(collection, id);

        return new ResponseEntity<>(responseTotalTrip, HttpStatus.OK);
    }

    @GetMapping("/trip/average-income/driver/{driverId}")
    public ResponseEntity<ResponseAverageIncomePerDrive> getAverageIncomePerDrive(@PathVariable("driverId") String driverId) {

        ResponseAverageIncomePerDrive responseAverageIncomePerDrive = tripService.getAverageIncomePerDrive(driverId);

        return new ResponseEntity<>(responseAverageIncomePerDrive, HttpStatus.OK);
    }
}
