package com.chutneytesting.task.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chutneytesting.task.TestLogger;
import com.chutneytesting.task.TestTarget;
import com.chutneytesting.task.spi.FinallyAction;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class FinalTaskTest {

    @Test
    void task_type_is_mandatory() {
        assertThatThrownBy(
            () -> new FinalTask(new TestLogger(), null, null, null, null, null, null, null, null)
        )
            .isInstanceOf(NullPointerException.class)
            .hasMessage("type is mandatory");
    }

    @Test
    void name_is_mandatory() {
        assertThatThrownBy(
            () -> new FinalTask(new TestLogger(), null, "", null, null, null, null, null, null)
        )
            .isInstanceOf(NullPointerException.class)
            .hasMessage("name is mandatory");
    }

    @Test
    void should_register_simple_finally_action() {
        TestLogger logger = new TestLogger();
        List<FinallyAction> registeredFinallyActions = new ArrayList<>();
        FinalTask sut = new FinalTask(logger, registeredFinallyActions::add, "debug", "a finally action", null, null, null, null, null);

        TaskExecutionResult result = sut.execute();

        assertThat(result.status).isEqualTo(TaskExecutionResult.Status.Success);
        assertThat(result.outputs).isEmpty();

        assertThat(logger.info).containsExactly("a finally action (debug) as finally action registered");
        assertThat(logger.errors).isEmpty();

        assertThat(registeredFinallyActions).hasSize(1);
        FinallyAction finallyAction = registeredFinallyActions.get(0);
        assertThat(finallyAction.name()).isEqualTo("a finally action");
        assertThat(finallyAction.type()).isEqualTo("debug");
        assertThat(finallyAction.target()).isEmpty();
        assertThat(finallyAction.inputs()).isEmpty();
        assertThat(finallyAction.validations()).isEmpty();
        assertThat(finallyAction.strategyType()).isEmpty();
        assertThat(finallyAction.strategyProperties()).isEmpty();
    }

    @Test
    void should_register_task_with_inputs_and_validations_and_strategy_finally_action() {
        TestLogger logger = new TestLogger();
        List<FinallyAction> registeredFinallyActions = new ArrayList<>();
        Target target = TestTarget.TestTargetBuilder.builder().withTargetId("target name").withUrl("url").build();

        Map<String, Object> inputs = Map.of("input 1", "value 1", "input 2", new Object());
        Map<String, Object> validations = Map.of("validation 1", true, "validation 2", new Object());
        String strategyType = "retry";
        Map<String, Object> strategyProperties = Map.of("timeout", "1 s", "delay", new Object());
        FinalTask sut = new FinalTask(logger, registeredFinallyActions::add, "complex task", "testing ??", target, inputs, validations, strategyType, strategyProperties);

        sut.execute();

        assertThat(registeredFinallyActions).hasSize(1);
        FinallyAction finallyAction = registeredFinallyActions.get(0);
        assertThat(finallyAction.name()).isEqualTo("testing ??");
        assertThat(finallyAction.type()).isEqualTo("complex task");
        assertThat(finallyAction.target()).contains(target);
        assertThat(finallyAction.inputs()).containsAllEntriesOf(inputs);
        assertThat(finallyAction.validations()).containsAllEntriesOf(validations);
        assertThat(finallyAction.strategyType()).contains(strategyType);
        assertThat(finallyAction.strategyProperties()).containsSame(strategyProperties);
    }
}
