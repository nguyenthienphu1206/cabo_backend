package cabo.backend.receiverservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestBookADriveEvent {

    private String bearerToken;

    private String customerId;

    private RequestBookADrive requestBookADrive;
}
