import * as i0 from '@angular/core';
import { InjectionToken, OnDestroy, ElementRef, AfterViewInit, EventEmitter } from '@angular/core';
import { Observable } from 'rxjs';

type FocusOrigin = 'touch' | 'mouse' | 'keyboard' | 'program' | null;
/**
 * Corresponds to the options that can be passed to the native `focus` event.
 * via https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/focus
 */
interface FocusOptions {
    /** Whether the browser should scroll to the element when it is focused. */
    preventScroll?: boolean;
}
/** Detection mode used for attributing the origin of a focus event. */
declare enum FocusMonitorDetectionMode {
    /**
     * Any mousedown, keydown, or touchstart event that happened in the previous
     * tick or the current tick will be used to assign a focus event's origin (to
     * either mouse, keyboard, or touch). This is the default option.
     */
    IMMEDIATE = 0,
    /**
     * A focus event's origin is always attributed to the last corresponding
     * mousedown, keydown, or touchstart event, no matter how long ago it occurred.
     */
    EVENTUAL = 1
}
/** Injectable service-level options for FocusMonitor. */
interface FocusMonitorOptions {
    detectionMode?: FocusMonitorDetectionMode;
}
/** InjectionToken for FocusMonitorOptions. */
declare const FOCUS_MONITOR_DEFAULT_OPTIONS: InjectionToken<FocusMonitorOptions>;
/** Monitors mouse and keyboard events to determine the cause of focus events. */
declare class FocusMonitor implements OnDestroy {
    private _ngZone;
    private _platform;
    private readonly _inputModalityDetector;
    /** The focus origin that the next focus event is a result of. */
    private _origin;
    /** The FocusOrigin of the last focus event tracked by the FocusMonitor. */
    private _lastFocusOrigin;
    /** Whether the window has just been focused. */
    private _windowFocused;
    /** The timeout id of the window focus timeout. */
    private _windowFocusTimeoutId;
    /** The timeout id of the origin clearing timeout. */
    private _originTimeoutId;
    /**
     * Whether the origin was determined via a touch interaction. Necessary as properly attributing
     * focus events to touch interactions requires special logic.
     */
    private _originFromTouchInteraction;
    /** Map of elements being monitored to their info. */
    private _elementInfo;
    /** The number of elements currently being monitored. */
    private _monitoredElementCount;
    /**
     * Keeps track of the root nodes to which we've currently bound a focus/blur handler,
     * as well as the number of monitored elements that they contain. We have to treat focus/blur
     * handlers differently from the rest of the events, because the browser won't emit events
     * to the document when focus moves inside of a shadow root.
     */
    private _rootNodeFocusListenerCount;
    /**
     * The specified detection mode, used for attributing the origin of a focus
     * event.
     */
    private readonly _detectionMode;
    /**
     * Event listener for `focus` events on the window.
     * Needs to be an arrow function in order to preserve the context when it gets bound.
     */
    private _windowFocusListener;
    /** Used to reference correct document/window */
    protected _document?: Document | null | undefined;
    /** Subject for stopping our InputModalityDetector subscription. */
    private readonly _stopInputModalityDetector;
    constructor(...args: unknown[]);
    /**
     * Event listener for `focus` and 'blur' events on the document.
     * Needs to be an arrow function in order to preserve the context when it gets bound.
     */
    private _rootNodeFocusAndBlurListener;
    /**
     * Monitors focus on an element and applies appropriate CSS classes.
     * @param element The element to monitor
     * @param checkChildren Whether to count the element as focused when its children are focused.
     * @returns An observable that emits when the focus state of the element changes.
     *     When the element is blurred, null will be emitted.
     */
    monitor(element: HTMLElement, checkChildren?: boolean): Observable<FocusOrigin>;
    /**
     * Monitors focus on an element and applies appropriate CSS classes.
     * @param element The element to monitor
     * @param checkChildren Whether to count the element as focused when its children are focused.
     * @returns An observable that emits when the focus state of the element changes.
     *     When the element is blurred, null will be emitted.
     */
    monitor(element: ElementRef<HTMLElement>, checkChildren?: boolean): Observable<FocusOrigin>;
    /**
     * Stops monitoring an element and removes all focus classes.
     * @param element The element to stop monitoring.
     */
    stopMonitoring(element: HTMLElement): void;
    /**
     * Stops monitoring an element and removes all focus classes.
     * @param element The element to stop monitoring.
     */
    stopMonitoring(element: ElementRef<HTMLElement>): void;
    /**
     * Focuses the element via the specified focus origin.
     * @param element Element to focus.
     * @param origin Focus origin.
     * @param options Options that can be used to configure the focus behavior.
     */
    focusVia(element: HTMLElement, origin: FocusOrigin, options?: FocusOptions): void;
    /**
     * Focuses the element via the specified focus origin.
     * @param element Element to focus.
     * @param origin Focus origin.
     * @param options Options that can be used to configure the focus behavior.
     */
    focusVia(element: ElementRef<HTMLElement>, origin: FocusOrigin, options?: FocusOptions): void;
    ngOnDestroy(): void;
    /** Access injected document if available or fallback to global document reference */
    private _getDocument;
    /** Use defaultView of injected document if available or fallback to global window reference */
    private _getWindow;
    private _getFocusOrigin;
    /**
     * Returns whether the focus event should be attributed to touch. Recall that in IMMEDIATE mode, a
     * touch origin isn't immediately reset at the next tick (see _setOrigin). This means that when we
     * handle a focus event following a touch interaction, we need to determine whether (1) the focus
     * event was directly caused by the touch interaction or (2) the focus event was caused by a
     * subsequent programmatic focus call triggered by the touch interaction.
     * @param focusEventTarget The target of the focus event under examination.
     */
    private _shouldBeAttributedToTouch;
    /**
     * Sets the focus classes on the element based on the given focus origin.
     * @param element The element to update the classes on.
     * @param origin The focus origin.
     */
    private _setClasses;
    /**
     * Updates the focus origin. If we're using immediate detection mode, we schedule an async
     * function to clear the origin at the end of a timeout. The duration of the timeout depends on
     * the origin being set.
     * @param origin The origin to set.
     * @param isFromInteraction Whether we are setting the origin from an interaction event.
     */
    private _setOrigin;
    /**
     * Handles focus events on a registered element.
     * @param event The focus event.
     * @param element The monitored element.
     */
    private _onFocus;
    /**
     * Handles blur events on a registered element.
     * @param event The blur event.
     * @param element The monitored element.
     */
    _onBlur(event: FocusEvent, element: HTMLElement): void;
    private _emitOrigin;
    private _registerGlobalListeners;
    private _removeGlobalListeners;
    /** Updates all the state on an element once its focus origin has changed. */
    private _originChanged;
    /**
     * Collects the `MonitoredElementInfo` of a particular element and
     * all of its ancestors that have enabled `checkChildren`.
     * @param element Element from which to start the search.
     */
    private _getClosestElementsInfo;
    /**
     * Returns whether an interaction is likely to have come from the user clicking the `label` of
     * an `input` or `textarea` in order to focus it.
     * @param focusEventTarget Target currently receiving focus.
     */
    private _isLastInteractionFromInputLabel;
    static ɵfac: i0.ɵɵFactoryDeclaration<FocusMonitor, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<FocusMonitor>;
}
/**
 * Directive that determines how a particular element was focused (via keyboard, mouse, touch, or
 * programmatically) and adds corresponding classes to the element.
 *
 * There are two variants of this directive:
 * 1) cdkMonitorElementFocus: does not consider an element to be focused if one of its children is
 *    focused.
 * 2) cdkMonitorSubtreeFocus: considers an element focused if it or any of its children are focused.
 */
declare class CdkMonitorFocus implements AfterViewInit, OnDestroy {
    private _elementRef;
    private _focusMonitor;
    private _monitorSubscription;
    private _focusOrigin;
    readonly cdkFocusChange: EventEmitter<FocusOrigin>;
    constructor(...args: unknown[]);
    get focusOrigin(): FocusOrigin;
    ngAfterViewInit(): void;
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkMonitorFocus, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkMonitorFocus, "[cdkMonitorElementFocus], [cdkMonitorSubtreeFocus]", ["cdkMonitorFocus"], {}, { "cdkFocusChange": "cdkFocusChange"; }, never, never, true, never>;
}

export { CdkMonitorFocus, FOCUS_MONITOR_DEFAULT_OPTIONS, FocusMonitor, FocusMonitorDetectionMode };
export type { FocusMonitorOptions, FocusOptions, FocusOrigin };
