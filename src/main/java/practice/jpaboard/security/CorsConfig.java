package practice.jpaboard.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        System.out.println("CorsConfig.corsFilter() 함수 실행");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); //내 서버가 응답할 때 json을 자바스크립트에서 처리할 수 있게 할지 설정
        config.addAllowedOrigin("*"); //모든 ip에 응답을 허용
        config.addAllowedHeader("*"); //모든 헤더에 응답을 허용
        config.addAllowedMethod("*"); //모든 매서드의 요청을 허용
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
