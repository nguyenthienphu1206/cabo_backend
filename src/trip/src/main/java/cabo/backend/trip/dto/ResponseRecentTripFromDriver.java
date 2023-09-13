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

    private String cost;

    private String distance;

    private long startTime;

    private long pickUpTime;

    private long endTime;

    private String driverStartLocation;

    private String toLocation;
}
