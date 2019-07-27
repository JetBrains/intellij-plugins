/**
 * @license
 * Copyright Google Inc. All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { Type } from '../type';
import { InjectorType } from './defs';
import { InjectionToken } from './injection_token';
import { Injector } from './injector';
import { InjectFlags } from './injector_compatibility';
import { ClassProvider, ConstructorProvider, ExistingProvider, FactoryProvider, StaticClassProvider, StaticProvider, TypeProvider, ValueProvider } from './provider';
/**
 * Internal type for a single provider in a deep provider array.
 */
declare type SingleProvider = TypeProvider | ValueProvider | ClassProvider | ConstructorProvider | ExistingProvider | FactoryProvider | StaticClassProvider;
/**
 * Create a new `Injector` which is configured using a `defType` of `InjectorType<any>`s.
 *
 * @publicApi
 */
export declare function createInjector(defType: any, parent?: Injector | null, additionalProviders?: StaticProvider[] | null): Injector;
export declare class R3Injector {
    readonly parent: Injector;
    /**
     * Map of tokens to records which contain the instances of those tokens.
     */
    private records;
    /**
     * The transitive set of `InjectorType`s which define this injector.
     */
    private injectorDefTypes;
    /**
     * Set of values instantiated by this injector which contain `ngOnDestroy` lifecycle hooks.
     */
    private onDestroy;
    /**
     * Flag indicating this injector provides the APP_ROOT_SCOPE token, and thus counts as the
     * root scope.
     */
    private readonly isRootInjector;
    /**
     * Flag indicating that this injector was previously destroyed.
     */
    private destroyed;
    constructor(def: InjectorType<any>, additionalProviders: StaticProvider[] | null, parent: Injector);
    /**
     * Destroy the injector and release references to every instance or provider associated with it.
     *
     * Also calls the `OnDestroy` lifecycle hooks of every instance that was created for which a
     * hook was found.
     */
    destroy(): void;
    get<T>(token: Type<T> | InjectionToken<T>, notFoundValue?: any, flags?: InjectFlags): T;
    private assertNotDestroyed;
    /**
     * Add an `InjectorType` or `InjectorDefTypeWithProviders` and all of its transitive providers
     * to this injector.
     */
    private processInjectorType;
    /**
     * Process a `SingleProvider` and add it.
     */
    private processProvider;
    private hydrate;
    private injectableDefInScope;
}
/**
 * Converts a `SingleProvider` into a factory function.
 *
 * @param provider provider to convert to factory
 */
export declare function providerToFactory(provider: SingleProvider, ngModuleType?: InjectorType<any>, providers?: any[]): () => any;
export declare function isTypeProvider(value: SingleProvider): value is TypeProvider;
export {};
