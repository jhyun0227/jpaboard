package practice.jpaboard.member.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import practice.jpaboard.member.dto.MemberDto;
import practice.jpaboard.member.dto.MemberJoinDto;
import practice.jpaboard.member.dto.MemberLoginDto;
import practice.jpaboard.member.entity.Member;
import practice.jpaboard.member.repository.MemberRepository;
import practice.jpaboard.member.service.MemberLoginService;
import practice.jpaboard.member.service.MemberService;
import practice.jpaboard.security.jwt.TokenDto;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

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
    public List<MemberDto> memberList() {
        return memberService.memberList();
    }

}
