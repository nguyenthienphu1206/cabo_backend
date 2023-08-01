package cabo.backend.trip.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.cloud.firestore.DocumentReference;
import com.google.firebase.cloud.FirestoreClient;

import java.io.IOException;

public class DocumentReferenceDeserializer extends JsonDeserializer<DocumentReference> {
    @Override
    public DocumentReference deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String path = p.getValueAsString();
        // Tạo DocumentReference từ đường dẫn
        return FirestoreClient.getFirestore().document(path);
    }
}
