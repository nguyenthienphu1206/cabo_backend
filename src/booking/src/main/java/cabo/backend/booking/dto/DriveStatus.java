package cabo.backend.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriveStatus {

    private String bearerToken;

    private String fcmToken;

    private String tripId;

    private String status;
}
