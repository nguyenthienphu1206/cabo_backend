package cabo.backend.taxistatusservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDriveDone {

    private String fcmToken;

    private String tripId;
}
