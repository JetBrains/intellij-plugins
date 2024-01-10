import { CdkScrollable } from '@angular/cdk/scrolling';
import { ComponentFactoryResolver } from '@angular/core';
import { ComponentPortal } from '@angular/cdk/portal';
import { ComponentRef } from '@angular/core';
import { ComponentType } from '@angular/cdk/portal';
import { Direction } from '@angular/cdk/bidi';
import { Directionality } from '@angular/cdk/bidi';
import { ElementRef } from '@angular/core';
import { EmbeddedViewRef } from '@angular/core';
import { EventEmitter } from '@angular/core';
import * as i0 from '@angular/core';
import * as i1 from '@angular/cdk/bidi';
import * as i2 from '@angular/cdk/portal';
import * as i3 from '@angular/cdk/scrolling';
import { InjectionToken } from '@angular/core';
import { Injector } from '@angular/core';
import { Location as Location_2 } from '@angular/common';
import { NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { OnChanges } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { Platform } from '@angular/cdk/platform';
import { PortalOutlet } from '@angular/cdk/portal';
import { ScrollDispatcher } from '@angular/cdk/scrolling';
import { SimpleChanges } from '@angular/core';
import { Subject } from 'rxjs';
import { TemplatePortal } from '@angular/cdk/portal';
import { TemplateRef } from '@angular/core';
import { ViewContainerRef } from '@angular/core';
import { ViewportRuler } from '@angular/cdk/scrolling';

/**
 * Service for dispatching events that land on the body to appropriate overlay ref,
 * if any. It maintains a list of attached overlays to determine best suited overlay based
 * on event target and order of overlay opens.
 */
declare abstract class BaseOverlayDispatcher implements OnDestroy {
    /** Currently attached overlays in the order they were attached. */
    _attachedOverlays: OverlayRef[];
    protected _document: Document;
    protected _isAttached: boolean;
    constructor(document: any);
    ngOnDestroy(): void;
    /** Add a new overlay to the list of attached overlay refs. */
    add(overlayRef: OverlayRef): void;
    /** Remove an overlay from the list of attached overlay refs. */
    remove(overlayRef: OverlayRef): void;
    /** Detaches the global event listener. */
    protected abstract detach(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<BaseOverlayDispatcher, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<BaseOverlayDispatcher>;
}

/**
 * Strategy that will prevent the user from scrolling while the overlay is visible.
 */
export declare class BlockScrollStrategy implements ScrollStrategy {
    private _viewportRuler;
    private _previousHTMLStyles;
    private _previousScrollPosition;
    private _isEnabled;
    private _document;
    constructor(_viewportRuler: ViewportRuler, document: any);
    /** Attaches this scroll strategy to an overlay. */
    attach(): void;
    /** Blocks page-level scroll while the attached overlay is open. */
    enable(): void;
    /** Unblocks page-level scroll while the attached overlay is open. */
    disable(): void;
    private _canBeEnabled;
}

/** Injection token that determines the scroll handling while the connected overlay is open. */
declare const CDK_CONNECTED_OVERLAY_SCROLL_STRATEGY: InjectionToken<() => ScrollStrategy>;

/** @docs-private */
declare const CDK_CONNECTED_OVERLAY_SCROLL_STRATEGY_PROVIDER: {
    provide: InjectionToken<() => ScrollStrategy>;
    deps: (typeof Overlay)[];
    useFactory: typeof CDK_CONNECTED_OVERLAY_SCROLL_STRATEGY_PROVIDER_FACTORY;
};

/** @docs-private */
declare function CDK_CONNECTED_OVERLAY_SCROLL_STRATEGY_PROVIDER_FACTORY(overlay: Overlay): () => RepositionScrollStrategy;

/**
 * Directive to facilitate declarative creation of an
 * Overlay using a FlexibleConnectedPositionStrategy.
 */
export declare class CdkConnectedOverlay implements OnDestroy, OnChanges {
    private _overlay;
    private _dir;
    private _overlayRef;
    private _templatePortal;
    private _backdropSubscription;
    private _attachSubscription;
    private _detachSubscription;
    private _positionSubscription;
    private _offsetX;
    private _offsetY;
    private _position;
    private _scrollStrategyFactory;
    private _disposeOnNavigation;
    /** Origin for the connected overlay. */
    origin: CdkOverlayOrigin | FlexibleConnectedPositionStrategyOrigin;
    /** Registered connected position pairs. */
    positions: ConnectedPosition[];
    /**
     * This input overrides the positions input if specified. It lets users pass
     * in arbitrary positioning strategies.
     */
    positionStrategy: FlexibleConnectedPositionStrategy;
    /** The offset in pixels for the overlay connection point on the x-axis */
    get offsetX(): number;
    set offsetX(offsetX: number);
    /** The offset in pixels for the overlay connection point on the y-axis */
    get offsetY(): number;
    set offsetY(offsetY: number);
    /** The width of the overlay panel. */
    width: number | string;
    /** The height of the overlay panel. */
    height: number | string;
    /** The min width of the overlay panel. */
    minWidth: number | string;
    /** The min height of the overlay panel. */
    minHeight: number | string;
    /** The custom class to be set on the backdrop element. */
    backdropClass: string | string[];
    /** The custom class to add to the overlay pane element. */
    panelClass: string | string[];
    /** Margin between the overlay and the viewport edges. */
    viewportMargin: number;
    /** Strategy to be used when handling scroll events while the overlay is open. */
    scrollStrategy: ScrollStrategy;
    /** Whether the overlay is open. */
    open: boolean;
    /** Whether the overlay can be closed by user interaction. */
    disableClose: boolean;
    /** CSS selector which to set the transform origin. */
    transformOriginSelector: string;
    /** Whether or not the overlay should attach a backdrop. */
    hasBackdrop: boolean;
    /** Whether or not the overlay should be locked when scrolling. */
    lockPosition: boolean;
    /** Whether the overlay's width and height can be constrained to fit within the viewport. */
    flexibleDimensions: boolean;
    /** Whether the overlay can grow after the initial open when flexible positioning is turned on. */
    growAfterOpen: boolean;
    /** Whether the overlay can be pushed on-screen if none of the provided positions fit. */
    push: boolean;
    /** Whether the overlay should be disposed of when the user goes backwards/forwards in history. */
    get disposeOnNavigation(): boolean;
    set disposeOnNavigation(value: boolean);
    /** Event emitted when the backdrop is clicked. */
    readonly backdropClick: EventEmitter<MouseEvent>;
    /** Event emitted when the position has changed. */
    readonly positionChange: EventEmitter<ConnectedOverlayPositionChange>;
    /** Event emitted when the overlay has been attached. */
    readonly attach: EventEmitter<void>;
    /** Event emitted when the overlay has been detached. */
    readonly detach: EventEmitter<void>;
    /** Emits when there are keyboard events that are targeted at the overlay. */
    readonly overlayKeydown: EventEmitter<KeyboardEvent>;
    /** Emits when there are mouse outside click events that are targeted at the overlay. */
    readonly overlayOutsideClick: EventEmitter<MouseEvent>;
    constructor(_overlay: Overlay, templateRef: TemplateRef<any>, viewContainerRef: ViewContainerRef, scrollStrategyFactory: any, _dir: Directionality);
    /** The associated overlay reference. */
    get overlayRef(): OverlayRef;
    /** The element's layout direction. */
    get dir(): Direction;
    ngOnDestroy(): void;
    ngOnChanges(changes: SimpleChanges): void;
    /** Creates an overlay */
    private _createOverlay;
    /** Builds the overlay config based on the directive's inputs */
    private _buildConfig;
    /** Updates the state of a position strategy, based on the values of the directive inputs. */
    private _updatePositionStrategy;
    /** Returns the position strategy of the overlay to be set on the overlay config */
    private _createPositionStrategy;
    private _getFlexibleConnectedPositionStrategyOrigin;
    /** Attaches the overlay and subscribes to backdrop clicks if backdrop exists */
    private _attachOverlay;
    /** Detaches the overlay and unsubscribes to backdrop clicks if backdrop exists */
    private _detachOverlay;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkConnectedOverlay, [null, null, null, null, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkConnectedOverlay, "[cdk-connected-overlay], [connected-overlay], [cdkConnectedOverlay]", ["cdkConnectedOverlay"], { "origin": { "alias": "cdkConnectedOverlayOrigin"; "required": false; }; "positions": { "alias": "cdkConnectedOverlayPositions"; "required": false; }; "positionStrategy": { "alias": "cdkConnectedOverlayPositionStrategy"; "required": false; }; "offsetX": { "alias": "cdkConnectedOverlayOffsetX"; "required": false; }; "offsetY": { "alias": "cdkConnectedOverlayOffsetY"; "required": false; }; "width": { "alias": "cdkConnectedOverlayWidth"; "required": false; }; "height": { "alias": "cdkConnectedOverlayHeight"; "required": false; }; "minWidth": { "alias": "cdkConnectedOverlayMinWidth"; "required": false; }; "minHeight": { "alias": "cdkConnectedOverlayMinHeight"; "required": false; }; "backdropClass": { "alias": "cdkConnectedOverlayBackdropClass"; "required": false; }; "panelClass": { "alias": "cdkConnectedOverlayPanelClass"; "required": false; }; "viewportMargin": { "alias": "cdkConnectedOverlayViewportMargin"; "required": false; }; "scrollStrategy": { "alias": "cdkConnectedOverlayScrollStrategy"; "required": false; }; "open": { "alias": "cdkConnectedOverlayOpen"; "required": false; }; "disableClose": { "alias": "cdkConnectedOverlayDisableClose"; "required": false; }; "transformOriginSelector": { "alias": "cdkConnectedOverlayTransformOriginOn"; "required": false; }; "hasBackdrop": { "alias": "cdkConnectedOverlayHasBackdrop"; "required": false; }; "lockPosition": { "alias": "cdkConnectedOverlayLockPosition"; "required": false; }; "flexibleDimensions": { "alias": "cdkConnectedOverlayFlexibleDimensions"; "required": false; }; "growAfterOpen": { "alias": "cdkConnectedOverlayGrowAfterOpen"; "required": false; }; "push": { "alias": "cdkConnectedOverlayPush"; "required": false; }; "disposeOnNavigation": { "alias": "cdkConnectedOverlayDisposeOnNavigation"; "required": false; }; }, { "backdropClick": "backdropClick"; "positionChange": "positionChange"; "attach": "attach"; "detach": "detach"; "overlayKeydown": "overlayKeydown"; "overlayOutsideClick": "overlayOutsideClick"; }, never, never, true, never>;
    static ngAcceptInputType_hasBackdrop: unknown;
    static ngAcceptInputType_lockPosition: unknown;
    static ngAcceptInputType_flexibleDimensions: unknown;
    static ngAcceptInputType_growAfterOpen: unknown;
    static ngAcceptInputType_push: unknown;
    static ngAcceptInputType_disposeOnNavigation: unknown;
}

/**
 * Directive applied to an element to make it usable as an origin for an Overlay using a
 * ConnectedPositionStrategy.
 */
export declare class CdkOverlayOrigin {
    /** Reference to the element on which the directive is applied. */
    elementRef: ElementRef;
    constructor(
    /** Reference to the element on which the directive is applied. */
    elementRef: ElementRef);
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkOverlayOrigin, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkOverlayOrigin, "[cdk-overlay-origin], [overlay-origin], [cdkOverlayOrigin]", ["cdkOverlayOrigin"], {}, {}, never, never, true, never>;
}

export { CdkScrollable }

/**
 * Strategy that will close the overlay as soon as the user starts scrolling.
 */
export declare class CloseScrollStrategy implements ScrollStrategy {
    private _scrollDispatcher;
    private _ngZone;
    private _viewportRuler;
    private _config?;
    private _scrollSubscription;
    private _overlayRef;
    private _initialScrollPosition;
    constructor(_scrollDispatcher: ScrollDispatcher, _ngZone: NgZone, _viewportRuler: ViewportRuler, _config?: CloseScrollStrategyConfig | undefined);
    /** Attaches this scroll strategy to an overlay. */
    attach(overlayRef: OverlayRef): void;
    /** Enables the closing of the attached overlay on scroll. */
    enable(): void;
    /** Disables the closing the attached overlay on scroll. */
    disable(): void;
    detach(): void;
    /** Detaches the overlay ref and disables the scroll strategy. */
    private _detach;
}

/**
 * Config options for the CloseScrollStrategy.
 */
declare interface CloseScrollStrategyConfig {
    /** Amount of pixels the user has to scroll before the overlay is closed. */
    threshold?: number;
}

export { ComponentType }

/** The change event emitted by the strategy when a fallback position is used. */
export declare class ConnectedOverlayPositionChange {
    /** The position used as a result of this change. */
    connectionPair: ConnectionPositionPair;
    /** @docs-private */
    scrollableViewProperties: ScrollingVisibility;
    constructor(
    /** The position used as a result of this change. */
    connectionPair: ConnectionPositionPair, 
    /** @docs-private */
    scrollableViewProperties: ScrollingVisibility);
}

/** A connected position as specified by the user. */
export declare interface ConnectedPosition {
    originX: 'start' | 'center' | 'end';
    originY: 'top' | 'center' | 'bottom';
    overlayX: 'start' | 'center' | 'end';
    overlayY: 'top' | 'center' | 'bottom';
    weight?: number;
    offsetX?: number;
    offsetY?: number;
    panelClass?: string | string[];
}

/** The points of the origin element and the overlay element to connect. */
export declare class ConnectionPositionPair {
    /** Offset along the X axis. */
    offsetX?: number | undefined;
    /** Offset along the Y axis. */
    offsetY?: number | undefined;
    /** Class(es) to be applied to the panel while this position is active. */
    panelClass?: string | string[] | undefined;
    /** X-axis attachment point for connected overlay origin. Can be 'start', 'end', or 'center'. */
    originX: HorizontalConnectionPos;
    /** Y-axis attachment point for connected overlay origin. Can be 'top', 'bottom', or 'center'. */
    originY: VerticalConnectionPos;
    /** X-axis attachment point for connected overlay. Can be 'start', 'end', or 'center'. */
    overlayX: HorizontalConnectionPos;
    /** Y-axis attachment point for connected overlay. Can be 'top', 'bottom', or 'center'. */
    overlayY: VerticalConnectionPos;
    constructor(origin: OriginConnectionPosition, overlay: OverlayConnectionPosition, 
    /** Offset along the X axis. */
    offsetX?: number | undefined, 
    /** Offset along the Y axis. */
    offsetY?: number | undefined, 
    /** Class(es) to be applied to the panel while this position is active. */
    panelClass?: string | string[] | undefined);
}

/**
 * A strategy for positioning overlays. Using this strategy, an overlay is given an
 * implicit position relative some origin element. The relative position is defined in terms of
 * a point on the origin element that is connected to a point on the overlay element. For example,
 * a basic dropdown is connecting the bottom-left corner of the origin to the top-left corner
 * of the overlay.
 */
export declare class FlexibleConnectedPositionStrategy implements PositionStrategy {
    private _viewportRuler;
    private _document;
    private _platform;
    private _overlayContainer;
    /** The overlay to which this strategy is attached. */
    private _overlayRef;
    /** Whether we're performing the very first positioning of the overlay. */
    private _isInitialRender;
    /** Last size used for the bounding box. Used to avoid resizing the overlay after open. */
    private _lastBoundingBoxSize;
    /** Whether the overlay was pushed in a previous positioning. */
    private _isPushed;
    /** Whether the overlay can be pushed on-screen on the initial open. */
    private _canPush;
    /** Whether the overlay can grow via flexible width/height after the initial open. */
    private _growAfterOpen;
    /** Whether the overlay's width and height can be constrained to fit within the viewport. */
    private _hasFlexibleDimensions;
    /** Whether the overlay position is locked. */
    private _positionLocked;
    /** Cached origin dimensions */
    private _originRect;
    /** Cached overlay dimensions */
    private _overlayRect;
    /** Cached viewport dimensions */
    private _viewportRect;
    /** Cached container dimensions */
    private _containerRect;
    /** Amount of space that must be maintained between the overlay and the edge of the viewport. */
    private _viewportMargin;
    /** The Scrollable containers used to check scrollable view properties on position change. */
    private _scrollables;
    /** Ordered list of preferred positions, from most to least desirable. */
    _preferredPositions: ConnectionPositionPair[];
    /** The origin element against which the overlay will be positioned. */
    _origin: FlexibleConnectedPositionStrategyOrigin;
    /** The overlay pane element. */
    private _pane;
    /** Whether the strategy has been disposed of already. */
    private _isDisposed;
    /**
     * Parent element for the overlay panel used to constrain the overlay panel's size to fit
     * within the viewport.
     */
    private _boundingBox;
    /** The last position to have been calculated as the best fit position. */
    private _lastPosition;
    /** Subject that emits whenever the position changes. */
    private readonly _positionChanges;
    /** Subscription to viewport size changes. */
    private _resizeSubscription;
    /** Default offset for the overlay along the x axis. */
    private _offsetX;
    /** Default offset for the overlay along the y axis. */
    private _offsetY;
    /** Selector to be used when finding the elements on which to set the transform origin. */
    private _transformOriginSelector;
    /** Keeps track of the CSS classes that the position strategy has applied on the overlay panel. */
    private _appliedPanelClasses;
    /** Amount by which the overlay was pushed in each axis during the last time it was positioned. */
    private _previousPushAmount;
    /** Observable sequence of position changes. */
    positionChanges: Observable<ConnectedOverlayPositionChange>;
    /** Ordered list of preferred positions, from most to least desirable. */
    get positions(): ConnectionPositionPair[];
    constructor(connectedTo: FlexibleConnectedPositionStrategyOrigin, _viewportRuler: ViewportRuler, _document: Document, _platform: Platform, _overlayContainer: OverlayContainer);
    /** Attaches this position strategy to an overlay. */
    attach(overlayRef: OverlayRef): void;
    /**
     * Updates the position of the overlay element, using whichever preferred position relative
     * to the origin best fits on-screen.
     *
     * The selection of a position goes as follows:
     *  - If any positions fit completely within the viewport as-is,
     *      choose the first position that does so.
     *  - If flexible dimensions are enabled and at least one satisfies the given minimum width/height,
     *      choose the position with the greatest available size modified by the positions' weight.
     *  - If pushing is enabled, take the position that went off-screen the least and push it
     *      on-screen.
     *  - If none of the previous criteria were met, use the position that goes off-screen the least.
     * @docs-private
     */
    apply(): void;
    detach(): void;
    /** Cleanup after the element gets destroyed. */
    dispose(): void;
    /**
     * This re-aligns the overlay element with the trigger in its last calculated position,
     * even if a position higher in the "preferred positions" list would now fit. This
     * allows one to re-align the panel without changing the orientation of the panel.
     */
    reapplyLastPosition(): void;
    /**
     * Sets the list of Scrollable containers that host the origin element so that
     * on reposition we can evaluate if it or the overlay has been clipped or outside view. Every
     * Scrollable must be an ancestor element of the strategy's origin element.
     */
    withScrollableContainers(scrollables: CdkScrollable[]): this;
    /**
     * Adds new preferred positions.
     * @param positions List of positions options for this overlay.
     */
    withPositions(positions: ConnectedPosition[]): this;
    /**
     * Sets a minimum distance the overlay may be positioned to the edge of the viewport.
     * @param margin Required margin between the overlay and the viewport edge in pixels.
     */
    withViewportMargin(margin: number): this;
    /** Sets whether the overlay's width and height can be constrained to fit within the viewport. */
    withFlexibleDimensions(flexibleDimensions?: boolean): this;
    /** Sets whether the overlay can grow after the initial open via flexible width/height. */
    withGrowAfterOpen(growAfterOpen?: boolean): this;
    /** Sets whether the overlay can be pushed on-screen if none of the provided positions fit. */
    withPush(canPush?: boolean): this;
    /**
     * Sets whether the overlay's position should be locked in after it is positioned
     * initially. When an overlay is locked in, it won't attempt to reposition itself
     * when the position is re-applied (e.g. when the user scrolls away).
     * @param isLocked Whether the overlay should locked in.
     */
    withLockedPosition(isLocked?: boolean): this;
    /**
     * Sets the origin, relative to which to position the overlay.
     * Using an element origin is useful for building components that need to be positioned
     * relatively to a trigger (e.g. dropdown menus or tooltips), whereas using a point can be
     * used for cases like contextual menus which open relative to the user's pointer.
     * @param origin Reference to the new origin.
     */
    setOrigin(origin: FlexibleConnectedPositionStrategyOrigin): this;
    /**
     * Sets the default offset for the overlay's connection point on the x-axis.
     * @param offset New offset in the X axis.
     */
    withDefaultOffsetX(offset: number): this;
    /**
     * Sets the default offset for the overlay's connection point on the y-axis.
     * @param offset New offset in the Y axis.
     */
    withDefaultOffsetY(offset: number): this;
    /**
     * Configures that the position strategy should set a `transform-origin` on some elements
     * inside the overlay, depending on the current position that is being applied. This is
     * useful for the cases where the origin of an animation can change depending on the
     * alignment of the overlay.
     * @param selector CSS selector that will be used to find the target
     *    elements onto which to set the transform origin.
     */
    withTransformOriginOn(selector: string): this;
    /**
     * Gets the (x, y) coordinate of a connection point on the origin based on a relative position.
     */
    private _getOriginPoint;
    /**
     * Gets the (x, y) coordinate of the top-left corner of the overlay given a given position and
     * origin point to which the overlay should be connected.
     */
    private _getOverlayPoint;
    /** Gets how well an overlay at the given point will fit within the viewport. */
    private _getOverlayFit;
    /**
     * Whether the overlay can fit within the viewport when it may resize either its width or height.
     * @param fit How well the overlay fits in the viewport at some position.
     * @param point The (x, y) coordinates of the overlay at some position.
     * @param viewport The geometry of the viewport.
     */
    private _canFitWithFlexibleDimensions;
    /**
     * Gets the point at which the overlay can be "pushed" on-screen. If the overlay is larger than
     * the viewport, the top-left corner will be pushed on-screen (with overflow occurring on the
     * right and bottom).
     *
     * @param start Starting point from which the overlay is pushed.
     * @param rawOverlayRect Dimensions of the overlay.
     * @param scrollPosition Current viewport scroll position.
     * @returns The point at which to position the overlay after pushing. This is effectively a new
     *     originPoint.
     */
    private _pushOverlayOnScreen;
    /**
     * Applies a computed position to the overlay and emits a position change.
     * @param position The position preference
     * @param originPoint The point on the origin element where the overlay is connected.
     */
    private _applyPosition;
    /** Sets the transform origin based on the configured selector and the passed-in position.  */
    private _setTransformOrigin;
    /**
     * Gets the position and size of the overlay's sizing container.
     *
     * This method does no measuring and applies no styles so that we can cheaply compute the
     * bounds for all positions and choose the best fit based on these results.
     */
    private _calculateBoundingBoxRect;
    /**
     * Sets the position and size of the overlay's sizing wrapper. The wrapper is positioned on the
     * origin's connection point and stretches to the bounds of the viewport.
     *
     * @param origin The point on the origin element where the overlay is connected.
     * @param position The position preference
     */
    private _setBoundingBoxStyles;
    /** Resets the styles for the bounding box so that a new positioning can be computed. */
    private _resetBoundingBoxStyles;
    /** Resets the styles for the overlay pane so that a new positioning can be computed. */
    private _resetOverlayElementStyles;
    /** Sets positioning styles to the overlay element. */
    private _setOverlayElementStyles;
    /** Gets the exact top/bottom for the overlay when not using flexible sizing or when pushing. */
    private _getExactOverlayY;
    /** Gets the exact left/right for the overlay when not using flexible sizing or when pushing. */
    private _getExactOverlayX;
    /**
     * Gets the view properties of the trigger and overlay, including whether they are clipped
     * or completely outside the view of any of the strategy's scrollables.
     */
    private _getScrollVisibility;
    /** Subtracts the amount that an element is overflowing on an axis from its length. */
    private _subtractOverflows;
    /** Narrows the given viewport rect by the current _viewportMargin. */
    private _getNarrowedViewportRect;
    /** Whether the we're dealing with an RTL context */
    private _isRtl;
    /** Determines whether the overlay uses exact or flexible positioning. */
    private _hasExactPosition;
    /** Retrieves the offset of a position along the x or y axis. */
    private _getOffset;
    /** Validates that the current position match the expected values. */
    private _validatePositions;
    /** Adds a single CSS class or an array of classes on the overlay panel. */
    private _addPanelClasses;
    /** Clears the classes that the position strategy has applied from the overlay panel. */
    private _clearPanelClasses;
    /** Returns the ClientRect of the current origin. */
    private _getOriginRect;
}

/** Possible values that can be set as the origin of a FlexibleConnectedPositionStrategy. */
export declare type FlexibleConnectedPositionStrategyOrigin = ElementRef | Element | (Point & {
    width?: number;
    height?: number;
});

/**
 * Alternative to OverlayContainer that supports correct displaying of overlay elements in
 * Fullscreen mode
 * https://developer.mozilla.org/en-US/docs/Web/API/Element/requestFullScreen
 *
 * Should be provided in the root component.
 */
export declare class FullscreenOverlayContainer extends OverlayContainer implements OnDestroy {
    private _fullScreenEventName;
    private _fullScreenListener;
    constructor(_document: any, platform: Platform);
    ngOnDestroy(): void;
    protected _createContainer(): void;
    private _adjustParentForFullscreenChange;
    private _addFullscreenChangeListener;
    private _getEventName;
    /**
     * When the page is put into fullscreen mode, a specific element is specified.
     * Only that element and its children are visible when in fullscreen mode.
     */
    getFullscreenElement(): Element;
    static ɵfac: i0.ɵɵFactoryDeclaration<FullscreenOverlayContainer, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<FullscreenOverlayContainer>;
}

/**
 * A strategy for positioning overlays. Using this strategy, an overlay is given an
 * explicit position relative to the browser's viewport. We use flexbox, instead of
 * transforms, in order to avoid issues with subpixel rendering which can cause the
 * element to become blurry.
 */
export declare class GlobalPositionStrategy implements PositionStrategy {
    /** The overlay to which this strategy is attached. */
    private _overlayRef;
    private _cssPosition;
    private _topOffset;
    private _bottomOffset;
    private _alignItems;
    private _xPosition;
    private _xOffset;
    private _width;
    private _height;
    private _isDisposed;
    attach(overlayRef: OverlayRef): void;
    /**
     * Sets the top position of the overlay. Clears any previously set vertical position.
     * @param value New top offset.
     */
    top(value?: string): this;
    /**
     * Sets the left position of the overlay. Clears any previously set horizontal position.
     * @param value New left offset.
     */
    left(value?: string): this;
    /**
     * Sets the bottom position of the overlay. Clears any previously set vertical position.
     * @param value New bottom offset.
     */
    bottom(value?: string): this;
    /**
     * Sets the right position of the overlay. Clears any previously set horizontal position.
     * @param value New right offset.
     */
    right(value?: string): this;
    /**
     * Sets the overlay to the start of the viewport, depending on the overlay direction.
     * This will be to the left in LTR layouts and to the right in RTL.
     * @param offset Offset from the edge of the screen.
     */
    start(value?: string): this;
    /**
     * Sets the overlay to the end of the viewport, depending on the overlay direction.
     * This will be to the right in LTR layouts and to the left in RTL.
     * @param offset Offset from the edge of the screen.
     */
    end(value?: string): this;
    /**
     * Sets the overlay width and clears any previously set width.
     * @param value New width for the overlay
     * @deprecated Pass the `width` through the `OverlayConfig`.
     * @breaking-change 8.0.0
     */
    width(value?: string): this;
    /**
     * Sets the overlay height and clears any previously set height.
     * @param value New height for the overlay
     * @deprecated Pass the `height` through the `OverlayConfig`.
     * @breaking-change 8.0.0
     */
    height(value?: string): this;
    /**
     * Centers the overlay horizontally with an optional offset.
     * Clears any previously set horizontal position.
     *
     * @param offset Overlay offset from the horizontal center.
     */
    centerHorizontally(offset?: string): this;
    /**
     * Centers the overlay vertically with an optional offset.
     * Clears any previously set vertical position.
     *
     * @param offset Overlay offset from the vertical center.
     */
    centerVertically(offset?: string): this;
    /**
     * Apply the position to the element.
     * @docs-private
     */
    apply(): void;
    /**
     * Cleans up the DOM changes from the position strategy.
     * @docs-private
     */
    dispose(): void;
}


/** Horizontal dimension of a connection point on the perimeter of the origin or overlay element. */
export declare type HorizontalConnectionPos = 'start' | 'center' | 'end';

declare namespace i4 {
    export {
        CDK_CONNECTED_OVERLAY_SCROLL_STRATEGY_PROVIDER_FACTORY,
        CDK_CONNECTED_OVERLAY_SCROLL_STRATEGY,
        CdkOverlayOrigin,
        CdkConnectedOverlay,
        CDK_CONNECTED_OVERLAY_SCROLL_STRATEGY_PROVIDER
    }
}

/** An object where all of its properties cannot be written. */
declare type ImmutableObject<T> = {
    readonly [P in keyof T]: T[P];
};

/** Scroll strategy that doesn't do anything. */
export declare class NoopScrollStrategy implements ScrollStrategy {
    /** Does nothing, as this scroll strategy is a no-op. */
    enable(): void;
    /** Does nothing, as this scroll strategy is a no-op. */
    disable(): void;
    /** Does nothing, as this scroll strategy is a no-op. */
    attach(): void;
}

/** A connection point on the origin element. */
export declare interface OriginConnectionPosition {
    originX: HorizontalConnectionPos;
    originY: VerticalConnectionPos;
}

/**
 * Service to create Overlays. Overlays are dynamically added pieces of floating UI, meant to be
 * used as a low-level building block for other components. Dialogs, tooltips, menus,
 * selects, etc. can all be built using overlays. The service should primarily be used by authors
 * of re-usable components rather than developers building end-user applications.
 *
 * An overlay *is* a PortalOutlet, so any kind of Portal can be loaded into one.
 */
export declare class Overlay {
    /** Scrolling strategies that can be used when creating an overlay. */
    scrollStrategies: ScrollStrategyOptions;
    private _overlayContainer;
    private _componentFactoryResolver;
    private _positionBuilder;
    private _keyboardDispatcher;
    private _injector;
    private _ngZone;
    private _document;
    private _directionality;
    private _location;
    private _outsideClickDispatcher;
    private _animationsModuleType?;
    private _appRef;
    constructor(
    /** Scrolling strategies that can be used when creating an overlay. */
    scrollStrategies: ScrollStrategyOptions, _overlayContainer: OverlayContainer, _componentFactoryResolver: ComponentFactoryResolver, _positionBuilder: OverlayPositionBuilder, _keyboardDispatcher: OverlayKeyboardDispatcher, _injector: Injector, _ngZone: NgZone, _document: any, _directionality: Directionality, _location: Location_2, _outsideClickDispatcher: OverlayOutsideClickDispatcher, _animationsModuleType?: string | undefined);
    /**
     * Creates an overlay.
     * @param config Configuration applied to the overlay.
     * @returns Reference to the created overlay.
     */
    create(config?: OverlayConfig): OverlayRef;
    /**
     * Gets a position builder that can be used, via fluent API,
     * to construct and configure a position strategy.
     * @returns An overlay position builder.
     */
    position(): OverlayPositionBuilder;
    /**
     * Creates the DOM element for an overlay and appends it to the overlay container.
     * @returns Newly-created pane element
     */
    private _createPaneElement;
    /**
     * Creates the host element that wraps around an overlay
     * and can be used for advanced positioning.
     * @returns Newly-create host element.
     */
    private _createHostElement;
    /**
     * Create a DomPortalOutlet into which the overlay content can be loaded.
     * @param pane The DOM element to turn into a portal outlet.
     * @returns A portal outlet for the given DOM element.
     */
    private _createPortalOutlet;
    static ɵfac: i0.ɵɵFactoryDeclaration<Overlay, [null, null, null, null, null, null, null, null, null, null, null, { optional: true; }]>;
    static ɵprov: i0.ɵɵInjectableDeclaration<Overlay>;
}

/** Initial configuration used when creating an overlay. */
export declare class OverlayConfig {
    /** Strategy with which to position the overlay. */
    positionStrategy?: PositionStrategy;
    /** Strategy to be used when handling scroll events while the overlay is open. */
    scrollStrategy?: ScrollStrategy;
    /** Custom class to add to the overlay pane. */
    panelClass?: string | string[];
    /** Whether the overlay has a backdrop. */
    hasBackdrop?: boolean;
    /** Custom class to add to the backdrop */
    backdropClass?: string | string[];
    /** The width of the overlay panel. If a number is provided, pixel units are assumed. */
    width?: number | string;
    /** The height of the overlay panel. If a number is provided, pixel units are assumed. */
    height?: number | string;
    /** The min-width of the overlay panel. If a number is provided, pixel units are assumed. */
    minWidth?: number | string;
    /** The min-height of the overlay panel. If a number is provided, pixel units are assumed. */
    minHeight?: number | string;
    /** The max-width of the overlay panel. If a number is provided, pixel units are assumed. */
    maxWidth?: number | string;
    /** The max-height of the overlay panel. If a number is provided, pixel units are assumed. */
    maxHeight?: number | string;
    /**
     * Direction of the text in the overlay panel. If a `Directionality` instance
     * is passed in, the overlay will handle changes to its value automatically.
     */
    direction?: Direction | Directionality;
    /**
     * Whether the overlay should be disposed of when the user goes backwards/forwards in history.
     * Note that this usually doesn't include clicking on links (unless the user is using
     * the `HashLocationStrategy`).
     */
    disposeOnNavigation?: boolean;
    constructor(config?: OverlayConfig);
}

/** A connection point on the overlay element. */
export declare interface OverlayConnectionPosition {
    overlayX: HorizontalConnectionPos;
    overlayY: VerticalConnectionPos;
}

/** Container inside which all overlays will render. */
export declare class OverlayContainer implements OnDestroy {
    protected _platform: Platform;
    protected _containerElement: HTMLElement;
    protected _document: Document;
    constructor(document: any, _platform: Platform);
    ngOnDestroy(): void;
    /**
     * This method returns the overlay container element. It will lazily
     * create the element the first time it is called to facilitate using
     * the container in non-browser environments.
     * @returns the container element
     */
    getContainerElement(): HTMLElement;
    /**
     * Create the overlay container element, which is simply a div
     * with the 'cdk-overlay-container' class on the document body.
     */
    protected _createContainer(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<OverlayContainer, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<OverlayContainer>;
}

/**
 * Service for dispatching keyboard events that land on the body to appropriate overlay ref,
 * if any. It maintains a list of attached overlays to determine best suited overlay based
 * on event target and order of overlay opens.
 */
export declare class OverlayKeyboardDispatcher extends BaseOverlayDispatcher {
    /** @breaking-change 14.0.0 _ngZone will be required. */
    private _ngZone?;
    constructor(document: any, 
    /** @breaking-change 14.0.0 _ngZone will be required. */
    _ngZone?: NgZone | undefined);
    /** Add a new overlay to the list of attached overlay refs. */
    add(overlayRef: OverlayRef): void;
    /** Detaches the global keyboard event listener. */
    protected detach(): void;
    /** Keyboard event listener that will be attached to the body. */
    private _keydownListener;
    static ɵfac: i0.ɵɵFactoryDeclaration<OverlayKeyboardDispatcher, [null, { optional: true; }]>;
    static ɵprov: i0.ɵɵInjectableDeclaration<OverlayKeyboardDispatcher>;
}

export declare class OverlayModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<OverlayModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<OverlayModule, never, [typeof i1.BidiModule, typeof i2.PortalModule, typeof i3.ScrollingModule, typeof i4.CdkConnectedOverlay, typeof i4.CdkOverlayOrigin], [typeof i4.CdkConnectedOverlay, typeof i4.CdkOverlayOrigin, typeof i3.ScrollingModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<OverlayModule>;
}

/**
 * Service for dispatching mouse click events that land on the body to appropriate overlay ref,
 * if any. It maintains a list of attached overlays to determine best suited overlay based
 * on event target and order of overlay opens.
 */
export declare class OverlayOutsideClickDispatcher extends BaseOverlayDispatcher {
    private _platform;
    /** @breaking-change 14.0.0 _ngZone will be required. */
    private _ngZone?;
    private _cursorOriginalValue;
    private _cursorStyleIsSet;
    private _pointerDownEventTarget;
    constructor(document: any, _platform: Platform, 
    /** @breaking-change 14.0.0 _ngZone will be required. */
    _ngZone?: NgZone | undefined);
    /** Add a new overlay to the list of attached overlay refs. */
    add(overlayRef: OverlayRef): void;
    /** Detaches the global keyboard event listener. */
    protected detach(): void;
    private _addEventListeners;
    /** Store pointerdown event target to track origin of click. */
    private _pointerDownListener;
    /** Click event listener that will be attached to the body propagate phase. */
    private _clickListener;
    static ɵfac: i0.ɵɵFactoryDeclaration<OverlayOutsideClickDispatcher, [null, null, { optional: true; }]>;
    static ɵprov: i0.ɵɵInjectableDeclaration<OverlayOutsideClickDispatcher>;
}

/** Builder for overlay position strategy. */
export declare class OverlayPositionBuilder {
    private _viewportRuler;
    private _document;
    private _platform;
    private _overlayContainer;
    constructor(_viewportRuler: ViewportRuler, _document: any, _platform: Platform, _overlayContainer: OverlayContainer);
    /**
     * Creates a global position strategy.
     */
    global(): GlobalPositionStrategy;
    /**
     * Creates a flexible position strategy.
     * @param origin Origin relative to which to position the overlay.
     */
    flexibleConnectedTo(origin: FlexibleConnectedPositionStrategyOrigin): FlexibleConnectedPositionStrategy;
    static ɵfac: i0.ɵɵFactoryDeclaration<OverlayPositionBuilder, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<OverlayPositionBuilder>;
}

/**
 * Reference to an overlay that has been created with the Overlay service.
 * Used to manipulate or dispose of said overlay.
 */
export declare class OverlayRef implements PortalOutlet {
    private _portalOutlet;
    private _host;
    private _pane;
    private _config;
    private _ngZone;
    private _keyboardDispatcher;
    private _document;
    private _location;
    private _outsideClickDispatcher;
    private _animationsDisabled;
    private _backdropElement;
    private _backdropTimeout;
    private readonly _backdropClick;
    private readonly _attachments;
    private readonly _detachments;
    private _positionStrategy;
    private _scrollStrategy;
    private _locationChanges;
    private _backdropClickHandler;
    private _backdropTransitionendHandler;
    /**
     * Reference to the parent of the `_host` at the time it was detached. Used to restore
     * the `_host` to its original position in the DOM when it gets re-attached.
     */
    private _previousHostParent;
    /** Stream of keydown events dispatched to this overlay. */
    readonly _keydownEvents: Subject<KeyboardEvent>;
    /** Stream of mouse outside events dispatched to this overlay. */
    readonly _outsidePointerEvents: Subject<MouseEvent>;
    constructor(_portalOutlet: PortalOutlet, _host: HTMLElement, _pane: HTMLElement, _config: ImmutableObject<OverlayConfig>, _ngZone: NgZone, _keyboardDispatcher: OverlayKeyboardDispatcher, _document: Document, _location: Location_2, _outsideClickDispatcher: OverlayOutsideClickDispatcher, _animationsDisabled?: boolean);
    /** The overlay's HTML element */
    get overlayElement(): HTMLElement;
    /** The overlay's backdrop HTML element. */
    get backdropElement(): HTMLElement | null;
    /**
     * Wrapper around the panel element. Can be used for advanced
     * positioning where a wrapper with specific styling is
     * required around the overlay pane.
     */
    get hostElement(): HTMLElement;
    attach<T>(portal: ComponentPortal<T>): ComponentRef<T>;
    attach<T>(portal: TemplatePortal<T>): EmbeddedViewRef<T>;
    attach(portal: any): any;
    /**
     * Detaches an overlay from a portal.
     * @returns The portal detachment result.
     */
    detach(): any;
    /** Cleans up the overlay from the DOM. */
    dispose(): void;
    /** Whether the overlay has attached content. */
    hasAttached(): boolean;
    /** Gets an observable that emits when the backdrop has been clicked. */
    backdropClick(): Observable<MouseEvent>;
    /** Gets an observable that emits when the overlay has been attached. */
    attachments(): Observable<void>;
    /** Gets an observable that emits when the overlay has been detached. */
    detachments(): Observable<void>;
    /** Gets an observable of keydown events targeted to this overlay. */
    keydownEvents(): Observable<KeyboardEvent>;
    /** Gets an observable of pointer events targeted outside this overlay. */
    outsidePointerEvents(): Observable<MouseEvent>;
    /** Gets the current overlay configuration, which is immutable. */
    getConfig(): OverlayConfig;
    /** Updates the position of the overlay based on the position strategy. */
    updatePosition(): void;
    /** Switches to a new position strategy and updates the overlay position. */
    updatePositionStrategy(strategy: PositionStrategy): void;
    /** Update the size properties of the overlay. */
    updateSize(sizeConfig: OverlaySizeConfig): void;
    /** Sets the LTR/RTL direction for the overlay. */
    setDirection(dir: Direction | Directionality): void;
    /** Add a CSS class or an array of classes to the overlay pane. */
    addPanelClass(classes: string | string[]): void;
    /** Remove a CSS class or an array of classes from the overlay pane. */
    removePanelClass(classes: string | string[]): void;
    /**
     * Returns the layout direction of the overlay panel.
     */
    getDirection(): Direction;
    /** Switches to a new scroll strategy. */
    updateScrollStrategy(strategy: ScrollStrategy): void;
    /** Updates the text direction of the overlay panel. */
    private _updateElementDirection;
    /** Updates the size of the overlay element based on the overlay config. */
    private _updateElementSize;
    /** Toggles the pointer events for the overlay pane element. */
    private _togglePointerEvents;
    /** Attaches a backdrop for this overlay. */
    private _attachBackdrop;
    /**
     * Updates the stacking order of the element, moving it to the top if necessary.
     * This is required in cases where one overlay was detached, while another one,
     * that should be behind it, was destroyed. The next time both of them are opened,
     * the stacking will be wrong, because the detached element's pane will still be
     * in its original DOM position.
     */
    private _updateStackingOrder;
    /** Detaches the backdrop (if any) associated with the overlay. */
    detachBackdrop(): void;
    /** Toggles a single CSS class or an array of classes on an element. */
    private _toggleClasses;
    /** Detaches the overlay content next time the zone stabilizes. */
    private _detachContentWhenStable;
    /** Disposes of a scroll strategy. */
    private _disposeScrollStrategy;
    /** Removes a backdrop element from the DOM. */
    private _disposeBackdrop;
}

/** Size properties for an overlay. */
export declare interface OverlaySizeConfig {
    width?: number | string;
    height?: number | string;
    minWidth?: number | string;
    minHeight?: number | string;
    maxWidth?: number | string;
    maxHeight?: number | string;
}

/** A simple (x, y) coordinate. */
declare interface Point {
    x: number;
    y: number;
}

/** Strategy for setting the position on an overlay. */
export declare interface PositionStrategy {
    /** Attaches this position strategy to an overlay. */
    attach(overlayRef: OverlayRef): void;
    /** Updates the position of the overlay element. */
    apply(): void;
    /** Called when the overlay is detached. */
    detach?(): void;
    /** Cleans up any DOM modifications made by the position strategy, if necessary. */
    dispose(): void;
}

/**
 * Strategy that will update the element position as the user is scrolling.
 */
export declare class RepositionScrollStrategy implements ScrollStrategy {
    private _scrollDispatcher;
    private _viewportRuler;
    private _ngZone;
    private _config?;
    private _scrollSubscription;
    private _overlayRef;
    constructor(_scrollDispatcher: ScrollDispatcher, _viewportRuler: ViewportRuler, _ngZone: NgZone, _config?: RepositionScrollStrategyConfig | undefined);
    /** Attaches this scroll strategy to an overlay. */
    attach(overlayRef: OverlayRef): void;
    /** Enables repositioning of the attached overlay on scroll. */
    enable(): void;
    /** Disables repositioning of the attached overlay on scroll. */
    disable(): void;
    detach(): void;
}

/**
 * Config options for the RepositionScrollStrategy.
 */
export declare interface RepositionScrollStrategyConfig {
    /** Time in milliseconds to throttle the scroll events. */
    scrollThrottle?: number;
    /** Whether to close the overlay once the user has scrolled away completely. */
    autoClose?: boolean;
}

export { ScrollDispatcher }

/**
 * Set of properties regarding the position of the origin and overlay relative to the viewport
 * with respect to the containing Scrollable elements.
 *
 * The overlay and origin are clipped if any part of their bounding client rectangle exceeds the
 * bounds of any one of the strategy's Scrollable's bounding client rectangle.
 *
 * The overlay and origin are outside view if there is no overlap between their bounding client
 * rectangle and any one of the strategy's Scrollable's bounding client rectangle.
 *
 *       -----------                    -----------
 *       | outside |                    | clipped |
 *       |  view   |              --------------------------
 *       |         |              |     |         |        |
 *       ----------               |     -----------        |
 *  --------------------------    |                        |
 *  |                        |    |      Scrollable        |
 *  |                        |    |                        |
 *  |                        |     --------------------------
 *  |      Scrollable        |
 *  |                        |
 *  --------------------------
 *
 *  @docs-private
 */
export declare class ScrollingVisibility {
    isOriginClipped: boolean;
    isOriginOutsideView: boolean;
    isOverlayClipped: boolean;
    isOverlayOutsideView: boolean;
}

/**
 * Describes a strategy that will be used by an overlay to handle scroll events while it is open.
 */
export declare interface ScrollStrategy {
    /** Enable this scroll strategy (called when the attached overlay is attached to a portal). */
    enable: () => void;
    /** Disable this scroll strategy (called when the attached overlay is detached from a portal). */
    disable: () => void;
    /** Attaches this `ScrollStrategy` to an overlay. */
    attach: (overlayRef: OverlayRef) => void;
    /** Detaches the scroll strategy from the current overlay. */
    detach?: () => void;
}

/**
 * Options for how an overlay will handle scrolling.
 *
 * Users can provide a custom value for `ScrollStrategyOptions` to replace the default
 * behaviors. This class primarily acts as a factory for ScrollStrategy instances.
 */
export declare class ScrollStrategyOptions {
    private _scrollDispatcher;
    private _viewportRuler;
    private _ngZone;
    private _document;
    constructor(_scrollDispatcher: ScrollDispatcher, _viewportRuler: ViewportRuler, _ngZone: NgZone, document: any);
    /** Do nothing on scroll. */
    noop: () => NoopScrollStrategy;
    /**
     * Close the overlay as soon as the user scrolls.
     * @param config Configuration to be used inside the scroll strategy.
     */
    close: (config?: CloseScrollStrategyConfig) => CloseScrollStrategy;
    /** Block scrolling. */
    block: () => BlockScrollStrategy;
    /**
     * Update the overlay's position on scroll.
     * @param config Configuration to be used inside the scroll strategy.
     * Allows debouncing the reposition calls.
     */
    reposition: (config?: RepositionScrollStrategyConfig) => RepositionScrollStrategy;
    static ɵfac: i0.ɵɵFactoryDeclaration<ScrollStrategyOptions, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<ScrollStrategyOptions>;
}

export declare const STANDARD_DROPDOWN_ADJACENT_POSITIONS: ConnectedPosition[];

export declare const STANDARD_DROPDOWN_BELOW_POSITIONS: ConnectedPosition[];

/**
 * Validates whether a horizontal position property matches the expected values.
 * @param property Name of the property being validated.
 * @param value Value of the property being validated.
 * @docs-private
 */
export declare function validateHorizontalPosition(property: string, value: HorizontalConnectionPos): void;

/**
 * Validates whether a vertical position property matches the expected values.
 * @param property Name of the property being validated.
 * @param value Value of the property being validated.
 * @docs-private
 */
export declare function validateVerticalPosition(property: string, value: VerticalConnectionPos): void;

/** Vertical dimension of a connection point on the perimeter of the origin or overlay element. */
export declare type VerticalConnectionPos = 'top' | 'center' | 'bottom';

export { ViewportRuler }

export { }
