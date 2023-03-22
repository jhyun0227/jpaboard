package practice.jpaboard.member.dto;

import lombok.Data;

@Data
public class MemberLoginDto {

    private String memberLoginId;
    private String memberPassword;

    public MemberLoginDto(String memberLoginId, String memberPassword) {
        this.memberLoginId = memberLoginId;
        this.memberPassword = memberPassword;
    }
}
