package practice.jpaboard.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
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
    @GetMapping("/login")
    public ResponseEntity<?> login(@Validated @RequestBody MemberLoginDto memberLoginDto, HttpServletResponse httpServletResponse) {
        TokenDto tokenDto = memberLoginService.login(memberLoginDto);

        //RefreshToken 쿠키 저장
        ResponseCookie httpCookie = ResponseCookie.from("refresh-Token", tokenDto.getRefreshToken())
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

    /**
     * 토큰을 검증
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateAccessToken(@RequestHeader("Authorization") String accessToken) {
        if (memberLoginService.accessTokenValidate(accessToken)) {
            //토큰 유효가 만료되기 전이거나, 단순히 만료만 되었다면 재발급 필요 없음
            return ResponseEntity.status(HttpStatus.OK).build();
        } else {
            //토큰의 기한이 만료되었거나, 다른 예외가 발생할 경우 재발급이 필요하다.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }




}
