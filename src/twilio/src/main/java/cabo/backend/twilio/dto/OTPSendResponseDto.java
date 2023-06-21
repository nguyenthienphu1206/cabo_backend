package cabo.backend.twilio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OTPSendResponseDto {

    private OtpStatus status;
    private String message;
}
