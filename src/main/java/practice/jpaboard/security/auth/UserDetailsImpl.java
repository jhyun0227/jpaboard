package practice.jpaboard.security.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import practice.jpaboard.member.entity.Member;

import java.util.ArrayList;
import java.util.Collection;

@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private final Member member;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities =  new ArrayList<>();
        authorities.add(() -> String.valueOf(member.getRole()));

        return authorities;
    }

    @Override
    public String getUsername() {
        return member.getMemberLoginId();
    }

    @Override
    public String getPassword() {
        return member.getMemberPassword();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    //Auditing을 위해 추가
    public String getMemberNickname() {
        return member.getMemberNickname();
    }
}
