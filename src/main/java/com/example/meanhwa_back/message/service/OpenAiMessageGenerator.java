package com.example.meanhwa_back.message.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.meanhwa_back.message.config.OpenAiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** OpenAI Chat Completions API로 메시지 생성 (실패 시 템플릿 fallback). */
@Component
@Primary
public class OpenAiMessageGenerator implements MessageGenerator {
    private static final Logger log = LoggerFactory.getLogger(OpenAiMessageGenerator.class);

    private final OpenAiProperties properties;
    private final TemplateMessageGenerator fallbackGenerator;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public OpenAiMessageGenerator(
            OpenAiProperties properties,
            TemplateMessageGenerator fallbackGenerator,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.fallbackGenerator = fallbackGenerator;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory(properties.getTimeoutMillis()))
                .build();
    }

    /**
     * 입력 컨텍스트를 바탕으로 선물 메시지를 생성한다.
     */
    @Override
    public String generate(MessageContext context) {
        if (isBlank(properties.getApiKey())) {
            return fallbackGenerator.generate(context);
        }

        try {
            String response = restClient.post()
                    .uri("/v1/responses")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody(context))
                    .retrieve()
                    .body(String.class);
            String message = extractMessage(response);
            if (isBlank(message)) {
                log.warn("OpenAI message generation returned an empty response; using template fallback. model={}",
                        properties.getModel());
                return fallbackGenerator.generate(context);
            }
            return message.trim();
        } catch (RestClientException | IllegalArgumentException exception) {
            log.warn("OpenAI message generation failed; using template fallback. model={}, reason={}",
                    properties.getModel(),
                    exception.getClass().getSimpleName());
            return fallbackGenerator.generate(context);
        }
    }

    private Map<String, Object> requestBody(MessageContext context) {
        return Map.of(
                "model", properties.getModel(),
                "instructions", """
                        당신은 민화(Meanhwa)의 선물 메시지 작성 도우미입니다.
                        한국어로만 답하고, 과장된 표현 없이 따뜻하고 자연스러운 선물 메시지를 작성하세요.
                        출력은 메시지 본문만 작성하세요.
                        """,
                "input", prompt(context),
                "max_output_tokens", properties.getMaxOutputTokens()
        );
    }

    private String prompt(MessageContext context) {
        String tagNames = context.selectedTags().isEmpty()
                ? "없음"
                : context.selectedTags()
                        .stream()
                        .map(tag -> tag.getName())
                        .collect(Collectors.joining(", "));

        return """
                수신자: %s
                발신자: %s
                식물명: %s
                대표 꽃말: %s
                선택 태그: %s

                위 정보를 바탕으로 3~5문장 분량의 선물 메시지를 작성하세요.
                마지막에는 발신자 이름을 자연스럽게 포함하세요.
                """.formatted(
                context.receiverName(),
                context.senderName(),
                context.flower().getName(),
                context.flower().getCoreMeaning(),
                tagNames
        );
    }

    private String extractMessage(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            String outputText = textOrNull(root.path("output_text"));
            if (outputText != null) {
                return outputText;
            }

            for (JsonNode output : iterable(root.path("output"))) {
                for (JsonNode content : iterable(output.path("content"))) {
                    String text = textOrNull(content.path("text"));
                    if (text != null) {
                        return text;
                    }
                }
            }
            return null;
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid OpenAI response.", exception);
        }
    }

    private Iterable<JsonNode> iterable(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        return node::elements;
    }

    private String textOrNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull() || node.asText().isBlank()) {
            return null;
        }
        return node.asText();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private SimpleClientHttpRequestFactory requestFactory(int timeoutMillis) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMillis);
        factory.setReadTimeout(timeoutMillis);
        return factory;
    }
}
