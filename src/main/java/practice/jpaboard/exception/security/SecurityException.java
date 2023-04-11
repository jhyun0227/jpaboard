package practice.jpaboard.exception.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SecurityException extends RuntimeException {
    private SecurityError SecurityError;
}
