package cabo.backend.driver.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GeoPoint {
    private double latitude;
    private double longitude;

    @JsonCreator
    public GeoPoint(@JsonProperty("latitude") double latitude, @JsonProperty("longitude") double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }


}
