package cabo.backend.driver.dto;

import cabo.backend.driver.serializer.DocumentReferenceSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.cloud.firestore.DocumentReference;
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
    private String driverStatus;

    @JsonSerialize(using = DocumentReferenceSerializer.class)
    private DocumentReference vehicleId;
}
