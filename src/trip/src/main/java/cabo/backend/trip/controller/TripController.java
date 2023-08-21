package cabo.backend.trip.controller;

import cabo.backend.trip.dto.*;
import cabo.backend.trip.dto.ResponseStatus;
import cabo.backend.trip.service.TripService;
import com.google.cloud.firestore.GeoPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class TripController {

    private TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping("/trip/get-driver-location/{tripId}")
    public ResponseEntity<GeoPoint> getDriverLocation(@RequestHeader("Authorization") String bearerToken,
                                                      @PathVariable("tripId") String tripId) {

        GeoPoint driverLocation = tripService.getDriverLocation(bearerToken, tripId);

        return new ResponseEntity<>(driverLocation, HttpStatus.OK);
    }

    @PostMapping("/trip")
    public ResponseEntity<ResponseTripId> createTrip(@RequestHeader("Authorization") String bearerToken,
                                                     @RequestBody CreateTripDto createTripDto) {

        log.info("createTripDto: " + createTripDto);

        ResponseTripId responseTripId = tripService.createTrip(bearerToken, createTripDto);

        return new ResponseEntity<>(responseTripId, HttpStatus.CREATED);
    }

    @GetMapping("/trip/{tripId}")
    public ResponseEntity<TripDto> getTripById(@RequestHeader("Authorization") String bearerToken,
                                               @PathVariable("tripId") String tripId) {

        TripDto tripDto = tripService.getTripById(bearerToken, tripId);

        return new ResponseEntity<>(tripDto, HttpStatus.OK);
    }

    @GetMapping("/trip")
    public ResponseEntity<List<TripDto>> getAllTrip(@RequestHeader("Authorization") String bearerToken) {

        List<TripDto> tripDtos = tripService.getAllTrip(bearerToken);

        return new ResponseEntity<>(tripDtos, HttpStatus.OK);
    }

    @GetMapping("/trip/customer/{customerId}")
    public ResponseEntity<List<TripDto>> getTripByCustomerId(@RequestHeader("Authorization") String bearerToken,
                                                             @PathVariable("customerId") String customerId) {

        List<TripDto> tripDtos = tripService.getTripByCustomerId(bearerToken, customerId);

        return new ResponseEntity<>(tripDtos, HttpStatus.OK);
    }

    @GetMapping("/trip/driver/{driverId}")
    public ResponseEntity<List<TripDto>> getTripByDriverId(@RequestHeader("Authorization") String bearerToken,
                                                             @PathVariable("driverId") String driverId) {

        List<TripDto> tripDtos = tripService.getTripByDriverId(bearerToken, driverId);

        return new ResponseEntity<>(tripDtos, HttpStatus.OK);
    }

    @GetMapping("/trip/recent-trip/customer/{customerId}")
    public ResponseEntity<ResponseRecentTripFromCustomer> getRecentTripFromCustomer(@RequestHeader("Authorization") String bearerToken,
                                                                                    @PathVariable("customerId") String customerId) {

        ResponseRecentTripFromCustomer responseRecentTripFromCustomer = tripService.getRecentTripFromCustomer(bearerToken, customerId);

        return new ResponseEntity<>(responseRecentTripFromCustomer, HttpStatus.OK);
    }

    @GetMapping("/trip/recent-trip/driver/{driverId}")
    public ResponseEntity<ResponseRecentTripFromDriver> getRecentTripFromDriver(@RequestHeader("Authorization") String bearerToken,
                                                                                @PathVariable("driverId") String driverId) {

        ResponseRecentTripFromDriver responseRecentTripFromDriver = tripService.getRecentTripFromDriver(bearerToken, driverId);

        return new ResponseEntity<>(responseRecentTripFromDriver, HttpStatus.OK);
    }

    @GetMapping("/trip/total-trip/{user}/{id}")
    public ResponseEntity<ResponseTotalTrip> getTotalTrip(@RequestHeader("Authorization") String bearerToken,
                                                          @PathVariable("user") String userType,
                                                          @PathVariable("id") String id) {

        ResponseTotalTrip responseTotalTrip = tripService.getTotalTrip(bearerToken, userType, id);

        return new ResponseEntity<>(responseTotalTrip, HttpStatus.OK);
    }

    @GetMapping("/trip/average-income/driver/{driverId}")
    public ResponseEntity<ResponseAverageIncomePerDrive> getAverageIncomePerDrive(@RequestHeader("Authorization") String bearerToken,
                                                                                  @PathVariable("driverId") String driverId) {

        ResponseAverageIncomePerDrive responseAverageIncomePerDrive = tripService.getAverageIncomePerDrive(bearerToken, driverId);

        return new ResponseEntity<>(responseAverageIncomePerDrive, HttpStatus.OK);
    }


    @GetMapping("/trip/{tripId}/get-driverId")
    public ResponseEntity<String> getDriverIdByTripId(@RequestHeader("Authorization") String bearerToken,
                                                      @PathVariable("tripId") String tripId) {

        String driverId = tripService.getDriverIdByTripId(bearerToken, tripId);

        return new ResponseEntity<>(driverId, HttpStatus.OK);
    }

    @PostMapping("/trip/drive-booking/accept-drive")
    public ResponseEntity<ResponseStatus> sendReceivedDriverInfo(@RequestHeader("Authorization") String bearerToken,
                                                                 @RequestBody RequestReceivedDriverInfo requestReceivedDriverInfo) {

        ResponseStatus responseStatus = tripService.sendReceivedDriverInfo(bearerToken, requestReceivedDriverInfo);

        return new ResponseEntity<>(responseStatus, HttpStatus.CREATED);
    }

    @PostMapping("/trip/confirm-pickup-location")
    public ResponseEntity<ResponseStatus> confirmPickupLocationArrival(@RequestHeader("Authorization") String bearerToken,
                                                                       @RequestBody PickUpAndCompletionLocation pickUpLocation) {

        ResponseStatus responseStatus = tripService.confirmPickupLocationArrival(bearerToken, pickUpLocation);

        return new ResponseEntity<>(responseStatus, HttpStatus.OK);
    }

    @PostMapping("/trip/confirm-completed-location")
    public ResponseEntity<ResponseStatus> confirmDriverTripCompletion(@RequestHeader("Authorization") String bearerToken,
                                                                      @RequestBody PickUpAndCompletionLocation completionLocation) {

        ResponseStatus responseStatus = tripService.confirmDriverTripCompletion(bearerToken, completionLocation);

        return new ResponseEntity<>(responseStatus, HttpStatus.OK);
    }

    @PutMapping("/trip/{tripId}")
    public ResponseEntity<TripDto> updateTripStatus(@RequestHeader("Authorization") String bearerToken,
                                                           @PathVariable("tripId") String tripId,
                                                           @RequestParam("status") String status) {

        TripDto tripDto = tripService.updateTripStatus(bearerToken, tripId, status);

        return new ResponseEntity<>(tripDto, HttpStatus.OK);
    }

    @DeleteMapping("/trip/{tripId}")
    public ResponseEntity<ResponseStatus> deleteTrip(@RequestHeader("Authorization") String bearerToken,
                                                     @PathVariable("tripId") String tripId) {

        ResponseStatus responseStatus;

        try {
            tripService.deleteTrip(bearerToken, tripId);

            responseStatus = new ResponseStatus(new Date(), "Successfully");

            return new ResponseEntity<>(responseStatus, HttpStatus.OK);
        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }
}
