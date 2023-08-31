package cabo.backend.receiverservice.dto;

import cabo.backend.receiverservice.entity.GeoPoint;
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

    private String distance;

    private String cost;

    private String carType;

    private int paymentType;
}
