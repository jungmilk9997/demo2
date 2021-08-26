package com.example.demo.domain.respository;

import com.example.demo.domain.entity.BoardEntity;
import com.example.demo.domain.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<BoardEntity,Long> {

    Page<BoardEntity> findByTitleContaining(String keyword, Pageable pageable);

    @Query(value = "select * from Board Where id=:keyword", nativeQuery = true)
    Page<BoardEntity> findByIdContaining(String keyword, Pageable pageable);

    //@Query(value = "select * from Board Where writer=:keyword", nativeQuery = true)
    Page<BoardEntity> findByWriterContaining(String keyword, Pageable pageable);

    Long countByTitleContaining(String keyword);
    Long countByWriterContaining(String keyword);

    @Query(value = "select count (keyword) from Board", nativeQuery = true)
    Long countByIdContaining(String keyword);

    @Query(value = "select * from Board where id>:boardEntity_id order by id asc limit 1", nativeQuery = true)
    Optional<BoardEntity> findNext(@Param("boardEntity_id")Long no);

    @Query(value = "select * from Board where id<:boardEntity_id order by id desc limit 1", nativeQuery = true)
    Optional<BoardEntity> findPrevious(@Param("boardEntity_id")Long no);

    @Query(value = "select * from Board where id>:boardEntity_id and category=:boardEntity_category order by id asc limit 1", nativeQuery = true)
    Optional<BoardEntity> findNext(@Param("boardEntity_id")Long no, @Param("boardEntity_category")String category );
    //자기 게시글보다 작은것중에 번호 젤 작은 것

    @Query(value = "select * from Board where id<:boardEntity_id and category=:boardEntity_category order by id desc limit 1", nativeQuery = true)
    Optional<BoardEntity> findPrevious(@Param("boardEntity_id")Long no, @Param("boardEntity_category")String category);
    //자기 게시글보다 큰 것중에 번호 젤 큰거


    Optional<BoardEntity> findFirstByOrderById();
    Optional<BoardEntity> findFirstByOrderByIdDesc();

    Page<BoardEntity> findByCategoryContaining(String category, Pageable pageable);
    Long countByCategory(String category);

    //@Query(value = "select * from Board where title=:keyword and category =: category",nativeQuery = true)
    Page<BoardEntity> findByCategoryContainingAndTitle(String category, String keyword,Pageable pageable);

    //@Query(value = "select * from Board where writer=:keyword and category =: category",nativeQuery = true)
    Page<BoardEntity> findByCategoryContainingAndWriter(String category, String keyword,Pageable pageable);

    //@Query(value = "select count(* where title=:keyword and category =: category) from Board",nativeQuery = true)
    Long countByCategoryContainingAndTitle(String category,String keyword);

    //@Query(value = "select count(* where writer=:keyword and category =: category) from Board",nativeQuery = true)
    Long countByCategoryContainingAndWriter(String category,String keyword);

}
