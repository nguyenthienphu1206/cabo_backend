package cabo.backend.booking.service;

import cabo.backend.booking.dto.NotificationDto;
import cabo.backend.booking.dto.RequestBookADrive;
import cabo.backend.booking.dto.RequestGPS;
import cabo.backend.booking.dto.ResponseDriverInformation;

public interface BookingService {

    void sendNotificationToAllDevices(NotificationDto notificationDto);

    void collectGPSFromDriver(String bearerToken, RequestGPS requestGPS);

    ResponseDriverInformation getDriverInformation(String bearerToken, String customerId, RequestBookADrive requestBooking);
}
