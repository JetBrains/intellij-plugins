import * as i0 from '@angular/core';
import { OnDestroy, NgZone, EnvironmentInjector, Renderer2, ComponentRef, EmbeddedViewRef, ElementRef, OnChanges, EventEmitter, SimpleChanges } from '@angular/core';
import { Direction, Directionality, BidiModule } from './bidi-module.d-BSI86Zrk.js';
import { PortalOutlet, ComponentPortal, TemplatePortal, PortalModule } from './portal-directives.d-C698lRc2.js';
import { CdkScrollable, ScrollingModule } from './scrolling-module.d-CUKr8D_p.js';
import { Location } from '@angular/common';
import { Subject, Observable } from 'rxjs';
import { ViewportRuler } from './scrolling/index.js';
import { Platform } from './platform.d-cnFZCLss.js';
import { _CdkPrivateStyleLoader } from './style-loader.d-DbvWk0ty.js';

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
    constructor(...args: unknown[]);
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
 * Service for dispatching keyboard events that land on the body to appropriate overlay ref,
 * if any. It maintains a list of attached overlays to determine best suited overlay based
 * on event target and order of overlay opens.
 */
declare class OverlayKeyboardDispatcher extends BaseOverlayDispatcher {
    private _ngZone;
    private _renderer;
    private _cleanupKeydown;
    /** Add a new overlay to the list of attached overlay refs. */
    add(overlayRef: OverlayRef): void;
    /** Detaches the global keyboard event listener. */
    protected detach(): void;
    /** Keyboard event listener that will be attached to the body. */
    private _keydownListener;
    static ɵfac: i0.ɵɵFactoryDeclaration<OverlayKeyboardDispatcher, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<OverlayKeyboardDispatcher>;
}

/**
 * Service for dispatching mouse click events that land on the body to appropriate overlay ref,
 * if any. It maintains a list of attached overlays to determine best suited overlay based
 * on event target and order of overlay opens.
 */
declare class OverlayOutsideClickDispatcher extends BaseOverlayDispatcher {
    private _platform;
    private _ngZone;
    private _renderer;
    private _cursorOriginalValue;
    private _cursorStyleIsSet;
    private _pointerDownEventTarget;
    private _cleanups;
    /** Add a new overlay to the list of attached overlay refs. */
    add(overlayRef: OverlayRef): void;
    /** Detaches the global keyboard event listener. */
    protected detach(): void;
    /** Store pointerdown event target to track origin of click. */
    private _pointerDownListener;
    /** Click event listener that will be attached to the body propagate phase. */
    private _clickListener;
    static ɵfac: i0.ɵɵFactoryDeclaration<OverlayOutsideClickDispatcher, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<OverlayOutsideClickDispatcher>;
}

/**
 * Describes a strategy that will be used by an overlay to handle scroll events while it is open.
 */
interface ScrollStrategy {
    /** Enable this scroll strategy (called when the attached overlay is attached to a portal). */
    enable: () => void;
    /** Disable this scroll strategy (called when the attached overlay is detached from a portal). */
    disable: () => void;
    /** Attaches this `ScrollStrategy` to an overlay. */
    attach: (overlayRef: OverlayRef) => void;
    /** Detaches the scroll strategy from the current overlay. */
    detach?: () => void;
}

/** An object where all of its properties cannot be written. */
type ImmutableObject<T> = {
    readonly [P in keyof T]: T[P];
};
/**
 * Reference to an overlay that has been created with the Overlay service.
 * Used to manipulate or dispose of said overlay.
 */
declare class OverlayRef implements PortalOutlet {
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
    private _injector;
    private _renderer;
    private readonly _backdropClick;
    private readonly _attachments;
    private readonly _detachments;
    private _positionStrategy;
    private _scrollStrategy;
    private _locationChanges;
    private _backdropRef;
    /**
     * Reference to the parent of the `_host` at the time it was detached. Used to restore
     * the `_host` to its original position in the DOM when it gets re-attached.
     */
    private _previousHostParent;
    /** Stream of keydown events dispatched to this overlay. */
    readonly _keydownEvents: Subject<KeyboardEvent>;
    /** Stream of mouse outside events dispatched to this overlay. */
    readonly _outsidePointerEvents: Subject<MouseEvent>;
    private _renders;
    private _afterRenderRef;
    /** Reference to the currently-running `afterNextRender` call. */
    private _afterNextRenderRef;
    constructor(_portalOutlet: PortalOutlet, _host: HTMLElement, _pane: HTMLElement, _config: ImmutableObject<OverlayConfig>, _ngZone: NgZone, _keyboardDispatcher: OverlayKeyboardDispatcher, _document: Document, _location: Location, _outsideClickDispatcher: OverlayOutsideClickDispatcher, _animationsDisabled: boolean | undefined, _injector: EnvironmentInjector, _renderer: Renderer2);
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
    private _detachContentWhenEmpty;
    /** Disposes of a scroll strategy. */
    private _disposeScrollStrategy;
}
/** Size properties for an overlay. */
interface OverlaySizeConfig {
    width?: number | string;
    height?: number | string;
    minWidth?: number | string;
    minHeight?: number | string;
    maxWidth?: number | string;
    maxHeight?: number | string;
}

/** Strategy for setting the position on an overlay. */
interface PositionStrategy {
    /** Attaches this position strategy to an overlay. */
    attach(overlayRef: OverlayRef): void;
    /** Updates the position of the overlay element. */
    apply(): void;
    /** Called when the overlay is detached. */
    detach?(): void;
    /** Cleans up any DOM modifications made by the position strategy, if necessary. */
    dispose(): void;
}

/** Initial configuration used when creating an overlay. */
declare class OverlayConfig {
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

/** Horizontal dimension of a connection point on the perimeter of the origin or overlay element. */
type HorizontalConnectionPos = 'start' | 'center' | 'end';
/** Vertical dimension of a connection point on the perimeter of the origin or overlay element. */
type VerticalConnectionPos = 'top' | 'center' | 'bottom';
/** A connection point on the origin element. */
interface OriginConnectionPosition {
    originX: HorizontalConnectionPos;
    originY: VerticalConnectionPos;
}
/** A connection point on the overlay element. */
interface OverlayConnectionPosition {
    overlayX: HorizontalConnectionPos;
    overlayY: VerticalConnectionPos;
}
/** The points of the origin element and the overlay element to connect. */
declare class ConnectionPositionPair {
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
declare class ScrollingVisibility {
    isOriginClipped: boolean;
    isOriginOutsideView: boolean;
    isOverlayClipped: boolean;
    isOverlayOutsideView: boolean;
}
/** The change event emitted by the strategy when a fallback position is used. */
declare class ConnectedOverlayPositionChange {
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
/**
 * Validates whether a vertical position property matches the expected values.
 * @param property Name of the property being validated.
 * @param value Value of the property being validated.
 * @docs-private
 */
declare function validateVerticalPosition(property: string, value: VerticalConnectionPos): void;
/**
 * Validates whether a horizontal position property matches the expected values.
 * @param property Name of the property being validated.
 * @param value Value of the property being validated.
 * @docs-private
 */
declare function validateHorizontalPosition(property: string, value: HorizontalConnectionPos): void;

/** Container inside which all overlays will render. */
declare class OverlayContainer implements OnDestroy {
    protected _platform: Platform;
    protected _containerElement: HTMLElement;
    protected _document: Document;
    protected _styleLoader: _CdkPrivateStyleLoader;
    constructor(...args: unknown[]);
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
    /** Loads the structural styles necessary for the overlay to work. */
    protected _loadStyles(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<OverlayContainer, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<OverlayContainer>;
}

/** Possible values that can be set as the origin of a FlexibleConnectedPositionStrategy. */
type FlexibleConnectedPositionStrategyOrigin = ElementRef | Element | (Point & {
    width?: number;
    height?: number;
});
/**
 * A strategy for positioning overlays. Using this strategy, an overlay is given an
 * implicit position relative some origin element. The relative position is defined in terms of
 * a point on the origin element that is connected to a point on the overlay element. For example,
 * a basic dropdown is connecting the bottom-left corner of the origin to the top-left corner
 * of the overlay.
 */
declare class FlexibleConnectedPositionStrategy implements PositionStrategy {
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
    /** The last calculated scroll visibility. Only tracked  */
    private _lastScrollVisibility;
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
    /** Returns the DOMRect of the current origin. */
    private _getOriginRect;
}
/** A simple (x, y) coordinate. */
interface Point {
    x: number;
    y: number;
}
/** A connected position as specified by the user. */
interface ConnectedPosition {
    originX: 'start' | 'center' | 'end';
    originY: 'top' | 'center' | 'bottom';
    overlayX: 'start' | 'center' | 'end';
    overlayY: 'top' | 'center' | 'bottom';
    weight?: number;
    offsetX?: number;
    offsetY?: number;
    panelClass?: string | string[];
}
declare const STANDARD_DROPDOWN_BELOW_POSITIONS: ConnectedPosition[];
declare const STANDARD_DROPDOWN_ADJACENT_POSITIONS: ConnectedPosition[];

/**
 * Directive applied to an element to make it usable as an origin for an Overlay using a
 * ConnectedPositionStrategy.
 */
declare class CdkOverlayOrigin {
    elementRef: ElementRef<any>;
    constructor(...args: unknown[]);
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkOverlayOrigin, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkOverlayOrigin, "[cdk-overlay-origin], [overlay-origin], [cdkOverlayOrigin]", ["cdkOverlayOrigin"], {}, {}, never, never, true, never>;
}
/**
 * Directive to facilitate declarative creation of an
 * Overlay using a FlexibleConnectedPositionStrategy.
 */
declare class CdkConnectedOverlay implements OnDestroy, OnChanges {
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
    private _ngZone;
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
    constructor(...args: unknown[]);
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
    private _getOrigin;
    private _getOriginElement;
    /** Attaches the overlay. */
    attachOverlay(): void;
    /** Detaches the overlay. */
    detachOverlay(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkConnectedOverlay, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkConnectedOverlay, "[cdk-connected-overlay], [connected-overlay], [cdkConnectedOverlay]", ["cdkConnectedOverlay"], { "origin": { "alias": "cdkConnectedOverlayOrigin"; "required": false; }; "positions": { "alias": "cdkConnectedOverlayPositions"; "required": false; }; "positionStrategy": { "alias": "cdkConnectedOverlayPositionStrategy"; "required": false; }; "offsetX": { "alias": "cdkConnectedOverlayOffsetX"; "required": false; }; "offsetY": { "alias": "cdkConnectedOverlayOffsetY"; "required": false; }; "width": { "alias": "cdkConnectedOverlayWidth"; "required": false; }; "height": { "alias": "cdkConnectedOverlayHeight"; "required": false; }; "minWidth": { "alias": "cdkConnectedOverlayMinWidth"; "required": false; }; "minHeight": { "alias": "cdkConnectedOverlayMinHeight"; "required": false; }; "backdropClass": { "alias": "cdkConnectedOverlayBackdropClass"; "required": false; }; "panelClass": { "alias": "cdkConnectedOverlayPanelClass"; "required": false; }; "viewportMargin": { "alias": "cdkConnectedOverlayViewportMargin"; "required": false; }; "scrollStrategy": { "alias": "cdkConnectedOverlayScrollStrategy"; "required": false; }; "open": { "alias": "cdkConnectedOverlayOpen"; "required": false; }; "disableClose": { "alias": "cdkConnectedOverlayDisableClose"; "required": false; }; "transformOriginSelector": { "alias": "cdkConnectedOverlayTransformOriginOn"; "required": false; }; "hasBackdrop": { "alias": "cdkConnectedOverlayHasBackdrop"; "required": false; }; "lockPosition": { "alias": "cdkConnectedOverlayLockPosition"; "required": false; }; "flexibleDimensions": { "alias": "cdkConnectedOverlayFlexibleDimensions"; "required": false; }; "growAfterOpen": { "alias": "cdkConnectedOverlayGrowAfterOpen"; "required": false; }; "push": { "alias": "cdkConnectedOverlayPush"; "required": false; }; "disposeOnNavigation": { "alias": "cdkConnectedOverlayDisposeOnNavigation"; "required": false; }; }, { "backdropClick": "backdropClick"; "positionChange": "positionChange"; "attach": "attach"; "detach": "detach"; "overlayKeydown": "overlayKeydown"; "overlayOutsideClick": "overlayOutsideClick"; }, never, never, true, never>;
    static ngAcceptInputType_hasBackdrop: unknown;
    static ngAcceptInputType_lockPosition: unknown;
    static ngAcceptInputType_flexibleDimensions: unknown;
    static ngAcceptInputType_growAfterOpen: unknown;
    static ngAcceptInputType_push: unknown;
    static ngAcceptInputType_disposeOnNavigation: unknown;
}

declare class OverlayModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<OverlayModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<OverlayModule, never, [typeof BidiModule, typeof PortalModule, typeof ScrollingModule, typeof CdkConnectedOverlay, typeof CdkOverlayOrigin], [typeof CdkConnectedOverlay, typeof CdkOverlayOrigin, typeof ScrollingModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<OverlayModule>;
}

export { CdkConnectedOverlay, CdkOverlayOrigin, ConnectedOverlayPositionChange, ConnectionPositionPair, FlexibleConnectedPositionStrategy, OverlayConfig, OverlayContainer, OverlayKeyboardDispatcher, OverlayModule, OverlayOutsideClickDispatcher, OverlayRef, STANDARD_DROPDOWN_ADJACENT_POSITIONS, STANDARD_DROPDOWN_BELOW_POSITIONS, ScrollingVisibility, validateHorizontalPosition, validateVerticalPosition };
export type { ConnectedPosition, FlexibleConnectedPositionStrategyOrigin, HorizontalConnectionPos, OriginConnectionPosition, OverlayConnectionPosition, OverlaySizeConfig, PositionStrategy, ScrollStrategy, VerticalConnectionPos };
