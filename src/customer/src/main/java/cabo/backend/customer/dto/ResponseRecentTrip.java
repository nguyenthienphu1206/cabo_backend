package cabo.backend.customer.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseRecentTrip {

    private String cost;

    private String distance;

    private long startTime;

    private long endTime;

    private String customerOrderLocation;

    private String toLocation;

    private int paymentType;
}
