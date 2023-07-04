package cabo.backend.driver.service;

import cabo.backend.driver.dto.DriverDto;
import cabo.backend.driver.dto.ResponseDriverDetails;

public interface DriverService {

    String getDriverId(String idToken, String fullName);

    String saveDriver(DriverDto driverDto);

    ResponseDriverDetails getDriverDetails(String driverId);

    Boolean checkPhoneExistence(String phoneNumber);
}
