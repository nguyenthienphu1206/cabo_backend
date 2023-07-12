package cabo.backend.driver.service;

import cabo.backend.driver.dto.*;

public interface DriverService {

    String registerInfo(String idToken, RequestRegistryInfo requestRegistryInfo);

    String saveDriver(String idToken, DriverDto driverDto);

    ResponseDriverDetails getDriverDetails(String idToken, String driverId);

    Boolean checkPhoneExistence(String idToken, String phoneNumber);

    String registerDriverVehicle(String idToken, String driverId, RequestRegisterVehicle requestRegisterVehicle);

    ResponseCheckIn checkIn(String bearerToken, RequestCheckIn requestCheckIn);
}
