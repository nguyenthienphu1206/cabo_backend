package cabo.backend.vehicle.service.impl;

import cabo.backend.vehicle.dto.VehicleDto;
import cabo.backend.vehicle.entity.Vehicle;
import cabo.backend.vehicle.service.VehicleService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VehicleServiceImpl implements VehicleService {

    private ModelMapper modelMapper;

    private static final String COLLECTION_NAME = "vehicles";

    @Override
    public String registerVehicle(VehicleDto vehicleDto) {

        Vehicle vehicle = Vehicle.builder()
                .slot(vehicleDto.getSlot())
                .type(vehicleDto.getType())
                .regNo(vehicleDto.getRegNo())
                .brand(vehicleDto.getBrand())
                .build();

        Firestore dbFirestore = FirestoreClient.getFirestore();

        DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document();

        ApiFuture<WriteResult> collectionApiFuture = documentReference.set(vehicle);
        log.info("Test2 ----> ");
        String vehicleId = documentReference.getId();

        return vehicleId;
    }

    private FirebaseToken decodeToken(String idToken) {

        FirebaseToken decodedToken;

        try {
            decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }

        return decodedToken;
    }
}
