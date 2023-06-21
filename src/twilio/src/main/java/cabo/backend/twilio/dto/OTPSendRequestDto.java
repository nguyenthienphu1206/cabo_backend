package cabo.backend.twilio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OTPSendRequestDto {

    private String phoneNumber;
    private String oneTimePassword;
}
