package cabo.backend.customer.service;

import cabo.backend.customer.dto.CustomerDto;
import cabo.backend.customer.dto.RequestRegisterCustomer;
import cabo.backend.customer.dto.ResponseOverview;

public interface CustomerService {

    String registerCustomer(String idToken, RequestRegisterCustomer requestRegisterCustomer);

    String saveCustomer(String idToken, CustomerDto customerDto);

    CustomerDto getCustomerDetails(String idToken, String customerId);

    Boolean checkPhoneExistence(String idToken, String phoneNumber);

    ResponseOverview getOverview(String idToken, String customerId);
}
