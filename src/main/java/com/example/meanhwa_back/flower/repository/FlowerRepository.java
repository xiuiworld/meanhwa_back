package com.example.meanhwa_back.flower.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.example.meanhwa_back.flower.domain.Flower;
import com.example.meanhwa_back.flower.domain.ManagementLevel;
import com.example.meanhwa_back.flower.domain.PriceRange;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
/**
 * 꽃 조회와 검색 쿼리를 담당하는 JPA 저장소.
 * 삭제되지 않은 꽃만 노출하는 active 조회 메서드를 중심으로 공개 API와 관리자 통계를 지원한다.
 */

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
                or lower(f.description) like lower(concat('%', :keyword, '%'))
              )
              and (:priceRange is null or f.priceRange = :priceRange)
              and (
                :isPetSafe is null
                or (:isPetSafe = true and f.isToxicToPets = false)
                or (:isPetSafe = false and f.isToxicToPets = true)
              )
              and (:managementLevel is null or f.managementLevel = :managementLevel)
              and (
                :tagCount = 0
                or f.id in (
                    select m.flower.id
                    from FlowerTagMapping m
                    where m.tag.id in :tagIds
                      and m.tag.deletedAt is null
                    group by m.flower.id
                    having count(distinct m.tag.id) = :tagCount
                )
              )
            """)
    Page<Flower> search(
            @Param("keyword") String keyword,
            @Param("priceRange") PriceRange priceRange,
            @Param("isPetSafe") Boolean isPetSafe,
            @Param("managementLevel") ManagementLevel managementLevel,
            @Param("tagIds") Collection<Long> tagIds,
            @Param("tagCount") long tagCount,
            Pageable pageable
    );
}
