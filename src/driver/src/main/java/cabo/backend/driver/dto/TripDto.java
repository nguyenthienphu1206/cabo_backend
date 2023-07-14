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
public class TripDto {

    private long cost;

    private double distance;

    private long startTime;

    private long pickUpTime;

    private long endTime;

    private GeoPoint driverStartLocation;

    private GeoPoint toLocation;
}
