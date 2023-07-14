package cabo.backend.trip.dto;

import com.google.cloud.firestore.GeoPoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseRecentTripFromDriver {

    private long cost;

    private double distance;

    private long startTime;

    private long pickUpTime;

    private long endTime;

    private GeoPoint driverStartLocation;

    private GeoPoint toLocation;
}
