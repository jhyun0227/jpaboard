package practice.jpaboard.member.entity;

import lombok.*;
import practice.jpaboard.board.entity.Board;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;
    private String memberLoginId;
    private String memberPassword;
    private String memberNickname;

    @OneToMany(mappedBy = "member")
    private List<Board> boards = new ArrayList<>();
}
