/**
 * @license
 * Copyright Google Inc. All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import {Injector} from '@angular/core';
import {Observable} from 'rxjs';
import {Event} from './events';
import {ChildrenOutletContexts} from './router_outlet_context';
import {RouterStateSnapshot} from './router_state';

/**
 * This class bundles the actions involved in preactivation of a route.
 */
export declare class PreActivation {
    private future;
    private curr;
    private moduleInjector;
    private forwardEvent?;
    private canActivateChecks;
    private canDeactivateChecks;
    constructor(future: RouterStateSnapshot, curr: RouterStateSnapshot, moduleInjector: Injector, forwardEvent?: ((evt: Event) => void) | undefined);
    initialize(parentContexts: ChildrenOutletContexts): void;
    checkGuards(): Observable<boolean>;
    resolveData(paramsInheritanceStrategy: 'emptyOnly' | 'always'): Observable<any>;
    isDeactivating(): boolean;
    isActivating(): boolean;
    /**
     * Iterates over child routes and calls recursive `setupRouteGuards` to get `this` instance in
     * proper state to run `checkGuards()` method.
     */
    private setupChildRouteGuards;
    /**
     * Iterates over child routes and calls recursive `setupRouteGuards` to get `this` instance in
     * proper state to run `checkGuards()` method.
     */
    private setupRouteGuards;
    private shouldRunGuardsAndResolvers;
    private deactivateRouteAndItsChildren;
    private runCanDeactivateChecks;
    private runCanActivateChecks;
    /**
     * This should fire off `ActivationStart` events for each route being activated at this
     * level.
     * In other words, if you're activating `a` and `b` below, `path` will contain the
     * `ActivatedRouteSnapshot`s for both and we will fire `ActivationStart` for both. Always
     * return
     * `true` so checks continue to run.
     */
    private fireActivationStart;
    /**
     * This should fire off `ChildActivationStart` events for each route being activated at this
     * level.
     * In other words, if you're activating `a` and `b` below, `path` will contain the
     * `ActivatedRouteSnapshot`s for both and we will fire `ChildActivationStart` for both. Always
     * return
     * `true` so checks continue to run.
     */
    private fireChildActivationStart;
    private runCanActivate;
    private runCanActivateChild;
    private extractCanActivateChild;
    private runCanDeactivate;
    private runResolve;
    private resolveNode;
    private getResolver;
    private getToken;
}
