package com.worldcrossword.google.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

    private String id;
    private String email;
    private Boolean verified_email;
    private String name;
    private String given_name;
    private String picture;
    private String locale;
}
