package cabo.backend.driver.dto;

import cabo.backend.driver.entity.GeoPoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestReceivedDriverInfo {

    private String driverId;

    private String tripId;

    private GeoPoint currentLocation;
}
