package practice.jpaboard.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import practice.jpaboard.security.auth.UserDetailsImpl;

import javax.persistence.EntityManager;
import java.util.Optional;

@Configuration
public class ProjectConfig {

    /**
     * Querydsl을 위한 JPAQueryFactory 설정
     */
    @Bean
    JPAQueryFactory jpaQueryFactory(EntityManager em) {
        return new JPAQueryFactory(em);
    }

    /**
     * 계정 비밀번호 암호화를 위한 BCryptPasswordEncoder 설정
     */
    @Bean
    BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Auditing을 하기 위한 설정
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAware<String>() {
            @Override
            public Optional<String> getCurrentAuditor() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                System.out.println("Auditing Authentication is present = " + (authentication != null));

                if (authentication == null || !authentication.isAuthenticated()) {
                    return Optional.empty();
                }

                UserDetailsImpl userDetailsImpl = (UserDetailsImpl) authentication.getPrincipal();
                return Optional.of(userDetailsImpl.getMemberNickname());
            }
        };
    }
}
