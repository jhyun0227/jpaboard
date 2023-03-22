package practice.jpaboard.member.dto;

import lombok.Data;

@Data
public class MemberJoinDto {

    private String memberLoginId;
    private String memberPassword;
    private String memberNickname;

}
