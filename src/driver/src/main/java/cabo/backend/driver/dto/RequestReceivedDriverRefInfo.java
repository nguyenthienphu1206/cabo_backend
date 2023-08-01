package cabo.backend.driver.dto;

import cabo.backend.driver.entity.GeoPoint;
import com.google.cloud.firestore.DocumentReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestReceivedDriverRefInfo {

    private DocumentReference driverId;

    private String tripId;

    private GeoPoint currentLocation;
}
