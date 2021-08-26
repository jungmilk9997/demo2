package com.example.demo.Controller;

import com.example.demo.dto.PostDto;
import com.example.demo.service.PostService;
import org.springframework.web.bind.annotation.PostMapping;

public class PostController {

    private PostService postService;

    /*@PostMapping("/post/{no}")
    public String writeComment( PostDto postDto){
        postService.saveComment(postDto);

        return "redirect:/post/{no}";
    }*/
}
