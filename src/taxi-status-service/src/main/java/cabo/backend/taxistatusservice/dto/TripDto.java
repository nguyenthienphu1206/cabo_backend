package cabo.backend.taxistatusservice.dto;

import cabo.backend.taxistatusservice.entity.GeoPoint;
import lombok.*;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripDto {

    private long cost;

    private String customerName;

    private String driverName;

    private double distance;

    private long startTime;

    private long pickUpTime;

    private long endTime;

    private GeoPoint customerOrderLocation;

    private GeoPoint driverStartLocation;

    private GeoPoint toLocation;

    private int paymentType;

    private String status;

    private long updatedAt;
}
