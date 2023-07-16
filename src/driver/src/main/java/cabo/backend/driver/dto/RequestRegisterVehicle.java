package cabo.backend.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestRegisterVehicle {

    private int slot;

    @NotEmpty(message = "Type should not empty")
    private String type;

    @NotEmpty(message = "RegNo should not empty")
    private String regNo;

    @NotEmpty(message = "Brand should not empty")
    private String brand;
}
