package com.worldcrossword.puzzle.service;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    private String sessionId;
    private String googldId;
    private String sessionName;
}
