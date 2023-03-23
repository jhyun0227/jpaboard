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
import practice.jpaboard.member.service.MemberService;

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

    private final MemberRepository memberRepository;

/*
    @GetMapping("/join")
    public String joinform(@ModelAttribute MemberJoinDto memberJoinDto, Model model) {
        model.addAttribute("memberJoinDto", memberJoinDto);
        return "member/joinForm";
    }
*/

    @PostMapping("/join")
    public Long join(@Validated @RequestBody MemberJoinDto memberJoinDto) {
        Long memberId = memberService.joinMember(memberJoinDto);
        return memberId;
    }

    @GetMapping("/login")
    public Long login(@Validated @RequestBody MemberLoginDto memberLoginDto, HttpServletRequest httpServletRequest) {
        Member member = memberService.login(memberLoginDto);

//        세션이 있으면 세션 반환, 없으면 신규 세션 생성
//        HttpSession session = httpServletRequest.getSession();
//        session.setAttribute("loginMember", member);

        return member.getMemberId();
    }


    @GetMapping("/list")
    public List<MemberDto> memberList(Model model) {
        return memberService.memberList();
    }

}
