package cabo.backend.bingmap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestAddress {

    private String customerOrderLocation;

    private String toLocation;
}
