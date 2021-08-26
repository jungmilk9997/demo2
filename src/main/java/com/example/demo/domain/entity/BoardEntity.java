package com.example.demo.domain.entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "board")
public class BoardEntity extends TimeEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10, nullable = false)
    private String category;

    @Column(length = 10, nullable = false)
    private String writer;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private int hintCnt=0;

    @Column(nullable = false)
    private String pwd;

    @Column
    private Long fileId;

    @Builder
    public BoardEntity(Long id,  Long fileId,String category, String title, String content, String writer, int hintCnt,String pwd) {
        this.id = id;
        this.category=category;
        this.writer = writer;
        this.title = title;
        this.content = content;
        this.hintCnt=hintCnt;
        this.pwd = pwd;
        this.fileId = fileId;
    }
}