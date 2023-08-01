package cabo.backend.customer.dto;

import cabo.backend.customer.entity.GeoPoint;
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
}
