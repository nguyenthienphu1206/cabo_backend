package cabo.backend.vehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VehicleDto {
    private int slot;

    private String type;

    private String regNo;

    private String brand;
}
