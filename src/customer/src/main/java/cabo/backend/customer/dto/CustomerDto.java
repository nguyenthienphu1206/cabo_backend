package cabo.backend.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDto {
    private String uid;
    private String fullName;
    private String phoneNumber;
    private String avatar;
    private Boolean vip;
    private Boolean isRegisteredOnApp;
}
