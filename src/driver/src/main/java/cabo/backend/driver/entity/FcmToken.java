package cabo.backend.driver.entity;

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

    private Boolean isDriver;

    private String uid;
}
