/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { NgZone } from '@angular/core';
import { MatchMedia } from '../match-media';
import { BreakPointRegistry } from '../../breakpoints/break-point-registry';
import * as i0 from "@angular/core";
/**
 * MockMatchMedia mocks calls to the Window API matchMedia with a build of a simulated
 * MockMediaQueryListener. Methods are available to simulate an activation of a mediaQuery
 * range and to clearAll mediaQuery listeners.
 */
export declare class MockMatchMedia extends MatchMedia {
    private _breakpoints;
    autoRegisterQueries: boolean;
    useOverlaps: boolean;
    constructor(_zone: NgZone, _platformId: Object, _document: any, _breakpoints: BreakPointRegistry);
    /** Easy method to clear all listeners for all mediaQueries */
    clearAll(): void;
    /** Feature to support manual, simulated activation of a mediaQuery. */
    activate(mediaQuery: string, useOverlaps?: boolean): boolean;
    /** Converts an optional mediaQuery alias to a specific, valid mediaQuery */
    _validateQuery(queryOrAlias: string): string;
    /**
     * Manually onMediaChange any overlapping mediaQueries to simulate
     * similar functionality in the window.matchMedia()
     */
    private _activateWithOverlaps;
    /**
     *
     */
    private _activateByAlias;
    /**
     *
     */
    private _activateByQuery;
    /** Deactivate all current MQLs and reset the buffer */
    private _deactivateAll;
    /** Insure the mediaQuery is registered with MatchMedia */
    private _registerMediaQuery;
    /**
     * Call window.matchMedia() to build a MediaQueryList; which
     * supports 0..n listeners for activation/deactivation
     */
    protected buildMQL(query: string): MediaQueryList;
    protected get hasActivated(): boolean;
    static ɵfac: i0.ɵɵFactoryDeclaration<MockMatchMedia, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<MockMatchMedia>;
}
/**
 * Special internal class to simulate a MediaQueryList and
 * - supports manual activation to simulate mediaQuery matching
 * - manages listeners
 */
export declare class MockMediaQueryList implements MediaQueryList {
    private _mediaQuery;
    private _isActive;
    private _listeners;
    get matches(): boolean;
    get media(): string;
    constructor(_mediaQuery: string);
    /**
     * Destroy the current list by deactivating the
     * listeners and clearing the internal list
     */
    destroy(): void;
    /** Notify all listeners that 'matches === TRUE' */
    activate(): MockMediaQueryList;
    /** Notify all listeners that 'matches === false' */
    deactivate(): MockMediaQueryList;
    /** Add a listener to our internal list to activate later */
    addListener(listener: MediaQueryListListener): void;
    /** Don't need to remove listeners in the testing environment */
    removeListener(_: MediaQueryListListener | null): void;
    addEventListener<K extends keyof MediaQueryListEventMap>(_: K, __: (this: MediaQueryList, ev: MediaQueryListEventMap[K]) => any, ___?: boolean | AddEventListenerOptions): void;
    removeEventListener<K extends keyof MediaQueryListEventMap>(_: K, __: (this: MediaQueryList, ev: MediaQueryListEventMap[K]) => any, ___?: boolean | EventListenerOptions): void;
    dispatchEvent(_: Event): boolean;
    onchange: MediaQueryListListener;
}
/**
 * Pre-configured provider for MockMatchMedia
 */
export declare const MockMatchMediaProvider: {
    provide: typeof MatchMedia;
    useClass: typeof MockMatchMedia;
};
declare type MediaQueryListListener = ((this: MediaQueryList, ev: MediaQueryListEvent) => any) | null;
export {};
