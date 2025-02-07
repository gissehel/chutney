package com.chutneytesting.component.scenario.infra;

import com.chutneytesting.component.execution.domain.ExecutableComposedStep;
import com.chutneytesting.component.execution.domain.StepImplementation;
import com.chutneytesting.component.scenario.domain.ComposableStep;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ExecutableComposedStepMapper {

    private final RawImplementationMapper rawImplementationMapper;

    public ExecutableComposedStepMapper(RawImplementationMapper rawImplementationMapper) {
        this.rawImplementationMapper = rawImplementationMapper;
    }

    List<ExecutableComposedStep> composableToExecutable(List<ComposableStep> composableSteps) {
        return composableSteps.stream()
            .map(this::composableToExecutable)
            .collect(Collectors.toList());
    }

    ExecutableComposedStep composableToExecutable(ComposableStep fs) {
        return ExecutableComposedStep.builder()
            .withName(fs.name)
            .withStrategy(fs.strategy)
            .withSteps(composableToExecutable(fs.steps))
            .withImplementation(toStepImplementation(fs.implementation))
            .withParameters(fs.defaultParameters)
            .withExecutionParameters(fs.executionParameters)
            .build();
    }

    private Optional<StepImplementation> toStepImplementation(Optional<String> rawImplementation) {
        return rawImplementation.map(rawImplementationMapper::deserialize);
    }

}
