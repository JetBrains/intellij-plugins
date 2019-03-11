/**
 * @license
 * Copyright Google Inc. All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { Type } from '../type';
import { InjectionToken } from './injection_token';
import { Injector } from './injector';
/**
 * Injection flags for DI.
 *
 * @publicApi
 */
export declare enum InjectFlags {
    Default = 0,
    /**
     * Specifies that an injector should retrieve a dependency from any injector until reaching the
     * host element of the current component. (Only used with Element Injector)
     */
    Host = 1,
    /** Don't descend into ancestors of the node requesting injection. */
    Self = 2,
    /** Skip the node that is requesting injection. */
    SkipSelf = 4,
    /** Inject `defaultValue` instead if token not found. */
    Optional = 8
}
export declare function setCurrentInjector(injector: Injector | null | undefined): Injector | undefined | null;
/**
 * Sets the current inject implementation.
 */
export declare function setInjectImplementation(impl: (<T>(token: Type<T> | InjectionToken<T>, flags?: InjectFlags) => T | null) | undefined): (<T>(token: Type<T> | InjectionToken<T>, flags?: InjectFlags) => T | null) | undefined;
export declare function injectInjectorOnly<T>(token: Type<T> | InjectionToken<T>): T;
export declare function injectInjectorOnly<T>(token: Type<T> | InjectionToken<T>, flags?: InjectFlags): T | null;
/**
 * Injects a token from the currently active injector.
 *
 * This function must be used in the context of a factory function such as one defined for an
 * `InjectionToken`, and will throw an error if not called from such a context.
 *
 * @usageNotes
 * ### Example
 *
 * {@example core/di/ts/injector_spec.ts region='ShakeableInjectionToken'}
 *
 * Within such a factory function `inject` is utilized to request injection of a dependency, instead
 * of providing an additional array of dependencies as was common to do with `useFactory` providers.
 * `inject` is faster and more type-safe.
 *
 * @publicApi
 */
export declare function inject<T>(token: Type<T> | InjectionToken<T>): T;
export declare function inject<T>(token: Type<T> | InjectionToken<T>, flags?: InjectFlags): T | null;
/**
 * Injects `root` tokens in limp mode.
 *
 * If no injector exists, we can still inject tree-shakable providers which have `providedIn` set to
 * `"root"`. This is known as the limp mode injection. In such case the value is stored in the
 * `InjectableDef`.
 */
export declare function injectRootLimpMode<T>(token: Type<T> | InjectionToken<T>, notFoundValue: T | undefined, flags: InjectFlags): T | null;
export declare function injectArgs(types: (Type<any> | InjectionToken<any> | any[])[]): any[];
