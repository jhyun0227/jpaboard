package practice.jpaboard.exception.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberException extends RuntimeException {
    private MemberError memberError;
}
