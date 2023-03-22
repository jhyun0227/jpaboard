package practice.jpaboard.util;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import practice.jpaboard.board.entity.Board;
import practice.jpaboard.board.repository.BoardRepository;
import practice.jpaboard.member.entity.Member;
import practice.jpaboard.member.repository.MemberRepository;

import javax.annotation.PostConstruct;

@RestController
@RequiredArgsConstructor
public class InitController {

    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;

    @PostConstruct
    public void initMemberData() {
        for (int i = 1; i <= 10; i++) {
            Member member = Member.builder()
                    .memberLoginId("test" + i)
                    .memberPassword("test" + i)
                    .memberNickname("test" + i)
                    .build();

            memberRepository.save(member);

            Board board = Board.builder()
                    .boardTitle("test board" + i)
                    .boardContent("test board" + i)
                    .member(member)
                    .build();

            boardRepository.save(board);
        }
    }
}
