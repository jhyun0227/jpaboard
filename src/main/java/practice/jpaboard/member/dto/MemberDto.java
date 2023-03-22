package practice.jpaboard.member.dto;

import lombok.Data;
import practice.jpaboard.member.entity.Member;

@Data
public class MemberDto {

    private String memberLoginId;
    private String memberNickname;

    public MemberDto(String memberLoginId, String memberNickname) {
        this.memberLoginId = memberLoginId;
        this.memberNickname = memberNickname;
    }
}
