package cabo.backend.taxistatusservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class TaxiStatusServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaxiStatusServiceApplication.class, args);
	}

}
