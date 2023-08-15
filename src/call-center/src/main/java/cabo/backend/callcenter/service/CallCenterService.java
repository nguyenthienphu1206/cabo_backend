package cabo.backend.callcenter.service;

import cabo.backend.callcenter.dto.RequestBookADrive;
import cabo.backend.callcenter.dto.ResponseStatus;
import cabo.backend.callcenter.dto.TripDto;

import java.util.List;

public interface CallCenterService {

    void subscribeNotification(String bearerToken, String fcmToken);

    ResponseStatus sendInfoCustomerFromCallCenter(String bearerToken, RequestBookADrive requestBookADrive);

    List<TripDto> getAllTrips(String bearerToken);
}
