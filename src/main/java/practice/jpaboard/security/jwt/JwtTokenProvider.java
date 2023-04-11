package practice.jpaboard.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import practice.jpaboard.security.SecurityProperties;
import practice.jpaboard.security.auth.UserDetailsImpl;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
@Transactional(readOnly = true)
public class JwtTokenProvider implements InitializingBean {
    private final UserDetailsService userDetailsService;
    private final String secretKey;
    private static Key signingKey;
    private final Long accessTokenValidityInMilliseconds;
    private final Long refreshTokenValidityInMilliseconds;

    public JwtTokenProvider(UserDetailsService userDetailsService,
                            @Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.access-token-validity-in-seconds}") Long accessTokenValidityInSeconds,
                            @Value("${jwt.refresh-token-validity-in-seconds}") Long refreshTokenValidityInSeconds) {
        this.userDetailsService = userDetailsService;
        this.secretKey = secretKey;
        this.accessTokenValidityInMilliseconds = accessTokenValidityInSeconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInSeconds * 1000;
    }

    //시크릿 키 설정
    @Override
    public void afterPropertiesSet() throws Exception {
        byte[] secretKeyBytes = Decoders.BASE64.decode(secretKey);
        signingKey = Keys.hmacShaKeyFor(secretKeyBytes);
    }

    /**
     * 로그인 인증이 끝난 후 반환할 Token Dto
     */
    public TokenDto createToken(String memberLoginId, String authority) {
        Long now = System.currentTimeMillis();

        String accessToken = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS512")
                .setExpiration(new Date(now + accessTokenValidityInMilliseconds))
                .setSubject("accessToken")
                .claim(SecurityProperties.URL, true)
                .claim(SecurityProperties.MEMBER_LOGIN_ID, memberLoginId)
                .claim(SecurityProperties.AUTHORITY_KEY, authority)
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();

        String refreshToken = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS512")
                .setExpiration(new Date(now + refreshTokenValidityInMilliseconds))
                .setSubject("refreshToken")
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();

        return new TokenDto("Bearer " + accessToken, refreshToken);
    }

    /**
     * AuthenticationFilter에서 토큰을 검증하기 위한 메서드
     */
    public boolean validateAccessToken(String accessToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(accessToken);

            return true;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String refreshToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(refreshToken);

            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature.");
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token.");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token.");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token.");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty.");
        } catch (NullPointerException e){
            log.error("JWT Token is empty.");
        }
        return false;
    }

    /**
     * Filter에서 토큰이 유효한지 검증 후, 유효할 경우 요청마다 전달된 토큰에서 사용자의 정보를 얻어
     * SecurityContext에 저장할 Authentication 객체를 반환하는 메서드
     */
    public Authentication getAuthentication(String accessToken) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(accessToken)
                .getBody();

        String memberLoginId = claims.get(SecurityProperties.MEMBER_LOGIN_ID).toString();
        UserDetails userDetails = userDetailsService.loadUserByUsername(memberLoginId);

        //비밀번호는 Authentication 객체에 넣을 필요 없다.
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    /**
     * 토큰으로부터 유효기간을 조회
     */
    public long getTokenExpirationTime(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration().getTime();
    }

    // 재발급 검증 API에서 사용
    public boolean validateAccessTokenOnlyExpired(String accessToken) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();

            Date expirationDate = claims.getExpiration();
            return expirationDate.before(new Date());
        } catch(ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
