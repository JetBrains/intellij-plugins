import { Observable } from 'rxjs';
import { BreakPointRegistry } from '../breakpoints/break-point-registry';
import { MatchMedia } from '../match-media/match-media';
import { MediaChange } from '../media-change';
import { PrintHook } from './print-hook';
import * as i0 from "@angular/core";
declare type ClearCallback = () => void;
declare type UpdateCallback = (val: any) => void;
export interface ElementMatcher {
    element: HTMLElement;
    key: string;
    value: any;
}
/**
 * MediaMarshaller - register responsive values from directives and
 *                   trigger them based on media query events
 */
export declare class MediaMarshaller {
    protected matchMedia: MatchMedia;
    protected breakpoints: BreakPointRegistry;
    protected hook: PrintHook;
    private activatedBreakpoints;
    private elementMap;
    private elementKeyMap;
    private watcherMap;
    private updateMap;
    private clearMap;
    private subject;
    get activatedAlias(): string;
    constructor(matchMedia: MatchMedia, breakpoints: BreakPointRegistry, hook: PrintHook);
    /**
     * Update styles on breakpoint activates or deactivates
     * @param mc
     */
    onMediaChange(mc: MediaChange): void;
    /**
     * initialize the marshaller with necessary elements for delegation on an element
     * @param element
     * @param key
     * @param updateFn optional callback so that custom bp directives don't have to re-provide this
     * @param clearFn optional callback so that custom bp directives don't have to re-provide this
     * @param extraTriggers other triggers to force style updates (e.g. layout, directionality, etc)
     */
    init(element: HTMLElement, key: string, updateFn?: UpdateCallback, clearFn?: ClearCallback, extraTriggers?: Observable<any>[]): void;
    /**
     * get the value for an element and key and optionally a given breakpoint
     * @param element
     * @param key
     * @param bp
     */
    getValue(element: HTMLElement, key: string, bp?: string): any;
    /**
     * whether the element has values for a given key
     * @param element
     * @param key
     */
    hasValue(element: HTMLElement, key: string): boolean;
    /**
     * Set the value for an input on a directive
     * @param element the element in question
     * @param key the type of the directive (e.g. flex, layout-gap, etc)
     * @param bp the breakpoint suffix (empty string = default)
     * @param val the value for the breakpoint
     */
    setValue(element: HTMLElement, key: string, val: any, bp: string): void;
    /** Track element value changes for a specific key */
    trackValue(element: HTMLElement, key: string): Observable<ElementMatcher>;
    /** update all styles for all elements on the current breakpoint */
    updateStyles(): void;
    /**
     * clear the styles for a given element
     * @param element
     * @param key
     */
    clearElement(element: HTMLElement, key: string): void;
    /**
     * update a given element with the activated values for a given key
     * @param element
     * @param key
     * @param value
     */
    updateElement(element: HTMLElement, key: string, value: any): void;
    /**
     * release all references to a given element
     * @param element
     */
    releaseElement(element: HTMLElement): void;
    /**
     * trigger an update for a given element and key (e.g. layout)
     * @param element
     * @param key
     */
    triggerUpdate(element: HTMLElement, key?: string): void;
    /** Cross-reference for HTMLElement with directive key */
    private buildElementKeyMap;
    /**
     * Other triggers that should force style updates:
     * - directionality
     * - layout changes
     * - mutationobserver updates
     */
    private watchExtraTriggers;
    /** Breakpoint locator by mediaQuery */
    private findByQuery;
    /**
     * get the fallback breakpoint for a given element, starting with the current breakpoint
     * @param bpMap
     * @param key
     */
    private getActivatedValues;
    /**
     * Watch for mediaQuery breakpoint activations
     */
    private observeActivations;
    static ɵfac: i0.ɵɵFactoryDeclaration<MediaMarshaller, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<MediaMarshaller>;
}
export {};
