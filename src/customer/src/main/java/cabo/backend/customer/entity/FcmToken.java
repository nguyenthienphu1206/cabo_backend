package cabo.backend.customer.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FcmToken {

    private String fcmToken;

    private String fcmClient;

    private String uid;
}
