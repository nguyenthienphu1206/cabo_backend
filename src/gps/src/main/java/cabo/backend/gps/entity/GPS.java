package cabo.backend.gps.entity;

import com.google.cloud.firestore.DocumentReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GPS {

    private DocumentReference driverId;

    private GeoPoint currentLocation;
}
