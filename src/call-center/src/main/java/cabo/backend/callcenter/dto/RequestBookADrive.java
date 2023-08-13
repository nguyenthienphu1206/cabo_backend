package cabo.backend.callcenter.dto;

import cabo.backend.callcenter.entity.GeoPoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestBookADrive {

    private GeoPoint customerOrderLocation;

    private GeoPoint toLocation;

    private String customerPhoneNumber;

    private double distance;

    private long cost;

    private String carType;

    private int paymentType;
}
