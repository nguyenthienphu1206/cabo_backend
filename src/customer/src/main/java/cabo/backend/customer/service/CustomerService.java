package cabo.backend.customer.service;

import cabo.backend.customer.dto.*;

import java.util.List;

public interface CustomerService {

    DocumentRef getDocumentById(String bearerToken, String customerId);

    String getNameByCustomerId(String bearerToken, String customerId);

    String getUidByCustomerId(String customerId);

    ResponseFullNameAndPhone getNameAndPhoneByCustomerId(String bearerToken, String customerId);

    String registerCustomer(String idToken, RequestRegisterCustomer requestRegisterCustomer);

    String createCustomerIfPhoneNumberNotRegistered(String bearerToken, String phoneNumber);

    String saveCustomer(String idToken, CustomerDto customerDto);

    CustomerDto getCustomerDetails(String idToken, String customerId);

    Boolean checkPhoneExistence(String idToken, String phoneNumber);

    ResponseOverview getOverview(String idToken, String customerId);

    ResponseDriverInformation bookADrive(String bearerToken, String customerId, RequestBookADrive requestBookADrive);

    void subscribeNotification(String bearerToken, String fcmToken);
}
