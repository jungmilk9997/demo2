package com.example.demo.dto;
import com.example.demo.domain.entity.BoardEntity;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Getter
@Setter
// 자동으로 접근자, 설정자 생성해줌
@ToString //ToString 메소드 자동생성
@NoArgsConstructor //파라미터가 없는 기본생성자 생성해줌
public class BoardDto {
    private Long id;

    @NotBlank(message = "작성자 이름은 필수 입력 값입니다.")
    private String writer;

    @NotBlank(message = "카테고리값은 필수 입력 값입니다.")
    private String category;

    @NotBlank(message = "글 제목은 필수 입력 값입니다.")
    private String title;

    @NotBlank(message = "글 내용은 필수 입력 값입니다.")
    private String content;

    @NotBlank(message = "글 비밀번호는 필수 입력 값입니다.")
    private String pwd;

    private int hintCnt;
    private int hint;
    private Long fileId;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    public BoardEntity toEntity(){
        BoardEntity board = BoardEntity.builder()
                .id(id)
                .category(category)
                .writer(writer)
                .title(title)
                .content(content)
                .hintCnt(hintCnt)
                .fileId(fileId)
                .pwd(pwd)
                .build();
        return board;
    }


    @Builder //
    public BoardDto(Long id, String category,String title, String content, String writer, LocalDateTime createdDate, LocalDateTime modifiedDate, int hintCnt, Long fileId, String pwd) {
        this.id = id;
        this.category= category;
        this.writer = writer;
        this.title = title;
        this.content = content;
        this.hintCnt= hintCnt;
        this.fileId = fileId;
        this.pwd= pwd;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
    }
}
