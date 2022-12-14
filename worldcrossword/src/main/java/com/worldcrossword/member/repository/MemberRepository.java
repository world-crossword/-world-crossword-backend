package com.worldcrossword.member.repository;

import com.worldcrossword.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRepository {
    private final EntityManager em;

    public void save(Member member) {
        em.persist(member);
    }

    public Boolean existsByGoogleId(String googleId) {
        return em.createQuery("select count(m) > 0 from Member m where m.googleId = :googleId", Boolean.class)
                .setParameter("googleId", googleId)
                .getSingleResult();
    }

    public Optional<Member> findByGoogleId(String googleId) {
        List<Member> memberList = em.createQuery("select m from Member m where m.googleId = :googleId", Member.class)
                .setParameter("googleId", googleId)
                .getResultList();
        if(memberList.isEmpty()) return Optional.empty();
        return Optional.of(memberList.get(0));
    }

    public Optional<Member> findById(Long memberId) {
        return Optional.ofNullable(em.find(Member.class, memberId));
    }
}
