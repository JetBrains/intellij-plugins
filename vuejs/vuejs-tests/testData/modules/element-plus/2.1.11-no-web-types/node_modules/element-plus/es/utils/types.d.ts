export { isArray, isFunction, isObject, isString, isDate, isPromise, isSymbol, } from '@vue/shared';
export { isBoolean, isNumber } from '@vueuse/core';
export { isVNode } from 'vue';
export declare const isUndefined: (val: any) => val is undefined;
export declare const isEmpty: (val: unknown) => boolean;
export declare const isElement: (e: unknown) => e is Element;
export declare const isPropAbsent: (prop: unknown) => prop is null | undefined;
