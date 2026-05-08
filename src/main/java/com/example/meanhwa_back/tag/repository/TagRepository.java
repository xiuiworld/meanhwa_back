package com.example.meanhwa_back.tag.repository;

import java.util.Collection;

import com.example.meanhwa_back.tag.domain.Tag;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
    long countByIdIn(Collection<Long> ids);
}
