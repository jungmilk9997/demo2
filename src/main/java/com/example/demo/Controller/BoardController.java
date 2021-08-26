package com.example.demo.Controller;

import com.example.demo.domain.entity.BoardEntity;
import com.example.demo.domain.entity.PostEntity;
import com.example.demo.dto.BoardDto;
import com.example.demo.dto.FileDto;
import com.example.demo.dto.PostDto;
import com.example.demo.service.BoardService;
import com.example.demo.service.FileService;
import com.example.demo.service.PostService;
import com.example.demo.util.MD5Generator;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;


@Controller //뷰에 표시될 데이터가 있는 모델 객체를 만들고 올바른 뷰를 선택하는 일 담당
@AllArgsConstructor //모든 필드값을 파라미터로 받는 생성자를 만들어줌
public class BoardController {
    private BoardService boardService;
    private PostService postService;
    private FileService fileService;


    /*목록페이지*/

    //리스트 불러옴. 전체 게시물 제목이랑 여러가지를 보드리스트에 담아둠
    /*@GetMapping("/") //requestMapping(value="", method=requestMethod.get)을 대신하는 코드
    public String list(Model model, @RequestParam(value="page", defaultValue = "1") Integer pageNum) {
        List<BoardDto> boardList = boardService.getBoardlist(pageNum);
        Integer[] pageList = boardService.getPageList(pageNum);

        model.addAttribute("boardList", boardList);
        model.addAttribute("pageList", pageList);
        return "board/list.html";
    }*/

    @GetMapping("/") //requestMapping(value="", method=requestMethod.get)을 대신하는 코드
    public String list(Model model,  @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable, @RequestParam(value="page", defaultValue = "1") Integer curPageNum) {
        model.addAttribute("boardList", boardService.getBoardList(pageable));
        Integer[] pageList = boardService.getBoardPageList(curPageNum);
        model.addAttribute("pageList",pageList);

        return "board/list.html";
    }

    //Get은 게시판 노출 처리 담담, post는 작성한 글을 DB에 저장하는 담당

    @GetMapping("/category/{category}")
    public String listNote(@PathVariable("category") String category,Model model, @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable, @RequestParam(value="page", defaultValue = "1") Integer curPageNum) {

        System.out.println(category);
        model.addAttribute("boardList", boardService.getBoardList(category,pageable));
        Integer[] pageList = boardService.getBoardPageList(category,curPageNum);

        model.addAttribute("pageList", pageList);
        return "board/listNote.html";
    }


    //게시글 작성
    @GetMapping("/post")
    public String write(BoardDto boardDto) {
        return "board/write.html";
    }

    //검색기능(리스트 불러오는 보드리스트를 여기서 해줌)
    @GetMapping("/search")
    public String search(@RequestParam(value="keyword") String keyword,@RequestParam(value="keyField") String keyField, Model model, @RequestParam(value="page", defaultValue = "1") Integer pageNum,@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        if(keyword.length()<1){
            model.addAttribute("boardList", boardService.getBoardList(pageable));
            Integer[] pageList = boardService.getBoardPageList(pageNum);
            model.addAttribute("pageList",pageList);

            return "board/list.html";
        }

        Page<BoardEntity> boardDtoList = boardService.searchPosts(keyField,keyword,pageable);
        model.addAttribute("keyword",keyword);

        model.addAttribute("boardList", boardDtoList);
        Integer[] pageList = boardService.getSearchPageList(keyField,keyword,pageNum);

        model.addAttribute("pageList", pageList);
        model.addAttribute("keyField",keyField);
        return "board/list.html";
    }

    @GetMapping("/category/{category}/search")
    public String search(@RequestParam(value="keyword") String keyword,
                         @PathVariable(value="category") String category,
                         @RequestParam(value="keyField") String keyField, Model model,
                         @RequestParam(value="page", defaultValue = "1") Integer pageNum,
                         @PageableDefault(size = 3, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        System.out.println(category);
        if(keyword.length()<1){
            model.addAttribute("boardList", boardService.getBoardList(category,pageable));
            Integer[] pageList = boardService.getBoardPageList(category,pageNum);

            model.addAttribute("pageList", pageList);
            model.addAttribute("category",category);
            return "board/listNote.html";
        }

        Page<BoardEntity> boardDtoList = boardService.searchPosts(category,keyField,keyword,pageable);


        model.addAttribute("boardList", boardDtoList);
        Integer[] pageList = boardService.getSearchPageList(category,keyField,keyword,pageNum);

        model.addAttribute("boardList", boardDtoList);
        model.addAttribute("pageList", pageList);
        model.addAttribute("keyField",keyField);
        model.addAttribute("keyword",keyword);
        model.addAttribute("category",category);
        return "board/listNote.html";
    }

    //게시글 작성한 것을 데베에 올림, 유효성 검사
    @PostMapping("/post")
    public String write(@RequestParam("file") MultipartFile files, @Valid BoardDto boardDto, Errors errors, Model model) {

        try {
            if(files.getOriginalFilename().length()!=0) {
                String origFilename = files.getOriginalFilename();
                String filename = new MD5Generator(origFilename).toString()+(int) (Math.random()*100+1);

                String savePath = System.getProperty("user.dir") + "\\files";
                if (!new File(savePath).exists()) {
                    try{
                        new File(savePath).mkdir();
                    }
                    catch(Exception e){
                        e.getStackTrace();
                    }
                }
                String filePath = savePath + "\\" + filename;
                files.transferTo(new File(filePath));

                FileDto fileDto = new FileDto();
                fileDto.setOrigFilename(origFilename);
                fileDto.setFilename(filename);
                fileDto.setFilePath(filePath);

                Long fileId = fileService.saveFile(fileDto);
                boardDto.setFileId(fileId);
            }


            if(errors.hasErrors()){
                model.addAttribute("BoardDto", boardDto);

                Map<String, String> validatorResult = boardService.validateHandling(errors);
                for (String key : validatorResult.keySet()) {
                    model.addAttribute(key, validatorResult.get(key));
                }

                return "board/write.html";

            }

            boardService.savePost(boardDto);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return "redirect:/";

    }


    /*상세페이지*/

    //링크식에서 /post/{board.id}이걸 누르면 이리로 이동함
    @GetMapping("/post/{no}")
    public String detail(@PathVariable("no") Long no, Model model, PostDto postDto , @RequestParam(value="page", defaultValue = "1") Integer curPageNum,@PageableDefault(size = 3,sort = "boardEntity",direction = Sort.Direction.ASC) Pageable pageable) {

        //상세페이지 내용 boardDto, 댓글 내용 postDto에 담아줌

        BoardDto boardDTO = boardService.getPost(no);
        model.addAttribute("boardDto", boardDTO);
        //List<PostDto> postList = postService.getComment(no);
        //model.addAttribute("postList",postList);
        String filePrint=null;

        if(boardDTO.getFileId()!=null){
            FileDto fileDto = fileService.getFile(boardDTO.getFileId());
            model.addAttribute("fileDto",fileDto);


            if(fileDto.getOrigFilename().contains("PNG")||fileDto.getOrigFilename().contains("png")||
                    fileDto.getOrigFilename().contains("JPG")||fileDto.getOrigFilename().contains("jpg")||
                    fileDto.getOrigFilename().contains("GIF")||fileDto.getOrigFilename().contains("gif")||
                    fileDto.getOrigFilename().contains("JPEG")||fileDto.getOrigFilename().contains("jpeg")){

                 filePrint="print";
            }

        }
        model.addAttribute("fileprint",filePrint);

        //댓글페이징

        //no가 게시글 번호
        Page<PostEntity> pagePostList = postService.getPostList(pageable, no);
        Integer[] pageList = postService.getPageList(no, curPageNum);

        model.addAttribute("postList", pagePostList);
        model.addAttribute("pageList", pageList);


        BoardDto nextBoard = boardService.getNext(no);
        BoardDto PreviousBoard = boardService.getPrevious(no);

        model.addAttribute("nextBoard", nextBoard);
        model.addAttribute("previousBoard", PreviousBoard);
        return "board/detail.html";
        //반환값이 상세페이지임 모두 로딩해두고 이동하는 곳임
    }

    @GetMapping("/category/{category}/post/{no}")
    public String detail(@PathVariable("no") Long no,@PathVariable("category") String category, Model model, PostDto postDto , @RequestParam(value="page", defaultValue = "1") Integer curPageNum,@PageableDefault(size = 3,sort = "boardEntity",direction = Sort.Direction.ASC) Pageable pageable) {

        //상세페이지 내용 boardDto, 댓글 내용 postDto에 담아줌

        BoardDto boardDTO = boardService.getPost(no);
        model.addAttribute("boardDto", boardDTO);
        //List<PostDto> postList = postService.getComment(no);
        //model.addAttribute("postList",postList);
        if(boardDTO.getFileId()!=null){
            FileDto fileDto = fileService.getFile(boardDTO.getFileId());
            model.addAttribute("fileDto",fileDto);}

        //댓글페이징

        Page<PostEntity> pagePostList = postService.getPostList(pageable, no);
        Integer[] pageList = postService.getPageList(no, curPageNum);

        model.addAttribute("postList", pagePostList);
        model.addAttribute("pageList", pageList);

        System.out.println(category);
        System.out.println(no);

        BoardDto nextBoard = boardService.getNext(category,no);
        BoardDto PreviousBoard = boardService.getPrevious(category,no);
        model.addAttribute("nextBoard", nextBoard);
        model.addAttribute("previousBoard", PreviousBoard);
        model.addAttribute("category",category);
        return "board/detailNote.html";
        //반환값이 상세페이지임 모두 로딩해두고 이동하는 곳임
    }

    //첨부파일 다운로드
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> fileDownload(@PathVariable("fileId") Long fileId) throws IOException {
        FileDto fileDto = fileService.getFile(fileId);
        Path path = Paths.get(fileDto.getFilePath());
        Resource resource = new InputStreamResource(Files.newInputStream(path));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDto.getOrigFilename() + "\"")
                .body(resource);
    }

    //게시글 수정 비밀번호 확인페이지
    @GetMapping("/post/{no}/checkReBoard")
    public String check(@PathVariable("no")Long no,Model model) {
        BoardDto boardDTO = boardService.getPostLast(no);
        model.addAttribute("boardDto", boardDTO);
        return "board/checkReBoard.html";
    }

    //게시글 수정 비밀번호 확인 후 수정페이지 연결
    @PostMapping("/post/{no}/checkReBoard")
    public String check( @PathVariable("no")Long no, BoardDto boardDTO, Model model){
        BoardDto boardDTTO = boardService.getPostLast(no);
        //model.addAttribute("boardDto", boardDTTO);

        if(boardDTTO.getPwd().equals(boardDTO.getPwd())){

            BoardDto boardDTOO = boardService.getPostLast(no);
            model.addAttribute("boardDto", boardDTOO);

            if(boardDTOO.getFileId()!=null){
                FileDto fileDto = fileService.getFile(boardDTOO.getFileId());
                model.addAttribute("fileDto",fileDto);
            }

            return "board/update.html";
        }

        return "board/checkReBoard.html";
    }

    //게시글 삭제 비밀번호 확인
    @GetMapping("/post/{no}/checkDeleteBoard")
    public String checkDelete(@PathVariable("no")Long no,Model model) {
        BoardDto boardDTO = boardService.getPostLast(no);
        model.addAttribute("boardDto", boardDTO);
        return "board/checkDeleteBoard.html";
    }

    //게시글 삭제 비밀번호 확인 후 삭제
    @DeleteMapping("/post/{no}/checkDeleteBoard")
    public String checkDelete(@PathVariable("no") Long no,BoardDto boardDTO) {
        BoardDto boardDTTO = boardService.getPost(no);
        System.out.println(boardDTTO.getFileId());

        if(boardDTTO.getPwd().equals(boardDTO.getPwd())) {

            if(boardDTTO.getFileId()!=null){
                fileService.deletePost(boardDTTO.getFileId());}

            boardService.deletePost(no);


            return "redirect:/";
        }

        return "board/checkDeleteBoard.html";
    }

    //댓글입력
    @PostMapping("/post/{no}")
    public String writeComment( @PathVariable("no")Long no,@RequestParam(value="page", defaultValue = "1") Integer pageNum,@PageableDefault(size = 10, sort = "boardEntity", direction = Sort.Direction.ASC) Pageable pageable,@Valid  PostDto postDto, Errors errors, Model model, BoardDto boardDTO){

        //유효성 검사
        if(errors.hasErrors()){
            model.addAttribute("postDto",postDto);
            BoardDto boardDTOO = boardService.getPost(no);
            model.addAttribute("boardDto", boardDTOO);
            Page<PostEntity> postList = postService.getPostList(pageable, no);
            model.addAttribute("postList",postList);


            Map<String, String> validatorResult = boardService.validateHandling(errors);
            for (String key : validatorResult.keySet()) {
                model.addAttribute(key, validatorResult.get(key));
            }

            if(boardDTOO.getFileId()!=null){
                FileDto fileDto = fileService.getFile(boardDTOO.getFileId());
                model.addAttribute("fileDto",fileDto);}

            Integer[] pageList = postService.getPageList(no, pageNum);

            model.addAttribute("pageList", pageList);

            return "board/detail.html";
        }

        postService.saveComment(postDto);


        return "redirect:/post/{no}";
        //상세페이지 다시 로딩한다
    }

    //댓글 수정
    @GetMapping("/post/{no1}/{no2}/checkRePost") //no1이 게시물 번호 no2가 댓글 번호
    public String checkPost(@PathVariable("no1")Long no1,@PathVariable("no2") Long no2,Model model) {
        PostDto postDTO = postService.getPostCommentLast(no2);
        model.addAttribute("postDto", postDTO);
        BoardDto boardDTO = boardService.getPostLast(no1);
        model.addAttribute("boardDto", boardDTO);
        System.out.println("여기까지실행됨아악");
        return "board/checkRePost.html";
    }

    //댓글 비밀번호 확인 후 수정페이지 연결
    @PostMapping("/post/{no1}/{no2}/checkRePost")
    public String checkPost( @PathVariable("no1")Long no1,@PathVariable("no2") Long no2, BoardDto boardDTO, Model model,PostDto postDto){
        BoardDto boardDTTO = boardService.getPost(no1);
        model.addAttribute("boardDto", boardDTTO);
        PostDto postDto2 = postService.getPostCommentLast(no2);

        System.out.println("여기까지실행됨");
        System.out.println(postDto.getPwd());
        System.out.println(postDto2.getPwd());

        if(postDto2.getPwd().equals(postDto.getPwd())){

            BoardDto boardDTOO = boardService.getPostLast(no1);
            model.addAttribute("boardDto", boardDTOO);
            PostDto postDTO = postService.getPostCommentLast(no2);
            model.addAttribute("postDto", postDTO);
            System.out.println("여기까지실행됨dhdhdh");
            return "board/updateComment.html";
        }

        return "board/checkRePost.html";
    }

    //댓글 삭제 비밀번호 확인
    @GetMapping("/post/{no1}/{no2}/checkDeletePost")
    public String checkDeletePost(@PathVariable("no1")Long no1,@PathVariable("no2") Long no2,Model model) {
        PostDto postDTO = postService.getPostCommentLast(no2);
        model.addAttribute("postDto", postDTO);
        BoardDto boardDTO = boardService.getPostLast(no1);
        model.addAttribute("boardDto", boardDTO);
        return "board/checkDeletePost.html";
    }

    //댓글 삭제 비밀번호 확인 후 삭제
    @DeleteMapping("/post/{no1}/{no2}/checkDeletePost")
    public String checkDeletePost(@PathVariable("no1") Long no1,@PathVariable("no2") Long no2,PostDto postDto,BoardDto boardDTO,Model model) {
        PostDto postDto2 = postService.getPostCommentLast(no2);

        System.out.println("여기까지실행됨");
        System.out.println(postDto.getPwd());
        System.out.println(postDto2.getPwd());

        if(postDto2.getPwd().equals(postDto.getPwd())){
            postService.deleteComment(no2);
            return "redirect:/post/{no1}";
        }

        return "board/checkDeletePost.html";
    }


    /*수정 페이지*/

    //첨부파일 삭제
    @DeleteMapping("/post/delete/file/{no}/{no2}")
    public String deleteFile(@PathVariable("no") Long no,@PathVariable("no2") Long no2,Model model) {
        BoardDto boardDTTO = boardService.getPostLast(no);
        model.addAttribute("boardDto", boardDTTO);
        fileService.deletePost(boardDTTO.getFileId());
        boardDTTO.setFileId(null);
        boardService.savePost(boardDTTO);
        BoardDto boardDTO = boardService.getPostLast(no);
        model.addAttribute("boardDto", boardDTO);
        return "board/update.html";
    }

    //수정하는거 확실히함
    @Modifying
    @Transactional
    @PutMapping("/post/edit/{no}")
    public String update(@RequestParam("file") MultipartFile files,@PathVariable("no") Long no, Model model, @Valid  BoardDto boardDto, Errors errors) {
        model.addAttribute("BoardDto", boardDto);

        try{
            BoardDto boardDto2 = boardService.getPostLast(no);
            if(boardDto2.getFileId() != boardDto.getFileId()){
                fileService.deletePost(boardDto2.getFileId());
             }

            if(files.getOriginalFilename().length()>0) { //첨부하는 파일이 있으면 생성을 한다, 없으면 else로 넘어간다.
                String origFilename = files.getOriginalFilename();
                String filename = new MD5Generator(origFilename).toString();
                String savePath = System.getProperty("user.dir") + "\\files";
                if (!new File(savePath).exists()) {
                    try{
                        new File(savePath).mkdir();
                    }
                    catch(Exception e){
                        e.getStackTrace();
                    }
                }
                String filePath = savePath + "\\" + filename;

                //경로저장

                files.transferTo(new File(filePath));

                //파일 생성
                FileDto fileDto = new FileDto();
                if(boardDto.getFileId()!=null){ //근데 이미 첨부파일이 있으면
                    fileService.deletePost(boardDto.getFileId()); //기존꺼를 삭제하고
                    boardDto.setFileId(null); //보드테이블의 파일 아이디를 널로 만든다.
                }
                fileDto.setOrigFilename(origFilename);
                fileDto.setFilename(filename);
                fileDto.setFilePath(filePath);
                //아이디는 자동생성

                Long fileId = fileService.saveFile(fileDto);
                boardDto.setFileId(fileId);
                boardService.savePost(boardDto);
            }else {
                boardService.savePost(boardDto);}

            //유효성검사
            if(errors.hasErrors()){
                model.addAttribute("boardDto", boardDto);

                Map<String, String> validatorResult = boardService.validateHandling(errors);
                for (String key : validatorResult.keySet()) {
                    model.addAttribute(key, validatorResult.get(key));
                }


                boardDto = boardService.getPostLast(no);
                model.addAttribute("boardDto", boardDto);
                return "board/update.html";

            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        model.addAttribute("boardDto", boardDto);
        return "redirect:/post/{no}";
    }

    //게시글 삭제 기능
    /*@DeleteMapping("/post/{no}")
    public String delete(@PathVariable("no") Long no) {
        boardService.deletePost(no);
        return "redirect:/";
        //기본페이지로 다시 연결한다
    }*/

    //댓글 삭제기능
    /*@DeleteMapping("/post/delete/{no}/{no2}") //DELETE를 위한 HTTP요청 처리 어노테이션
    public String deleteComment(@PathVariable("no") Long no,@PathVariable("no2") Long no2,Model model) {
        postService.deleteComment(no);

        return "redirect:/post/{no2}";
    }*/

    //게시글 수정하는 페이지 보여줌
    /*@GetMapping("/post/edit/{no}")
    public String edit(@PathVariable("no") Long no, Model model, BoardDto boardDto) {
        BoardDto boardDTO = boardService.getPostLast(no);
        //no에 해당하는 내용들만 boardDTO에 담아서 나중에 boardDto에 담아준다.
        model.addAttribute("boardDto", boardDTO);

        if(boardDTO.getFileId()!=null){
            FileDto fileDto = fileService.getFile(boardDTO.getFileId());
            model.addAttribute("fileDto",fileDto);
        }
        return "board/update.html";
    }*/

    //POST와 PUT의 차이 : 멱등성(동일한 요청을 한번 보낼때와 여러번 보낼때 같은 효과)의 유무, POST는 멱동성이 없다.


    //댓글수정페이지 로드
    /*@GetMapping("/post/comment/edit/{no}/{no2}")
   public String editComment(@PathVariable("no") Long no,@PathVariable("no2") Long no2, Model model) {
       PostDto postDTO = postService.getPostCommentLast(no);
       model.addAttribute("postDto", postDTO);
       BoardDto boardDTO = boardService.getPostLast(no2);
       model.addAttribute("boardDto", boardDTO);
       return "board/updateComment.html";
   }*/


    /*댓글 수정 페이지*/

   //댓글수정해서 넣음
    @PutMapping("/post/comment/edit/{no}/{no2}")
    public String updateComment(@PathVariable("no") Long no,@PathVariable("no2") Long no2, Model model, @Valid PostDto postDTO, BoardDto boardDTO, Errors errors) {
        //유효성검사
        if(errors.hasErrors()){
            model.addAttribute("postDto", postDTO);
            model.addAttribute("boardDto", boardDTO);

            Map<String, String> validatorResult = postService.validateHandling(errors);
            for (String key : validatorResult.keySet()) {
                model.addAttribute(key, validatorResult.get(key));
            }

            boardDTO = boardService.getPostLast(no);
            model.addAttribute("boardDto", boardDTO);
            FileDto fileDto = fileService.getFile(boardDTO.getFileId());
        model.addAttribute("fileDto",fileDto);
            return "board/updateComment.html";
        }

        postService.saveComment(postDTO);
        return "redirect:/post/{no2}";
    }



}
