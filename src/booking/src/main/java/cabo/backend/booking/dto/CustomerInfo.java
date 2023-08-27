package cabo.backend.booking.dto;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerInfo {

    private String fullName;
    private String phoneNumber;
    private String avatar;
}
