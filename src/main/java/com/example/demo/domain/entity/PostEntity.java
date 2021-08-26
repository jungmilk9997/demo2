package com.example.demo.domain.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name="post")
public class PostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(name="name")
    private String name;

    @Column(name="content")
    private String content;

    @Column(nullable = false)
    private String pwd;

    @ManyToOne(targetEntity = BoardEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "boardEntity")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private BoardEntity boardEntity;

    @Column(nullable = true)
    private Long postNum;


    @Builder
    public PostEntity(Long Id, String name, String content, BoardEntity boardEntity,String pwd,Long postNum){
        this.Id=Id;
        this.name=name;
        this.content=content;
        this.pwd=pwd;
        this.boardEntity=boardEntity;
        this.postNum=postNum;
    }


}
