package com.worldcrossword.puzzle.dto;

import com.worldcrossword.puzzle.entity.DictionaryEntity;
import com.worldcrossword.puzzle.entity.PuzzleEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictionaryDTO {

    private String word;
    private String mean;
    private String part;
    private Long completion;
    private Long solver_id;

    public DictionaryDTO(PuzzleEntity puzzle) {
        DictionaryEntity dictionary = puzzle.getDictionary();
        this.word = dictionary.getEnglish();
        this.mean = dictionary.getMean();
        this.part = dictionary.getPart();
        this.completion = puzzle.getCompletion();
        if(completion == 1) this.solver_id = puzzle.getMember().getId();
    }
}
