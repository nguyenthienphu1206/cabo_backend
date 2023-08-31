package cabo.backend.booking.service;

import cabo.backend.booking.dto.*;

public interface BookingService {

    void sendNotification(NotificationDto notificationDto);

    void collectGPSFromDriver(String bearerToken, RequestGPS requestGPS);

    void sendGPSToServer(RequestUpdateGPSInDrive requestUpdateGPSInDrive);

    ResponseDriverInformation getDriverInformation(String bearerToken, String customerId, RequestBookADrive requestBooking);
}
