package cabo.backend.driver.service;

import cabo.backend.driver.dto.DriverDto;

public interface DriverService {

    String getDriverId(String idToken);

    String saveDriver(DriverDto driverDto);

    DriverDto getDriverDetails(String driverId);

    Boolean checkPhoneExistence(String phoneNumber);
}
