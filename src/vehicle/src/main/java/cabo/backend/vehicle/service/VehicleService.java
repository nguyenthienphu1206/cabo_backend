package cabo.backend.vehicle.service;

import cabo.backend.vehicle.dto.VehicleDto;

public interface VehicleService {

    VehicleDto getVehicle(String vehicleId);

    String registerVehicle(VehicleDto vehicleDto);
}
