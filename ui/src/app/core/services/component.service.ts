import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, publishReplay, refCount } from 'rxjs/operators';

import { environment } from '@env/environment';

import {
    ComponentTask,
    Implementation,
    InputTask,
    KeyValue,
    ScenarioComponent,
    Strategy,
    Task
} from '@model';

@Injectable({
    providedIn: 'root'
})
export class ComponentService {

    private resourceUrl = '/api/task/v1';
    private stepUrl = '/api/steps/v1';
    private componentUrl = '/api/scenario/component-edition';
    private stepExecutionUrl = '/api/ui/componentstep/execution/v1';

    private tasks: Observable<Array<Task>>;

    constructor(
        private httpClient: HttpClient) {
    }

    findAllTasks(): Observable<Array<Task>> {
        if (!this.tasks) {
            this.tasks = this.httpClient.get<Array<Task>>(environment.backend + this.resourceUrl)
                .pipe(
                    map((res: Array<Task>) => {
                        res = res.map(t => new Task(
                            t.identifier,
                            t.inputs.map(ti => new InputTask(ti.name, ti.type)),
                            t.target
                        ));
                        res.sort((a, b) => a.identifier.localeCompare(b.identifier));
                        return res;
                    }),
                    publishReplay(1),
                    refCount()
                );
        }
        return this.tasks;
    }

    findAllComponent(): Observable<Array<ComponentTask>> {
        return this.httpClient.get<Array<ComponentTask>>(environment.backend + this.stepUrl + '/all')
            .pipe(map((res: Array<ComponentTask>) => {
                return res.map(c => this.mapToComponentTask(c, true));
            }));
    }

    save(component: ComponentTask): Observable<any> {
        return this.httpClient.post(
            environment.backend + this.stepUrl,
            this.mapToComponentTaskDto(component), {responseType: 'text'}
        );
    }

    delete(id: string): Observable<void> {
        return this.httpClient.delete(environment.backend + this.stepUrl + `/${id}`)
            .pipe(map(() => {
            }));
    }

    execute(component: ComponentTask, env: string): Observable<Object> {
        return this.httpClient.post(environment.backend + `${this.stepExecutionUrl}/${component.id}/${env}`, '')
            .pipe(map((res: Object) => {
                return res;
            }));
    }

    saveComponentTestCase(scenarioComponent: ScenarioComponent): Observable<any> {
        return this.httpClient.post(
            environment.backend + this.componentUrl,
            this.mapScenarioComponentToDto(scenarioComponent), {responseType: 'text'}
        );
    }

    findComponentTestCase(id: string): Observable<ScenarioComponent> {
        return this.httpClient.get<ScenarioComponent>(environment.backend + `${this.componentUrl}/${id}`).pipe(
            map(value => this.mapJsonToScenarioComponent(value, true))
        );
    }

    findComponentTestCaseExecutableParameters(id: string): Observable<Array<KeyValue>> {
        return this.httpClient.get<Array<KeyValue>>(environment.backend + `${this.componentUrl}/${id}/executable/parameters`);
    }

    findComponentTestCaseWithoutDeserializeImpl(id: string): Observable<ScenarioComponent> {
        return this.httpClient.get<ScenarioComponent>(environment.backend + `${this.componentUrl}/${id}/executable`).pipe(
            map(value => this.mapJsonToScenarioComponent(value, false))
        );
    }

    deleteComponentTestCase(id: string): Observable<void> {
        return this.httpClient.delete(environment.backend + `${this.componentUrl}/${id}`)
            .pipe(map(() => {
            }));
    }

    findParents(id: string): Observable<any> {
        return this.httpClient.get(environment.backend + `${this.stepUrl}/${id}/parents`);
    }

    private mapScenarioComponentToDto(scenarioComponent: ScenarioComponent): TestCaseComponentDto {
        return new TestCaseComponentDto(scenarioComponent.id,
            scenarioComponent.title,
            scenarioComponent.description,
            scenarioComponent.creationDate,
            scenarioComponent.updateDate,
            scenarioComponent.version,
            scenarioComponent.author,
            new ScenarioComponentDto(
                scenarioComponent.componentSteps.map((componentTask: ComponentTask) => this.mapComponentTaskToDto(componentTask)),
                scenarioComponent.parameters
            ),
            scenarioComponent.computedParameters,
            scenarioComponent.tags,
            scenarioComponent.datasetId
        );
    }

    private mapComponentTaskToDto(componentTask: ComponentTask): ComponentTaskDto {
        return new ComponentTaskDto(
            componentTask.id,
            componentTask.name,
            null,
            null,
            [],
            componentTask.parameters,
            componentTask.strategy,
            componentTask.computedParameters);
    }

    private mapJsonToScenarioComponent(jsonObject: any, withDeserializeImplementation: boolean): ScenarioComponent {
        return new ScenarioComponent(
            jsonObject.id,
            jsonObject.title,
            jsonObject.description,
            jsonObject.creationDate,
            jsonObject.updateDate,
            jsonObject.version,
            jsonObject.author,
            jsonObject.scenario.componentSteps.map((json: any) => this.mapToComponentTask(json, withDeserializeImplementation)),
            jsonObject.scenario.parameters.map(elt => new KeyValue(elt.key, elt.value)),
            jsonObject.computedParameters.map(elt => new KeyValue(elt.key, elt.value)),
            jsonObject.tags,
            jsonObject.datasetId
        );
    }

    private mapToComponentTask(jsonObject: any, withDeserializeImplementation: boolean): ComponentTask {
        let impl = Implementation.deserialize(JSON.parse(jsonObject.task));

        if (jsonObject.task && !withDeserializeImplementation) {
            impl =  JSON.parse(jsonObject.task);
        }

        return new ComponentTask(
            jsonObject.name,
            impl,
            jsonObject.steps.map(c => this.mapToComponentTask(c, withDeserializeImplementation)),
            jsonObject.parameters.map(elt => new KeyValue(elt.key, elt.value)),
            jsonObject.computedParameters.map(elt => new KeyValue(elt.key, elt.value)),
            jsonObject.tags,
            jsonObject.strategy != null ? new Strategy(jsonObject.strategy.type, jsonObject.strategy.parameters) : null,
            jsonObject.id
        );

    }

    private mapToComponentTaskDto(component: ComponentTask): ComponentTaskDto {
        return new ComponentTaskDto(
            component.id,
            component.name,
            null,
            component.implementation != null ? JSON.stringify(component.implementation) : null,
            component.children.map(c => this.mapToComponentTaskDto(c)),
            component.parameters,
            component.strategy,
            component.computedParameters,
            component.tags
        );
    }
}

export class TestCaseComponentDto {
    constructor(
        public id?: string,
        public title: string = 'Title',
        public description: string = 'Description',
        public creationDate?: Date,
        public updateDate?: Date,
        public version?: number,
        public author?: string,
        public scenario: ScenarioComponentDto = new ScenarioComponentDto(),
        public computedParameters: Array<KeyValue> = [],
        public tags: Array<string> = [],
        public datasetId: string = null) {
    }
}

export class ScenarioComponentDto {
    constructor(
        public componentSteps: Array<ComponentTaskDto> = [],
        public parameters: Array<KeyValue> = []) {
    }
}

export class ComponentTaskDto {
    constructor(
        public id: string,
        public name: string,
        public usage: string,
        public task: string,
        public steps: Array<ComponentTaskDto>,
        public parameters: Array<KeyValue>,
        public strategy: Strategy,
        public computedParameters: Array<KeyValue> = [],
        public tags: Array<String> = []) {
    }
}
