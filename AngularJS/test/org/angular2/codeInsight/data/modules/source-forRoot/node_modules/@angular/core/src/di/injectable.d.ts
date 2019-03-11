/**
 * @license
 * Copyright Google Inc. All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { compileInjectable as render3CompileInjectable } from '../render3/jit/injectable';
import { Type } from '../type';
import { TypeDecorator } from '../util/decorators';
import { InjectableDef } from './defs';
import { ClassSansProvider, ConstructorSansProvider, ExistingSansProvider, FactorySansProvider, StaticClassSansProvider, ValueSansProvider } from './provider';
/**
 * Injectable providers used in `@Injectable` decorator.
 *
 * @publicApi
 */
export declare type InjectableProvider = ValueSansProvider | ExistingSansProvider | StaticClassSansProvider | ConstructorSansProvider | FactorySansProvider | ClassSansProvider;
/**
 * Type of the Injectable decorator / constructor function.
 *
 * @publicApi
 */
export interface InjectableDecorator {
    /**
     * Marks a class as available to `Injector` for creation.
     *
     * @see [Introduction to Services and DI](guide/architecture-services)
     * @see [Dependency Injection Guide](guide/dependency-injection)
     *
     * @usageNotes
     *
     * The following example shows how service classes are properly marked as
     * injectable.
     *
     * {@example core/di/ts/metadata_spec.ts region='Injectable'}
     *
     * `Injector` throws an error if it tries to instantiate a class that
     * is not decorated with `@Injectable`, as shown in the following example.
     *
     * {@example core/di/ts/metadata_spec.ts region='InjectableThrows'}
     *
     */
    (): TypeDecorator;
    (options?: {
        providedIn: Type<any> | 'root' | null;
    } & InjectableProvider): TypeDecorator;
    new (): Injectable;
    new (options?: {
        providedIn: Type<any> | 'root' | null;
    } & InjectableProvider): Injectable;
}
/**
 * Type of the Injectable metadata.
 *
 * @publicApi
 */
export interface Injectable {
    /**
     * Determines which injectors will provide the injectable,
     * by either associating it with an @NgModule or other `InjectorType`,
     * or by specifying that this injectable should be provided in the
     * 'root' injector, which will be the application-level injector in most apps.
     */
    providedIn?: Type<any> | 'root' | null;
}
/**
 * Injectable decorator and metadata.
 *
 * @Annotation
 * @publicApi
 */
export declare const Injectable: InjectableDecorator;
/**
 * Type representing injectable service.
 *
 * @publicApi
 */
export interface InjectableType<T> extends Type<T> {
    ngInjectableDef: InjectableDef<T>;
}
export declare const SWITCH_COMPILE_INJECTABLE__POST_R3__: typeof render3CompileInjectable;
