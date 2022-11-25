package com.worldcrossword.puzzle.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SessionRequestResDto {
    // true, false, solving, not_solving
    private String stat;
    private String sessionName;
    private String message;

    private String word;
}
