package cabo.backend.vehicle.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Vehicle {
    private String slot;

    private String type;

    private String regNo;

    private String brand;
}
