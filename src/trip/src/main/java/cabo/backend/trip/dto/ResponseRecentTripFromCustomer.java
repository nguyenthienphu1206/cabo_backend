package cabo.backend.trip.dto;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.GeoPoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseRecentTripFromCustomer {

    private Long cost;

    private double distance;

    private Long startTime;

    private Long endTime;

    private GeoPoint customerOrderLocation;

    private GeoPoint toLocation;

    private int paymentType;
}
