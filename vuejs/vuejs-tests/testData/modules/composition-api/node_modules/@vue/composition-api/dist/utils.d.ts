import Vue from 'vue';

export declare function isNative(Ctor: any): boolean;
export declare const hasSymbol: boolean;
export declare const noopFn: any;
export declare function proxy(target: any, key: string, { get, set }: {
    get?: Function;
    set?: Function;
}): void;
export declare function def(obj: Object, key: string, val: any, enumerable?: boolean): void;
export declare function hasOwn(obj: Object | any[], key: string): boolean;
export declare function assert(condition: any, msg: string): void;
export declare function isArray<T>(x: unknown): x is T[];
export declare function isObject(val: unknown): val is Record<any, any>;
export declare function isPlainObject(x: unknown): x is Record<any, any>;
export declare function isFunction(x: unknown): x is Function;
export declare function warn(msg: string, vm?: Vue): void;
export declare function logError(err: Error, vm: Vue, info: string): void;
