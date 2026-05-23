package com.example.meanhwa_back.flower.domain;

import com.example.meanhwa_back.tag.domain.Tag;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** 꽃-태그 연관 및 큐레이션 가중치(weight). */
@Entity
@Table(
        name = "flower_tag_mappings",
        indexes = {
                @Index(name = "idx_flower_tag", columnList = "flower_id, tag_id"),
                @Index(name = "idx_tag_flower", columnList = "tag_id, flower_id")
        }
)
/**
 * 꽃과 태그의 다대다 관계와 추천 가중치를 담는 매핑 엔티티.
 * 큐레이션 점수 계산은 이 엔티티의 weight를 기준으로 태그 매칭 강도를 반영한다.
 */
public class FlowerTagMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flower_id", nullable = false)
    private Flower flower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    private int weight;

    protected FlowerTagMapping() {
    }

    public FlowerTagMapping(Flower flower, Tag tag, int weight) {
        this.flower = flower;
        this.tag = tag;
        this.weight = weight;
    }

    public Long getId() {
        return id;
    }

    public Flower getFlower() {
        return flower;
    }

    public Tag getTag() {
        return tag;
    }

    public int getWeight() {
        return weight;
    }
}
