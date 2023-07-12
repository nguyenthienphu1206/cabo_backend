package cabo.backend.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestCheckIn {

    @NotNull(message = "Check-In should not empty")
    private Long checkInAt;

    private Long checkOutAt;

    @NotEmpty(message = "Driver Id should not empty")
    private String driverId;
}
