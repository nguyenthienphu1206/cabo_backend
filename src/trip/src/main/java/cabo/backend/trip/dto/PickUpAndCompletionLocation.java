package cabo.backend.trip.dto;

import cabo.backend.trip.entity.GeoPoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PickUpAndCompletionLocation {

    private String driverId;

    private String tripId;

    private GeoPoint currentLocation;
}
