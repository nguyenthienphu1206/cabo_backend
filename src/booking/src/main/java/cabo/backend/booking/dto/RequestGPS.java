package cabo.backend.booking.dto;

import cabo.backend.booking.entity.GeoPoint;
import com.google.cloud.firestore.DocumentReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestGPS {

    private String uid;

    private GeoPoint currentLocation;
}
