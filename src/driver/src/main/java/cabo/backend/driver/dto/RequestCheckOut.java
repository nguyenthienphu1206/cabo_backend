package cabo.backend.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestCheckOut {

    @NotNull(message = "Check-In should not empty")
    private Long checkInAt;

    @NotNull(message = "Check-Out should not empty")
    private Long checkOutAt;

    @NotEmpty(message = "Driver Id should not empty")
    private String driverId;
}
