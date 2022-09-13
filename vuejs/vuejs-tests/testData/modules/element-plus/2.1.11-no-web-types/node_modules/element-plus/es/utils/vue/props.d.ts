import type { ExtractPropTypes, PropType } from 'vue';
declare const wrapperKey: unique symbol;
export declare type PropWrapper<T> = {
    [wrapperKey]: T;
};
export declare const propKey = "__elPropsReservedKey";
declare type ResolveProp<T> = ExtractPropTypes<{
    key: {
        type: T;
        required: true;
    };
}>['key'];
declare type ResolvePropType<T> = ResolveProp<T> extends {
    type: infer V;
} ? V : ResolveProp<T>;
declare type ResolvePropTypeWithReadonly<T> = Readonly<T> extends Readonly<Array<infer A>> ? ResolvePropType<A[]> : ResolvePropType<T>;
declare type IfUnknown<T, V> = [unknown] extends [T] ? V : T;
export declare type BuildPropOption<T, D extends BuildPropType<T, V, C>, R, V, C> = {
    type?: T;
    values?: readonly V[];
    required?: R;
    default?: R extends true ? never : D extends Record<string, unknown> | Array<any> ? () => D : (() => D) | D;
    validator?: ((val: any) => val is C) | ((val: any) => boolean);
};
declare type _BuildPropType<T, V, C> = (T extends PropWrapper<unknown> ? T[typeof wrapperKey] : [V] extends [never] ? ResolvePropTypeWithReadonly<T> : never) | V | C;
export declare type BuildPropType<T, V, C> = _BuildPropType<IfUnknown<T, never>, IfUnknown<V, never>, IfUnknown<C, never>>;
declare type _BuildPropDefault<T, D> = [T] extends [
    Record<string, unknown> | Array<any> | Function
] ? D : D extends () => T ? ReturnType<D> : D;
export declare type BuildPropDefault<T, D, R> = R extends true ? {
    readonly default?: undefined;
} : {
    readonly default: Exclude<D, undefined> extends never ? undefined : Exclude<_BuildPropDefault<T, D>, undefined>;
};
export declare type BuildPropReturn<T, D, R, V, C> = {
    readonly type: PropType<BuildPropType<T, V, C>>;
    readonly required: IfUnknown<R, false>;
    readonly validator: ((val: unknown) => boolean) | undefined;
    [propKey]: true;
} & BuildPropDefault<BuildPropType<T, V, C>, IfUnknown<D, never>, IfUnknown<R, false>>;
/**
 * @description Build prop. It can better optimize prop types
 * @description 生成 prop，能更好地优化类型
 * @example
  // limited options
  // the type will be PropType<'light' | 'dark'>
  buildProp({
    type: String,
    values: ['light', 'dark'],
  } as const)
  * @example
  // limited options and other types
  // the type will be PropType<'small' | 'large' | number>
  buildProp({
    type: [String, Number],
    values: ['small', 'large'],
    validator: (val: unknown): val is number => typeof val === 'number',
  } as const)
  @link see more: https://github.com/element-plus/element-plus/pull/3341
 */
export declare function buildProp<T = never, D extends BuildPropType<T, V, C> = never, R extends boolean = false, V = never, C = never>(option: BuildPropOption<T, D, R, V, C>, key?: string): BuildPropReturn<T, D, R, V, C>;
declare type NativePropType = [
    ((...args: any) => any) | {
        new (...args: any): any;
    } | undefined | null
];
export declare const buildProps: <O extends { [K in keyof O]: O[K] extends BuildPropReturn<any, any, any, any, any> ? O[K] : [O[K]] extends NativePropType ? O[K] : O[K] extends BuildPropOption<infer T, infer D, infer R, infer V, infer C> ? D extends BuildPropType<T, V, C> ? BuildPropOption<T, D, R, V, C> : never : never; }>(props: O) => { [K_1 in keyof O]: O[K_1] extends {
    __elPropsReservedKey: boolean;
} ? O[K_1] : [O[K_1]] extends NativePropType ? O[K_1] : O[K_1] extends BuildPropOption<infer T_1, infer _D, infer R_1, infer V_1, infer C_1> ? BuildPropReturn<T_1, O[K_1]["default"], R_1, V_1, C_1> : never; };
export declare const definePropType: <T>(val: any) => PropWrapper<T>;
export {};
