package com.worldcrossword.google.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleToken {

    private String access_token;
    private Long expires_in;
    private String refresh_token;
    private String scope;
    private String token_type;
    private String id_token;
}
