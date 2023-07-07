package cabo.backend.driver.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.cloud.firestore.DocumentReference;

import java.io.IOException;

public class DocumentReferenceSerializer extends JsonSerializer<DocumentReference> {

    @Override
    public void serialize(DocumentReference value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.getPath());
    }
}
