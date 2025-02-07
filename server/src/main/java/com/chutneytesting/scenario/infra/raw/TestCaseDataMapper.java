package com.chutneytesting.scenario.infra.raw;

import com.chutneytesting.execution.domain.GwtScenarioMarshaller;
import com.chutneytesting.scenario.api.raw.mapper.GwtScenarioMapper;
import com.chutneytesting.scenario.domain.gwt.GwtTestCase;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadataImpl;

// call it in repo instead of repo aggregator
public class TestCaseDataMapper {

    private static final GwtScenarioMarshaller marshaller = new GwtScenarioMapper();

    public static TestCaseData toDto(GwtTestCase testCase) {
        return TestCaseData.builder()
            .withContentVersion("v2.1")
            .withId(testCase.metadata.id)
            .withTitle(testCase.metadata.title)
            .withCreationDate(testCase.metadata.creationDate)
            .withDescription(testCase.metadata.description)
            .withTags(testCase.metadata.tags)
            .withExecutionParameters(testCase.executionParameters)
            .withRawScenario(marshaller.serialize(testCase.scenario))
            .withAuthor(testCase.metadata.author)
            .withUpdateDate(testCase.metadata.updateDate)
            .withVersion(testCase.metadata.version)
            .build();
    }

    public static GwtTestCase fromDto(TestCaseData testCaseData) {
        switch (testCaseData.contentVersion) {
            case "v2.1":
                return fromV2_1(testCaseData);
            default:
                throw new RuntimeException("Cannot deserialize test case [" + testCaseData.id + "], unknown version [" + testCaseData.contentVersion + "]");
        }
    }

    private static GwtTestCase fromV2_1(TestCaseData dto) {
        return GwtTestCase.builder()
            .withMetadata(TestCaseMetadataImpl.builder()
                .withId(dto.id)
                .withTitle(dto.title)
                .withDescription(dto.description)
                .withCreationDate(dto.creationDate)
                .withTags(dto.tags)
                .withAuthor(dto.author)
                .withUpdateDate(dto.updateDate)
                .withVersion(dto.version)
                .build())
            .withExecutionParameters(dto.executionParameters)
            .withScenario(marshaller.deserialize(dto.title, dto.description, dto.rawScenario))
            .build();
    }
}
