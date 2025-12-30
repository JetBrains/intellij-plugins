import { ComponentMirror, Injector, InputSignal, OutputEmitterRef, Type } from '@angular/core';
type Inputs<T> = {
    [K in keyof T as T[K] extends InputSignal<infer R> ? K : never]?: T[K] extends InputSignal<infer R> ? R : never;
};
type Outputs<T> = {
    [K in keyof T as T[K] extends OutputEmitterRef<infer R> ? K : never]?: T[K] extends OutputEmitterRef<infer R> ? OutputEmitterRef<R>['emit'] : never;
};
type OptionalKeys<T, K = keyof T> = K extends keyof T ? T[K] extends Required<T>[K] ? undefined extends T[K] ? K : never : K : never;
interface FlexRenderRequiredOptions<TInputs extends Record<string, any>, TOutputs extends Record<string, any>> {
    /**
     * Component instance inputs. They will be set via [componentRef.setInput API](https://angular.dev/api/core/ComponentRef#setInput)
     */
    inputs: TInputs;
    /**
     * Component instance outputs.
     */
    outputs?: TOutputs;
    /**
     * Optional {@link Injector} that will be used when rendering the component
     */
    injector?: Injector;
}
interface FlexRenderOptions<TInputs extends Record<string, any>, TOutputs extends Record<string, any>> {
    /**
     * Component instance inputs. They will be set via [componentRef.setInput API](https://angular.dev/api/core/ComponentRef#setInput)
     */
    inputs?: TInputs;
    /**
     * Component instance outputs.
     */
    outputs?: TOutputs;
    /**
     * Optional {@link Injector} that will be used when rendering the component
     */
    injector?: Injector;
}
/**
 * Helper function to create a [@link FlexRenderComponent] instance, with better type-safety.
 *
 * - options object must be passed when the given component instance contains at least one required signal input.
 * - options/inputs is typed with the given component inputs
 * - options/outputs is typed with the given component outputs
 */
export declare function flexRenderComponent<TComponent = any, TInputs extends Inputs<TComponent> = Inputs<TComponent>, TOutputs extends Outputs<TComponent> = Outputs<TComponent>>(component: Type<TComponent>, ...options: OptionalKeys<TInputs> extends never ? [FlexRenderOptions<TInputs, TOutputs>?] : [FlexRenderRequiredOptions<TInputs, TOutputs>]): FlexRenderComponent<TComponent>;
/**
 * Wrapper class for a component that will be used as content for {@link FlexRenderDirective}
 *
 * Prefer {@link flexRenderComponent} helper for better type-safety
 */
export declare class FlexRenderComponent<TComponent = any> {
    readonly component: Type<TComponent>;
    readonly inputs?: Inputs<TComponent>;
    readonly injector?: Injector;
    readonly outputs?: Outputs<TComponent>;
    readonly mirror: ComponentMirror<TComponent>;
    readonly allowedInputNames: string[];
    readonly allowedOutputNames: string[];
    constructor(component: Type<TComponent>, inputs?: Inputs<TComponent>, injector?: Injector, outputs?: Outputs<TComponent>);
}
export {};
