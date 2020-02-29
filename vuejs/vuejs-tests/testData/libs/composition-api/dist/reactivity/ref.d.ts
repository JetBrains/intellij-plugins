import {Data} from '../component';
import {HasDefined} from '../types/basic';

declare type BailTypes = Function | Map<any, any> | Set<any> | WeakMap<any, any> | WeakSet<any>;
export interface Ref<T> {
    value: T;
}
export declare type UnwrapRef<T> = T extends Ref<infer V> ? UnwrapRef2<V> : T extends BailTypes ? T : T extends object ? {
    [K in keyof T]: UnwrapRef2<T[K]>;
} : T;
declare type UnwrapRef2<T> = T extends Ref<infer V> ? UnwrapRef3<V> : T extends BailTypes ? T : T extends object ? {
    [K in keyof T]: UnwrapRef3<T[K]>;
} : T;
declare type UnwrapRef3<T> = T extends Ref<infer V> ? UnwrapRef4<V> : T extends BailTypes ? T : T extends object ? {
    [K in keyof T]: UnwrapRef4<T[K]>;
} : T;
declare type UnwrapRef4<T> = T extends Ref<infer V> ? UnwrapRef5<V> : T extends BailTypes ? T : T extends object ? {
    [K in keyof T]: UnwrapRef5<T[K]>;
} : T;
declare type UnwrapRef5<T> = T extends Ref<infer V> ? UnwrapRef6<V> : T extends BailTypes ? T : T extends object ? {
    [K in keyof T]: UnwrapRef6<T[K]>;
} : T;
declare type UnwrapRef6<T> = T extends Ref<infer V> ? UnwrapRef7<V> : T extends BailTypes ? T : T extends object ? {
    [K in keyof T]: UnwrapRef7<T[K]>;
} : T;
declare type UnwrapRef7<T> = T extends Ref<infer V> ? UnwrapRef8<V> : T extends BailTypes ? T : T extends object ? {
    [K in keyof T]: UnwrapRef8<T[K]>;
} : T;
declare type UnwrapRef8<T> = T extends Ref<infer V> ? UnwrapRef9<V> : T extends BailTypes ? T : T extends object ? {
    [K in keyof T]: UnwrapRef9<T[K]>;
} : T;
declare type UnwrapRef9<T> = T extends Ref<infer V> ? UnwrapRef10<V> : T extends BailTypes ? T : T extends object ? {
    [K in keyof T]: UnwrapRef10<T[K]>;
} : T;
declare type UnwrapRef10<T> = T extends Ref<infer V> ? V : T;
interface RefOption<T> {
    get(): T;
    set?(x: T): void;
}
declare class RefImpl<T> implements Ref<T> {
    value: T;
    constructor({ get, set }: RefOption<T>);
}
export declare function createRef<T>(options: RefOption<T>): RefImpl<T>;
declare type RefValue<T> = T extends Ref<infer V> ? V : UnwrapRef<T>;
export declare function ref<T = undefined>(): Ref<T | undefined>;
export declare function ref<T = null>(raw: null): Ref<T | null>;
export declare function ref<S, T = unknown, R = HasDefined<S> extends true ? S : RefValue<T>>(raw: T): Ref<R>;
export declare function isRef<T>(value: any): value is Ref<T>;
declare type Refs<Data> = {
    [K in keyof Data]: Data[K] extends Ref<infer V> ? Ref<V> : Ref<Data[K]>;
};
export declare function toRefs<T extends Data = Data>(obj: T): Refs<T>;
export {};
