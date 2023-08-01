package cabo.backend.bingmap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BingMapsResponse {

    private List<ResourceSet> resourceSets;
}
