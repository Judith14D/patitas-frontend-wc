package pe.edu.cibertec.patitas_frontend_wc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PatitasFrontendWcApplication {

	public static void main(String[] args) {
		SpringApplication.run(PatitasFrontendWcApplication.class, args);
	}

}
