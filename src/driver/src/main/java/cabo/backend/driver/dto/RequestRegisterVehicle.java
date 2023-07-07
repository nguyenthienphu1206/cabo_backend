package cabo.backend.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestRegisterVehicle {

    private int slot;

    private String type;

    private String regNo;

    private String brand;
}
