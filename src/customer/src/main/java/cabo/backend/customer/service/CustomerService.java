package cabo.backend.customer.service;

import cabo.backend.customer.dto.CustomerDto;
import cabo.backend.customer.entity.Customer;

public interface CustomerService {

    String getCustomerId(String idToken, String fullName);

    String saveCustomer(String idToken, CustomerDto customerDto);

    CustomerDto getCustomerDetails(String idToken, String customerId);

    Boolean checkPhoneExistence(String idToken, String phoneNumber);
}
