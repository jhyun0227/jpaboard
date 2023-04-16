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
import practice.jpaboard.member.service.RedisService;
import practice.jpaboard.security.SecurityProperties;

import java.security.Key;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

@Slf4j
@Component
@Transactional(readOnly = true)
public class JwtTokenProvider implements InitializingBean {
    private final UserDetailsService userDetailsService;
    private final RedisService redisService;
    private final String secretKey;
    private static Key signingKey;
    private final Long accessTokenValidityInMilliseconds;
    private final Long refreshTokenValidityInMilliseconds;

    public JwtTokenProvider(UserDetailsService userDetailsService,
                            RedisService redisService,
                            @Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.access-token-validity-in-seconds}") Long accessTokenValidityInSeconds,
                            @Value("${jwt.refresh-token-validity-in-seconds}") Long refreshTokenValidityInSeconds) {
        this.userDetailsService = userDetailsService;
        this.redisService = redisService;
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
    public String createAccessToken(String memberLoginId, String authority) {
        Long now = System.currentTimeMillis();

        Date nowDate = new Date(now);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(nowDate);

        log.info("now = {}", now);
        log.info("dateString = {}", dateString);

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS512")
                .setExpiration(new Date(now + accessTokenValidityInMilliseconds))
                .setSubject("accessToken")
                .claim(SecurityProperties.URL, true)
                .claim(SecurityProperties.MEMBER_LOGIN_ID, memberLoginId)
                .claim(SecurityProperties.AUTHORITY_KEY, authority)
                .claim("issueDate", dateString)
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String createRefreshToken() {
        Long now = System.currentTimeMillis();

        Date nowDate = new Date(now);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(nowDate);

        log.info("now = {}", now);
        log.info("dateString = {}", dateString);

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS512")
                .setExpiration(new Date(now + refreshTokenValidityInMilliseconds))
                .setSubject("refreshToken")
                .claim("issueDate", dateString)
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * AuthenticationFilter에서 토큰이 유효한지 확인하기 위한 메서드
     * Silent Refresh를 위해서 만료된 토큰도 일단 반환한다.
     */
    public boolean validateAccessToken(String accessToken) {
        if (redisService.getValues(accessToken) != null //NPE방지
                && redisService.getValues(accessToken).equals("logout")) {
            return false;
        }

        Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(accessToken);

        return true;
    }

    /**
     * RefreshToken이 유효한지 확인하는 메서드
     */
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
        //토큰에서 claim을 추출
        Claims claims = getClaims(accessToken);
        String memberLoginId = claims.get(SecurityProperties.MEMBER_LOGIN_ID).toString();

        //토큰에 보관되어있던 memberLoginId를 이용해 UserDetails를 얻는다.
        UserDetails userDetails = userDetailsService.loadUserByUsername(memberLoginId);

        log.info("memberLoginId = {}", memberLoginId);
        log.info("userDetails = {}", userDetails.toString());

        //비밀번호는 Authentication 객체에 넣을 필요 없다.
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    /**
     * 토큰의 유효기간을 확인하는 메서드
     * 유효기간만 만료된 유효한 토큰일 경우 true 반환
     */
    public boolean validateAccessTokenOnlyExpired(String accessToken) {
        try {
            Claims claims = getClaims(accessToken);
            Date expirationDate = claims.getExpiration();

            //만료시간이 현재시간보다 이전인지 확인하는 로직
            //만료시간이 현재시간보다 과거이면 true
            //만료시간이 현재시간보다 미래이면 false
            return expirationDate.before(new Date());
        } catch(ExpiredJwtException e) {
            return true;
        }

        /*
        catch (Exception e) {
            return false;
        }
        */
    }

    /**
     * 토큰으로부터 유효기간을 조회
     */
    public long getTokenExpirationTime(String token) {
        Claims claims = getClaims(token);

        return claims.getExpiration().getTime();
    }

    /**
     * 토큰으로 부터 Claim을 추출하는 메서드
     */
    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
