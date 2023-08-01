package cabo.backend.trip.dto;

import cabo.backend.trip.entity.GeoPoint;
import com.google.cloud.firestore.DocumentReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestReceivedDriverInfo {

    private DocumentReference driverId;

    private String tripId;

    private GeoPoint currentLocation;
}
