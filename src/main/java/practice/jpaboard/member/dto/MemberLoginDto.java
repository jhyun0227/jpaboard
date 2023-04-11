package practice.jpaboard.member.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class MemberLoginDto {

    @NotBlank(message = "ID를 입력해주세요.")
    private String memberLoginId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String memberPassword;

    public MemberLoginDto(String memberLoginId, String memberPassword) {
        this.memberLoginId = memberLoginId;
        this.memberPassword = memberPassword;
    }
}
