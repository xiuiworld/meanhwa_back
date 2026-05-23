package com.example.meanhwa_back.curation.dto;

import java.util.List;

import com.example.meanhwa_back.flower.domain.Flower;
import com.example.meanhwa_back.flower.domain.PriceRange;
import com.example.meanhwa_back.flower.dto.TagSummaryResponse;

/**
 * 큐레이션 결과 목록에 표시할 추천 꽃 응답 DTO.
 * 추천 점수, 추천 사유, 매칭 태그를 함께 담아 결과 화면의 설명력을 높인다.
 */
public record CurationFlowerResponse(
        Long flowerId,
        String name,
        String imageUrl,
        String coreMeaning,
        PriceRange priceRange,
        boolean isPetSafe,
        int score,
        String recommendationReason,
        List<TagSummaryResponse> matchedTags
) {
    /**
     * 도메인 값들을 조합해 이 API 응답 DTO를 만든다.
     */
    public static CurationFlowerResponse of(Flower flower, int score, List<TagSummaryResponse> matchedTags) {
        boolean isPetSafe = !flower.isToxicToPets();
        return new CurationFlowerResponse(
                flower.getId(),
                flower.getName(),
                flower.getImageUrl(),
                flower.getCoreMeaning(),
                flower.getPriceRange(),
                isPetSafe,
                score,
                recommendationReason(flower, matchedTags, isPetSafe),
                matchedTags
        );
    }

    private static String recommendationReason(
            Flower flower,
            List<TagSummaryResponse> matchedTags,
            boolean isPetSafe
    ) {
        String base = matchedTags.isEmpty()
                ? noTagReason(flower)
                : "%s 조건과 잘 맞고 %s 예산대에 어울리는 추천입니다.".formatted(
                        matchedTagNames(matchedTags),
                        priceLabel(flower.getPriceRange())
                );

        if (isPetSafe) {
            return base + " 반려동물에게도 비교적 안전한 식물입니다.";
        }
        return base + " 반려동물과 함께라면 배치에 주의하세요.";
    }

    private static String noTagReason(Flower flower) {
        String meaning = flower.getCoreMeaning();
        String meaningPhrase = meaning == null || meaning.isBlank()
                ? "마음을 전하기"
                : "\"%s\"의 의미를 전하기".formatted(meaning);

        return "%s은(는) %s 예산대에 맞고 %s 좋은 추천입니다.".formatted(
                flower.getName(),
                priceLabel(flower.getPriceRange()),
                meaningPhrase
        );
    }

    private static String matchedTagNames(List<TagSummaryResponse> matchedTags) {
        return matchedTags.stream()
                .map(TagSummaryResponse::name)
                .limit(3)
                .reduce((left, right) -> left + ", " + right)
                .orElse("선택한 태그");
    }

    private static String priceLabel(PriceRange priceRange) {
        return switch (priceRange) {
            case LOW -> "5만원 이하";
            case MEDIUM -> "5~10만원";
            case HIGH -> "10만원 이상";
            case PREMIUM -> "프리미엄";
        };
    }
}
