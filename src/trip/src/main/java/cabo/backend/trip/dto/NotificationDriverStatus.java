package cabo.backend.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDriverStatus {

    private String status;

    private String fcmToken;

    private String tripId;

    private NotificationDto notificationDto;
}
