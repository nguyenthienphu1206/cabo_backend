package cabo.backend.driver.service;

import cabo.backend.driver.dto.*;

import java.util.List;

public interface DriverService {

    DocumentRef getDocumentById(String bearerToken, String driverId);

    String getNameByDriverId(String bearerToken, String driverId);

    List<TripDto> getAllTripsById(String bearerToken, String driverId);

    DriverInfo getDriverInfoByDriverIdAndTripId(String bearerToken, String driverId, String tripId);

    String getUidByDriverId(String bearerToken, String driverId);

    Integer getDriverStatusIntByUid(String uid);

    String registerInfo(String idToken, RequestRegistryInfo requestRegistryInfo);

    ResponseDriverDetails getDriverDetails(String idToken, String driverId);

    Boolean checkPhoneExistence(String idToken, String phoneNumber);

    String registerDriverVehicle(String idToken, String driverId, RequestRegisterVehicle requestRegisterVehicle);

    ResponseStatus checkIn(String bearerToken, RequestCheckIn requestCheckIn);

    ResponseStatus checkOut(String bearerToken, RequestCheckOut requestCheckOut);

    ResponseStatus updateDriverStatus(String bearerToken, String driverId, int status);

    ResponseOverview getOverview(String idToken, String driverId);

    void subscribeNotification(String bearerToken, String fcmToken);

    ResponseStatus sendReceivedDriverInfo(String bearerToken, RequestReceivedDriverInfo requestReceivedDriverInfo);

    ResponseStatus sendGPS(String bearerToken, RequestGPS requestGPS);
}
