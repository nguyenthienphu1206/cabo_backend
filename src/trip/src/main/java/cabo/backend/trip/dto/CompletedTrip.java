package cabo.backend.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompletedTrip {

    private String tripId;

    private long cost;

    private String customerName;

    private String driverName;

    private double distance;

    private long startTime;

    private long pickUpTime;

    private long endTime;

    private String customerOrderLocation;

    private String driverStartLocation;

    private String toLocation;

    private String customerPhoneNumber;

    private int paymentType;

    private String status;

    private long updatedAt;
}
