package cabo.backend.driver.service;

import cabo.backend.driver.dto.NotificationDto;
import cabo.backend.driver.dto.RequestGPS;
import cabo.backend.driver.dto.RequestUidAndNotification;
import cabo.backend.driver.dto.ResponseStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;


@FeignClient(url = "${BOOKING_SERVICE_URL}", value = "BOOKING-SERVICE")
public interface BookingServiceClient {

    @PostMapping("/api/v1/booking/send-notification-designate-driver")
    ResponseStatus sendNotificationToDesignatedDriver(@RequestHeader("Authorization") String bearerToken,
                                                               @RequestBody RequestUidAndNotification requestUidAndNotification);

    @DeleteMapping("/api/v1/booking/remove-all-gps")
    ResponseStatus removeAllGPS(@RequestHeader("Authorization") String bearerToken);

    @PostMapping("/api/v1/booking/drive-booking/current-gps")
    ResponseStatus collectGPS(@RequestHeader("Authorization") String bearerToken,
                              @RequestBody RequestGPS requestGPS);
}
