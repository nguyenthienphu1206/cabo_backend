package cabo.backend.driver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Driver {

    private String uid;
    private String fullName;
    private String phoneNumber;
    private String avatar;
    private String carId;
}
