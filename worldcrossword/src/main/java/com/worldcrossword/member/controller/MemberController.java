package com.worldcrossword.member.controller;

import com.worldcrossword.member.dto.MeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class MemberController {

    @GetMapping("/member/me")
    public ResponseEntity<MeDTO> whoAmI(HttpServletRequest req) {
        Long memberId = (Long) req.getAttribute("memberId");
        return new ResponseEntity<>(new MeDTO(memberId), HttpStatus.OK);
    }
}
