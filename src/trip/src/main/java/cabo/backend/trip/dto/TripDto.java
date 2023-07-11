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
public class TripDto {

    private long cost;

    private DocumentReference customerId;

    private DocumentReference driverId;

    private double distance;

    private long startTime;

    private long endTime;

    private GeoPoint fromLocation;

    private GeoPoint toLocation;

    private int paymentType;
}
