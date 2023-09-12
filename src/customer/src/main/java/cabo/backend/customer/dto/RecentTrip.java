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

    private String cost;

    private String distance;

    private long startTime;

    private long endTime;

    private String customerOrderLocation;

    private String toLocation;

    private int paymentType;
}
