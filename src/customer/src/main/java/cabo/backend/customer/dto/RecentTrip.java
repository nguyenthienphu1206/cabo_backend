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
public class RecentTrip {

    private long cost;

    private double distance;

    private long startTime;

    private long endTime;

    private GeoPoint customerOrderLocation;

    private GeoPoint toLocation;

    private int paymentType;
}
