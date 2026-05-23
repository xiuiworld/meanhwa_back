package com.example.meanhwa_back.curation.wizard;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import com.example.meanhwa_back.curation.wizard.config.CurationFlowCatalog;
import com.example.meanhwa_back.curation.wizard.config.CurationFlowDocument;
import com.example.meanhwa_back.curation.wizard.domain.CurationStepKey;
import org.junit.jupiter.api.Test;

class CurationWizardCatalogConsistencyTest {
    private static final Path ROOT = Path.of("").toAbsolutePath();

    @Test
    void stepThreeAndFourCopyMatchesCurrentPlanningText() throws IOException {
        CurationFlowCatalog catalog = new CurationFlowCatalog();
        CurationFlowDocument.StepDocument step3 = catalog.requireStep(CurationStepKey.EMOTION);
        CurationFlowDocument.StepDocument step4 = catalog.requireStep(CurationStepKey.FLOWER_MEANING);

        assertThat(step3.getDefaultQuestionTitle()).isEqualTo("어떤 마음을 전하고 싶나요?");
        assertThat(step3.getDefaultQuestionSubtitle()).isEqualTo("선물에 담을 감정을 골라주세요.");
        assertThat(step4.getDefaultQuestionTitle()).isEqualTo("어떤 꽃말의 결에 가까운가요?");
        assertThat(step4.getDefaultQuestionSubtitle()).isEqualTo("전하고 싶은 마음을 조금 더 구체적으로 들려주세요.");
        assertThat(catalog.resolveQuestionTitle(CurationStepKey.FLOWER_MEANING,
                Map.of(CurationStepKey.RECIPIENT, "FAMILY")))
                .isEqualTo("가족에게 전달하고 싶은 꽃말은 무엇인가요?");

        String docs = read("docs/curation-wizard-api.md");
        assertThat(docs)
                .contains("어떤 **마음**을 전하고 싶나요? 선물에 담을 감정을 골라주세요.")
                .contains("어떤 **꽃말의 결**에 가까운가요? 전하고 싶은 마음을 조금 더 구체적으로 들려주세요.")
                .contains("`RECIPIENT=FAMILY`")
                .contains("\"가족에게 전달하고 싶은 꽃말은 무엇인가요?\"");
    }

    @Test
    void flowerMeaningLabelsStayInSyncAcrossYamlSqlMigrationAndDocs() throws IOException {
        Map<String, String> expectedLabelsByCode = expectedFlowerMeaningLabelsByCode();

        assertThat(expectedLabelsByCode).hasSize(60);
        assertMeaningLabelsPresent(expectedLabelsByCode, read("src/main/resources/data.sql"),
                "data.sql", (code, label) -> "'MEANING', '" + label + "', '" + code + "'");
        assertFlywayMeaningLabelsPresent(expectedLabelsByCode,
                read("src/main/resources/db/migration/mysql/V2__seed_curation_reference_data.sql")
                        + "\n"
                        + read("src/main/resources/db/migration/mysql/V5__fix_comfort_4_label.sql"));
        assertMeaningLabelsPresent(expectedLabelsByCode, read("docs/curation-wizard-api.md"),
                "curation wizard docs",
                (code, label) -> "| `" + emotionCode(code) + "` | `" + code + "` | " + label + " |");
    }

    private static Map<String, String> expectedFlowerMeaningLabelsByCode() {
        Map<String, String> expected = new LinkedHashMap<>();
        new CurationFlowCatalog().getDocument().getFlowerMeaningLabels().forEach((emotionCode, labels) -> {
            for (int index = 0; index < labels.size(); index++) {
                expected.put(emotionCode + "_" + (index + 1), labels.get(index));
            }
        });
        return expected;
    }

    private static void assertMeaningLabelsPresent(
            Map<String, String> expectedLabelsByCode,
            String source,
            String sourceName,
            BiFunction<String, String, String> snippet
    ) {
        List<String> missing = expectedLabelsByCode.entrySet().stream()
                .map(entry -> snippet.apply(entry.getKey(), entry.getValue()))
                .filter(expectedSnippet -> !source.contains(expectedSnippet))
                .toList();

        assertThat(missing)
                .as("%s must contain all Step4 flower meaning labels from YAML", sourceName)
                .isEmpty();
    }

    private static void assertFlywayMeaningLabelsPresent(
            Map<String, String> expectedLabelsByCode,
            String source
    ) {
        List<String> missing = expectedLabelsByCode.entrySet().stream()
                .filter(entry -> !containsMeaningSeed(source, entry.getKey(), entry.getValue())
                        && !containsMeaningCorrection(source, entry.getKey(), entry.getValue()))
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .toList();

        assertThat(missing)
                .as("flyway reference data migrations must seed or correct all Step4 flower meaning labels from YAML")
                .isEmpty();
    }

    private static boolean containsMeaningSeed(String source, String code, String label) {
        return source.contains("'MEANING', '" + label + "', '" + code + "'");
    }

    private static boolean containsMeaningCorrection(String source, String code, String label) {
        return source.contains("""
                UPDATE tags
                SET name = '%s'
                WHERE code = '%s'
                """.formatted(label, code));
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(ROOT.resolve(relativePath));
    }

    private static String emotionCode(String meaningCode) {
        return meaningCode.substring(0, meaningCode.lastIndexOf('_'));
    }
}
