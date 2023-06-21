package cabo.backend.twilio.service;

import cabo.backend.twilio.config.TwilioConfig;
import cabo.backend.twilio.dto.OTPSendRequestDto;
import cabo.backend.twilio.dto.OTPSendResponseDto;
import cabo.backend.twilio.dto.OtpStatus;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class TwilioOTPService {

    private TwilioConfig twilioConfig;

    private Map<String, String> otpMap = new HashMap<>();

    public TwilioOTPService(TwilioConfig twilioConfig) {
        this.twilioConfig = twilioConfig;
    }

    @PostConstruct
    public void initTwilio() {
        Twilio.init(twilioConfig.getAccountSid(), twilioConfig.getAuthToken());
    }

    public OTPSendResponseDto sendOTPForLogin(OTPSendRequestDto otpSendRequestDto) {

        OTPSendResponseDto otpSendResponseDto = null;

        try {
            PhoneNumber to = new PhoneNumber(otpSendRequestDto.getPhoneNumber());
            PhoneNumber from = new PhoneNumber(twilioConfig.getTrialNumber());
            String otp = generateOTP();
            String otpMessage = "Your OTP is: " + otp;

            Message message = Message
                    .creator(to, from, otpMessage)
                    .create();

            //otpMap.put(phoneNumber, otp);

            otpSendResponseDto = new OTPSendResponseDto(OtpStatus.DELIVERED, otpMessage);

        } catch (Exception ex) {
            otpSendResponseDto = new OTPSendResponseDto(OtpStatus.FAILDED, ex.getMessage());
        }

        return otpSendResponseDto;
    }

    public String validateOTP(String numberPhone, String userInputOtp) {
        if (userInputOtp.equals(otpMap.get(numberPhone))) {
            return "Valid OTP please proceed with your transaction !";
        } else {
            return "Invalid OTP please retry !";
        }
    }

    private String generateOTP() {

        return new DecimalFormat("0000")
                .format(new Random().nextInt(9999));
    }
}
