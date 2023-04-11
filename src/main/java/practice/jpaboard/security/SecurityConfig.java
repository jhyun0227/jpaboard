package practice.jpaboard.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import practice.jpaboard.security.exception.JwtAccessDeniedHandler;
import practice.jpaboard.security.exception.JwtAuthenticationEntryPoint;
import practice.jpaboard.security.jwt.JwtAuthenticationFilter;
import practice.jpaboard.security.jwt.JwtTokenProvider;

@Configuration
@EnableWebSecurity //Security를 활성화하고, Security Filter가 Spring FilterChain에 등록
@EnableGlobalMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                //jwt 토큰 사용을 위한 설정
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) //세션을 사용하지 않겠다는 의미

                .and()
                .formLogin().disable() //form 형식의 로그인이 아닌 restful 사용
                .httpBasic().disable() //세션을 사용하지 않기 때문에 기본 httpBasic 방식은 보안성 문제로 사용하지 않는다. token을 사용한다는 의미

                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)

                //예외처리
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)

                //인가 설정
                .and()
                .authorizeRequests()
                .antMatchers("/member/login", "/member/join").permitAll()
                .antMatchers("/member/list").hasRole("ADMIN")
                .anyRequest().authenticated();

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
