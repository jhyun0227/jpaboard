package practice.jpaboard.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
    private final CorsConfig corsConfig;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                //jwt 토큰 사용을 위한 설정
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) //세션을 사용하지 않겠다는 의미

                .and()
                .formLogin().disable() //form 형식의 로그인이 아닌 restful 사용
                .httpBasic().disable() //세션을 사용하지 않기 때문에 기본 httpBasic 방식은 보안성 문제로 사용하지 않는다. token을 사용한다는 의미

                //formLogin.disable()을 하면 UsernmaePasswordAuthenticationFilter가 동작을 하지 않는다.
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
                .apply(new MyCustomDsl())

                //예외처리
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint) //인증 에러에 대한 처리
                .accessDeniedHandler(jwtAccessDeniedHandler) //인가 예외에 대한 처리

                //인가 설정
                .and()
                .authorizeRequests()
                .antMatchers("/member/login", "/member/join", "/member/reissue").permitAll()
                .antMatchers("/member/list").hasRole("ADMIN")
                .anyRequest().authenticated();

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    public class MyCustomDsl extends AbstractHttpConfigurer<MyCustomDsl, HttpSecurity> {
        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                    //SPA와 사용시 포트가 다르기때문에 이 설정을 통해 CORS 허용 로직을 작성해야한다.
                    .addFilter(corsConfig.corsFilter()); //@CrossOrigin(인증이 없을떄), Security Filter에 등록(인증이 있을떄)

        }
    }
}
