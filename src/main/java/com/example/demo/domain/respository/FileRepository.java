package com.example.demo.domain.respository;

import com.example.demo.domain.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository  extends JpaRepository<FileEntity, Long> {
}
