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

    private String cost;

    private String distance;

    private long startTime;

    private long endTime;

    private String customerOrderLocation;

    private String toLocation;

    private int paymentType;
}
