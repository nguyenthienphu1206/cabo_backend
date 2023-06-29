package cabo.backend.customer.service;

import cabo.backend.customer.dto.CustomerDto;
import cabo.backend.customer.entity.Customer;

public interface CustomerService {

    String getCustomerId(String idToken);

    String saveCustomer(CustomerDto customerDto);

    Customer getCustomerDetails(String customerId);

    Boolean checkPhoneExistence(String phoneNumber);
}
