package com.chutneytesting.execution.domain;

import com.chutneytesting.scenario.domain.gwt.GwtScenario;
import com.chutneytesting.scenario.domain.gwt.GwtTestCase;
import com.chutneytesting.server.core.domain.execution.ExecutionRequest;
import com.chutneytesting.server.core.domain.execution.processor.TestCasePreProcessor;
import com.chutneytesting.server.core.domain.globalvar.GlobalvarRepository;
import java.util.Map;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;

@Component
public class GwtDataSetPreProcessor implements TestCasePreProcessor<GwtTestCase> {

    private final GlobalvarRepository globalvarRepository;
    private final GwtScenarioMarshaller marshaller;

    public GwtDataSetPreProcessor(GwtScenarioMarshaller marshaller, GlobalvarRepository globalvarRepository) {
        this.marshaller = marshaller;
        this.globalvarRepository = globalvarRepository;
    }

    @Override
    public GwtTestCase apply(ExecutionRequest executionRequest) {
        GwtTestCase testCase = (GwtTestCase) executionRequest.testCase;
        return GwtTestCase.builder()
            .withMetadata(testCase.metadata)
            .withExecutionParameters(testCase.executionParameters)
            .withScenario(replaceParams(testCase.scenario, testCase.executionParameters))
            .build();
    }

    private GwtScenario replaceParams(GwtScenario scenario, Map<String, String> dataSet) {
        String blob = marshaller.serialize(scenario);
        return marshaller.deserialize(scenario.title, scenario.description, replaceParams(blob, globalvarRepository.getFlatMap(), dataSet, StringEscapeUtils::escapeJson));
    }

}
