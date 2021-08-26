package com.example.demo.dto;

import com.example.demo.domain.entity.BoardEntity;
import com.example.demo.domain.entity.PostEntity;
import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class PostDto {
    private Long Id;

    @NotBlank(message = "작성자 이름은 필수 입력 값입니다.")
    private String name;

    @NotBlank(message = "댓글 내용은 필수 입력 값입니다.")
    private String content;

    @NotBlank(message = "댓글 비밀번호는 필수 입력 값입니다.")
    private String pwd;

    private BoardEntity boardEntity;
    private Long postNum;

    public PostEntity toPostEntity(){
        PostEntity postEntity = PostEntity.builder()
                .Id(Id)
                .name(name)
                .content(content)
                .boardEntity(boardEntity)
                .pwd(pwd)
                .postNum(postNum)
                .build();
        return postEntity;
    }

    @Builder
    public PostDto(Long Id, String name, String content, BoardEntity boardEntity,String pwd,Long postNum){
        this.Id=Id;
        this.name=name;
        this.content=content;
        this.pwd=pwd;
        this.boardEntity=boardEntity;
        this.postNum=postNum;
    }
}

