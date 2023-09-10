package bg.digistrict.digistricserviceregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class DigistricServiceRegistryApplication {

	public static void main(String[] args) {
		SpringApplication.run(DigistricServiceRegistryApplication.class, args);
	}
}