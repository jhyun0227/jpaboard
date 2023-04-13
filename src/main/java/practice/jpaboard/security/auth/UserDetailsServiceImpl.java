package practice.jpaboard.security.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import practice.jpaboard.exception.member.MemberError;
import practice.jpaboard.exception.member.MemberException;
import practice.jpaboard.member.entity.Member;
import practice.jpaboard.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String memberLoginId) throws UsernameNotFoundException {
        Member member = memberRepository.findByMemberLoginId(memberLoginId)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다. 아이디를 확인해주세요."));

        if (member != null) {
            UserDetailsImpl userDetails = new UserDetailsImpl(member);
            return userDetails;
        }

        return null;
    }
}
