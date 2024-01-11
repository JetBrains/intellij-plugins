import * as i0 from '@angular/core';
import { NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { OnDestroy } from '@angular/core';
import { Platform } from '@angular/cdk/platform';

/** Utility for checking the matching state of @media queries. */
export declare class BreakpointObserver implements OnDestroy {
    private _mediaMatcher;
    private _zone;
    /**  A map of all media queries currently being listened for. */
    private _queries;
    /** A subject for all other observables to takeUntil based on. */
    private readonly _destroySubject;
    constructor(_mediaMatcher: MediaMatcher, _zone: NgZone);
    /** Completes the active subject, signalling to all other observables to complete. */
    ngOnDestroy(): void;
    /**
     * Whether one or more media queries match the current viewport size.
     * @param value One or more media queries to check.
     * @returns Whether any of the media queries match.
     */
    isMatched(value: string | readonly string[]): boolean;
    /**
     * Gets an observable of results for the given queries that will emit new results for any changes
     * in matching of the given queries.
     * @param value One or more media queries to check.
     * @returns A stream of matches for the given queries.
     */
    observe(value: string | readonly string[]): Observable<BreakpointState>;
    /** Registers a specific query to be listened for. */
    private _registerQuery;
    static ɵfac: i0.ɵɵFactoryDeclaration<BreakpointObserver, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<BreakpointObserver>;
}


export declare const Breakpoints: {
    XSmall: string;
    Small: string;
    Medium: string;
    Large: string;
    XLarge: string;
    Handset: string;
    Tablet: string;
    Web: string;
    HandsetPortrait: string;
    TabletPortrait: string;
    WebPortrait: string;
    HandsetLandscape: string;
    TabletLandscape: string;
    WebLandscape: string;
};

/** The current state of a layout breakpoint. */
export declare interface BreakpointState {
    /** Whether the breakpoint is currently matching. */
    matches: boolean;
    /**
     * A key boolean pair for each query provided to the observe method,
     * with its current matched state.
     */
    breakpoints: {
        [key: string]: boolean;
    };
}

export declare class LayoutModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<LayoutModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<LayoutModule, never, never, never>;
    static ɵinj: i0.ɵɵInjectorDeclaration<LayoutModule>;
}

/** A utility for calling matchMedia queries. */
export declare class MediaMatcher {
    private _platform;
    private _nonce?;
    /** The internal matchMedia method to return back a MediaQueryList like object. */
    private _matchMedia;
    constructor(_platform: Platform, _nonce?: string | null | undefined);
    /**
     * Evaluates the given media query and returns the native MediaQueryList from which results
     * can be retrieved.
     * Confirms the layout engine will trigger for the selector query provided and returns the
     * MediaQueryList for the query provided.
     */
    matchMedia(query: string): MediaQueryList;
    static ɵfac: i0.ɵɵFactoryDeclaration<MediaMatcher, [null, { optional: true; }]>;
    static ɵprov: i0.ɵɵInjectableDeclaration<MediaMatcher>;
}

export { }
