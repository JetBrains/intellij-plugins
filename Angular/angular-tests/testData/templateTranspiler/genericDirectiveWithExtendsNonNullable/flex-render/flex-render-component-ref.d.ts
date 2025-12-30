import { ComponentRef, Injector, OutputEmitterRef } from '@angular/core';
import { FlexRenderComponent } from './flex-render-component';
import * as i0 from "@angular/core";
export declare class FlexRenderComponentFactory {
    #private;
    createComponent<T>(flexRenderComponent: FlexRenderComponent<T>, componentInjector: Injector): FlexRenderComponentRef<T>;
    static ɵfac: i0.ɵɵFactoryDeclaration<FlexRenderComponentFactory, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<FlexRenderComponentFactory>;
}
export declare class FlexRenderComponentRef<T> {
    #private;
    readonly componentRef: ComponentRef<T>;
    readonly componentInjector: Injector;
    constructor(componentRef: ComponentRef<T>, componentData: FlexRenderComponent<T>, componentInjector: Injector);
    get component(): import("@angular/core").Type<T>;
    get inputs(): {};
    get outputs(): {};
    /**
     * Get component input and output diff by the given item
     */
    diff(item: FlexRenderComponent<T>): {
        inputDiff: import("@angular/core").KeyValueChanges<string, unknown>;
        outputDiff: import("@angular/core").KeyValueChanges<string, (value: unknown) => void>;
    };
    /**
     *
     * @param compare Whether the current ref component instance is the same as the given one
     */
    eqType(compare: FlexRenderComponent<T>): boolean;
    /**
     * Tries to update current component refs input by the new given content component.
     */
    update(content: FlexRenderComponent<T>): void;
    markAsDirty(): void;
    setInputs(inputs: Record<string, unknown>): void;
    setInput(key: string, value: unknown): void;
    setOutputs(outputs: Record<string, OutputEmitterRef<unknown>['emit'] | null | undefined>): void;
    setOutput(outputName: string, emit: OutputEmitterRef<unknown>['emit'] | undefined | null): void;
}
