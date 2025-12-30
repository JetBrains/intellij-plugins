/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { NgZone, OnDestroy } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { MediaChange } from '../media-change';
import * as i0 from "@angular/core";
/**
 * MediaMonitor configures listeners to mediaQuery changes and publishes an Observable facade to
 * convert mediaQuery change callbacks to subscriber notifications. These notifications will be
 * performed within the ng Zone to trigger change detections and component updates.
 *
 * NOTE: both mediaQuery activations and de-activations are announced in notifications
 */
export declare class MatchMedia implements OnDestroy {
    protected _zone: NgZone;
    protected _platformId: Object;
    protected _document: any;
    /** Initialize source with 'all' so all non-responsive APIs trigger style updates */
    readonly source: BehaviorSubject<MediaChange>;
    registry: Map<string, MediaQueryList>;
    private readonly pendingRemoveListenerFns;
    constructor(_zone: NgZone, _platformId: Object, _document: any);
    /**
     * Publish list of all current activations
     */
    get activations(): string[];
    /**
     * For the specified mediaQuery?
     */
    isActive(mediaQuery: string): boolean;
    /**
     * External observers can watch for all (or a specific) mql changes.
     *
     * If a mediaQuery is not specified, then ALL mediaQuery activations will
     * be announced.
     */
    observe(): Observable<MediaChange>;
    observe(mediaQueries: string[]): Observable<MediaChange>;
    observe(mediaQueries: string[], filterOthers: boolean): Observable<MediaChange>;
    /**
     * Based on the BreakPointRegistry provider, register internal listeners for each unique
     * mediaQuery. Each listener emits specific MediaChange data to observers
     */
    registerQuery(mediaQuery: string | string[]): MediaChange[];
    ngOnDestroy(): void;
    /**
     * Call window.matchMedia() to build a MediaQueryList; which
     * supports 0..n listeners for activation/deactivation
     */
    protected buildMQL(query: string): MediaQueryList;
    protected _observable$: Observable<MediaChange>;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatchMedia, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<MatchMedia>;
}
