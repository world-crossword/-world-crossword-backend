package com.worldcrossword.member.service;

import com.worldcrossword.member.entity.Member;
import com.worldcrossword.member.entity.Role;
import com.worldcrossword.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public void checkMember(String googleId) {
        if(!memberRepository.existsByGoogleId(googleId)) {
            memberRepository.save(Member.builder()
                    .googleId(googleId)
                    .role(Role.ROLE_USER)
                    .build());
        }
    }
}
