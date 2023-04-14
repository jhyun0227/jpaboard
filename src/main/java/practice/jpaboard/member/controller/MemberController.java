package practice.jpaboard.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import practice.jpaboard.member.dto.MemberDto;
import practice.jpaboard.member.dto.MemberJoinDto;
import practice.jpaboard.member.dto.MemberLoginDto;
import practice.jpaboard.member.service.MemberLoginService;
import practice.jpaboard.member.service.MemberService;
import practice.jpaboard.security.jwt.TokenDto;
import practice.jpaboard.util.dto.ResponseDto;
import practice.jpaboard.util.dto.StatusCode;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final MemberLoginService memberLoginService;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenValidityInSeconds;

    @PostMapping("/join")
    public ResponseEntity<?> join(@Validated @RequestBody MemberJoinDto memberJoinDto) {
        Long memberId = memberService.joinMember(memberJoinDto);

        HashMap<String, Long> data = new HashMap<>();
        data.put("memberId", memberId);

        ResponseDto<?> result
                = ResponseDto.successDto(StatusCode.SUCCESS, data, "정상적으로 회원가입 되었습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/list")
    public ResponseEntity<?> memberList() {
        List<MemberDto> memberList = memberService.memberList();

        ResponseDto<?> result
                = ResponseDto.successDto(StatusCode.SUCCESS, memberList, "정상 조회 되었습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }





    //============= 로그인 관련 컨트롤러 =============//
    @PostMapping("/login")
    public ResponseEntity<?> login(@Validated @RequestBody MemberLoginDto memberLoginDto) {
        TokenDto tokenDto = memberLoginService.login(memberLoginDto);

        //RefreshToken 쿠키 저장
        ResponseCookie httpCookie = ResponseCookie.from("Refresh-Token", tokenDto.getRefreshToken())
                .maxAge(refreshTokenValidityInSeconds)
                .httpOnly(true)
                .secure(true)
                .build();

        ResponseDto<?> result
                = ResponseDto.successDto(StatusCode.SUCCESS, tokenDto, "정상적으로 로그인 되었습니다.");

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenDto.getAccessToken())
                .header(HttpHeaders.SET_COOKIE, httpCookie.toString())
                .body(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String accessToken,
                                    @CookieValue(name = "Refresh-Token") String refreshToken) {
        memberLoginService.logout(accessToken, refreshToken);

        ResponseDto<?> result =
                ResponseDto.successDto(StatusCode.SUCCESS, null, "정상적으로 로그아웃 되었습니다.");

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    /**
     * 토큰을 검증하는 메서드
     */
//    @GetMapping("/validate")
//    public ResponseEntity<?> validateAccessToken(@RequestHeader("Authorization") String accessToken) {
//        if (!memberLoginService.accessTokenValidate(accessToken)) {
//            //토큰 유효가 만료되기 전
//            ResponseDto<?> result
//                    = ResponseDto.successDto(StatusCode.SUCCESS, null, "유효한 토큰입니다.");
//            return ResponseEntity.status(HttpStatus.OK).body(result);
//        } else {
//            //토큰의 기한이 만료되었거나, 다른 예외가 발생할 경우 재발급이 필요하다.
//            ResponseDto<?> result
//                    = ResponseDto.successDto(StatusCode.FAIL, null, "토큰 만료, redirection = " + "/reissue");
//
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//    }

    /**
     * 토큰을 재발급 하는 메서드
     * 1. Access Token이 만료되고 Refresh Token은 만료가 안된 경우 -> Access Token만 재발급
     * 2. 둘다 만료가 된 경우 -> 로그인 유도 -> data에 null값을 보내서 재로그인 유
     */
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(@RequestHeader("Authorization") String accessToken,
                                     @CookieValue(name = "Refresh-Token") String refreshToken) {

        String updatedAccessToken
                = memberLoginService.reissueToken(accessToken, refreshToken);

        //재발급에 실패하면 전부 /login 유도
        if (StringUtils.hasText(updatedAccessToken)) {
            ResponseDto<?> failResult =
                    ResponseDto.failDto(StatusCode.FAIL, null, "로그인이 만료되었습니다. 다시 로그인해주세요. Redirection URL = " + "/login");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(failResult);
        }

        //정상적으로 재발급
        ResponseDto<?> successResult =
                ResponseDto.successDto(StatusCode.SUCCESS, new TokenDto(accessToken, null), "Access Token을 재발급하였습니다.");

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + updatedAccessToken)
                .body(successResult);
    }
}
