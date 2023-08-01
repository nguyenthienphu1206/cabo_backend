package cabo.backend.vehicle.service;

import cabo.backend.vehicle.dto.DocumentRef;
import cabo.backend.vehicle.dto.VehicleDto;

public interface VehicleService {

    DocumentRef getDocumentById(String bearerToken, String vehicleId);

    VehicleDto getVehicle(String bearerToken, String vehicleId);

    String registerVehicle(String bearerToken, VehicleDto vehicleDto);
}
