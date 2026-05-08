package com.example.meanhwa_back.flower.repository;

import java.util.Collection;
import java.util.List;

import com.example.meanhwa_back.flower.domain.FlowerTagMapping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FlowerTagMappingRepository extends JpaRepository<FlowerTagMapping, Long> {

    @Query("""
            select m
            from FlowerTagMapping m
            join fetch m.tag
            where m.flower.id = :flowerId
            order by m.tag.category asc, m.tag.name asc
            """)
    List<FlowerTagMapping> findByFlowerIdWithTag(@Param("flowerId") Long flowerId);

    @Query("""
            select m
            from FlowerTagMapping m
            join fetch m.flower
            join fetch m.tag
            where m.tag.id in :tagIds
            """)
    List<FlowerTagMapping> findByTagIdsWithFlowerAndTag(@Param("tagIds") Collection<Long> tagIds);
}
