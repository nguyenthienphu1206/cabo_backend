package cabo.backend.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestIdTokenDto {
    private String idToken;
    private String fullName;
}
