package com.worldcrossword.member.repository;

import com.worldcrossword.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

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
}
