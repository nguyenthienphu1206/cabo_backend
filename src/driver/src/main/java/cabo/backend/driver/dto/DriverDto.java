package cabo.backend.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverDto {

    private String uid;
    private String fullName;
    private String phoneNumber;
    private String avatar;
    private String carId;
}
