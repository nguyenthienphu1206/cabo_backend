package cabo.backend.vehicle.service;

import cabo.backend.vehicle.dto.VehicleDto;

public interface VehicleService {

    String registerVehicle(String idToken, VehicleDto vehicleDto);
}