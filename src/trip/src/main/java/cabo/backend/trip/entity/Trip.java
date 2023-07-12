package cabo.backend.trip.entity;

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
public class Trip {

    private Long cost;

    private DocumentReference customerId;

    private DocumentReference driverId;

    private double distance;

    private Long startTime;

    private Long pickUpTime;

    private Long endTime;

    private GeoPoint customerOrderLocation;

    private GeoPoint driverStartLocation;

    private GeoPoint toLocation;

    private int paymentType;
}
