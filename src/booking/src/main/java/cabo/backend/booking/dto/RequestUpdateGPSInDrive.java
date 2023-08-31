package cabo.backend.booking.dto;

import cabo.backend.booking.entity.GeoPoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestUpdateGPSInDrive {

    private String uid;

    private String customerId;

    private GeoPoint currentLocation;

    private GeoPoint customerOrderLocation;
}
