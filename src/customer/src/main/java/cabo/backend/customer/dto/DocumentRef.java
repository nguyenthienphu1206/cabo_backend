package cabo.backend.customer.dto;

import cabo.backend.customer.serializer.DocumentReferenceSerializer;
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
public class DocumentRef {

    @JsonSerialize(using = DocumentReferenceSerializer.class)
    private DocumentReference documentReference;
}
