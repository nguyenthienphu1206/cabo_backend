package cabo.backend.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestRegistryInfo {

    @NotEmpty(message = "Full name should not empty")
    private String fullName;

    @NotEmpty(message = "Phone Number should not empty")
    private String phoneNumber;
}
