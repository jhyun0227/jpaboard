package practice.jpaboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import practice.jpaboard.security.auth.UserDetailsImpl;

@EnableJpaAuditing
@SpringBootApplication
public class JpaboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpaboardApplication.class, args);
	}

}
