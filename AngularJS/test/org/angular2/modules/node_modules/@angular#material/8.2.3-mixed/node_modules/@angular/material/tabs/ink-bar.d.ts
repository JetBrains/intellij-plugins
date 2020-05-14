/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { ElementRef, InjectionToken, NgZone } from '@angular/core';
/**
 * Interface for a a MatInkBar positioner method, defining the positioning and width of the ink
 * bar in a set of tabs.
 */
import * as ɵngcc0 from '@angular/core';
export interface _MatInkBarPositioner {
    (element: HTMLElement): {
        left: string;
        width: string;
    };
}
/** Injection token for the MatInkBar's Positioner. */
export declare const _MAT_INK_BAR_POSITIONER: InjectionToken<_MatInkBarPositioner>;
/**
 * The default positioner function for the MatInkBar.
 * @docs-private
 */
export declare function _MAT_INK_BAR_POSITIONER_FACTORY(): _MatInkBarPositioner;
/**
 * The ink-bar is used to display and animate the line underneath the current active tab label.
 * @docs-private
 */
export declare class MatInkBar {
    private _elementRef;
    private _ngZone;
    private _inkBarPositioner;
    _animationMode?: string | undefined;
    constructor(_elementRef: ElementRef<HTMLElement>, _ngZone: NgZone, _inkBarPositioner: _MatInkBarPositioner, _animationMode?: string | undefined);
    /**
     * Calculates the styles from the provided element in order to align the ink-bar to that element.
     * Shows the ink bar if previously set as hidden.
     * @param element
     */
    alignToElement(element: HTMLElement): void;
    /** Shows the ink bar. */
    show(): void;
    /** Hides the ink bar. */
    hide(): void;
    /**
     * Sets the proper styles to the ink bar element.
     * @param element
     */
    private _setStyles;
    static ɵfac: ɵngcc0.ɵɵFactoryDef<MatInkBar, [null, null, null, { optional: true; }]>;
    static ɵdir: ɵngcc0.ɵɵDirectiveDefWithMeta<MatInkBar, "mat-ink-bar", never, {}, {}, never>;
}

//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiaW5rLWJhci5kLnRzIiwic291cmNlcyI6WyJpbmstYmFyLmQudHMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBOztBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7OztBQUNBIiwic291cmNlc0NvbnRlbnQiOlsiLyoqXG4gKiBAbGljZW5zZVxuICogQ29weXJpZ2h0IEdvb2dsZSBMTEMgQWxsIFJpZ2h0cyBSZXNlcnZlZC5cbiAqXG4gKiBVc2Ugb2YgdGhpcyBzb3VyY2UgY29kZSBpcyBnb3Zlcm5lZCBieSBhbiBNSVQtc3R5bGUgbGljZW5zZSB0aGF0IGNhbiBiZVxuICogZm91bmQgaW4gdGhlIExJQ0VOU0UgZmlsZSBhdCBodHRwczovL2FuZ3VsYXIuaW8vbGljZW5zZVxuICovXG5pbXBvcnQgeyBFbGVtZW50UmVmLCBJbmplY3Rpb25Ub2tlbiwgTmdab25lIH0gZnJvbSAnQGFuZ3VsYXIvY29yZSc7XG4vKipcbiAqIEludGVyZmFjZSBmb3IgYSBhIE1hdElua0JhciBwb3NpdGlvbmVyIG1ldGhvZCwgZGVmaW5pbmcgdGhlIHBvc2l0aW9uaW5nIGFuZCB3aWR0aCBvZiB0aGUgaW5rXG4gKiBiYXIgaW4gYSBzZXQgb2YgdGFicy5cbiAqL1xuZXhwb3J0IGludGVyZmFjZSBfTWF0SW5rQmFyUG9zaXRpb25lciB7XG4gICAgKGVsZW1lbnQ6IEhUTUxFbGVtZW50KToge1xuICAgICAgICBsZWZ0OiBzdHJpbmc7XG4gICAgICAgIHdpZHRoOiBzdHJpbmc7XG4gICAgfTtcbn1cbi8qKiBJbmplY3Rpb24gdG9rZW4gZm9yIHRoZSBNYXRJbmtCYXIncyBQb3NpdGlvbmVyLiAqL1xuZXhwb3J0IGRlY2xhcmUgY29uc3QgX01BVF9JTktfQkFSX1BPU0lUSU9ORVI6IEluamVjdGlvblRva2VuPF9NYXRJbmtCYXJQb3NpdGlvbmVyPjtcbi8qKlxuICogVGhlIGRlZmF1bHQgcG9zaXRpb25lciBmdW5jdGlvbiBmb3IgdGhlIE1hdElua0Jhci5cbiAqIEBkb2NzLXByaXZhdGVcbiAqL1xuZXhwb3J0IGRlY2xhcmUgZnVuY3Rpb24gX01BVF9JTktfQkFSX1BPU0lUSU9ORVJfRkFDVE9SWSgpOiBfTWF0SW5rQmFyUG9zaXRpb25lcjtcbi8qKlxuICogVGhlIGluay1iYXIgaXMgdXNlZCB0byBkaXNwbGF5IGFuZCBhbmltYXRlIHRoZSBsaW5lIHVuZGVybmVhdGggdGhlIGN1cnJlbnQgYWN0aXZlIHRhYiBsYWJlbC5cbiAqIEBkb2NzLXByaXZhdGVcbiAqL1xuZXhwb3J0IGRlY2xhcmUgY2xhc3MgTWF0SW5rQmFyIHtcbiAgICBwcml2YXRlIF9lbGVtZW50UmVmO1xuICAgIHByaXZhdGUgX25nWm9uZTtcbiAgICBwcml2YXRlIF9pbmtCYXJQb3NpdGlvbmVyO1xuICAgIF9hbmltYXRpb25Nb2RlPzogc3RyaW5nIHwgdW5kZWZpbmVkO1xuICAgIGNvbnN0cnVjdG9yKF9lbGVtZW50UmVmOiBFbGVtZW50UmVmPEhUTUxFbGVtZW50PiwgX25nWm9uZTogTmdab25lLCBfaW5rQmFyUG9zaXRpb25lcjogX01hdElua0JhclBvc2l0aW9uZXIsIF9hbmltYXRpb25Nb2RlPzogc3RyaW5nIHwgdW5kZWZpbmVkKTtcbiAgICAvKipcbiAgICAgKiBDYWxjdWxhdGVzIHRoZSBzdHlsZXMgZnJvbSB0aGUgcHJvdmlkZWQgZWxlbWVudCBpbiBvcmRlciB0byBhbGlnbiB0aGUgaW5rLWJhciB0byB0aGF0IGVsZW1lbnQuXG4gICAgICogU2hvd3MgdGhlIGluayBiYXIgaWYgcHJldmlvdXNseSBzZXQgYXMgaGlkZGVuLlxuICAgICAqIEBwYXJhbSBlbGVtZW50XG4gICAgICovXG4gICAgYWxpZ25Ub0VsZW1lbnQoZWxlbWVudDogSFRNTEVsZW1lbnQpOiB2b2lkO1xuICAgIC8qKiBTaG93cyB0aGUgaW5rIGJhci4gKi9cbiAgICBzaG93KCk6IHZvaWQ7XG4gICAgLyoqIEhpZGVzIHRoZSBpbmsgYmFyLiAqL1xuICAgIGhpZGUoKTogdm9pZDtcbiAgICAvKipcbiAgICAgKiBTZXRzIHRoZSBwcm9wZXIgc3R5bGVzIHRvIHRoZSBpbmsgYmFyIGVsZW1lbnQuXG4gICAgICogQHBhcmFtIGVsZW1lbnRcbiAgICAgKi9cbiAgICBwcml2YXRlIF9zZXRTdHlsZXM7XG59XG4iXX0=