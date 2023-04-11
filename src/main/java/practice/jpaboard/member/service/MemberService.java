package practice.jpaboard.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.jpaboard.exception.member.MemberError;
import practice.jpaboard.exception.member.MemberException;
import practice.jpaboard.member.dto.MemberDto;
import practice.jpaboard.member.dto.MemberJoinDto;
import practice.jpaboard.member.entity.Member;
import practice.jpaboard.member.entity.Role;
import practice.jpaboard.member.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public Long joinMember(MemberJoinDto memberJoinDto) {
        memberRepository.findByMemberLoginId(memberJoinDto.getMemberLoginId())
                        .ifPresent(member -> {
                            throw new MemberException(MemberError.DUPLICATED_MEMBER_LOGIN_ID);
                        });

        //비밀번호 암호화
        String encodedPassword = bCryptPasswordEncoder.encode(memberJoinDto.getMemberPassword());

        Member member = Member.builder()
                .memberLoginId(memberJoinDto.getMemberLoginId())
                .memberPassword(encodedPassword)
                .memberNickname(memberJoinDto.getMemberNickname())
                .role(Role.ROLE_MEMBER)
                .build();

        memberRepository.save(member);

        return member.getMemberId();
    }

    @Transactional(readOnly = true)
    public List<MemberDto> memberList() {
        List<Member> members = memberRepository.findAll();

        List<MemberDto> result = new ArrayList<>();

        for (Member member : members) {
            MemberDto memberDto = new MemberDto(member.getMemberLoginId(), member.getMemberNickname());

            result.add(memberDto);
        }

        return result;
    }

}
