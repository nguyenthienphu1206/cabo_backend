package cabo.backend.driver.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Attendance {

    private Long checkInAt;

    private Long checkOutAt;

    private String driverId;
}
