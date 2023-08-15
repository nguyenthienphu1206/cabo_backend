package cabo.backend.bingmap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    private String addressLine;

    private String adminDistrict2;

    private String adminDistrict;

    private String countryRegion;
}
