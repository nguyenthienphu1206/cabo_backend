package cabo.backend.twilio.controller;

import cabo.backend.twilio.dto.OTPSendRequestDto;
import cabo.backend.twilio.dto.OTPSendResponseDto;
import cabo.backend.twilio.service.TwilioOTPService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/")
public class TwilioOTPController {

    private TwilioOTPService twilioOTPService;

    public TwilioOTPController(TwilioOTPService twilioOTPService) {
        this.twilioOTPService = twilioOTPService;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<OTPSendResponseDto> sendOTP(@RequestBody OTPSendRequestDto otpSendRequestDto) {

        OTPSendResponseDto otpSendResponseDto = twilioOTPService.sendOTPForLogin(otpSendRequestDto);

        return new ResponseEntity<>(otpSendResponseDto, HttpStatus.OK);
    }
}
