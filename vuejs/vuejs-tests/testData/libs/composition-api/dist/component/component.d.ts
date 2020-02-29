import Vue, {ComponentOptions as Vue2ComponentOptions, VNode, VueConstructor} from 'vue';
import {ComponentPropsOptions, ExtractPropTypes} from './componentProps';
import {UnwrapRef} from '../reactivity';
import {HasDefined} from '../types/basic';

export declare type Data = {
    [key: string]: unknown;
};
export declare type ComponentInstance = InstanceType<VueConstructor>;
export declare type ComponentRenderProxy<P = {}, S = {}, PublicProps = P> = {
    $data: S;
    $props: PublicProps;
    $attrs: Data;
    $refs: Data;
    $slots: Data;
    $root: ComponentInstance | null;
    $parent: ComponentInstance | null;
    $emit: (event: string, ...args: unknown[]) => void;
} & P & S;
declare type VueConstructorProxy<PropsOptions, RawBindings> = VueConstructor & {
    new (...args: any[]): ComponentRenderProxy<ExtractPropTypes<PropsOptions>, UnwrapRef<RawBindings>, ExtractPropTypes<PropsOptions, false>>;
};
declare type VueProxy<PropsOptions, RawBindings> = Vue2ComponentOptions<Vue, UnwrapRef<RawBindings>, never, never, PropsOptions, ExtractPropTypes<PropsOptions, false>> & VueConstructorProxy<PropsOptions, RawBindings>;
export interface SetupContext {
    readonly attrs: Record<string, string>;
    readonly slots: {
        [key: string]: (...args: any[]) => VNode[];
    };
    readonly parent: ComponentInstance | null;
    readonly root: ComponentInstance;
    readonly listeners: {
        [key: string]: Function;
    };
    emit(event: string, ...args: any[]): void;
}
export declare type SetupFunction<Props, RawBindings> = (this: void, props: Props, ctx: SetupContext) => RawBindings | (() => VNode | null);
interface ComponentOptionsWithProps<PropsOptions = ComponentPropsOptions, RawBindings = Data, Props = ExtractPropTypes<PropsOptions>> {
    props?: PropsOptions;
    setup?: SetupFunction<Props, RawBindings>;
}
interface ComponentOptionsWithoutProps<Props = unknown, RawBindings = Data> {
    props?: undefined;
    setup?: SetupFunction<Props, RawBindings>;
}
export declare function defineComponent<RawBindings>(options: ComponentOptionsWithoutProps<unknown, RawBindings>): VueProxy<unknown, RawBindings>;
export declare function defineComponent<Props, RawBindings = Data, PropsOptions extends ComponentPropsOptions = ComponentPropsOptions>(options: (HasDefined<Props> extends true ? ComponentOptionsWithProps<PropsOptions, RawBindings, Props> : ComponentOptionsWithProps<PropsOptions, RawBindings>) & Omit<Vue2ComponentOptions<Vue>, keyof ComponentOptionsWithProps<never, never>>): VueProxy<PropsOptions, RawBindings>;
export declare function createComponent<RawBindings>(options: ComponentOptionsWithoutProps<unknown, RawBindings>): VueProxy<unknown, RawBindings>;
export declare function createComponent<Props, RawBindings = Data, PropsOptions extends ComponentPropsOptions = ComponentPropsOptions>(options: (HasDefined<Props> extends true ? ComponentOptionsWithProps<PropsOptions, RawBindings, Props> : ComponentOptionsWithProps<PropsOptions, RawBindings>) & Omit<Vue2ComponentOptions<Vue>, keyof ComponentOptionsWithProps<never, never>>): VueProxy<PropsOptions, RawBindings>;
export {};
