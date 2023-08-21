package cabo.backend.receiverservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ReceiverServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReceiverServiceApplication.class, args);
	}

}
