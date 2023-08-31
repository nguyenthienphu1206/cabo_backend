package cabo.backend.taxistatusservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DriveStatus {

    private String fcmToken;

    private String tripId;

    private TripDto tripDto;
}
