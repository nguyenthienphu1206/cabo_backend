package cabo.backend.driver.dto;

import cabo.backend.driver.serializer.DocumentReferenceSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.cloud.firestore.DocumentReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDriverDetails {

    private String fullName;
    private String phoneNumber;
    private String avatar;

    @JsonSerialize(using = DocumentReferenceSerializer.class)
    private DocumentReference carId;
}
