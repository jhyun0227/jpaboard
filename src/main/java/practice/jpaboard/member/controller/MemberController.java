package practice.jpaboard.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
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
    public ResponseDto<Map<String, Long>> join(@Validated @RequestBody MemberJoinDto memberJoinDto) {
        Long memberId = memberService.joinMember(memberJoinDto);

        HashMap<String, Long> data = new HashMap<>();
        data.put("memberId", memberId);

        return new ResponseDto<>(StatusCode.SUCCESS, data, "정상적으로 회원가입 되었습니다.", null);
    }

    @GetMapping("/login")
    public ResponseDto<TokenDto> login(@Validated @RequestBody MemberLoginDto memberLoginDto, HttpServletResponse httpServletResponse) {
        TokenDto token = memberLoginService.login(memberLoginDto);

        //AccessToken 헤더 저장
        httpServletResponse.addHeader("Authorization", "Bearer " + token.getAccessToken());

        return new ResponseDto<>(StatusCode.SUCCESS, token, "정상적으로 로그인 되었습니다.", null);
    }


    @GetMapping("/list")
    public ResponseDto<List<MemberDto>> memberList() {
        List<MemberDto> memberList = memberService.memberList();

        return new ResponseDto<>(StatusCode.SUCCESS, memberList, "정상 조회", null);
    }

}
