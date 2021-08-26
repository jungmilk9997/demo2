package com.example.demo.service;

import com.example.demo.domain.entity.FileEntity;
import com.example.demo.domain.respository.FileRepository;
import com.example.demo.dto.FileDto;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.File;

@Service
public class FileService {
    private FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Transactional
    public Long saveFile(FileDto fileDto) {
        return fileRepository.save(fileDto.toEntity()).getId();
    }

    @Transactional
    public FileDto getFile(Long id) {
        FileEntity file = fileRepository.findById(id).get();

        FileDto fileDto = FileDto.builder()
                .id(id)
                .origFilename(file.getOrigFilename())
                .filename(file.getFilename())
                .filePath(file.getFilePath())
                .build();

        if(fileDto.getOrigFilename().contains("PNG")||fileDto.getOrigFilename().contains("png")||
        fileDto.getOrigFilename().contains("JPG")||fileDto.getOrigFilename().contains("jpg")||
                fileDto.getOrigFilename().contains("GIF")||fileDto.getOrigFilename().contains("gif")||
                fileDto.getOrigFilename().contains("JPG")||fileDto.getOrigFilename().contains("jpeg")){

        }
        return fileDto;
    }

    public void deletePost(Long fileId) {

        String filePath=getFile(fileId).getFilePath();
        File deleteFile = new File(filePath);
        if(deleteFile.exists()){deleteFile.delete();}
        System.out.println("파일삭제됨");
        fileRepository.deleteById(fileId);

    }
}