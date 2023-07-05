package cabo.backend.vehicle.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
public class FirebaseInitialization {

    @PostConstruct
    public void initialization() {
        FileInputStream serviceAccount = null;

        try {
            serviceAccount = new FileInputStream("./vehicle/serviceAccountKey.json");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        FirebaseOptions options = null;
        try {
            options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://cabo-backend-f8715-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FirebaseApp.initializeApp(options);

    }
}
