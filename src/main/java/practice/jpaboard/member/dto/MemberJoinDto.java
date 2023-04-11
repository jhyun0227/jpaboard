package practice.jpaboard.member.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class MemberJoinDto {

    @NotBlank
    private String memberLoginId;
    @NotBlank
    private String memberPassword;
    @NotBlank
    private String memberNickname;

}
