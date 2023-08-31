package cabo.backend.bingmap.dto;

import cabo.backend.bingmap.entity.GeoPoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestOriginsAndDestinationsLocation {

    private GeoPoint fromLocation;

    private GeoPoint toLocation;

    private String vehicleType;
}
