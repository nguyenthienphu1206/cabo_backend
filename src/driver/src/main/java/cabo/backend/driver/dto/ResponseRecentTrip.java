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
public class ResponseRecentTrip {

    private String cost;

    private String distance;

    private long startTime;

    private long pickUpTime;

    private long endTime;

    private String driverStartLocation;

    private String toLocation;
}
