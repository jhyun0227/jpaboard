package practice.jpaboard.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.jpaboard.exception.member.MemberError;
import practice.jpaboard.exception.member.MemberException;
import practice.jpaboard.member.dto.MemberDto;
import practice.jpaboard.member.dto.MemberJoinDto;
import practice.jpaboard.member.dto.MemberLoginDto;
import practice.jpaboard.member.entity.Member;
import practice.jpaboard.member.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    public Long joinMember(MemberJoinDto memberJoinDto) {
        memberRepository.findByMemberLoginId(memberJoinDto.getMemberLoginId())
                        .ifPresent(member -> {
                            throw new MemberException(MemberError.DUPLICATED_MEMBER_LOGIN_ID);
                        });

        Member member = Member.builder()
                .memberLoginId(memberJoinDto.getMemberLoginId())
                .memberPassword(memberJoinDto.getMemberPassword())
                .memberNickname(memberJoinDto.getMemberNickname())
                .build();

        memberRepository.save(member);

        return member.getMemberId();
    }

    @Transactional(readOnly = true)
    public Member login(MemberLoginDto memberLoginDto) {
        Member findMember = memberRepository.findByMemberLoginId(memberLoginDto.getMemberLoginId())
                .orElseThrow(() -> {
                    throw new MemberException(MemberError.NOT_EXIST_MEMBER);
                });

        if (!findMember.getMemberPassword().equals(memberLoginDto.getMemberPassword())) {
            throw new MemberException(MemberError.INVALID_PASSWORD);
        }

        return findMember;
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
