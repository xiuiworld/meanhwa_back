package com.example.meanhwa_back.flower.repository;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

import com.example.meanhwa_back.flower.domain.Flower;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FlowerRepository extends JpaRepository<Flower, Long> {
    @Query("""
            select f
            from Flower f
            where f.id = :id
              and f.deletedAt is null
            """)
    Optional<Flower> findActiveById(@Param("id") Long id);

    @Query("""
            select count(f)
            from Flower f
            where f.deletedAt is null
            """)
    long countActive();

    @Query("""
            select f
            from Flower f
            where f.id in :ids
              and f.deletedAt is null
            """)
    List<Flower> findActiveByIdIn(@Param("ids") Collection<Long> ids);

    @Query("""
            select f
            from Flower f
            where f.deletedAt is null
            """)
    List<Flower> findAllActive(Sort sort);

    @Query("""
            select f
            from Flower f
            where f.deletedAt is null
              and (
                :keyword is null
                or lower(f.name) like lower(concat('%', :keyword, '%'))
                or lower(f.coreMeaning) like lower(concat('%', :keyword, '%'))
              )
            """)
    Page<Flower> search(@Param("keyword") String keyword, Pageable pageable);
}
