package com.example.demo.domain.respository;

import com.example.demo.domain.entity.BoardEntity;
import com.example.demo.domain.entity.PostEntity;
import com.example.demo.dto.PostDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardPostRepository extends JpaRepository<PostEntity, Long> {

    //@Query("Select p From PostEntity p where p.name = :name" )
    //List<PostEntity> findByName(@Param("name") String name);

    @Query("Select p From PostEntity p where p.boardEntity.id = :no order by p.postNum")
    Page<PostEntity> findByBoardEntity_Id( Pageable pageable, Long no);

    //@Query("Select p From PostEntity p where p.boardEntity.id = :boardEntity")

    //@Query("Select count(boardEntity) From PostEntity")
    Long countByBoardEntity_Id(Long no);


    //@Query(value = "select last_insert_id();",nativeQuery = true)
    //@Query(value = "select last_insert_id() from post p order by last_insert_id() limit 1  ;",nativeQuery = true)
    @Query(value = "select max(id) from post;",nativeQuery = true)
    Long autoIncrement();
}