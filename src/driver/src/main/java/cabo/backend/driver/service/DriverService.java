package cabo.backend.driver.service;

import cabo.backend.driver.dto.*;

public interface DriverService {

    String registerInfo(String idToken, RequestRegistryInfo requestRegistryInfo);

    String saveDriver(String idToken, DriverDto driverDto);

    ResponseDriverDetails getDriverDetails(String idToken, String driverId);

    Boolean checkPhoneExistence(String idToken, String phoneNumber);

    String registerDriverVehicle(String idToken, String driverId, RequestRegisterVehicle requestRegisterVehicle);

    ResponseCheckInOut checkIn(String bearerToken, RequestCheckIn requestCheckIn);

    ResponseCheckInOut checkOut(String bearerToken, RequestCheckOut requestCheckOut);

    ResponseOverview getOverview(String idToken, String driverId);
}
