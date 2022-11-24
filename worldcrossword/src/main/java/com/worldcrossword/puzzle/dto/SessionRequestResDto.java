package com.worldcrossword.puzzle.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SessionRequestResDto {
    private Boolean stat;
    private String sessionName;
    private String message;
    private String puzzle;
}
