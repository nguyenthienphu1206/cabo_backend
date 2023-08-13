package cabo.backend.callcenter.service;

import cabo.backend.callcenter.dto.RequestBookADrive;
import cabo.backend.callcenter.dto.ResponseStatus;

public interface CallCenterService {

    void subscribeNotification(String bearerToken, String fcmToken);

    ResponseStatus sendInfoCustomerFromCallCenter(String bearerToken, RequestBookADrive requestBookADrive);
}
