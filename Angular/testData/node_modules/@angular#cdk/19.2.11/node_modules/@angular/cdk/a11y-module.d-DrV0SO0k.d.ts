import * as i0 from '@angular/core';
import { NgZone, Injector, OnDestroy, AfterContentInit, OnChanges, DoCheck, SimpleChanges, InjectionToken } from '@angular/core';
import { ObserversModule } from './observers/index.js';
import { CdkMonitorFocus } from './focus-monitor.d-BBkiOKUH.js';

/**
 * Configuration for the isFocusable method.
 */
declare class IsFocusableConfig {
    /**
     * Whether to count an element as focusable even if it is not currently visible.
     */
    ignoreVisibility: boolean;
}
/**
 * Utility for checking the interactivity of an element, such as whether it is focusable or
 * tabbable.
 */
declare class InteractivityChecker {
    private _platform;
    constructor(...args: unknown[]);
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

/**
 * Class that allows for trapping focus within a DOM element.
 *
 * This class currently uses a relatively simple approach to focus trapping.
 * It assumes that the tab order is the same as DOM order, which is not necessarily true.
 * Things like `tabIndex > 0`, flex `order`, and shadow roots can cause the two to be misaligned.
 */
declare class FocusTrap {
    readonly _element: HTMLElement;
    private _checker;
    readonly _ngZone: NgZone;
    readonly _document: Document;
    /** @breaking-change 20.0.0 param to become required */
    readonly _injector?: Injector | undefined;
    private _startAnchor;
    private _endAnchor;
    private _hasAttached;
    protected startAnchorListener: () => boolean;
    protected endAnchorListener: () => boolean;
    /** Whether the focus trap is active. */
    get enabled(): boolean;
    set enabled(value: boolean);
    protected _enabled: boolean;
    constructor(_element: HTMLElement, _checker: InteractivityChecker, _ngZone: NgZone, _document: Document, deferAnchors?: boolean, 
    /** @breaking-change 20.0.0 param to become required */
    _injector?: Injector | undefined);
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
 */
declare class FocusTrapFactory {
    private _checker;
    private _ngZone;
    private _document;
    private _injector;
    constructor(...args: unknown[]);
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
/** Directive for trapping focus within a region. */
declare class CdkTrapFocus implements OnDestroy, AfterContentInit, OnChanges, DoCheck {
    private _elementRef;
    private _focusTrapFactory;
    /** Underlying FocusTrap instance. */
    focusTrap: FocusTrap;
    /** Previously focused element to restore focus to upon destroy when using autoCapture. */
    private _previouslyFocusedElement;
    /** Whether the focus trap is active. */
    get enabled(): boolean;
    set enabled(value: boolean);
    /**
     * Whether the directive should automatically move focus into the trapped region upon
     * initialization and return focus to the previous activeElement upon destruction.
     */
    autoCapture: boolean;
    constructor(...args: unknown[]);
    ngOnDestroy(): void;
    ngAfterContentInit(): void;
    ngDoCheck(): void;
    ngOnChanges(changes: SimpleChanges): void;
    private _captureFocus;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkTrapFocus, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkTrapFocus, "[cdkTrapFocus]", ["cdkTrapFocus"], { "enabled": { "alias": "cdkTrapFocus"; "required": false; }; "autoCapture": { "alias": "cdkTrapFocusAutoCapture"; "required": false; }; }, {}, never, never, true, never>;
    static ngAcceptInputType_enabled: unknown;
    static ngAcceptInputType_autoCapture: unknown;
}

/** Possible politeness levels. */
type AriaLivePoliteness = 'off' | 'polite' | 'assertive';
declare const LIVE_ANNOUNCER_ELEMENT_TOKEN: InjectionToken<HTMLElement | null>;
/**
 * @docs-private
 * @deprecated No longer used, will be removed.
 * @breaking-change 21.0.0
 */
declare function LIVE_ANNOUNCER_ELEMENT_TOKEN_FACTORY(): null;
/** Object that can be used to configure the default options for the LiveAnnouncer. */
interface LiveAnnouncerDefaultOptions {
    /** Default politeness for the announcements. */
    politeness?: AriaLivePoliteness;
    /** Default duration for the announcement messages. */
    duration?: number;
}
/** Injection token that can be used to configure the default options for the LiveAnnouncer. */
declare const LIVE_ANNOUNCER_DEFAULT_OPTIONS: InjectionToken<LiveAnnouncerDefaultOptions>;

declare class LiveAnnouncer implements OnDestroy {
    private _ngZone;
    private _defaultOptions;
    private _liveElement;
    private _document;
    private _previousTimeout;
    private _currentPromise;
    private _currentResolve;
    constructor(...args: unknown[]);
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
    /**
     * Some browsers won't expose the accessibility node of the live announcer element if there is an
     * `aria-modal` and the live announcer is outside of it. This method works around the issue by
     * pointing the `aria-owns` of all modals to the live announcer element.
     */
    private _exposeAnnouncerToModals;
    static ɵfac: i0.ɵɵFactoryDeclaration<LiveAnnouncer, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<LiveAnnouncer>;
}
/**
 * A directive that works similarly to aria-live, but uses the LiveAnnouncer to ensure compatibility
 * with a wider range of browsers and screen readers.
 */
declare class CdkAriaLive implements OnDestroy {
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
    constructor(...args: unknown[]);
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkAriaLive, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkAriaLive, "[cdkAriaLive]", ["cdkAriaLive"], { "politeness": { "alias": "cdkAriaLive"; "required": false; }; "duration": { "alias": "cdkAriaLiveDuration"; "required": false; }; }, {}, never, never, true, never>;
}

declare class A11yModule {
    constructor();
    static ɵfac: i0.ɵɵFactoryDeclaration<A11yModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<A11yModule, never, [typeof ObserversModule, typeof CdkAriaLive, typeof CdkTrapFocus, typeof CdkMonitorFocus], [typeof CdkAriaLive, typeof CdkTrapFocus, typeof CdkMonitorFocus]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<A11yModule>;
}

export { A11yModule, CdkAriaLive, CdkTrapFocus, FocusTrap, FocusTrapFactory, InteractivityChecker, IsFocusableConfig, LIVE_ANNOUNCER_DEFAULT_OPTIONS, LIVE_ANNOUNCER_ELEMENT_TOKEN, LIVE_ANNOUNCER_ELEMENT_TOKEN_FACTORY, LiveAnnouncer };
export type { AriaLivePoliteness, LiveAnnouncerDefaultOptions };
