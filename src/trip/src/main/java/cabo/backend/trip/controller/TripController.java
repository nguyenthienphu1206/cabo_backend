package cabo.backend.trip.controller;

import cabo.backend.trip.dto.*;
import cabo.backend.trip.dto.ResponseStatus;
import cabo.backend.trip.service.TripService;
import com.google.cloud.firestore.GeoPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class TripController {

    private TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping("/trip/get-driver-location/{tripId}")
    public ResponseEntity<GeoPoint> getDriverLocation(@PathVariable("tripId") String tripId) {

        GeoPoint driverLocation = tripService.getDriverLocation(tripId);

        return new ResponseEntity<>(driverLocation, HttpStatus.OK);
    }

    @PostMapping("/trip/create-trip")
    public ResponseEntity<ResponseTripId> createTrip(@RequestBody CreateTripDto createTripDto) {

        log.info("createTripDto: " + createTripDto);

        ResponseTripId responseTripId = tripService.createTrip(createTripDto);

        return new ResponseEntity<>(responseTripId, HttpStatus.CREATED);
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


    @GetMapping("/trip/{tripId}/get-driverId")
    public ResponseEntity<String> getDriverIdByTripId(@RequestHeader("Authorization") String bearerToken,
                                                      @PathVariable("tripId") String tripId) {

        String driverId = tripService.getDriverIdByTripId(bearerToken, tripId);

        return new ResponseEntity<>(driverId, HttpStatus.OK);
    }

    @PostMapping("/trip/drive-booking/accept-drive")
    public ResponseEntity<ResponseStatus> sendReceivedDriverInfo(@RequestBody RequestReceivedDriverInfo requestReceivedDriverInfo) {

        ResponseStatus responseStatus = tripService.sendReceivedDriverInfo(requestReceivedDriverInfo);

        return new ResponseEntity<>(responseStatus, HttpStatus.CREATED);
    }
}
