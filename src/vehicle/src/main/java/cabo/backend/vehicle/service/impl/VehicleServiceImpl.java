package cabo.backend.vehicle.service.impl;

import cabo.backend.vehicle.dto.DocumentRef;
import cabo.backend.vehicle.dto.VehicleDto;
import cabo.backend.vehicle.entity.Vehicle;
import cabo.backend.vehicle.exception.ResourceNotFoundException;
import cabo.backend.vehicle.service.VehicleService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class VehicleServiceImpl implements VehicleService {

    private ModelMapper modelMapper;

    private static final String COLLECTION_NAME_VEHICLE = "vehicles";

    private final CollectionReference collectionRefVehicle;

    private Firestore dbFirestore;

    public VehicleServiceImpl(Firestore dbFirestore) {

        this.dbFirestore = dbFirestore;

        this.collectionRefVehicle = dbFirestore.collection(COLLECTION_NAME_VEHICLE);
    }

    @Override
    public DocumentRef getDocumentById(String bearerToken, String vehicleId) {

        DocumentReference documentReference = collectionRefVehicle.document(vehicleId);

        DocumentRef documentRef = DocumentRef.builder()
                .documentReference(documentReference)
                .build();

        return documentRef;
    }

    @Override
    public VehicleDto getVehicle(String bearerToken, String vehicleId) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        DocumentReference documentReference = collectionRefVehicle.document(vehicleId);

        ApiFuture<DocumentSnapshot> future = documentReference.get();

        VehicleDto vehicleDto = null;

        try {
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                Vehicle vehicle = document.toObject(Vehicle.class);

                if (vehicle != null) {
                    vehicleDto = VehicleDto.builder()
                            .slot(vehicle.getSlot())
                            .type(vehicle.getType())
                            .regNo(vehicle.getRegNo())
                            .brand(vehicle.getBrand())
                            .build();
                }
            }
            else {
                throw new ResourceNotFoundException("Document", "VehicleId", vehicleId);
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return vehicleDto;
    }

    @Override
    public String registerVehicle(String bearerToken, VehicleDto vehicleDto) {

        String idToken = bearerToken.substring(7);

        //FirebaseToken decodedToken = decodeToken(idToken);

        Vehicle vehicle = Vehicle.builder()
                .slot(vehicleDto.getSlot())
                .type(vehicleDto.getType())
                .regNo(vehicleDto.getRegNo())
                .brand(vehicleDto.getBrand())
                .build();

        DocumentReference documentReference = collectionRefVehicle.document();

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
