package cabo.backend.driver.service;

import cabo.backend.driver.dto.DriverDto;
import cabo.backend.driver.dto.RequestRegisterVehicle;
import cabo.backend.driver.dto.RequestRegistryInfo;
import cabo.backend.driver.dto.ResponseDriverDetails;

public interface DriverService {

    String registerInfo(String idToken, RequestRegistryInfo requestRegistryInfo);

    String saveDriver(String idToken, DriverDto driverDto);

    ResponseDriverDetails getDriverDetails(String idToken, String driverId);

    Boolean checkPhoneExistence(String idToken, String phoneNumber);

    String registerDriverVehicle(String idToken, String driverId, RequestRegisterVehicle requestRegisterVehicle);
}
