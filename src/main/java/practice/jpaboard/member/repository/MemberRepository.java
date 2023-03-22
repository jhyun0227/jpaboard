package practice.jpaboard.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import practice.jpaboard.member.entity.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    public Optional<Member> findByMemberLoginId(String memberLoginId);

}
