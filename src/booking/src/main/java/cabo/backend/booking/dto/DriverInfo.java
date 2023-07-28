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
public class DriverInfo {

    private String fullName;

    private String phoneNumber;

    private String avatar;

    private String brand;

    private String regNo;

    private GeoPoint driverCurrentLocation;
}
