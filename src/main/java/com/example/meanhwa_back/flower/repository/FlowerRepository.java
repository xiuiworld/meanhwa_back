package com.example.meanhwa_back.flower.repository;

import com.example.meanhwa_back.flower.domain.Flower;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FlowerRepository extends JpaRepository<Flower, Long> {

    @Query("""
            select f
            from Flower f
            where :keyword is null
               or lower(f.name) like lower(concat('%', :keyword, '%'))
               or lower(f.coreMeaning) like lower(concat('%', :keyword, '%'))
            """)
    Page<Flower> search(@Param("keyword") String keyword, Pageable pageable);
}
