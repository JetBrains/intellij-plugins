export { CdkFixedSizeVirtualScroll, CdkScrollable, CdkScrollableModule, CdkVirtualForOf, CdkVirtualForOfContext, CdkVirtualScrollRepeater, CdkVirtualScrollViewport, CdkVirtualScrollable, CdkVirtualScrollableElement, CdkVirtualScrollableWindow, DEFAULT_SCROLL_TIME, ExtendedScrollToOptions, FixedSizeVirtualScrollStrategy, ScrollDispatcher, ScrollingModule, VIRTUAL_SCROLLABLE, VIRTUAL_SCROLL_STRATEGY, VirtualScrollStrategy, _Bottom, _End, _Left, _Right, _Start, _Top, _Without, _XAxis, _XOR, _YAxis, _fixedSizeVirtualScrollStrategyFactory } from '../scrolling-module.d-CUKr8D_p.js';
import * as i0 from '@angular/core';
import { OnDestroy } from '@angular/core';
import { Observable } from 'rxjs';
export { Dir as ɵɵDir } from '../bidi-module.d-BSI86Zrk.js';
import '../data-source.d-DAIyaEMO.js';
import '../number-property.d-BzBQchZ2.js';

/** Time in ms to throttle the resize events by default. */
declare const DEFAULT_RESIZE_TIME = 20;
/** Object that holds the scroll position of the viewport in each direction. */
interface ViewportScrollPosition {
    top: number;
    left: number;
}
/**
 * Simple utility for getting the bounds of the browser viewport.
 * @docs-private
 */
declare class ViewportRuler implements OnDestroy {
    private _platform;
    private _listeners;
    /** Cached viewport dimensions. */
    private _viewportSize;
    /** Stream of viewport change events. */
    private readonly _change;
    /** Used to reference correct document/window */
    protected _document: Document;
    constructor(...args: unknown[]);
    ngOnDestroy(): void;
    /** Returns the viewport's width and height. */
    getViewportSize(): Readonly<{
        width: number;
        height: number;
    }>;
    /** Gets a DOMRect for the viewport's bounds. */
    getViewportRect(): {
        top: number;
        left: number;
        bottom: number;
        right: number;
        height: number;
        width: number;
    };
    /** Gets the (top, left) scroll position of the viewport. */
    getViewportScrollPosition(): ViewportScrollPosition;
    /**
     * Returns a stream that emits whenever the size of the viewport changes.
     * This stream emits outside of the Angular zone.
     * @param throttleTime Time in milliseconds to throttle the stream.
     */
    change(throttleTime?: number): Observable<Event>;
    /** Use defaultView of injected document if available or fallback to global window reference */
    private _getWindow;
    /** Updates the cached viewport size. */
    private _updateViewportSize;
    static ɵfac: i0.ɵɵFactoryDeclaration<ViewportRuler, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<ViewportRuler>;
}

export { DEFAULT_RESIZE_TIME, ViewportRuler };
export type { ViewportScrollPosition };
