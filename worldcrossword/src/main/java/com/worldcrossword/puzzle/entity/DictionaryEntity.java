package com.worldcrossword.puzzle.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "dictionary")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DictionaryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String english;

    @Column(nullable = false)
    private String part;

    @Column(nullable = false, length = 10000)
    private String mean;
}
