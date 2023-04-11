package practice.jpaboard.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import practice.jpaboard.member.dto.MemberDto;
import practice.jpaboard.member.dto.MemberJoinDto;
import practice.jpaboard.member.dto.MemberLoginDto;
import practice.jpaboard.member.service.MemberLoginService;
import practice.jpaboard.member.service.MemberService;
import practice.jpaboard.security.auth.UserDetailsImpl;
import practice.jpaboard.security.jwt.TokenDto;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final MemberLoginService memberLoginService;

    @PostMapping("/join")
    public Long join(@Validated @RequestBody MemberJoinDto memberJoinDto) {
        return memberService.joinMember(memberJoinDto);
    }

    @GetMapping("/login")
    public TokenDto login(@Validated @RequestBody MemberLoginDto memberLoginDto) {
        return memberLoginService.login(memberLoginDto);
    }


    @GetMapping("/list")
    public List<MemberDto> memberList(Authentication authentication, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {

//        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) authentication.getPrincipal();

        return memberService.memberList();
    }

}
