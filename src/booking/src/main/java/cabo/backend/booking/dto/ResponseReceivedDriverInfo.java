package cabo.backend.booking.dto;

import cabo.backend.booking.entity.GeoPoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseReceivedDriverInfo {

    private String driverId;

    private String tripId;

    private GeoPoint currentLocation;
}
