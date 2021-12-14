/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { OnDestroy } from '@angular/core';
import { Observable } from 'rxjs';
import { MediaChange } from '../media-change';
import { MatchMedia } from '../match-media/match-media';
import { PrintHook } from '../media-marshaller/print-hook';
import { BreakPointRegistry } from '../breakpoints/break-point-registry';
import * as i0 from "@angular/core";
/**
 * MediaObserver enables applications to listen for 1..n mediaQuery activations and to determine
 * if a mediaQuery is currently activated.
 *
 * Since a breakpoint change will first deactivate 1...n mediaQueries and then possibly activate
 * 1..n mediaQueries, the MediaObserver will debounce notifications and report ALL *activations*
 * in 1 event notification. The reported activations will be sorted in descending priority order.
 *
 * This class uses the BreakPoint Registry to inject alias information into the raw MediaChange
 * notification. For custom mediaQuery notifications, alias information will not be injected and
 * those fields will be ''.
 *
 * Note: Developers should note that only mediaChange activations (not de-activations)
 *       are announced by the MediaObserver.
 *
 *  @usage
 *
 *  // RxJS
 *  import { filter } from 'rxjs/operators';
 *  import { MediaObserver } from '@angular/flex-layout';
 *
 *  @Component({ ... })
 *  export class AppComponent {
 *    status: string = '';
 *
 *    constructor(mediaObserver: MediaObserver) {
 *      const media$ = mediaObserver.asObservable().pipe(
 *        filter((changes: MediaChange[]) => true)   // silly noop filter
 *      );
 *
 *      media$.subscribe((changes: MediaChange[]) => {
 *        let status = '';
 *        changes.forEach( change => {
 *          status += `'${change.mqAlias}' = (${change.mediaQuery}) <br/>` ;
 *        });
 *        this.status = status;
 *     });
 *
 *    }
 *  }
 */
export declare class MediaObserver implements OnDestroy {
    protected breakpoints: BreakPointRegistry;
    protected matchMedia: MatchMedia;
    protected hook: PrintHook;
    /**
     * @deprecated Use `asObservable()` instead.
     * @breaking-change 8.0.0-beta.25
     * @deletion-target 10.0.0
     */
    readonly media$: Observable<MediaChange>;
    /** Filter MediaChange notifications for overlapping breakpoints */
    filterOverlaps: boolean;
    constructor(breakpoints: BreakPointRegistry, matchMedia: MatchMedia, hook: PrintHook);
    /**
     * Completes the active subject, signalling to all complete for all
     * MediaObserver subscribers
     */
    ngOnDestroy(): void;
    /**
     * Observe changes to current activation 'list'
     */
    asObservable(): Observable<MediaChange[]>;
    /**
     * Allow programmatic query to determine if one or more media query/alias match
     * the current viewport size.
     * @param value One or more media queries (or aliases) to check.
     * @returns Whether any of the media queries match.
     */
    isActive(value: string | string[]): boolean;
    /**
     * Register all the mediaQueries registered in the BreakPointRegistry
     * This is needed so subscribers can be auto-notified of all standard, registered
     * mediaQuery activations
     */
    private watchActivations;
    /**
     * Only pass/announce activations (not de-activations)
     *
     * Since multiple-mediaQueries can be activation in a cycle,
     * gather all current activations into a single list of changes to observers
     *
     * Inject associated (if any) alias information into the MediaChange event
     * - Exclude mediaQuery activations for overlapping mQs. List bounded mQ ranges only
     * - Exclude print activations that do not have an associated mediaQuery
     *
     * NOTE: the raw MediaChange events [from MatchMedia] do not
     *       contain important alias information; as such this info
     *       must be injected into the MediaChange
     */
    private buildObservable;
    /**
     * Find all current activations and prepare single list of activations
     * sorted by descending priority.
     */
    private findAllActivations;
    private readonly _media$;
    private readonly destroyed$;
    static ɵfac: i0.ɵɵFactoryDeclaration<MediaObserver, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<MediaObserver>;
}
