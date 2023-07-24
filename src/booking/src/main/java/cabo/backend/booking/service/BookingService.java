package cabo.backend.booking.service;

import cabo.backend.booking.dto.NotificationDto;

public interface BookingService {

    void sendNotificationToAllDevices(NotificationDto notificationDto);
}
