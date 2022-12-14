package com.worldcrossword.member.service;

import com.worldcrossword.member.entity.Member;
import com.worldcrossword.member.entity.Role;
import com.worldcrossword.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public void checkMember(String googleId) {
        if(!memberRepository.existsByGoogleId(googleId)) {
            memberRepository.save(Member.builder()
                    .score(0L)
                    .username("guest")
                    .googleId(googleId)
                    .role(Role.ROLE_USER)
                    .build());
        }
    }
}
