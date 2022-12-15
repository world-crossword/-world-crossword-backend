package com.worldcrossword.google.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

    private String iss;
    private String azp;
    private String aud;
    private String sub;
    private String email;
    private Boolean email_verified;
    private String at_hash;
    private Long iat;
    private Long exp;
    private String alg;
    private String kid;
    private String typ;
}
