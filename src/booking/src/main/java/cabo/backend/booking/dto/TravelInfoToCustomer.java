package cabo.backend.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TravelInfoToCustomer {

    private String fcmToken;

    private String driverRemainingDistance;

    private String driverRemainingTime;
}
