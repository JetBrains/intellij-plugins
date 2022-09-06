import { BooleanInput } from '@angular/cdk/coercion';
import { ChangeDetectorRef } from '@angular/core';
import { CollectionViewer } from '@angular/cdk/collections';
import { DataSource } from '@angular/cdk/collections';
import { Directionality } from '@angular/cdk/bidi';
import { DoCheck } from '@angular/core';
import { ElementRef } from '@angular/core';
import * as i0 from '@angular/core';
import * as i7 from '@angular/cdk/bidi';
import { InjectionToken } from '@angular/core';
import { IterableDiffers } from '@angular/core';
import { ListRange } from '@angular/cdk/collections';
import { NgIterable } from '@angular/core';
import { NgZone } from '@angular/core';
import { NumberInput } from '@angular/cdk/coercion';
import { Observable } from 'rxjs';
import { OnChanges } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { OnInit } from '@angular/core';
import { Platform } from '@angular/cdk/platform';
import { _RecycleViewRepeaterStrategy } from '@angular/cdk/collections';
import { Subject } from 'rxjs';
import { Subscription } from 'rxjs';
import { TemplateRef } from '@angular/core';
import { TrackByFunction } from '@angular/core';
import { ViewContainerRef } from '@angular/core';

export declare type _Bottom = {
    bottom?: number;
};

/** A virtual scroll strategy that supports fixed-size items. */
export declare class CdkFixedSizeVirtualScroll implements OnChanges {
    /** The size of the items in the list (in pixels). */
    get itemSize(): number;
    set itemSize(value: NumberInput);
    _itemSize: number;
    /**
     * The minimum amount of buffer rendered beyond the viewport (in pixels).
     * If the amount of buffer dips below this number, more items will be rendered. Defaults to 100px.
     */
    get minBufferPx(): number;
    set minBufferPx(value: NumberInput);
    _minBufferPx: number;
    /**
     * The number of pixels worth of buffer to render for when rendering new items. Defaults to 200px.
     */
    get maxBufferPx(): number;
    set maxBufferPx(value: NumberInput);
    _maxBufferPx: number;
    /** The scroll strategy used by this directive. */
    _scrollStrategy: FixedSizeVirtualScrollStrategy;
    ngOnChanges(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkFixedSizeVirtualScroll, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkFixedSizeVirtualScroll, "cdk-virtual-scroll-viewport[itemSize]", never, { "itemSize": "itemSize"; "minBufferPx": "minBufferPx"; "maxBufferPx": "maxBufferPx"; }, {}, never, never, false>;
}

/**
 * Sends an event when the directive's element is scrolled. Registers itself with the
 * ScrollDispatcher service to include itself as part of its collection of scrolling events that it
 * can be listened to through the service.
 */
export declare class CdkScrollable implements OnInit, OnDestroy {
    protected elementRef: ElementRef<HTMLElement>;
    protected scrollDispatcher: ScrollDispatcher;
    protected ngZone: NgZone;
    protected dir?: Directionality | undefined;
    protected readonly _destroyed: Subject<void>;
    protected _elementScrolled: Observable<Event>;
    constructor(elementRef: ElementRef<HTMLElement>, scrollDispatcher: ScrollDispatcher, ngZone: NgZone, dir?: Directionality | undefined);
    ngOnInit(): void;
    ngOnDestroy(): void;
    /** Returns observable that emits when a scroll event is fired on the host element. */
    elementScrolled(): Observable<Event>;
    /** Gets the ElementRef for the viewport. */
    getElementRef(): ElementRef<HTMLElement>;
    /**
     * Scrolls to the specified offsets. This is a normalized version of the browser's native scrollTo
     * method, since browsers are not consistent about what scrollLeft means in RTL. For this method
     * left and right always refer to the left and right side of the scrolling container irrespective
     * of the layout direction. start and end refer to left and right in an LTR context and vice-versa
     * in an RTL context.
     * @param options specified the offsets to scroll to.
     */
    scrollTo(options: ExtendedScrollToOptions): void;
    private _applyScrollToOptions;
    /**
     * Measures the scroll offset relative to the specified edge of the viewport. This method can be
     * used instead of directly checking scrollLeft or scrollTop, since browsers are not consistent
     * about what scrollLeft means in RTL. The values returned by this method are normalized such that
     * left and right always refer to the left and right side of the scrolling container irrespective
     * of the layout direction. start and end refer to left and right in an LTR context and vice-versa
     * in an RTL context.
     * @param from The edge to measure from.
     */
    measureScrollOffset(from: 'top' | 'left' | 'right' | 'bottom' | 'start' | 'end'): number;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkScrollable, [null, null, null, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkScrollable, "[cdk-scrollable], [cdkScrollable]", never, {}, {}, never, never, false>;
}

export declare class CdkScrollableModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkScrollableModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<CdkScrollableModule, [typeof i1.CdkScrollable], never, [typeof i1.CdkScrollable]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<CdkScrollableModule>;
}

/**
 * A directive similar to `ngForOf` to be used for rendering data inside a virtual scrolling
 * container.
 */
export declare class CdkVirtualForOf<T> implements CdkVirtualScrollRepeater<T>, CollectionViewer, DoCheck, OnDestroy {
    /** The view container to add items to. */
    private _viewContainerRef;
    /** The template to use when stamping out new items. */
    private _template;
    /** The set of available differs. */
    private _differs;
    /** The strategy used to render items in the virtual scroll viewport. */
    private _viewRepeater;
    /** The virtual scrolling viewport that these items are being rendered in. */
    private _viewport;
    /** Emits when the rendered view of the data changes. */
    readonly viewChange: Subject<ListRange>;
    /** Subject that emits when a new DataSource instance is given. */
    private readonly _dataSourceChanges;
    /** The DataSource to display. */
    get cdkVirtualForOf(): DataSource<T> | Observable<T[]> | NgIterable<T> | null | undefined;
    set cdkVirtualForOf(value: DataSource<T> | Observable<T[]> | NgIterable<T> | null | undefined);
    _cdkVirtualForOf: DataSource<T> | Observable<T[]> | NgIterable<T> | null | undefined;
    /**
     * The `TrackByFunction` to use for tracking changes. The `TrackByFunction` takes the index and
     * the item and produces a value to be used as the item's identity when tracking changes.
     */
    get cdkVirtualForTrackBy(): TrackByFunction<T> | undefined;
    set cdkVirtualForTrackBy(fn: TrackByFunction<T> | undefined);
    private _cdkVirtualForTrackBy;
    /** The template used to stamp out new elements. */
    set cdkVirtualForTemplate(value: TemplateRef<CdkVirtualForOfContext<T>>);
    /**
     * The size of the cache used to store templates that are not being used for re-use later.
     * Setting the cache size to `0` will disable caching. Defaults to 20 templates.
     */
    get cdkVirtualForTemplateCacheSize(): number;
    set cdkVirtualForTemplateCacheSize(size: NumberInput);
    /** Emits whenever the data in the current DataSource changes. */
    readonly dataStream: Observable<readonly T[]>;
    /** The differ used to calculate changes to the data. */
    private _differ;
    /** The most recent data emitted from the DataSource. */
    private _data;
    /** The currently rendered items. */
    private _renderedItems;
    /** The currently rendered range of indices. */
    private _renderedRange;
    /** Whether the rendered data should be updated during the next ngDoCheck cycle. */
    private _needsUpdate;
    private readonly _destroyed;
    constructor(
    /** The view container to add items to. */
    _viewContainerRef: ViewContainerRef, 
    /** The template to use when stamping out new items. */
    _template: TemplateRef<CdkVirtualForOfContext<T>>, 
    /** The set of available differs. */
    _differs: IterableDiffers, 
    /** The strategy used to render items in the virtual scroll viewport. */
    _viewRepeater: _RecycleViewRepeaterStrategy<T, T, CdkVirtualForOfContext<T>>, 
    /** The virtual scrolling viewport that these items are being rendered in. */
    _viewport: CdkVirtualScrollViewport, ngZone: NgZone);
    /**
     * Measures the combined size (width for horizontal orientation, height for vertical) of all items
     * in the specified range. Throws an error if the range includes items that are not currently
     * rendered.
     */
    measureRangeSize(range: ListRange, orientation: 'horizontal' | 'vertical'): number;
    ngDoCheck(): void;
    ngOnDestroy(): void;
    /** React to scroll state changes in the viewport. */
    private _onRenderedDataChange;
    /** Swap out one `DataSource` for another. */
    private _changeDataSource;
    /** Update the `CdkVirtualForOfContext` for all views. */
    private _updateContext;
    /** Apply changes to the DOM. */
    private _applyChanges;
    /** Update the computed properties on the `CdkVirtualForOfContext`. */
    private _updateComputedContextProperties;
    private _getEmbeddedViewArgs;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkVirtualForOf<any>, [null, null, null, null, { skipSelf: true; }, null]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkVirtualForOf<any>, "[cdkVirtualFor][cdkVirtualForOf]", never, { "cdkVirtualForOf": "cdkVirtualForOf"; "cdkVirtualForTrackBy": "cdkVirtualForTrackBy"; "cdkVirtualForTemplate": "cdkVirtualForTemplate"; "cdkVirtualForTemplateCacheSize": "cdkVirtualForTemplateCacheSize"; }, {}, never, never, false>;
}

/** The context for an item rendered by `CdkVirtualForOf` */
export declare type CdkVirtualForOfContext<T> = {
    /** The item value. */
    $implicit: T;
    /** The DataSource, Observable, or NgIterable that was passed to *cdkVirtualFor. */
    cdkVirtualForOf: DataSource<T> | Observable<T[]> | NgIterable<T>;
    /** The index of the item in the DataSource. */
    index: number;
    /** The number of items in the DataSource. */
    count: number;
    /** Whether this is the first item in the DataSource. */
    first: boolean;
    /** Whether this is the last item in the DataSource. */
    last: boolean;
    /** Whether the index is even. */
    even: boolean;
    /** Whether the index is odd. */
    odd: boolean;
};

/**
 * Extending the {@link CdkScrollable} to be used as scrolling container for virtual scrolling.
 */
export declare abstract class CdkVirtualScrollable extends CdkScrollable {
    constructor(elementRef: ElementRef<HTMLElement>, scrollDispatcher: ScrollDispatcher, ngZone: NgZone, dir?: Directionality);
    /**
     * Measure the viewport size for the provided orientation.
     *
     * @param orientation The orientation to measure the size from.
     */
    measureViewportSize(orientation: 'horizontal' | 'vertical'): number;
    /**
     * Measure the bounding ClientRect size including the scroll offset.
     *
     * @param from The edge to measure from.
     */
    abstract measureBoundingClientRectWithScrollOffset(from: 'left' | 'top' | 'right' | 'bottom'): number;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkVirtualScrollable, [null, null, null, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkVirtualScrollable, never, never, {}, {}, never, never, false>;
}

/**
 * Provides a virtual scrollable for the element it is attached to.
 */
export declare class CdkVirtualScrollableElement extends CdkVirtualScrollable {
    constructor(elementRef: ElementRef, scrollDispatcher: ScrollDispatcher, ngZone: NgZone, dir: Directionality);
    measureBoundingClientRectWithScrollOffset(from: 'left' | 'top' | 'right' | 'bottom'): number;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkVirtualScrollableElement, [null, null, null, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkVirtualScrollableElement, "[cdkVirtualScrollingElement]", never, {}, {}, never, never, false>;
}

/**
 * Provides as virtual scrollable for the global / window scrollbar.
 */
export declare class CdkVirtualScrollableWindow extends CdkVirtualScrollable {
    protected _elementScrolled: Observable<Event>;
    constructor(scrollDispatcher: ScrollDispatcher, ngZone: NgZone, dir: Directionality);
    measureBoundingClientRectWithScrollOffset(from: 'left' | 'top' | 'right' | 'bottom'): number;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkVirtualScrollableWindow, [null, null, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkVirtualScrollableWindow, "cdk-virtual-scroll-viewport[scrollWindow]", never, {}, {}, never, never, false>;
}

/**
 * An item to be repeated by the VirtualScrollViewport
 */
export declare interface CdkVirtualScrollRepeater<T> {
    readonly dataStream: Observable<readonly T[]>;
    measureRangeSize(range: ListRange, orientation: 'horizontal' | 'vertical'): number;
}

/** A viewport that virtualizes its scrolling with the help of `CdkVirtualForOf`. */
export declare class CdkVirtualScrollViewport extends CdkVirtualScrollable implements OnInit, OnDestroy {
    elementRef: ElementRef<HTMLElement>;
    private _changeDetectorRef;
    private _scrollStrategy;
    scrollable: CdkVirtualScrollable;
    private _platform;
    /** Emits when the viewport is detached from a CdkVirtualForOf. */
    private readonly _detachedSubject;
    /** Emits when the rendered range changes. */
    private readonly _renderedRangeSubject;
    /** The direction the viewport scrolls. */
    get orientation(): 'horizontal' | 'vertical';
    set orientation(orientation: 'horizontal' | 'vertical');
    private _orientation;
    /**
     * Whether rendered items should persist in the DOM after scrolling out of view. By default, items
     * will be removed.
     */
    get appendOnly(): boolean;
    set appendOnly(value: BooleanInput);
    private _appendOnly;
    /** Emits when the index of the first element visible in the viewport changes. */
    readonly scrolledIndexChange: Observable<number>;
    /** The element that wraps the rendered content. */
    _contentWrapper: ElementRef<HTMLElement>;
    /** A stream that emits whenever the rendered range changes. */
    readonly renderedRangeStream: Observable<ListRange>;
    /**
     * The total size of all content (in pixels), including content that is not currently rendered.
     */
    private _totalContentSize;
    /** A string representing the `style.width` property value to be used for the spacer element. */
    _totalContentWidth: string;
    /** A string representing the `style.height` property value to be used for the spacer element. */
    _totalContentHeight: string;
    /**
     * The CSS transform applied to the rendered subset of items so that they appear within the bounds
     * of the visible viewport.
     */
    private _renderedContentTransform;
    /** The currently rendered range of indices. */
    private _renderedRange;
    /** The length of the data bound to this viewport (in number of items). */
    private _dataLength;
    /** The size of the viewport (in pixels). */
    private _viewportSize;
    /** the currently attached CdkVirtualScrollRepeater. */
    private _forOf;
    /** The last rendered content offset that was set. */
    private _renderedContentOffset;
    /**
     * Whether the last rendered content offset was to the end of the content (and therefore needs to
     * be rewritten as an offset to the start of the content).
     */
    private _renderedContentOffsetNeedsRewrite;
    /** Whether there is a pending change detection cycle. */
    private _isChangeDetectionPending;
    /** A list of functions to run after the next change detection cycle. */
    private _runAfterChangeDetection;
    /** Subscription to changes in the viewport size. */
    private _viewportChanges;
    constructor(elementRef: ElementRef<HTMLElement>, _changeDetectorRef: ChangeDetectorRef, ngZone: NgZone, _scrollStrategy: VirtualScrollStrategy, dir: Directionality, scrollDispatcher: ScrollDispatcher, viewportRuler: ViewportRuler, scrollable: CdkVirtualScrollable);
    ngOnInit(): void;
    ngOnDestroy(): void;
    /** Attaches a `CdkVirtualScrollRepeater` to this viewport. */
    attach(forOf: CdkVirtualScrollRepeater<any>): void;
    /** Detaches the current `CdkVirtualForOf`. */
    detach(): void;
    /** Gets the length of the data bound to this viewport (in number of items). */
    getDataLength(): number;
    /** Gets the size of the viewport (in pixels). */
    getViewportSize(): number;
    /** Get the current rendered range of items. */
    getRenderedRange(): ListRange;
    measureBoundingClientRectWithScrollOffset(from: 'left' | 'top' | 'right' | 'bottom'): number;
    /**
     * Sets the total size of all content (in pixels), including content that is not currently
     * rendered.
     */
    setTotalContentSize(size: number): void;
    /** Sets the currently rendered range of indices. */
    setRenderedRange(range: ListRange): void;
    /**
     * Gets the offset from the start of the viewport to the start of the rendered data (in pixels).
     */
    getOffsetToRenderedContentStart(): number | null;
    /**
     * Sets the offset from the start of the viewport to either the start or end of the rendered data
     * (in pixels).
     */
    setRenderedContentOffset(offset: number, to?: 'to-start' | 'to-end'): void;
    /**
     * Scrolls to the given offset from the start of the viewport. Please note that this is not always
     * the same as setting `scrollTop` or `scrollLeft`. In a horizontal viewport with right-to-left
     * direction, this would be the equivalent of setting a fictional `scrollRight` property.
     * @param offset The offset to scroll to.
     * @param behavior The ScrollBehavior to use when scrolling. Default is behavior is `auto`.
     */
    scrollToOffset(offset: number, behavior?: ScrollBehavior): void;
    /**
     * Scrolls to the offset for the given index.
     * @param index The index of the element to scroll to.
     * @param behavior The ScrollBehavior to use when scrolling. Default is behavior is `auto`.
     */
    scrollToIndex(index: number, behavior?: ScrollBehavior): void;
    /**
     * Gets the current scroll offset from the start of the scrollable (in pixels).
     * @param from The edge to measure the offset from. Defaults to 'top' in vertical mode and 'start'
     *     in horizontal mode.
     */
    measureScrollOffset(from?: 'top' | 'left' | 'right' | 'bottom' | 'start' | 'end'): number;
    /**
     * Measures the offset of the viewport from the scrolling container
     * @param from The edge to measure from.
     */
    measureViewportOffset(from?: 'top' | 'left' | 'right' | 'bottom' | 'start' | 'end'): number;
    /** Measure the combined size of all of the rendered items. */
    measureRenderedContentSize(): number;
    /**
     * Measure the total combined size of the given range. Throws if the range includes items that are
     * not rendered.
     */
    measureRangeSize(range: ListRange): number;
    /** Update the viewport dimensions and re-render. */
    checkViewportSize(): void;
    /** Measure the viewport size. */
    private _measureViewportSize;
    /** Queue up change detection to run. */
    private _markChangeDetectionNeeded;
    /** Run change detection. */
    private _doChangeDetection;
    /** Calculates the `style.width` and `style.height` for the spacer element. */
    private _calculateSpacerSize;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkVirtualScrollViewport, [null, null, null, { optional: true; }, { optional: true; }, null, null, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<CdkVirtualScrollViewport, "cdk-virtual-scroll-viewport", never, { "orientation": "orientation"; "appendOnly": "appendOnly"; }, { "scrolledIndexChange": "scrolledIndexChange"; }, never, ["*"], false>;
}

/** Time in ms to throttle the resize events by default. */
export declare const DEFAULT_RESIZE_TIME = 20;

/** Time in ms to throttle the scrolling events by default. */
export declare const DEFAULT_SCROLL_TIME = 20;

export declare type _End = {
    end?: number;
};

/**
 * An extended version of ScrollToOptions that allows expressing scroll offsets relative to the
 * top, bottom, left, right, start, or end of the viewport rather than just the top and left.
 * Please note: the top and bottom properties are mutually exclusive, as are the left, right,
 * start, and end properties.
 */
export declare type ExtendedScrollToOptions = _XAxis & _YAxis & ScrollOptions;

/** Virtual scrolling strategy for lists with items of known fixed size. */
export declare class FixedSizeVirtualScrollStrategy implements VirtualScrollStrategy {
    private readonly _scrolledIndexChange;
    /** @docs-private Implemented as part of VirtualScrollStrategy. */
    scrolledIndexChange: Observable<number>;
    /** The attached viewport. */
    private _viewport;
    /** The size of the items in the virtually scrolling list. */
    private _itemSize;
    /** The minimum amount of buffer rendered beyond the viewport (in pixels). */
    private _minBufferPx;
    /** The number of buffer items to render beyond the edge of the viewport (in pixels). */
    private _maxBufferPx;
    /**
     * @param itemSize The size of the items in the virtually scrolling list.
     * @param minBufferPx The minimum amount of buffer (in pixels) before needing to render more
     * @param maxBufferPx The amount of buffer (in pixels) to render when rendering more.
     */
    constructor(itemSize: number, minBufferPx: number, maxBufferPx: number);
    /**
     * Attaches this scroll strategy to a viewport.
     * @param viewport The viewport to attach this strategy to.
     */
    attach(viewport: CdkVirtualScrollViewport): void;
    /** Detaches this scroll strategy from the currently attached viewport. */
    detach(): void;
    /**
     * Update the item size and buffer size.
     * @param itemSize The size of the items in the virtually scrolling list.
     * @param minBufferPx The minimum amount of buffer (in pixels) before needing to render more
     * @param maxBufferPx The amount of buffer (in pixels) to render when rendering more.
     */
    updateItemAndBufferSize(itemSize: number, minBufferPx: number, maxBufferPx: number): void;
    /** @docs-private Implemented as part of VirtualScrollStrategy. */
    onContentScrolled(): void;
    /** @docs-private Implemented as part of VirtualScrollStrategy. */
    onDataLengthChanged(): void;
    /** @docs-private Implemented as part of VirtualScrollStrategy. */
    onContentRendered(): void;
    /** @docs-private Implemented as part of VirtualScrollStrategy. */
    onRenderedOffsetChanged(): void;
    /**
     * Scroll to the offset for the given index.
     * @param index The index of the element to scroll to.
     * @param behavior The ScrollBehavior to use when scrolling.
     */
    scrollToIndex(index: number, behavior: ScrollBehavior): void;
    /** Update the viewport's total content size. */
    private _updateTotalContentSize;
    /** Update the viewport's rendered range. */
    private _updateRenderedRange;
}

/**
 * Provider factory for `FixedSizeVirtualScrollStrategy` that simply extracts the already created
 * `FixedSizeVirtualScrollStrategy` from the given directive.
 * @param fixedSizeDir The instance of `CdkFixedSizeVirtualScroll` to extract the
 *     `FixedSizeVirtualScrollStrategy` from.
 */
export declare function _fixedSizeVirtualScrollStrategyFactory(fixedSizeDir: CdkFixedSizeVirtualScroll): FixedSizeVirtualScrollStrategy;

declare namespace i1 {
    export {
        _Without,
        _XOR,
        _Top,
        _Bottom,
        _Left,
        _Right,
        _Start,
        _End,
        _XAxis,
        _YAxis,
        ExtendedScrollToOptions,
        CdkScrollable
    }
}

declare namespace i2 {
    export {
        _fixedSizeVirtualScrollStrategyFactory,
        FixedSizeVirtualScrollStrategy,
        CdkFixedSizeVirtualScroll
    }
}

declare namespace i3 {
    export {
        CdkVirtualForOfContext,
        CdkVirtualForOf
    }
}

declare namespace i4 {
    export {
        CdkVirtualScrollViewport
    }
}

declare namespace i5 {
    export {
        CdkVirtualScrollableWindow
    }
}

declare namespace i6 {
    export {
        CdkVirtualScrollableElement
    }
}

export declare type _Left = {
    left?: number;
};

export declare type _Right = {
    right?: number;
};

/**
 * Service contained all registered Scrollable references and emits an event when any one of the
 * Scrollable references emit a scrolled event.
 */
export declare class ScrollDispatcher implements OnDestroy {
    private _ngZone;
    private _platform;
    /** Used to reference correct document/window */
    protected _document: Document;
    constructor(_ngZone: NgZone, _platform: Platform, document: any);
    /** Subject for notifying that a registered scrollable reference element has been scrolled. */
    private readonly _scrolled;
    /** Keeps track of the global `scroll` and `resize` subscriptions. */
    _globalSubscription: Subscription | null;
    /** Keeps track of the amount of subscriptions to `scrolled`. Used for cleaning up afterwards. */
    private _scrolledCount;
    /**
     * Map of all the scrollable references that are registered with the service and their
     * scroll event subscriptions.
     */
    scrollContainers: Map<CdkScrollable, Subscription>;
    /**
     * Registers a scrollable instance with the service and listens for its scrolled events. When the
     * scrollable is scrolled, the service emits the event to its scrolled observable.
     * @param scrollable Scrollable instance to be registered.
     */
    register(scrollable: CdkScrollable): void;
    /**
     * De-registers a Scrollable reference and unsubscribes from its scroll event observable.
     * @param scrollable Scrollable instance to be deregistered.
     */
    deregister(scrollable: CdkScrollable): void;
    /**
     * Returns an observable that emits an event whenever any of the registered Scrollable
     * references (or window, document, or body) fire a scrolled event. Can provide a time in ms
     * to override the default "throttle" time.
     *
     * **Note:** in order to avoid hitting change detection for every scroll event,
     * all of the events emitted from this stream will be run outside the Angular zone.
     * If you need to update any data bindings as a result of a scroll event, you have
     * to run the callback using `NgZone.run`.
     */
    scrolled(auditTimeInMs?: number): Observable<CdkScrollable | void>;
    ngOnDestroy(): void;
    /**
     * Returns an observable that emits whenever any of the
     * scrollable ancestors of an element are scrolled.
     * @param elementOrElementRef Element whose ancestors to listen for.
     * @param auditTimeInMs Time to throttle the scroll events.
     */
    ancestorScrolled(elementOrElementRef: ElementRef | HTMLElement, auditTimeInMs?: number): Observable<CdkScrollable | void>;
    /** Returns all registered Scrollables that contain the provided element. */
    getAncestorScrollContainers(elementOrElementRef: ElementRef | HTMLElement): CdkScrollable[];
    /** Use defaultView of injected document if available or fallback to global window reference */
    private _getWindow;
    /** Returns true if the element is contained within the provided Scrollable. */
    private _scrollableContainsElement;
    /** Sets up the global scroll listeners. */
    private _addGlobalListener;
    /** Cleans up the global scroll listener. */
    private _removeGlobalListener;
    static ɵfac: i0.ɵɵFactoryDeclaration<ScrollDispatcher, [null, null, { optional: true; }]>;
    static ɵprov: i0.ɵɵInjectableDeclaration<ScrollDispatcher>;
}

/**
 * @docs-primary-export
 */
export declare class ScrollingModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<ScrollingModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<ScrollingModule, [typeof i2.CdkFixedSizeVirtualScroll, typeof i3.CdkVirtualForOf, typeof i4.CdkVirtualScrollViewport, typeof i5.CdkVirtualScrollableWindow, typeof i6.CdkVirtualScrollableElement], [typeof i7.BidiModule, typeof CdkScrollableModule], [typeof i7.BidiModule, typeof CdkScrollableModule, typeof i2.CdkFixedSizeVirtualScroll, typeof i3.CdkVirtualForOf, typeof i4.CdkVirtualScrollViewport, typeof i5.CdkVirtualScrollableWindow, typeof i6.CdkVirtualScrollableElement]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<ScrollingModule>;
}

export declare type _Start = {
    start?: number;
};

export declare type _Top = {
    top?: number;
};

/**
 * Simple utility for getting the bounds of the browser viewport.
 * @docs-private
 */
export declare class ViewportRuler implements OnDestroy {
    private _platform;
    /** Cached viewport dimensions. */
    private _viewportSize;
    /** Stream of viewport change events. */
    private readonly _change;
    /** Event listener that will be used to handle the viewport change events. */
    private _changeListener;
    /** Used to reference correct document/window */
    protected _document: Document;
    constructor(_platform: Platform, ngZone: NgZone, document: any);
    ngOnDestroy(): void;
    /** Returns the viewport's width and height. */
    getViewportSize(): Readonly<{
        width: number;
        height: number;
    }>;
    /** Gets a ClientRect for the viewport's bounds. */
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
    static ɵfac: i0.ɵɵFactoryDeclaration<ViewportRuler, [null, null, { optional: true; }]>;
    static ɵprov: i0.ɵɵInjectableDeclaration<ViewportRuler>;
}

/** Object that holds the scroll position of the viewport in each direction. */
export declare interface ViewportScrollPosition {
    top: number;
    left: number;
}

/** The injection token used to specify the virtual scrolling strategy. */
export declare const VIRTUAL_SCROLL_STRATEGY: InjectionToken<VirtualScrollStrategy>;

export declare const VIRTUAL_SCROLLABLE: InjectionToken<CdkVirtualScrollable>;

/** A strategy that dictates which items should be rendered in the viewport. */
export declare interface VirtualScrollStrategy {
    /** Emits when the index of the first element visible in the viewport changes. */
    scrolledIndexChange: Observable<number>;
    /**
     * Attaches this scroll strategy to a viewport.
     * @param viewport The viewport to attach this strategy to.
     */
    attach(viewport: CdkVirtualScrollViewport): void;
    /** Detaches this scroll strategy from the currently attached viewport. */
    detach(): void;
    /** Called when the viewport is scrolled (debounced using requestAnimationFrame). */
    onContentScrolled(): void;
    /** Called when the length of the data changes. */
    onDataLengthChanged(): void;
    /** Called when the range of items rendered in the DOM has changed. */
    onContentRendered(): void;
    /** Called when the offset of the rendered items changed. */
    onRenderedOffsetChanged(): void;
    /**
     * Scroll to the offset for the given index.
     * @param index The index of the element to scroll to.
     * @param behavior The ScrollBehavior to use when scrolling.
     */
    scrollToIndex(index: number, behavior: ScrollBehavior): void;
}

export declare type _Without<T> = {
    [P in keyof T]?: never;
};

export declare type _XAxis = _XOR<_XOR<_Left, _Right>, _XOR<_Start, _End>>;

export declare type _XOR<T, U> = (_Without<T> & U) | (_Without<U> & T);

export declare type _YAxis = _XOR<_Top, _Bottom>;

export { }
