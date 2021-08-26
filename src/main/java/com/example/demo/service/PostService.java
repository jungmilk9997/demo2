package com.example.demo.service;

import com.example.demo.domain.entity.BoardEntity;
import com.example.demo.domain.entity.PostEntity;
import com.example.demo.domain.respository.BoardPostRepository;
import com.example.demo.dto.BoardDto;
import com.example.demo.dto.PostDto;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import javax.transaction.Transactional;
import java.util.*;

import static java.lang.Long.valueOf;

@AllArgsConstructor
@Service
public class PostService {

    private BoardPostRepository boardPostRepository;
    private static final int BLOCK_PAGE_NUM_COUNT = 5;
    private static final int PAGE_POST_COUNT = 3;


    public Page<PostEntity> getPostList(Pageable pageable,Long no){
        return boardPostRepository.findByBoardEntity_Id(pageable, no);
    }


    public Integer[] getPageList(Long no, Integer curPageNum) {
        Integer[] pageList = new Integer[BLOCK_PAGE_NUM_COUNT];

        Double postsTotalCount = Double.valueOf(this.getPostCount(no));


        Integer totalLastPageNum = (int)(Math.ceil((postsTotalCount/PAGE_POST_COUNT)));
        System.out.println(totalLastPageNum);

        Integer[] page = new Integer[totalLastPageNum];

        Integer blockLastPageNum = (totalLastPageNum > curPageNum + BLOCK_PAGE_NUM_COUNT)
                ? curPageNum + BLOCK_PAGE_NUM_COUNT
                : totalLastPageNum;

        curPageNum = (curPageNum <= 3) ? 1 : curPageNum - 2;


        for (int val = curPageNum, idx = 0; val <= blockLastPageNum; val++, idx++) {
            page[idx] = idx;
        }

        return page;
    }

    public Long getPostCount(Long no) {
        return boardPostRepository.countByBoardEntity_Id(no);
    }


    //게시글에 맞는 댓글 리스트 불러옴
   /*@Transactional
    public List<PostDto> getComment(Long no) {


        //List<PostEntity> postEntities = boardPostRepository.findAll();
        List<PostEntity> postEntities = boardPostRepository.findByBoard(no);
        //List<PostEntity> postEntities=boardPostRepository.findByName("일일");
        List<PostDto> postList = new ArrayList<>();

        for ( PostEntity postEntity : postEntities) {
            PostDto postDTO = PostDto.builder()
                    .num(postEntity.getNum())
                    .name(postEntity.getName())
                    .content(postEntity.getContent())
                    .boardEntity(postEntity.getBoardEntity())
                    .pwd(postEntity.getPwd())
                    .build();

            postList.add(postDTO);
        }

        return postList;

    }*/

    @Transactional
    public Long saveComment(PostDto postDto){
        if(postDto.getPostNum()==null) {

            Long i=boardPostRepository.autoIncrement();
            if(i==null){
                long plus=1;
                postDto.setPostNum(plus);
            }else {
            System.out.println("받아온값"+i);
            int x=valueOf(i).intValue();
            System.out.println(x);
            int result=x+1;
            System.out.println(result);
            Long y = new Long(result);
            postDto.setPostNum(y);}

        }

        return boardPostRepository.save(postDto.toPostEntity()).getId();
    }


    public void deleteComment(Long no) {
        boardPostRepository.deleteById(no);
    }

    //댓글 각각을 불러옴
    public PostDto getPostCommentLast(Long no) {
        Optional<PostEntity> postEntityWrapper = boardPostRepository.findById(no);
        PostEntity postEntity = postEntityWrapper.get();

        PostDto postDTO = PostDto.builder()
                .Id(postEntity.getId())
                .name(postEntity.getName())
                .content(postEntity.getContent())
                .boardEntity(postEntity.getBoardEntity())
                .pwd(postEntity.getPwd())
                .postNum(postEntity.getPostNum())
                .build();

        boardPostRepository.save(postDTO.toPostEntity()).getId();

        return postDTO;
    }


    public Map<String, String> validateHandling(Errors errors) {
        Map<String, String> validatorResult = new HashMap<>();

        for (FieldError error : errors.getFieldErrors()) {
            String validKeyName = String.format("valid_%s", error.getField());
            validatorResult.put(validKeyName, error.getDefaultMessage());
        }

        return validatorResult;
    }


}