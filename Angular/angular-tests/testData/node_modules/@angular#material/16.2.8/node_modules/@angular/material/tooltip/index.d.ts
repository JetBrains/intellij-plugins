import { AfterViewInit } from '@angular/core';
import { AnimationTriggerMetadata } from '@angular/animations';
import { AriaDescriber } from '@angular/cdk/a11y';
import { BooleanInput } from '@angular/cdk/coercion';
import { ChangeDetectorRef } from '@angular/core';
import { ComponentType } from '@angular/cdk/overlay';
import { ConnectedPosition } from '@angular/cdk/overlay';
import { Directionality } from '@angular/cdk/bidi';
import { ElementRef } from '@angular/core';
import { FocusMonitor } from '@angular/cdk/a11y';
import * as i0 from '@angular/core';
import * as i2 from '@angular/cdk/a11y';
import * as i3 from '@angular/common';
import * as i4 from '@angular/cdk/overlay';
import * as i5 from '@angular/material/core';
import * as i6 from '@angular/cdk/scrolling';
import { InjectionToken } from '@angular/core';
import { NgZone } from '@angular/core';
import { NumberInput } from '@angular/cdk/coercion';
import { Observable } from 'rxjs';
import { OnDestroy } from '@angular/core';
import { OriginConnectionPosition } from '@angular/cdk/overlay';
import { Overlay } from '@angular/cdk/overlay';
import { OverlayConnectionPosition } from '@angular/cdk/overlay';
import { OverlayRef } from '@angular/cdk/overlay';
import { Platform } from '@angular/cdk/platform';
import { ScrollDispatcher } from '@angular/cdk/overlay';
import { ScrollStrategy } from '@angular/cdk/overlay';
import { ViewContainerRef } from '@angular/core';

/**
 * Creates an error to be thrown if the user supplied an invalid tooltip position.
 * @docs-private
 */
export declare function getMatTooltipInvalidPositionError(position: string): Error;

declare namespace i1 {
    export {
        getMatTooltipInvalidPositionError,
        MAT_TOOLTIP_SCROLL_STRATEGY_FACTORY,
        MAT_TOOLTIP_DEFAULT_OPTIONS_FACTORY,
        TooltipPosition,
        TooltipTouchGestures,
        TooltipVisibility,
        SCROLL_THROTTLE_MS,
        MAT_TOOLTIP_SCROLL_STRATEGY,
        MAT_TOOLTIP_SCROLL_STRATEGY_FACTORY_PROVIDER,
        MAT_TOOLTIP_DEFAULT_OPTIONS,
        MatTooltipDefaultOptions,
        TOOLTIP_PANEL_CLASS,
        _MatTooltipBase,
        MatTooltip,
        _TooltipComponentBase,
        TooltipComponent
    }
}

/** Injection token to be used to override the default options for `matTooltip`. */
export declare const MAT_TOOLTIP_DEFAULT_OPTIONS: InjectionToken<MatTooltipDefaultOptions>;

/** @docs-private */
export declare function MAT_TOOLTIP_DEFAULT_OPTIONS_FACTORY(): MatTooltipDefaultOptions;

/** Injection token that determines the scroll handling while a tooltip is visible. */
export declare const MAT_TOOLTIP_SCROLL_STRATEGY: InjectionToken<() => ScrollStrategy>;

/** @docs-private */
export declare function MAT_TOOLTIP_SCROLL_STRATEGY_FACTORY(overlay: Overlay): () => ScrollStrategy;

/** @docs-private */
export declare const MAT_TOOLTIP_SCROLL_STRATEGY_FACTORY_PROVIDER: {
    provide: InjectionToken<() => ScrollStrategy>;
    deps: (typeof Overlay)[];
    useFactory: typeof MAT_TOOLTIP_SCROLL_STRATEGY_FACTORY;
};

/**
 * Directive that attaches a material design tooltip to the host element. Animates the showing and
 * hiding of a tooltip provided position (defaults to below the element).
 *
 * https://material.io/design/components/tooltips.html
 */
export declare class MatTooltip extends _MatTooltipBase<TooltipComponent> {
    protected readonly _tooltipComponent: typeof TooltipComponent;
    protected readonly _cssClassPrefix = "mat-mdc";
    constructor(overlay: Overlay, elementRef: ElementRef<HTMLElement>, scrollDispatcher: ScrollDispatcher, viewContainerRef: ViewContainerRef, ngZone: NgZone, platform: Platform, ariaDescriber: AriaDescriber, focusMonitor: FocusMonitor, scrollStrategy: any, dir: Directionality, defaultOptions: MatTooltipDefaultOptions, _document: any);
    protected _addOffset(position: ConnectedPosition): ConnectedPosition;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatTooltip, [null, null, null, null, null, null, null, null, null, { optional: true; }, { optional: true; }, null]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatTooltip, "[matTooltip]", ["matTooltip"], {}, {}, never, never, false, never>;
}

/**
 * Animations used by MatTooltip.
 * @docs-private
 */
export declare const matTooltipAnimations: {
    readonly tooltipState: AnimationTriggerMetadata;
};

export declare abstract class _MatTooltipBase<T extends _TooltipComponentBase> implements OnDestroy, AfterViewInit {
    private _overlay;
    private _elementRef;
    private _scrollDispatcher;
    private _viewContainerRef;
    private _ngZone;
    private _platform;
    private _ariaDescriber;
    private _focusMonitor;
    protected _dir: Directionality;
    private _defaultOptions;
    _overlayRef: OverlayRef | null;
    _tooltipInstance: T | null;
    private _portal;
    private _position;
    private _positionAtOrigin;
    private _disabled;
    private _tooltipClass;
    private _scrollStrategy;
    private _viewInitialized;
    private _pointerExitEventsInitialized;
    protected abstract readonly _tooltipComponent: ComponentType<T>;
    protected _viewportMargin: number;
    private _currentPosition;
    protected readonly _cssClassPrefix: string;
    /** Allows the user to define the position of the tooltip relative to the parent element */
    get position(): TooltipPosition;
    set position(value: TooltipPosition);
    /**
     * Whether tooltip should be relative to the click or touch origin
     * instead of outside the element bounding box.
     */
    get positionAtOrigin(): boolean;
    set positionAtOrigin(value: BooleanInput);
    /** Disables the display of the tooltip. */
    get disabled(): boolean;
    set disabled(value: BooleanInput);
    /** The default delay in ms before showing the tooltip after show is called */
    get showDelay(): number;
    set showDelay(value: NumberInput);
    private _showDelay;
    /** The default delay in ms before hiding the tooltip after hide is called */
    get hideDelay(): number;
    set hideDelay(value: NumberInput);
    private _hideDelay;
    /**
     * How touch gestures should be handled by the tooltip. On touch devices the tooltip directive
     * uses a long press gesture to show and hide, however it can conflict with the native browser
     * gestures. To work around the conflict, Angular Material disables native gestures on the
     * trigger, but that might not be desirable on particular elements (e.g. inputs and draggable
     * elements). The different values for this option configure the touch event handling as follows:
     * - `auto` - Enables touch gestures for all elements, but tries to avoid conflicts with native
     *   browser gestures on particular elements. In particular, it allows text selection on inputs
     *   and textareas, and preserves the native browser dragging on elements marked as `draggable`.
     * - `on` - Enables touch gestures for all elements and disables native
     *   browser gestures with no exceptions.
     * - `off` - Disables touch gestures. Note that this will prevent the tooltip from
     *   showing on touch devices.
     */
    touchGestures: TooltipTouchGestures;
    /** The message to be displayed in the tooltip */
    get message(): string;
    set message(value: string);
    private _message;
    /** Classes to be passed to the tooltip. Supports the same syntax as `ngClass`. */
    get tooltipClass(): string | string[] | Set<string> | {
        [key: string]: any;
    };
    set tooltipClass(value: string | string[] | Set<string> | {
        [key: string]: any;
    });
    /** Manually-bound passive event listeners. */
    private readonly _passiveListeners;
    /** Reference to the current document. */
    private _document;
    /** Timer started at the last `touchstart` event. */
    private _touchstartTimeout;
    /** Emits when the component is destroyed. */
    private readonly _destroyed;
    constructor(_overlay: Overlay, _elementRef: ElementRef<HTMLElement>, _scrollDispatcher: ScrollDispatcher, _viewContainerRef: ViewContainerRef, _ngZone: NgZone, _platform: Platform, _ariaDescriber: AriaDescriber, _focusMonitor: FocusMonitor, scrollStrategy: any, _dir: Directionality, _defaultOptions: MatTooltipDefaultOptions, _document: any);
    ngAfterViewInit(): void;
    /**
     * Dispose the tooltip when destroyed.
     */
    ngOnDestroy(): void;
    /** Shows the tooltip after the delay in ms, defaults to tooltip-delay-show or 0ms if no input */
    show(delay?: number, origin?: {
        x: number;
        y: number;
    }): void;
    /** Hides the tooltip after the delay in ms, defaults to tooltip-delay-hide or 0ms if no input */
    hide(delay?: number): void;
    /** Shows/hides the tooltip */
    toggle(origin?: {
        x: number;
        y: number;
    }): void;
    /** Returns true if the tooltip is currently visible to the user */
    _isTooltipVisible(): boolean;
    /** Create the overlay config and position strategy */
    private _createOverlay;
    /** Detaches the currently-attached tooltip. */
    private _detach;
    /** Updates the position of the current tooltip. */
    private _updatePosition;
    /** Adds the configured offset to a position. Used as a hook for child classes. */
    protected _addOffset(position: ConnectedPosition): ConnectedPosition;
    /**
     * Returns the origin position and a fallback position based on the user's position preference.
     * The fallback position is the inverse of the origin (e.g. `'below' -> 'above'`).
     */
    _getOrigin(): {
        main: OriginConnectionPosition;
        fallback: OriginConnectionPosition;
    };
    /** Returns the overlay position and a fallback position based on the user's preference */
    _getOverlayPosition(): {
        main: OverlayConnectionPosition;
        fallback: OverlayConnectionPosition;
    };
    /** Updates the tooltip message and repositions the overlay according to the new message length */
    private _updateTooltipMessage;
    /** Updates the tooltip class */
    private _setTooltipClass;
    /** Inverts an overlay position. */
    private _invertPosition;
    /** Updates the class on the overlay panel based on the current position of the tooltip. */
    private _updateCurrentPositionClass;
    /** Binds the pointer events to the tooltip trigger. */
    private _setupPointerEnterEventsIfNeeded;
    private _setupPointerExitEventsIfNeeded;
    private _addListeners;
    private _platformSupportsMouseEvents;
    /** Listener for the `wheel` event on the element. */
    private _wheelListener;
    /** Disables the native browser gestures, based on how the tooltip has been configured. */
    private _disableNativeGesturesIfNecessary;
    static ɵfac: i0.ɵɵFactoryDeclaration<_MatTooltipBase<any>, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<_MatTooltipBase<any>, never, never, { "position": { "alias": "matTooltipPosition"; "required": false; }; "positionAtOrigin": { "alias": "matTooltipPositionAtOrigin"; "required": false; }; "disabled": { "alias": "matTooltipDisabled"; "required": false; }; "showDelay": { "alias": "matTooltipShowDelay"; "required": false; }; "hideDelay": { "alias": "matTooltipHideDelay"; "required": false; }; "touchGestures": { "alias": "matTooltipTouchGestures"; "required": false; }; "message": { "alias": "matTooltip"; "required": false; }; "tooltipClass": { "alias": "matTooltipClass"; "required": false; }; }, {}, never, never, false, never>;
}

/** Default `matTooltip` options that can be overridden. */
export declare interface MatTooltipDefaultOptions {
    /** Default delay when the tooltip is shown. */
    showDelay: number;
    /** Default delay when the tooltip is hidden. */
    hideDelay: number;
    /** Default delay when hiding the tooltip on a touch device. */
    touchendHideDelay: number;
    /** Default touch gesture handling for tooltips. */
    touchGestures?: TooltipTouchGestures;
    /** Default position for tooltips. */
    position?: TooltipPosition;
    /**
     * Default value for whether tooltips should be positioned near the click or touch origin
     * instead of outside the element bounding box.
     */
    positionAtOrigin?: boolean;
    /** Disables the ability for the user to interact with the tooltip element. */
    disableTooltipInteractivity?: boolean;
}

export declare class MatTooltipModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatTooltipModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatTooltipModule, [typeof i1.MatTooltip, typeof i1.TooltipComponent], [typeof i2.A11yModule, typeof i3.CommonModule, typeof i4.OverlayModule, typeof i5.MatCommonModule], [typeof i1.MatTooltip, typeof i1.TooltipComponent, typeof i5.MatCommonModule, typeof i6.CdkScrollableModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatTooltipModule>;
}

/** Time in ms to throttle repositioning after scroll events. */
export declare const SCROLL_THROTTLE_MS = 20;

/**
 * CSS class that will be attached to the overlay panel.
 * @deprecated
 * @breaking-change 13.0.0 remove this variable
 */
export declare const TOOLTIP_PANEL_CLASS = "mat-mdc-tooltip-panel";

/**
 * Internal component that wraps the tooltip's content.
 * @docs-private
 */
export declare class TooltipComponent extends _TooltipComponentBase {
    private _elementRef;
    _isMultiline: boolean;
    /** Reference to the internal tooltip element. */
    _tooltip: ElementRef<HTMLElement>;
    _showAnimation: string;
    _hideAnimation: string;
    constructor(changeDetectorRef: ChangeDetectorRef, _elementRef: ElementRef<HTMLElement>, animationMode?: string);
    protected _onShow(): void;
    /** Whether the tooltip text has overflown to the next line */
    private _isTooltipMultiline;
    static ɵfac: i0.ɵɵFactoryDeclaration<TooltipComponent, [null, null, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<TooltipComponent, "mat-tooltip-component", never, {}, {}, never, never, false, never>;
}

export declare abstract class _TooltipComponentBase implements OnDestroy {
    private _changeDetectorRef;
    /** Message to display in the tooltip */
    message: string;
    /** Classes to be added to the tooltip. Supports the same syntax as `ngClass`. */
    tooltipClass: string | string[] | Set<string> | {
        [key: string]: any;
    };
    /** The timeout ID of any current timer set to show the tooltip */
    private _showTimeoutId;
    /** The timeout ID of any current timer set to hide the tooltip */
    private _hideTimeoutId;
    /** Element that caused the tooltip to open. */
    _triggerElement: HTMLElement;
    /** Amount of milliseconds to delay the closing sequence. */
    _mouseLeaveHideDelay: number;
    /** Whether animations are currently disabled. */
    private _animationsDisabled;
    /** Reference to the internal tooltip element. */
    abstract _tooltip: ElementRef<HTMLElement>;
    /** Whether interactions on the page should close the tooltip */
    private _closeOnInteraction;
    /** Whether the tooltip is currently visible. */
    private _isVisible;
    /** Subject for notifying that the tooltip has been hidden from the view */
    private readonly _onHide;
    /** Name of the show animation and the class that toggles it. */
    protected abstract readonly _showAnimation: string;
    /** Name of the hide animation and the class that toggles it. */
    protected abstract readonly _hideAnimation: string;
    constructor(_changeDetectorRef: ChangeDetectorRef, animationMode?: string);
    /**
     * Shows the tooltip with an animation originating from the provided origin
     * @param delay Amount of milliseconds to the delay showing the tooltip.
     */
    show(delay: number): void;
    /**
     * Begins the animation to hide the tooltip after the provided delay in ms.
     * @param delay Amount of milliseconds to delay showing the tooltip.
     */
    hide(delay: number): void;
    /** Returns an observable that notifies when the tooltip has been hidden from view. */
    afterHidden(): Observable<void>;
    /** Whether the tooltip is being displayed. */
    isVisible(): boolean;
    ngOnDestroy(): void;
    /**
     * Interactions on the HTML body should close the tooltip immediately as defined in the
     * material design spec.
     * https://material.io/design/components/tooltips.html#behavior
     */
    _handleBodyInteraction(): void;
    /**
     * Marks that the tooltip needs to be checked in the next change detection run.
     * Mainly used for rendering the initial text before positioning a tooltip, which
     * can be problematic in components with OnPush change detection.
     */
    _markForCheck(): void;
    _handleMouseLeave({ relatedTarget }: MouseEvent): void;
    /**
     * Callback for when the timeout in this.show() gets completed.
     * This method is only needed by the mdc-tooltip, and so it is only implemented
     * in the mdc-tooltip, not here.
     */
    protected _onShow(): void;
    /** Event listener dispatched when an animation on the tooltip finishes. */
    _handleAnimationEnd({ animationName }: AnimationEvent): void;
    /** Cancels any pending animation sequences. */
    _cancelPendingAnimations(): void;
    /** Handles the cleanup after an animation has finished. */
    private _finalizeAnimation;
    /** Toggles the visibility of the tooltip element. */
    private _toggleVisibility;
    static ɵfac: i0.ɵɵFactoryDeclaration<_TooltipComponentBase, [null, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<_TooltipComponentBase, never, never, {}, {}, never, never, false, never>;
}

/** Possible positions for a tooltip. */
export declare type TooltipPosition = 'left' | 'right' | 'above' | 'below' | 'before' | 'after';

/**
 * Options for how the tooltip trigger should handle touch gestures.
 * See `MatTooltip.touchGestures` for more information.
 */
export declare type TooltipTouchGestures = 'auto' | 'on' | 'off';

/** Possible visibility states of a tooltip. */
export declare type TooltipVisibility = 'initial' | 'visible' | 'hidden';

export { }
