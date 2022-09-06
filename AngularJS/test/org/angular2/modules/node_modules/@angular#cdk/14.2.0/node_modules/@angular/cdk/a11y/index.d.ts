import { AfterContentInit } from '@angular/core';
import { AfterViewInit } from '@angular/core';
import { BooleanInput } from '@angular/cdk/coercion';
import { ContentObserver } from '@angular/cdk/observers';
import { DoCheck } from '@angular/core';
import { ElementRef } from '@angular/core';
import { EventEmitter } from '@angular/core';
import * as i0 from '@angular/core';
import * as i4 from '@angular/cdk/observers';
import { InjectionToken } from '@angular/core';
import { NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { OnChanges } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { Platform } from '@angular/cdk/platform';
import { QueryList } from '@angular/core';
import { SimpleChanges } from '@angular/core';
import { Subject } from 'rxjs';

export declare class A11yModule {
    constructor(highContrastModeDetector: HighContrastModeDetector);
    static ɵfac: i0.ɵɵFactoryDeclaration<A11yModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<A11yModule, [typeof i1.CdkAriaLive, typeof i2.CdkTrapFocus, typeof i3.CdkMonitorFocus], [typeof i4.ObserversModule], [typeof i1.CdkAriaLive, typeof i2.CdkTrapFocus, typeof i3.CdkMonitorFocus]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<A11yModule>;
}

export declare class ActiveDescendantKeyManager<T> extends ListKeyManager<Highlightable & T> {
    /**
     * Sets the active item to the item at the specified index and adds the
     * active styles to the newly active item. Also removes active styles
     * from the previously active item.
     * @param index Index of the item to be set as active.
     */
    setActiveItem(index: number): void;
    /**
     * Sets the active item to the item to the specified one and adds the
     * active styles to the it. Also removes active styles from the
     * previously active item.
     * @param item Item to be set as active.
     */
    setActiveItem(item: T): void;
}

/**
 * Utility that creates visually hidden elements with a message content. Useful for elements that
 * want to use aria-describedby to further describe themselves without adding additional visual
 * content.
 */
export declare class AriaDescriber implements OnDestroy {
    /**
     * @deprecated To be turned into a required parameter.
     * @breaking-change 14.0.0
     */
    private _platform?;
    private _document;
    /** Map of all registered message elements that have been placed into the document. */
    private _messageRegistry;
    /** Container for all registered messages. */
    private _messagesContainer;
    /** Unique ID for the service. */
    private readonly _id;
    constructor(_document: any, 
    /**
     * @deprecated To be turned into a required parameter.
     * @breaking-change 14.0.0
     */
    _platform?: Platform | undefined);
    /**
     * Adds to the host element an aria-describedby reference to a hidden element that contains
     * the message. If the same message has already been registered, then it will reuse the created
     * message element.
     */
    describe(hostElement: Element, message: string, role?: string): void;
    /**
     * Adds to the host element an aria-describedby reference to an already-existing message element.
     */
    describe(hostElement: Element, message: HTMLElement): void;
    /** Removes the host element's aria-describedby reference to the message. */
    removeDescription(hostElement: Element, message: string, role?: string): void;
    /** Removes the host element's aria-describedby reference to the message element. */
    removeDescription(hostElement: Element, message: HTMLElement): void;
    /** Unregisters all created message elements and removes the message container. */
    ngOnDestroy(): void;
    /**
     * Creates a new element in the visually hidden message container element with the message
     * as its content and adds it to the message registry.
     */
    private _createMessageElement;
    /** Deletes the message element from the global messages container. */
    private _deleteMessageElement;
    /** Creates the global container for all aria-describedby messages. */
    private _createMessagesContainer;
    /** Removes all cdk-describedby messages that are hosted through the element. */
    private _removeCdkDescribedByReferenceIds;
    /**
     * Adds a message reference to the element using aria-describedby and increments the registered
     * message's reference count.
     */
    private _addMessageReference;
    /**
     * Removes a message reference from the element using aria-describedby
     * and decrements the registered message's reference count.
     */
    private _removeMessageReference;
    /** Returns true if the element has been described by the provided message ID. */
    private _isElementDescribedByMessage;
    /** Determines whether a message can be described on a particular element. */
    private _canBeDescribed;
    /** Checks whether a node is an Element node. */
    private _isElementNode;
    static ɵfac: i0.ɵɵFactoryDeclaration<AriaDescriber, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<AriaDescriber>;
}

/** Possible politeness levels. */
export declare type AriaLivePoliteness = 'off' | 'polite' | 'assertive';

/**
 * Attribute given to each host element that is described by a message element.
 * @deprecated To be turned into a private variable.
 * @breaking-change 14.0.0
 */
export declare const CDK_DESCRIBEDBY_HOST_ATTRIBUTE = "cdk-describedby-host";

/**
 * ID prefix used for each created message element.
 * @deprecated To be turned into a private variable.
 * @breaking-change 14.0.0
 */
export declare const CDK_DESCRIBEDBY_ID_PREFIX = "cdk-describedby-message";

/**
 * A directive that works similarly to aria-live, but uses the LiveAnnouncer to ensure compatibility
 * with a wider range of browsers and screen readers.
 */
export declare class CdkAriaLive implements OnDestroy {
    private _elementRef;
    private _liveAnnouncer;
    private _contentObserver;
    private _ngZone;
    /** The aria-live politeness level to use when announcing messages. */
    get politeness(): AriaLivePoliteness;
    set politeness(value: AriaLivePoliteness);
    private _politeness;
    /** Time in milliseconds after which to clear out the announcer element. */
    duration: number;
    private _previousAnnouncedText?;
    private _subscription;
    constructor(_elementRef: ElementRef, _liveAnnouncer: LiveAnnouncer, _contentObserver: ContentObserver, _ngZone: NgZone);
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkAriaLive, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkAriaLive, "[cdkAriaLive]", ["cdkAriaLive"], { "politeness": "cdkAriaLive"; "duration": "cdkAriaLiveDuration"; }, {}, never, never, false>;
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
export declare class CdkMonitorFocus implements AfterViewInit, OnDestroy {
    private _elementRef;
    private _focusMonitor;
    private _monitorSubscription;
    private _focusOrigin;
    readonly cdkFocusChange: EventEmitter<FocusOrigin>;
    constructor(_elementRef: ElementRef<HTMLElement>, _focusMonitor: FocusMonitor);
    get focusOrigin(): FocusOrigin;
    ngAfterViewInit(): void;
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkMonitorFocus, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkMonitorFocus, "[cdkMonitorElementFocus], [cdkMonitorSubtreeFocus]", ["cdkMonitorFocus"], {}, { "cdkFocusChange": "cdkFocusChange"; }, never, never, false>;
}

/** Directive for trapping focus within a region. */
export declare class CdkTrapFocus implements OnDestroy, AfterContentInit, OnChanges, DoCheck {
    private _elementRef;
    private _focusTrapFactory;
    /** Underlying FocusTrap instance. */
    focusTrap: FocusTrap;
    /** Previously focused element to restore focus to upon destroy when using autoCapture. */
    private _previouslyFocusedElement;
    /** Whether the focus trap is active. */
    get enabled(): boolean;
    set enabled(value: BooleanInput);
    /**
     * Whether the directive should automatically move focus into the trapped region upon
     * initialization and return focus to the previous activeElement upon destruction.
     */
    get autoCapture(): boolean;
    set autoCapture(value: BooleanInput);
    private _autoCapture;
    constructor(_elementRef: ElementRef<HTMLElement>, _focusTrapFactory: FocusTrapFactory, 
    /**
     * @deprecated No longer being used. To be removed.
     * @breaking-change 13.0.0
     */
    _document: any);
    ngOnDestroy(): void;
    ngAfterContentInit(): void;
    ngDoCheck(): void;
    ngOnChanges(changes: SimpleChanges): void;
    private _captureFocus;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkTrapFocus, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkTrapFocus, "[cdkTrapFocus]", ["cdkTrapFocus"], { "enabled": "cdkTrapFocus"; "autoCapture": "cdkTrapFocusAutoCapture"; }, {}, never, never, false>;
}

/**
 * Class that allows for trapping focus within a DOM element.
 *
 * This class uses a strategy pattern that determines how it traps focus.
 * See FocusTrapInertStrategy.
 */
export declare class ConfigurableFocusTrap extends FocusTrap implements ManagedFocusTrap {
    private _focusTrapManager;
    private _inertStrategy;
    /** Whether the FocusTrap is enabled. */
    get enabled(): boolean;
    set enabled(value: boolean);
    constructor(_element: HTMLElement, _checker: InteractivityChecker, _ngZone: NgZone, _document: Document, _focusTrapManager: FocusTrapManager, _inertStrategy: FocusTrapInertStrategy, config: ConfigurableFocusTrapConfig);
    /** Notifies the FocusTrapManager that this FocusTrap will be destroyed. */
    destroy(): void;
    /** @docs-private Implemented as part of ManagedFocusTrap. */
    _enable(): void;
    /** @docs-private Implemented as part of ManagedFocusTrap. */
    _disable(): void;
}


/**
 * Options for creating a ConfigurableFocusTrap.
 */
export declare interface ConfigurableFocusTrapConfig {
    /**
     * Whether to defer the creation of FocusTrap elements to be done manually by the user.
     */
    defer: boolean;
}

/** Factory that allows easy instantiation of configurable focus traps. */
export declare class ConfigurableFocusTrapFactory {
    private _checker;
    private _ngZone;
    private _focusTrapManager;
    private _document;
    private _inertStrategy;
    constructor(_checker: InteractivityChecker, _ngZone: NgZone, _focusTrapManager: FocusTrapManager, _document: any, _inertStrategy?: FocusTrapInertStrategy);
    /**
     * Creates a focus-trapped region around the given element.
     * @param element The element around which focus will be trapped.
     * @param config The focus trap configuration.
     * @returns The created focus trap instance.
     */
    create(element: HTMLElement, config?: ConfigurableFocusTrapConfig): ConfigurableFocusTrap;
    /**
     * @deprecated Pass a config object instead of the `deferCaptureElements` flag.
     * @breaking-change 11.0.0
     */
    create(element: HTMLElement, deferCaptureElements: boolean): ConfigurableFocusTrap;
    static ɵfac: i0.ɵɵFactoryDeclaration<ConfigurableFocusTrapFactory, [null, null, null, null, { optional: true; }]>;
    static ɵprov: i0.ɵɵInjectableDeclaration<ConfigurableFocusTrapFactory>;
}

/**
 * Lightweight FocusTrapInertStrategy that adds a document focus event
 * listener to redirect focus back inside the FocusTrap.
 */
export declare class EventListenerFocusTrapInertStrategy implements FocusTrapInertStrategy {
    /** Focus event handler. */
    private _listener;
    /** Adds a document event listener that keeps focus inside the FocusTrap. */
    preventFocus(focusTrap: ConfigurableFocusTrap): void;
    /** Removes the event listener added in preventFocus. */
    allowFocus(focusTrap: ConfigurableFocusTrap): void;
    /**
     * Refocuses the first element in the FocusTrap if the focus event target was outside
     * the FocusTrap.
     *
     * This is an event listener callback. The event listener is added in runOutsideAngular,
     * so all this code runs outside Angular as well.
     */
    private _trapFocus;
}

/** InjectionToken for FocusMonitorOptions. */
export declare const FOCUS_MONITOR_DEFAULT_OPTIONS: InjectionToken<FocusMonitorOptions>;

/** The injection token used to specify the inert strategy. */
export declare const FOCUS_TRAP_INERT_STRATEGY: InjectionToken<FocusTrapInertStrategy>;

/**
 * This is the interface for focusable items (used by the FocusKeyManager).
 * Each item must know how to focus itself, whether or not it is currently disabled
 * and be able to supply its label.
 */
export declare interface FocusableOption extends ListKeyManagerOption {
    /** Focuses the `FocusableOption`. */
    focus(origin?: FocusOrigin): void;
}

export declare class FocusKeyManager<T> extends ListKeyManager<FocusableOption & T> {
    private _origin;
    /**
     * Sets the focus origin that will be passed in to the items for any subsequent `focus` calls.
     * @param origin Focus origin to be used when focusing items.
     */
    setFocusOrigin(origin: FocusOrigin): this;
    /**
     * Sets the active item to the item at the specified
     * index and focuses the newly active item.
     * @param index Index of the item to be set as active.
     */
    setActiveItem(index: number): void;
    /**
     * Sets the active item to the item that is specified and focuses it.
     * @param item Item to be set as active.
     */
    setActiveItem(item: T): void;
}

/** Monitors mouse and keyboard events to determine the cause of focus events. */
export declare class FocusMonitor implements OnDestroy {
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
    protected _document?: Document;
    /** Subject for stopping our InputModalityDetector subscription. */
    private readonly _stopInputModalityDetector;
    constructor(_ngZone: NgZone, _platform: Platform, _inputModalityDetector: InputModalityDetector, 
    /** @breaking-change 11.0.0 make document required */
    document: any | null, options: FocusMonitorOptions | null);
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
    focusVia(element: HTMLElement, origin: FocusOrigin, options?: FocusOptions_2): void;
    /**
     * Focuses the element via the specified focus origin.
     * @param element Element to focus.
     * @param origin Focus origin.
     * @param options Options that can be used to configure the focus behavior.
     */
    focusVia(element: ElementRef<HTMLElement>, origin: FocusOrigin, options?: FocusOptions_2): void;
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
    static ɵfac: i0.ɵɵFactoryDeclaration<FocusMonitor, [null, null, null, { optional: true; }, { optional: true; }]>;
    static ɵprov: i0.ɵɵInjectableDeclaration<FocusMonitor>;
}

/** Detection mode used for attributing the origin of a focus event. */
export declare const enum FocusMonitorDetectionMode {
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
export declare interface FocusMonitorOptions {
    detectionMode?: FocusMonitorDetectionMode;
}

/**
 * Corresponds to the options that can be passed to the native `focus` event.
 * via https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/focus
 */
declare interface FocusOptions_2 {
    /** Whether the browser should scroll to the element when it is focused. */
    preventScroll?: boolean;
}
export { FocusOptions_2 as FocusOptions }

export declare type FocusOrigin = 'touch' | 'mouse' | 'keyboard' | 'program' | null;

/**
 * Class that allows for trapping focus within a DOM element.
 *
 * This class currently uses a relatively simple approach to focus trapping.
 * It assumes that the tab order is the same as DOM order, which is not necessarily true.
 * Things like `tabIndex > 0`, flex `order`, and shadow roots can cause the two to be misaligned.
 *
 * @deprecated Use `ConfigurableFocusTrap` instead.
 * @breaking-change 11.0.0
 */
export declare class FocusTrap {
    readonly _element: HTMLElement;
    private _checker;
    readonly _ngZone: NgZone;
    readonly _document: Document;
    private _startAnchor;
    private _endAnchor;
    private _hasAttached;
    protected startAnchorListener: () => boolean;
    protected endAnchorListener: () => boolean;
    /** Whether the focus trap is active. */
    get enabled(): boolean;
    set enabled(value: boolean);
    protected _enabled: boolean;
    constructor(_element: HTMLElement, _checker: InteractivityChecker, _ngZone: NgZone, _document: Document, deferAnchors?: boolean);
    /** Destroys the focus trap by cleaning up the anchors. */
    destroy(): void;
    /**
     * Inserts the anchors into the DOM. This is usually done automatically
     * in the constructor, but can be deferred for cases like directives with `*ngIf`.
     * @returns Whether the focus trap managed to attach successfully. This may not be the case
     * if the target element isn't currently in the DOM.
     */
    attachAnchors(): boolean;
    /**
     * Waits for the zone to stabilize, then focuses the first tabbable element.
     * @returns Returns a promise that resolves with a boolean, depending
     * on whether focus was moved successfully.
     */
    focusInitialElementWhenReady(options?: FocusOptions): Promise<boolean>;
    /**
     * Waits for the zone to stabilize, then focuses
     * the first tabbable element within the focus trap region.
     * @returns Returns a promise that resolves with a boolean, depending
     * on whether focus was moved successfully.
     */
    focusFirstTabbableElementWhenReady(options?: FocusOptions): Promise<boolean>;
    /**
     * Waits for the zone to stabilize, then focuses
     * the last tabbable element within the focus trap region.
     * @returns Returns a promise that resolves with a boolean, depending
     * on whether focus was moved successfully.
     */
    focusLastTabbableElementWhenReady(options?: FocusOptions): Promise<boolean>;
    /**
     * Get the specified boundary element of the trapped region.
     * @param bound The boundary to get (start or end of trapped region).
     * @returns The boundary element.
     */
    private _getRegionBoundary;
    /**
     * Focuses the element that should be focused when the focus trap is initialized.
     * @returns Whether focus was moved successfully.
     */
    focusInitialElement(options?: FocusOptions): boolean;
    /**
     * Focuses the first tabbable element within the focus trap region.
     * @returns Whether focus was moved successfully.
     */
    focusFirstTabbableElement(options?: FocusOptions): boolean;
    /**
     * Focuses the last tabbable element within the focus trap region.
     * @returns Whether focus was moved successfully.
     */
    focusLastTabbableElement(options?: FocusOptions): boolean;
    /**
     * Checks whether the focus trap has successfully been attached.
     */
    hasAttached(): boolean;
    /** Get the first tabbable element from a DOM subtree (inclusive). */
    private _getFirstTabbableElement;
    /** Get the last tabbable element from a DOM subtree (inclusive). */
    private _getLastTabbableElement;
    /** Creates an anchor element. */
    private _createAnchor;
    /**
     * Toggles the `tabindex` of an anchor, based on the enabled state of the focus trap.
     * @param isEnabled Whether the focus trap is enabled.
     * @param anchor Anchor on which to toggle the tabindex.
     */
    private _toggleAnchorTabIndex;
    /**
     * Toggles the`tabindex` of both anchors to either trap Tab focus or allow it to escape.
     * @param enabled: Whether the anchors should trap Tab.
     */
    protected toggleAnchors(enabled: boolean): void;
    /** Executes a function when the zone is stable. */
    private _executeOnStable;
}

/**
 * Factory that allows easy instantiation of focus traps.
 * @deprecated Use `ConfigurableFocusTrapFactory` instead.
 * @breaking-change 11.0.0
 */
export declare class FocusTrapFactory {
    private _checker;
    private _ngZone;
    private _document;
    constructor(_checker: InteractivityChecker, _ngZone: NgZone, _document: any);
    /**
     * Creates a focus-trapped region around the given element.
     * @param element The element around which focus will be trapped.
     * @param deferCaptureElements Defers the creation of focus-capturing elements to be done
     *     manually by the user.
     * @returns The created focus trap instance.
     */
    create(element: HTMLElement, deferCaptureElements?: boolean): FocusTrap;
    static ɵfac: i0.ɵɵFactoryDeclaration<FocusTrapFactory, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<FocusTrapFactory>;
}

/**
 * A strategy that dictates how FocusTrap should prevent elements
 * outside of the FocusTrap from being focused.
 */
export declare interface FocusTrapInertStrategy {
    /** Makes all elements outside focusTrap unfocusable. */
    preventFocus(focusTrap: FocusTrap): void;
    /** Reverts elements made unfocusable by preventFocus to their previous state. */
    allowFocus(focusTrap: FocusTrap): void;
}

/** Injectable that ensures only the most recently enabled FocusTrap is active. */
declare class FocusTrapManager {
    private _focusTrapStack;
    /**
     * Disables the FocusTrap at the top of the stack, and then pushes
     * the new FocusTrap onto the stack.
     */
    register(focusTrap: ManagedFocusTrap): void;
    /**
     * Removes the FocusTrap from the stack, and activates the
     * FocusTrap that is the new top of the stack.
     */
    deregister(focusTrap: ManagedFocusTrap): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<FocusTrapManager, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<FocusTrapManager>;
}

/** Set of possible high-contrast mode backgrounds. */
export declare const enum HighContrastMode {
    NONE = 0,
    BLACK_ON_WHITE = 1,
    WHITE_ON_BLACK = 2
}

/**
 * Service to determine whether the browser is currently in a high-contrast-mode environment.
 *
 * Microsoft Windows supports an accessibility feature called "High Contrast Mode". This mode
 * changes the appearance of all applications, including web applications, to dramatically increase
 * contrast.
 *
 * IE, Edge, and Firefox currently support this mode. Chrome does not support Windows High Contrast
 * Mode. This service does not detect high-contrast mode as added by the Chrome "High Contrast"
 * browser extension.
 */
export declare class HighContrastModeDetector implements OnDestroy {
    private _platform;
    /**
     * Figuring out the high contrast mode and adding the body classes can cause
     * some expensive layouts. This flag is used to ensure that we only do it once.
     */
    private _hasCheckedHighContrastMode;
    private _document;
    private _breakpointSubscription;
    constructor(_platform: Platform, document: any);
    /** Gets the current high-contrast-mode for the page. */
    getHighContrastMode(): HighContrastMode;
    ngOnDestroy(): void;
    /** Applies CSS classes indicating high-contrast mode to document body (browser-only). */
    _applyBodyHighContrastModeCssClasses(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<HighContrastModeDetector, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<HighContrastModeDetector>;
}

/**
 * This is the interface for highlightable items (used by the ActiveDescendantKeyManager).
 * Each item must know how to style itself as active or inactive and whether or not it is
 * currently disabled.
 */
export declare interface Highlightable extends ListKeyManagerOption {
    /** Applies the styles for an active item to this item. */
    setActiveStyles(): void;
    /** Applies the styles for an inactive item to this item. */
    setInactiveStyles(): void;
}

declare namespace i1 {
    export {
        LiveAnnouncer,
        CdkAriaLive
    }
}

declare namespace i2 {
    export {
        FocusTrap,
        FocusTrapFactory,
        CdkTrapFocus
    }
}

declare namespace i3 {
    export {
        FocusOrigin,
        FocusOptions_2 as FocusOptions,
        FocusMonitorDetectionMode,
        FocusMonitorOptions,
        FOCUS_MONITOR_DEFAULT_OPTIONS,
        FocusMonitor,
        CdkMonitorFocus
    }
}

/**
 * Default options for the InputModalityDetector.
 *
 * Modifier keys are ignored by default (i.e. when pressed won't cause the service to detect
 * keyboard input modality) for two reasons:
 *
 * 1. Modifier keys are commonly used with mouse to perform actions such as 'right click' or 'open
 *    in new tab', and are thus less representative of actual keyboard interaction.
 * 2. VoiceOver triggers some keyboard events when linearly navigating with Control + Option (but
 *    confusingly not with Caps Lock). Thus, to have parity with other screen readers, we ignore
 *    these keys so as to not update the input modality.
 *
 * Note that we do not by default ignore the right Meta key on Safari because it has the same key
 * code as the ContextMenu key on other browsers. When we switch to using event.key, we can
 * distinguish between the two.
 */
export declare const INPUT_MODALITY_DETECTOR_DEFAULT_OPTIONS: InputModalityDetectorOptions;

/**
 * Injectable options for the InputModalityDetector. These are shallowly merged with the default
 * options.
 */
export declare const INPUT_MODALITY_DETECTOR_OPTIONS: InjectionToken<InputModalityDetectorOptions>;

/**
 * The input modalities detected by this service. Null is used if the input modality is unknown.
 */
export declare type InputModality = 'keyboard' | 'mouse' | 'touch' | null;

/**
 * Service that detects the user's input modality.
 *
 * This service does not update the input modality when a user navigates with a screen reader
 * (e.g. linear navigation with VoiceOver, object navigation / browse mode with NVDA, virtual PC
 * cursor mode with JAWS). This is in part due to technical limitations (i.e. keyboard events do not
 * fire as expected in these modes) but is also arguably the correct behavior. Navigating with a
 * screen reader is akin to visually scanning a page, and should not be interpreted as actual user
 * input interaction.
 *
 * When a user is not navigating but *interacting* with a screen reader, this service attempts to
 * update the input modality to keyboard, but in general this service's behavior is largely
 * undefined.
 */
export declare class InputModalityDetector implements OnDestroy {
    private readonly _platform;
    /** Emits whenever an input modality is detected. */
    readonly modalityDetected: Observable<InputModality>;
    /** Emits when the input modality changes. */
    readonly modalityChanged: Observable<InputModality>;
    /** The most recently detected input modality. */
    get mostRecentModality(): InputModality;
    /**
     * The most recently detected input modality event target. Is null if no input modality has been
     * detected or if the associated event target is null for some unknown reason.
     */
    _mostRecentTarget: HTMLElement | null;
    /** The underlying BehaviorSubject that emits whenever an input modality is detected. */
    private readonly _modality;
    /** Options for this InputModalityDetector. */
    private readonly _options;
    /**
     * The timestamp of the last touch input modality. Used to determine whether mousedown events
     * should be attributed to mouse or touch.
     */
    private _lastTouchMs;
    /**
     * Handles keydown events. Must be an arrow function in order to preserve the context when it gets
     * bound.
     */
    private _onKeydown;
    /**
     * Handles mousedown events. Must be an arrow function in order to preserve the context when it
     * gets bound.
     */
    private _onMousedown;
    /**
     * Handles touchstart events. Must be an arrow function in order to preserve the context when it
     * gets bound.
     */
    private _onTouchstart;
    constructor(_platform: Platform, ngZone: NgZone, document: Document, options?: InputModalityDetectorOptions);
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<InputModalityDetector, [null, null, null, { optional: true; }]>;
    static ɵprov: i0.ɵɵInjectableDeclaration<InputModalityDetector>;
}

/** Options to configure the behavior of the InputModalityDetector. */
export declare interface InputModalityDetectorOptions {
    /** Keys to ignore when detecting keyboard input modality. */
    ignoreKeys?: number[];
}

/**
 * Utility for checking the interactivity of an element, such as whether is is focusable or
 * tabbable.
 */
export declare class InteractivityChecker {
    private _platform;
    constructor(_platform: Platform);
    /**
     * Gets whether an element is disabled.
     *
     * @param element Element to be checked.
     * @returns Whether the element is disabled.
     */
    isDisabled(element: HTMLElement): boolean;
    /**
     * Gets whether an element is visible for the purposes of interactivity.
     *
     * This will capture states like `display: none` and `visibility: hidden`, but not things like
     * being clipped by an `overflow: hidden` parent or being outside the viewport.
     *
     * @returns Whether the element is visible.
     */
    isVisible(element: HTMLElement): boolean;
    /**
     * Gets whether an element can be reached via Tab key.
     * Assumes that the element has already been checked with isFocusable.
     *
     * @param element Element to be checked.
     * @returns Whether the element is tabbable.
     */
    isTabbable(element: HTMLElement): boolean;
    /**
     * Gets whether an element can be focused by the user.
     *
     * @param element Element to be checked.
     * @param config The config object with options to customize this method's behavior
     * @returns Whether the element is focusable.
     */
    isFocusable(element: HTMLElement, config?: IsFocusableConfig): boolean;
    static ɵfac: i0.ɵɵFactoryDeclaration<InteractivityChecker, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<InteractivityChecker>;
}


/** Gets whether an event could be a faked `mousedown` event dispatched by a screen reader. */
export declare function isFakeMousedownFromScreenReader(event: MouseEvent): boolean;

/** Gets whether an event could be a faked `touchstart` event dispatched by a screen reader. */
export declare function isFakeTouchstartFromScreenReader(event: TouchEvent): boolean;

/**
 * Configuration for the isFocusable method.
 */
export declare class IsFocusableConfig {
    /**
     * Whether to count an element as focusable even if it is not currently visible.
     */
    ignoreVisibility: boolean;
}

/**
 * This class manages keyboard events for selectable lists. If you pass it a query list
 * of items, it will set the active item correctly when arrow events occur.
 */
export declare class ListKeyManager<T extends ListKeyManagerOption> {
    private _items;
    private _activeItemIndex;
    private _activeItem;
    private _wrap;
    private readonly _letterKeyStream;
    private _typeaheadSubscription;
    private _vertical;
    private _horizontal;
    private _allowedModifierKeys;
    private _homeAndEnd;
    /**
     * Predicate function that can be used to check whether an item should be skipped
     * by the key manager. By default, disabled items are skipped.
     */
    private _skipPredicateFn;
    private _pressedLetters;
    constructor(_items: QueryList<T> | T[]);
    /**
     * Stream that emits any time the TAB key is pressed, so components can react
     * when focus is shifted off of the list.
     */
    readonly tabOut: Subject<void>;
    /** Stream that emits whenever the active item of the list manager changes. */
    readonly change: Subject<number>;
    /**
     * Sets the predicate function that determines which items should be skipped by the
     * list key manager.
     * @param predicate Function that determines whether the given item should be skipped.
     */
    skipPredicate(predicate: (item: T) => boolean): this;
    /**
     * Configures wrapping mode, which determines whether the active item will wrap to
     * the other end of list when there are no more items in the given direction.
     * @param shouldWrap Whether the list should wrap when reaching the end.
     */
    withWrap(shouldWrap?: boolean): this;
    /**
     * Configures whether the key manager should be able to move the selection vertically.
     * @param enabled Whether vertical selection should be enabled.
     */
    withVerticalOrientation(enabled?: boolean): this;
    /**
     * Configures the key manager to move the selection horizontally.
     * Passing in `null` will disable horizontal movement.
     * @param direction Direction in which the selection can be moved.
     */
    withHorizontalOrientation(direction: 'ltr' | 'rtl' | null): this;
    /**
     * Modifier keys which are allowed to be held down and whose default actions will be prevented
     * as the user is pressing the arrow keys. Defaults to not allowing any modifier keys.
     */
    withAllowedModifierKeys(keys: ListKeyManagerModifierKey[]): this;
    /**
     * Turns on typeahead mode which allows users to set the active item by typing.
     * @param debounceInterval Time to wait after the last keystroke before setting the active item.
     */
    withTypeAhead(debounceInterval?: number): this;
    /**
     * Configures the key manager to activate the first and last items
     * respectively when the Home or End key is pressed.
     * @param enabled Whether pressing the Home or End key activates the first/last item.
     */
    withHomeAndEnd(enabled?: boolean): this;
    /**
     * Sets the active item to the item at the index specified.
     * @param index The index of the item to be set as active.
     */
    setActiveItem(index: number): void;
    /**
     * Sets the active item to the specified item.
     * @param item The item to be set as active.
     */
    setActiveItem(item: T): void;
    /**
     * Sets the active item depending on the key event passed in.
     * @param event Keyboard event to be used for determining which element should be active.
     */
    onKeydown(event: KeyboardEvent): void;
    /** Index of the currently active item. */
    get activeItemIndex(): number | null;
    /** The active item. */
    get activeItem(): T | null;
    /** Gets whether the user is currently typing into the manager using the typeahead feature. */
    isTyping(): boolean;
    /** Sets the active item to the first enabled item in the list. */
    setFirstItemActive(): void;
    /** Sets the active item to the last enabled item in the list. */
    setLastItemActive(): void;
    /** Sets the active item to the next enabled item in the list. */
    setNextItemActive(): void;
    /** Sets the active item to a previous enabled item in the list. */
    setPreviousItemActive(): void;
    /**
     * Allows setting the active without any other effects.
     * @param index Index of the item to be set as active.
     */
    updateActiveItem(index: number): void;
    /**
     * Allows setting the active item without any other effects.
     * @param item Item to be set as active.
     */
    updateActiveItem(item: T): void;
    /**
     * This method sets the active item, given a list of items and the delta between the
     * currently active item and the new active item. It will calculate differently
     * depending on whether wrap mode is turned on.
     */
    private _setActiveItemByDelta;
    /**
     * Sets the active item properly given "wrap" mode. In other words, it will continue to move
     * down the list until it finds an item that is not disabled, and it will wrap if it
     * encounters either end of the list.
     */
    private _setActiveInWrapMode;
    /**
     * Sets the active item properly given the default mode. In other words, it will
     * continue to move down the list until it finds an item that is not disabled. If
     * it encounters either end of the list, it will stop and not wrap.
     */
    private _setActiveInDefaultMode;
    /**
     * Sets the active item to the first enabled item starting at the index specified. If the
     * item is disabled, it will move in the fallbackDelta direction until it either
     * finds an enabled item or encounters the end of the list.
     */
    private _setActiveItemByIndex;
    /** Returns the items as an array. */
    private _getItemsArray;
}

/** Modifier keys handled by the ListKeyManager. */
export declare type ListKeyManagerModifierKey = 'altKey' | 'ctrlKey' | 'metaKey' | 'shiftKey';

/** This interface is for items that can be passed to a ListKeyManager. */
export declare interface ListKeyManagerOption {
    /** Whether the option is disabled. */
    disabled?: boolean;
    /** Gets the label for this option. */
    getLabel?(): string;
}

/** Injection token that can be used to configure the default options for the LiveAnnouncer. */
export declare const LIVE_ANNOUNCER_DEFAULT_OPTIONS: InjectionToken<LiveAnnouncerDefaultOptions>;

export declare const LIVE_ANNOUNCER_ELEMENT_TOKEN: InjectionToken<HTMLElement | null>;

/** @docs-private */
export declare function LIVE_ANNOUNCER_ELEMENT_TOKEN_FACTORY(): null;

export declare class LiveAnnouncer implements OnDestroy {
    private _ngZone;
    private _defaultOptions?;
    private _liveElement;
    private _document;
    private _previousTimeout;
    private _currentPromise;
    private _currentResolve;
    constructor(elementToken: any, _ngZone: NgZone, _document: any, _defaultOptions?: LiveAnnouncerDefaultOptions | undefined);
    /**
     * Announces a message to screen readers.
     * @param message Message to be announced to the screen reader.
     * @returns Promise that will be resolved when the message is added to the DOM.
     */
    announce(message: string): Promise<void>;
    /**
     * Announces a message to screen readers.
     * @param message Message to be announced to the screen reader.
     * @param politeness The politeness of the announcer element.
     * @returns Promise that will be resolved when the message is added to the DOM.
     */
    announce(message: string, politeness?: AriaLivePoliteness): Promise<void>;
    /**
     * Announces a message to screen readers.
     * @param message Message to be announced to the screen reader.
     * @param duration Time in milliseconds after which to clear out the announcer element. Note
     *   that this takes effect after the message has been added to the DOM, which can be up to
     *   100ms after `announce` has been called.
     * @returns Promise that will be resolved when the message is added to the DOM.
     */
    announce(message: string, duration?: number): Promise<void>;
    /**
     * Announces a message to screen readers.
     * @param message Message to be announced to the screen reader.
     * @param politeness The politeness of the announcer element.
     * @param duration Time in milliseconds after which to clear out the announcer element. Note
     *   that this takes effect after the message has been added to the DOM, which can be up to
     *   100ms after `announce` has been called.
     * @returns Promise that will be resolved when the message is added to the DOM.
     */
    announce(message: string, politeness?: AriaLivePoliteness, duration?: number): Promise<void>;
    /**
     * Clears the current text from the announcer element. Can be used to prevent
     * screen readers from reading the text out again while the user is going
     * through the page landmarks.
     */
    clear(): void;
    ngOnDestroy(): void;
    private _createLiveElement;
    static ɵfac: i0.ɵɵFactoryDeclaration<LiveAnnouncer, [{ optional: true; }, null, null, { optional: true; }]>;
    static ɵprov: i0.ɵɵInjectableDeclaration<LiveAnnouncer>;
}

/** Object that can be used to configure the default options for the LiveAnnouncer. */
export declare interface LiveAnnouncerDefaultOptions {
    /** Default politeness for the announcements. */
    politeness?: AriaLivePoliteness;
    /** Default duration for the announcement messages. */
    duration?: number;
}

/**
 * A FocusTrap managed by FocusTrapManager.
 * Implemented by ConfigurableFocusTrap to avoid circular dependency.
 */
declare interface ManagedFocusTrap {
    _enable(): void;
    _disable(): void;
    focusInitialElementWhenReady(): Promise<boolean>;
}

/**
 * ID used for the body container where all messages are appended.
 * @deprecated No longer being used. To be removed.
 * @breaking-change 14.0.0
 */
export declare const MESSAGES_CONTAINER_ID = "cdk-describedby-message-container";

/**
 * Interface used to register message elements and keep a count of how many registrations have
 * the same message and the reference to the message element used for the `aria-describedby`.
 */
export declare interface RegisteredMessage {
    /** The element containing the message. */
    messageElement: Element;
    /** The number of elements that reference this message element via `aria-describedby`. */
    referenceCount: number;
}

export { }
