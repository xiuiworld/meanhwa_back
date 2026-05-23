package com.example.meanhwa_back.tag.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.example.meanhwa_back.tag.domain.Tag;
import com.example.meanhwa_back.tag.domain.TagCategory;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
/**
 * 태그 조회와 카테고리별 필터링을 담당하는 저장소.
 * 삭제되지 않은 태그만 공개 API와 큐레이션 검증에 사용한다.
 */

public interface TagRepository extends JpaRepository<Tag, Long> {
    @Query("""
            select t
            from Tag t
            where t.deletedAt is null
            """)
    List<Tag> findAllActive(Sort sort);

    @Query("""
            select t
            from Tag t
            where t.id = :id
              and t.deletedAt is null
            """)
    Optional<Tag> findActiveById(@Param("id") Long id);

    @Query("""
            select count(t)
            from Tag t
            where t.id in :ids
              and t.deletedAt is null
            """)
    long countActiveByIdIn(@Param("ids") Collection<Long> ids);

    @Query("""
            select t
            from Tag t
            where t.id in :ids
              and t.deletedAt is null
            """)
    List<Tag> findActiveByIdIn(@Param("ids") Collection<Long> ids);

    @Query("""
            select count(t) > 0
            from Tag t
            where t.category = :category
              and lower(t.name) = lower(:name)
              and t.deletedAt is null
              and (:excludedId is null or t.id <> :excludedId)
            """)
    boolean existsActiveByCategoryAndName(
            @Param("category") TagCategory category,
            @Param("name") String name,
            @Param("excludedId") Long excludedId
    );

    @Query("""
            select t
            from Tag t
            where t.code = :code
              and t.deletedAt is null
            """)
    Optional<Tag> findActiveByCode(@Param("code") String code);

    @Query("""
            select t
            from Tag t
            where t.code in :codes
              and t.deletedAt is null
            """)
    List<Tag> findActiveByCodeIn(@Param("codes") Collection<String> codes);
}
