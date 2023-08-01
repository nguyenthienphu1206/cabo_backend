package cabo.backend.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestUidAndNotification {

    private String uid;

    private NotificationDto notificationDto;
}
