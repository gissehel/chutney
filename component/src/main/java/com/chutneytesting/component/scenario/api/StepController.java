package com.chutneytesting.component.scenario.api;

import com.chutneytesting.component.scenario.api.dto.ComposableStepDto;
import com.chutneytesting.component.scenario.api.dto.ParentsStepDto;
import com.chutneytesting.component.scenario.domain.ComposableStepRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(StepController.BASE_URL)
@CrossOrigin(origins = "*")
public class StepController {

    static final String BASE_URL = "/api/steps/v1";

    private final ComposableStepRepository composableStepRepository;

    public StepController(ComposableStepRepository composableStepRepository) {
        this.composableStepRepository = composableStepRepository;
    }

    @PreAuthorize("hasAuthority('COMPONENT_WRITE')")
    @PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public String save(@RequestBody ComposableStepDto step) {
        return composableStepRepository.save(ComposableStepMapper.fromDto(step));
    }

    @PreAuthorize("hasAuthority('COMPONENT_WRITE')")
    @DeleteMapping(path = "/{stepId}")
    public void deleteById(@PathVariable String stepId) {
        composableStepRepository.deleteById(stepId);
    }

    @PreAuthorize("hasAuthority('COMPONENT_READ') or hasAuthority('SCENARIO_WRITE')")
    @GetMapping(path = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ComposableStepDto> findAll() {
        return composableStepRepository.findAll()
            .stream()
            .map(ComposableStepMapper::toDto)
            .sorted(ComposableStepDto.stepDtoComparator)
            .collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('COMPONENT_READ')")
    @GetMapping(path = "/{stepId}/parents", produces = MediaType.APPLICATION_JSON_VALUE)
    public ParentsStepDto findParents(@PathVariable String stepId) {
        return ParentStepMapper.toDto(composableStepRepository.findParents(stepId));
    }

    @PreAuthorize("hasAuthority('COMPONENT_READ')")
    @GetMapping(path = "/{stepId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ComposableStepDto findById(@PathVariable String stepId) {
        return ComposableStepMapper.toDto(
            composableStepRepository.findById(stepId)
        );
    }
}
