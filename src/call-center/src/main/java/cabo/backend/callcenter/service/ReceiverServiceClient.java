package cabo.backend.callcenter.service;

import cabo.backend.callcenter.dto.RequestBookADrive;
import cabo.backend.callcenter.dto.ResponseStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "${RECEIVER-SERVICE_URL}", value = "RECEIVER-SERVICE")
public interface ReceiverServiceClient {

    @PostMapping("/api/v1/receiver-service")
    ResponseStatus receiverAndBookDriverFromCallCenter(@RequestHeader("Authorization") String bearerToken,
                                                       @RequestBody RequestBookADrive requestBookADrive);
}
