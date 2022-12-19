package com.worldcrossword.puzzle.entity;

import com.worldcrossword.member.entity.Member;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name="puzzle")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PuzzleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String word;

    @Column
    private Long rowpoint;

    @Column
    private Long colpoint;

    @Column
    private Long endpoint;

    @Column
    private String direction;

    @Column
    private Long completion;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column
    private String sessionName;

    @ManyToOne(fetch = FetchType.LAZY)
    private DictionaryEntity dictionary;

    public void successPuzzle(Member member) {
        this.completion = 1L;
        this.member = member;
    }
}
