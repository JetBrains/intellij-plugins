import {AnyObject} from '../types/basic';
import {UnwrapRef} from './ref';

export declare function isReactive(obj: any): boolean;
/**
 * Auto unwrapping when access property
 */
export declare function defineAccessControl(target: AnyObject, key: any, val?: any): void;
/**
 * Make obj reactivity
 */
export declare function reactive<T = any>(obj: T): UnwrapRef<T>;
/**
 * Make sure obj can't be a reactive
 */
export declare function nonReactive<T = any>(obj: T): T;
