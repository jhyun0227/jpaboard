package practice.jpaboard.util;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import practice.jpaboard.board.entity.Board;
import practice.jpaboard.board.repository.BoardRepository;
import practice.jpaboard.member.dto.MemberJoinDto;
import practice.jpaboard.member.entity.Member;
import practice.jpaboard.member.repository.MemberRepository;
import practice.jpaboard.member.service.MemberService;

import javax.annotation.PostConstruct;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class InitController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;

    @PostConstruct
    public void initMemberData() {
        for (int i = 1; i <= 10; i++) {
            MemberJoinDto memberJoinDto = new MemberJoinDto();
            memberJoinDto.setMemberLoginId("test" + i);
            memberJoinDto.setMemberPassword("test" + i);
            memberJoinDto.setMemberNickname("test" + i);

            Long memberId = memberService.joinMember(memberJoinDto);
        }
    }
}
