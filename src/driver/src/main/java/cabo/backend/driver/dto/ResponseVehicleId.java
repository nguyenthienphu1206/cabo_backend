package cabo.backend.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseVehicleId {

    private Date timestamp;

    private String vehicleId;
}
