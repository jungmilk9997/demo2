package com.example.demo.service;

import com.example.demo.domain.entity.BoardEntity;
import com.example.demo.domain.respository.BoardRepository;
import com.example.demo.dto.BoardDto;
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

@AllArgsConstructor //모든 변수를 사용하는 생성자를 자동완성 시켜주는 어노테이션
@Service
public class BoardService {
    private BoardRepository boardRepository;
    private static final int BLOCK_PAGE_NUM_COUNT = 5;
    private static final int PAGE_POST_COUNT = 10;


    //게시글 작성한 것 저장해줌
    //pagenum 버전
    @Transactional //자동 롤백을 해주기 위해
    public Long savePost(BoardDto boardDto) {
        return boardRepository.save(boardDto.toEntity()).getId();
    }

    //게시글 목록을 불러옴
    @Transactional
    public List<BoardDto> getBoardlist(Integer pageNum) {
        Page<BoardEntity> page = boardRepository.findAll(PageRequest.of(pageNum - 1, PAGE_POST_COUNT, Sort.by(Sort.Direction.ASC, "createdDate")));

        List<BoardEntity> boardEntities = page.getContent();
        List<BoardDto> boardDtoList = new ArrayList<>();

        for (BoardEntity boardEntity : boardEntities) {
            boardDtoList.add(this.convertEntityToDto(boardEntity));
        }

        return boardDtoList;
    }

    //pageable 버전
    //전체 다 불러오는것
    @Transactional
    public Page<BoardEntity> getBoardList(Pageable pageable) {

        return boardRepository.findAll(pageable);

    }

    public Integer[] getBoardPageList(Integer curPageNum) {
        Integer[] pageList = new Integer[BLOCK_PAGE_NUM_COUNT];

        Double postsTotalCount = Double.valueOf(this.getBoardCount());


        Integer totalLastPageNum = (int)(Math.ceil((postsTotalCount/PAGE_POST_COUNT)));
        System.out.println(totalLastPageNum);

        Integer[] page = new Integer[totalLastPageNum];

        Integer blockLastPageNum = (totalLastPageNum > curPageNum + BLOCK_PAGE_NUM_COUNT)
                ? curPageNum + BLOCK_PAGE_NUM_COUNT
                : totalLastPageNum;

        curPageNum = (curPageNum <= 3) ? 1 : curPageNum - 2;


        for (int val = 1, idx = 0; val <= totalLastPageNum; val++, idx++) {
            page[idx] = idx;
        }

        return page;
    }

    @Transactional
    public Page<BoardEntity> getBoardList(String category,Pageable pageable) {
        return boardRepository.findByCategoryContaining(category, pageable);
    }

    @Transactional
    public Long getBoardCount(String category) {
        return boardRepository.countByCategory(category);
    }

    public Integer[] getBoardPageList(String category,Integer curPageNum) {
        Integer[] pageList = new Integer[BLOCK_PAGE_NUM_COUNT];

        Double postsTotalCount = Double.valueOf(this.getBoardCount(category));
        System.out.println(postsTotalCount);

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



    //해당하는 게시글 상세정보를 가져오면서 조회수 +1함
    @Transactional
    public BoardDto getPost(Long id) {
        Optional<BoardEntity> boardEntityWrapper = boardRepository.findById(id);
        BoardEntity boardEntity = boardEntityWrapper.get();
        BoardDto boardDTO=BoardDto.builder()
                .id(boardEntity.getId())
                .category(boardEntity.getCategory())
                .title(boardEntity.getTitle())
                .content(boardEntity.getContent())
                .writer(boardEntity.getWriter())
                .createdDate(boardEntity.getCreatedDate())
                .fileId(boardEntity.getFileId())
                .pwd(boardEntity.getPwd())
                .hintCnt(boardEntity.getHintCnt()+1)
                .build();

        boardRepository.save(boardDTO.toEntity()).getId();

        return boardDTO;
    }

    //그냥 게시물 정보만 가지고 와줌 조회수 안높여줌
    @Transactional
    public BoardDto getPostLast(Long id) {
        Optional<BoardEntity> boardEntityWrapper = boardRepository.findById(id);
        BoardEntity boardEntity = boardEntityWrapper.get();
        BoardDto boardDTO=BoardDto.builder()
                .id(boardEntity.getId())
                .category(boardEntity.getCategory())
                .title(boardEntity.getTitle())
                .content(boardEntity.getContent())
                .writer(boardEntity.getWriter())
                .createdDate(boardEntity.getCreatedDate())
                .hintCnt(boardEntity.getHintCnt())
                .fileId(boardEntity.getFileId())
                .pwd(boardEntity.getPwd())
                .build();


        return boardDTO;
    }

    //게시글을 삭제함
    @Transactional
    public void deletePost(Long id) {
        boardRepository.deleteById(id);
    }

    private BoardDto convertEntityToDto(BoardEntity boardEntity) {
        return BoardDto.builder()
                .id(boardEntity.getId())
                .category(boardEntity.getCategory())
                .title(boardEntity.getTitle())
                .content(boardEntity.getContent())
                .writer(boardEntity.getWriter())
                .createdDate(boardEntity.getCreatedDate())
                .hintCnt(boardEntity.getHintCnt())
                .fileId(boardEntity.getFileId())
                .pwd(boardEntity.getPwd())
                .build();
    }

    @Transactional
    public Long getBoardCount() {
        return boardRepository.count();
    }

    /*@Transactional
    public Long getSearchCount(String keyField,String keyword){
        String ID = "id";
        String TITLE = "title";
        String WRITER = "writer";
        System.out.println(keyField);

        if(keyField.equals(ID)){
            return boardRepository.countByIdContaining(keyword);
        }else if(keyField.equals(TITLE)){
        return boardRepository.countByTitleContaining(keyword);
        } else if(keyField.equals(WRITER)) {
            boardRepository.countByWriterContaining(keyword);}

        return boardRepository.countByTitleContaining(keyword);
    }*/

    //페이징
    public Integer[] getPageList(Integer curPageNum) {
        Integer[] pageList = new Integer[BLOCK_PAGE_NUM_COUNT];

        Double postsTotalCount = Double.valueOf(this.getBoardCount());

        Integer totalLastPageNum = (int)(Math.ceil((postsTotalCount/PAGE_POST_COUNT)));
        Integer[] page = new Integer[totalLastPageNum];

        Integer blockLastPageNum = (totalLastPageNum > curPageNum + BLOCK_PAGE_NUM_COUNT)
                ? curPageNum + BLOCK_PAGE_NUM_COUNT
                : totalLastPageNum;

        curPageNum = (curPageNum <= 3) ? 1 : curPageNum - 2;


        for (int val = curPageNum, idx = 0; val <= blockLastPageNum; val++, idx++) {
            page[idx] = val;
        }

        return page;
    }

    @Transactional
    public Page<BoardEntity> searchPosts(String keyField,String keyword, Pageable pageable) {
        String TITLE = "title";
        String WRITER = "writer";
        Page<BoardEntity> boardList =null;

        if(keyField.equals(TITLE)){
            boardList = boardRepository.findByTitleContaining(keyword, pageable);
        }else if(keyField.equals(WRITER)){
            boardList = boardRepository.findByWriterContaining(keyword, pageable);
        }

        return boardList;
    }

    public Long getSearchCountTitle(String keyword){
        return boardRepository.countByTitleContaining(keyword);
    }

    public Long getSearchCountWriter(String keyword){
        return boardRepository.countByWriterContaining(keyword);
    }


    public Integer[] getSearchPageList(String keyField,String keyword,Integer curPageNum) {
        String TITLE = "title";
        String WRITER = "writer";
        Integer[] pageList = new Integer[BLOCK_PAGE_NUM_COUNT];
        Double postsTotalCount=null;

        if(keyField.equals(TITLE)){
        postsTotalCount = Double.valueOf(this.getSearchCountTitle(keyword));
        }else if(keyField.equals(WRITER)){
            postsTotalCount = Double.valueOf(this.getSearchCountWriter(keyword));
        }

        Integer totalLastPageNum = (int)(Math.ceil((postsTotalCount/PAGE_POST_COUNT)));
        Integer[] page = new Integer[totalLastPageNum];

        Integer blockLastPageNum = (totalLastPageNum > curPageNum + BLOCK_PAGE_NUM_COUNT)
                ? curPageNum + BLOCK_PAGE_NUM_COUNT
                : totalLastPageNum;

        curPageNum = 1;


        for (int val = 1, idx = 0; val <= totalLastPageNum; val++, idx++) {
            page[idx] = idx;
        }

        return page;
    }

    @Transactional
    public Page<BoardEntity> searchPosts(String category,String keyField,String keyword, Pageable pageable) {
        String TITLE = "title";
        String WRITER = "writer";
        Page<BoardEntity> boardList =null;

        if(keyField.equals(TITLE)){
            boardList = boardRepository.findByCategoryContainingAndTitle(category,keyword, pageable);
        }else if(keyField.equals(WRITER)){
            boardList = boardRepository.findByCategoryContainingAndWriter(category,keyword, pageable);
        }

        return boardList;
    }

    public Integer[] getSearchPageList(String category,String keyField,String keyword,Integer curPageNum) {
        String TITLE = "title";
        String WRITER = "writer";
        Integer[] pageList = new Integer[BLOCK_PAGE_NUM_COUNT];
        Double postsTotalCount=null;

        System.out.println(category);
        if(keyField.equals(TITLE)){
            postsTotalCount = Double.valueOf(this.getSearchCountTitle(category,keyword));
            System.out.println(this.getSearchCountTitle(category,keyword));
        }else if(keyField.equals(WRITER)){
            postsTotalCount = Double.valueOf(this.getSearchCountWriter(category,keyword));
        }

        Integer totalLastPageNum = (int)(Math.ceil((postsTotalCount/PAGE_POST_COUNT)));
        Integer[] page = new Integer[totalLastPageNum];

        Integer blockLastPageNum = (totalLastPageNum > curPageNum + BLOCK_PAGE_NUM_COUNT)
                ? curPageNum + BLOCK_PAGE_NUM_COUNT
                : totalLastPageNum;

        curPageNum = 1;


        for (int val = curPageNum, idx = 0; val <= blockLastPageNum; val++, idx++) {
            page[idx] = idx;
        }

        return page;
    }
    public Long getSearchCountTitle(String category,String keyword){
        return boardRepository.countByCategoryContainingAndTitle(category,keyword);
    }

    public Long getSearchCountWriter(String category,String keyword){
        return boardRepository.countByCategoryContainingAndWriter(category,keyword);
    }


    //유효성 검사
    public Map<String, String> validateHandling(Errors errors) {
        Map<String, String> validatorResult = new HashMap<>();

        for (FieldError error : errors.getFieldErrors()) {
            String validKeyName = String.format("valid_%s", error.getField());
            validatorResult.put(validKeyName, error.getDefaultMessage());
        }

        return validatorResult;
    }

    public BoardDto getNext(Long id){
        Optional<BoardEntity> boardEntityWrapper;
        String a = boardRepository.findById(id)+"";
        String b = boardRepository.findFirstByOrderByIdDesc()+"";

        if(a.equals(b)){
            boardEntityWrapper = boardRepository.findById(id);
        }else {
            boardEntityWrapper = boardRepository.findNext(id);
        }

        BoardEntity boardEntity = boardEntityWrapper.get();
        BoardDto boardDto = BoardDto.builder()
                .id(boardEntity.getId())
                .category(boardEntity.getCategory())
                .title(boardEntity.getTitle())
                .content(boardEntity.getContent())
                .writer(boardEntity.getWriter())
                .createdDate(boardEntity.getCreatedDate())
                .hintCnt(boardEntity.getHintCnt())
                .fileId(boardEntity.getFileId())
                .pwd(boardEntity.getPwd())
                .build();

        return boardDto;

    }

    public BoardDto getPrevious(Long id){
        Optional<BoardEntity> boardEntityWrapper;
        String a = boardRepository.findById(id)+"";
        String b = boardRepository.findFirstByOrderById()+"";

        if(a.equals(b)){
            boardEntityWrapper = boardRepository.findById(id);
        }else {
            boardEntityWrapper = boardRepository.findPrevious(id);
        }

            BoardEntity boardEntity = boardEntityWrapper.get();

            BoardDto boardDto = BoardDto.builder()
                    .id(boardEntity.getId())
                    .category(boardEntity.getCategory())
                    .title(boardEntity.getTitle())
                    .content(boardEntity.getContent())
                    .writer(boardEntity.getWriter())
                    .createdDate(boardEntity.getCreatedDate())
                    .hintCnt(boardEntity.getHintCnt())
                    .fileId(boardEntity.getFileId())
                    .pwd(boardEntity.getPwd())
                    .build();

        System.out.println("이전글의 번호"+boardDto.getId());
        System.out.println("이전글의 카테고리"+boardDto.getCategory());

            return boardDto;

    }

    public BoardDto getNext(String category,Long id){
        Optional<BoardEntity> boardEntityWrapper1, boardEntityWrapper2;
        boardEntityWrapper1 = boardRepository.findById(id); //현재 게시글의 정보
        boardEntityWrapper2 = boardRepository.findNext(id, category); //다음 글의 정보

        BoardEntity boardEntity1 = boardEntityWrapper1.get();

        BoardDto boardDto1 = BoardDto.builder()
                .id(boardEntity1.getId())
                .category(boardEntity1.getCategory())
                .title(boardEntity1.getTitle())
                .content(boardEntity1.getContent())
                .writer(boardEntity1.getWriter())
                .createdDate(boardEntity1.getCreatedDate())
                .hintCnt(boardEntity1.getHintCnt())
                .fileId(boardEntity1.getFileId())
                .pwd(boardEntity1.getPwd())
                .build();

        System.out.println("현재글의 번호"+boardDto1.getId());
        System.out.println("현재글의 카테고리"+boardDto1.getCategory());

        if(boardEntityWrapper2.isPresent()){ //다음 글 있는지 없는지
            BoardEntity boardEntity2 = boardEntityWrapper2.get();

            BoardDto boardDto2 = BoardDto.builder()
                    .id(boardEntity2.getId())
                    .category(boardEntity2.getCategory())
                    .title(boardEntity2.getTitle())
                    .content(boardEntity2.getContent())
                    .writer(boardEntity2.getWriter())
                    .createdDate(boardEntity2.getCreatedDate())
                    .hintCnt(boardEntity2.getHintCnt())
                    .fileId(boardEntity2.getFileId())
                    .pwd(boardEntity2.getPwd())
                    .build();


            System.out.println("다음글의 번호"+boardDto2.getId());
            System.out.println("다음글의 카테고리"+boardDto2.getCategory());

            return boardDto2;}else{System.out.println("다음글 없음");}

        return boardDto1;
    }

    public BoardDto getPrevious(String category,Long id){
        Optional<BoardEntity> boardEntityWrapper1, boardEntityWrapper2;
            boardEntityWrapper1 = boardRepository.findById(id); //현재 게시글의 정보
            boardEntityWrapper2 = boardRepository.findPrevious(id, category); //현재 이전 게시글의 정보


        BoardEntity boardEntity1 = boardEntityWrapper1.get();

        BoardDto boardDto1 = BoardDto.builder()
                    .id(boardEntity1.getId())
                    .category(boardEntity1.getCategory())
                    .title(boardEntity1.getTitle())
                    .content(boardEntity1.getContent())
                    .writer(boardEntity1.getWriter())
                    .createdDate(boardEntity1.getCreatedDate())
                    .hintCnt(boardEntity1.getHintCnt())
                    .fileId(boardEntity1.getFileId())
                    .pwd(boardEntity1.getPwd())
                    .build();

        System.out.println("현재글의 번호"+boardDto1.getId());
        System.out.println("현재글의 카테고리"+boardDto1.getCategory());

        if(boardEntityWrapper2.isPresent()){ //이전글 있는지 없는지
        BoardEntity boardEntity2 = boardEntityWrapper2.get();

        BoardDto boardDto2 = BoardDto.builder()
                .id(boardEntity2.getId())
                .category(boardEntity2.getCategory())
                .title(boardEntity2.getTitle())
                .content(boardEntity2.getContent())
                .writer(boardEntity2.getWriter())
                .createdDate(boardEntity2.getCreatedDate())
                .hintCnt(boardEntity2.getHintCnt())
                .fileId(boardEntity2.getFileId())
                .pwd(boardEntity2.getPwd())
                .build();


        System.out.println("이전글의 번호"+boardDto2.getId());
        System.out.println("이전글의 카테고리"+boardDto2.getCategory());

        return boardDto2;}else{System.out.println("이전글 없음");}

        return boardDto1;
    }
}