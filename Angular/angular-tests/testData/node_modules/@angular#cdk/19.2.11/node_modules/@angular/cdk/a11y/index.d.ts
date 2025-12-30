import * as i0 from '@angular/core';
import { OnDestroy, Provider, QueryList, InjectionToken, NgZone, Injector } from '@angular/core';
export { ActiveDescendantKeyManager, Highlightable } from '../activedescendant-key-manager.d-DSYvyoT0.js';
export { FocusKeyManager, FocusableOption } from '../focus-key-manager.d-DCiEwxN7.js';
export { ListKeyManager, ListKeyManagerModifierKey, ListKeyManagerOption } from '../list-key-manager.d-CylnKWfo.js';
import { Subject, Observable } from 'rxjs';
import { TreeKeyManagerItem, TreeKeyManagerStrategy, TreeKeyManagerFactory, TreeKeyManagerOptions } from '../tree-key-manager-strategy.d-DipnXoCr.js';
import { FocusTrap, InteractivityChecker } from '../a11y-module.d-DrV0SO0k.js';
export { A11yModule, AriaLivePoliteness, CdkAriaLive, CdkTrapFocus, FocusTrapFactory, IsFocusableConfig, LIVE_ANNOUNCER_DEFAULT_OPTIONS, LIVE_ANNOUNCER_ELEMENT_TOKEN, LIVE_ANNOUNCER_ELEMENT_TOKEN_FACTORY, LiveAnnouncer, LiveAnnouncerDefaultOptions } from '../a11y-module.d-DrV0SO0k.js';
export { CdkMonitorFocus, FOCUS_MONITOR_DEFAULT_OPTIONS, FocusMonitor, FocusMonitorDetectionMode, FocusMonitorOptions, FocusOptions, FocusOrigin } from '../focus-monitor.d-BBkiOKUH.js';
import '../observers/index.js';
import '../number-property.d-BzBQchZ2.js';

/**
 * Interface used to register message elements and keep a count of how many registrations have
 * the same message and the reference to the message element used for the `aria-describedby`.
 */
interface RegisteredMessage {
    /** The element containing the message. */
    messageElement: Element;
    /** The number of elements that reference this message element via `aria-describedby`. */
    referenceCount: number;
}
/**
 * ID used for the body container where all messages are appended.
 * @deprecated No longer being used. To be removed.
 * @breaking-change 14.0.0
 */
declare const MESSAGES_CONTAINER_ID = "cdk-describedby-message-container";
/**
 * ID prefix used for each created message element.
 * @deprecated To be turned into a private variable.
 * @breaking-change 14.0.0
 */
declare const CDK_DESCRIBEDBY_ID_PREFIX = "cdk-describedby-message";
/**
 * Attribute given to each host element that is described by a message element.
 * @deprecated To be turned into a private variable.
 * @breaking-change 14.0.0
 */
declare const CDK_DESCRIBEDBY_HOST_ATTRIBUTE = "cdk-describedby-host";
/**
 * Utility that creates visually hidden elements with a message content. Useful for elements that
 * want to use aria-describedby to further describe themselves without adding additional visual
 * content.
 */
declare class AriaDescriber implements OnDestroy {
    private _platform;
    private _document;
    /** Map of all registered message elements that have been placed into the document. */
    private _messageRegistry;
    /** Container for all registered messages. */
    private _messagesContainer;
    /** Unique ID for the service. */
    private readonly _id;
    constructor(...args: unknown[]);
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

/**
 * Adds the given ID to the specified ARIA attribute on an element.
 * Used for attributes such as aria-labelledby, aria-owns, etc.
 */
declare function addAriaReferencedId(el: Element, attr: `aria-${string}`, id: string): void;
/**
 * Removes the given ID from the specified ARIA attribute on an element.
 * Used for attributes such as aria-labelledby, aria-owns, etc.
 */
declare function removeAriaReferencedId(el: Element, attr: `aria-${string}`, id: string): void;
/**
 * Gets the list of IDs referenced by the given ARIA attribute on an element.
 * Used for attributes such as aria-labelledby, aria-owns, etc.
 */
declare function getAriaReferenceIds(el: Element, attr: string): string[];

/**
 * @docs-private
 *
 * Opt-out of Tree of key manager behavior.
 *
 * When provided, Tree has same focus management behavior as before TreeKeyManager was introduced.
 *  - Tree does not respond to keyboard interaction
 *  - Tree node allows tabindex to be set by Input binding
 *  - Tree node allows tabindex to be set by attribute binding
 *
 * @deprecated NoopTreeKeyManager deprecated. Use TreeKeyManager or inject a
 * TreeKeyManagerStrategy instead. To be removed in a future version.
 *
 * @breaking-change 21.0.0
 */
declare class NoopTreeKeyManager<T extends TreeKeyManagerItem> implements TreeKeyManagerStrategy<T> {
    readonly _isNoopTreeKeyManager = true;
    readonly change: Subject<T | null>;
    destroy(): void;
    onKeydown(): void;
    getActiveItemIndex(): null;
    getActiveItem(): null;
    focusItem(): void;
}
/**
 * @docs-private
 *
 * Opt-out of Tree of key manager behavior.
 *
 * When provided, Tree has same focus management behavior as before TreeKeyManager was introduced.
 *  - Tree does not respond to keyboard interaction
 *  - Tree node allows tabindex to be set by Input binding
 *  - Tree node allows tabindex to be set by attribute binding
 *
 * @deprecated NoopTreeKeyManager deprecated. Use TreeKeyManager or inject a
 * TreeKeyManagerStrategy instead. To be removed in a future version.
 *
 * @breaking-change 21.0.0
 */
declare function NOOP_TREE_KEY_MANAGER_FACTORY<T extends TreeKeyManagerItem>(): TreeKeyManagerFactory<T>;
/**
 * @docs-private
 *
 * Opt-out of Tree of key manager behavior.
 *
 * When provided, Tree has same focus management behavior as before TreeKeyManager was introduced.
 *  - Tree does not respond to keyboard interaction
 *  - Tree node allows tabindex to be set by Input binding
 *  - Tree node allows tabindex to be set by attribute binding
 *
 * @deprecated NoopTreeKeyManager deprecated. Use TreeKeyManager or inject a
 * TreeKeyManagerStrategy instead. To be removed in a future version.
 *
 * @breaking-change 21.0.0
 */
declare const NOOP_TREE_KEY_MANAGER_FACTORY_PROVIDER: Provider;

/**
 * This class manages keyboard events for trees. If you pass it a QueryList or other list of tree
 * items, it will set the active item, focus, handle expansion and typeahead correctly when
 * keyboard events occur.
 */
declare class TreeKeyManager<T extends TreeKeyManagerItem> implements TreeKeyManagerStrategy<T> {
    /** The index of the currently active (focused) item. */
    private _activeItemIndex;
    /** The currently active (focused) item. */
    private _activeItem;
    /** Whether or not we activate the item when it's focused. */
    private _shouldActivationFollowFocus;
    /**
     * The orientation that the tree is laid out in. In `rtl` mode, the behavior of Left and
     * Right arrow are switched.
     */
    private _horizontalOrientation;
    /**
     * Predicate function that can be used to check whether an item should be skipped
     * by the key manager.
     *
     * The default value for this doesn't skip any elements in order to keep tree items focusable
     * when disabled. This aligns with ARIA guidelines:
     * https://www.w3.org/WAI/ARIA/apg/practices/keyboard-interface/#focusabilityofdisabledcontrols.
     */
    private _skipPredicateFn;
    /** Function to determine equivalent items. */
    private _trackByFn;
    /** Synchronous cache of the items to manage. */
    private _items;
    private _typeahead?;
    private _typeaheadSubscription;
    private _hasInitialFocused;
    private _initializeFocus;
    /**
     *
     * @param items List of TreeKeyManager options. Can be synchronous or asynchronous.
     * @param config Optional configuration options. By default, use 'ltr' horizontal orientation. By
     * default, do not skip any nodes. By default, key manager only calls `focus` method when items
     * are focused and does not call `activate`. If `typeaheadDefaultInterval` is `true`, use a
     * default interval of 200ms.
     */
    constructor(items: Observable<T[]> | QueryList<T> | T[], config: TreeKeyManagerOptions<T>);
    /** Stream that emits any time the focused item changes. */
    readonly change: Subject<T | null>;
    /** Cleans up the key manager. */
    destroy(): void;
    /**
     * Handles a keyboard event on the tree.
     * @param event Keyboard event that represents the user interaction with the tree.
     */
    onKeydown(event: KeyboardEvent): void;
    /** Index of the currently active item. */
    getActiveItemIndex(): number | null;
    /** The currently active item. */
    getActiveItem(): T | null;
    /** Focus the first available item. */
    private _focusFirstItem;
    /** Focus the last available item. */
    private _focusLastItem;
    /** Focus the next available item. */
    private _focusNextItem;
    /** Focus the previous available item. */
    private _focusPreviousItem;
    /**
     * Focus the provided item by index.
     * @param index The index of the item to focus.
     * @param options Additional focusing options.
     */
    focusItem(index: number, options?: {
        emitChangeEvent?: boolean;
    }): void;
    focusItem(item: T, options?: {
        emitChangeEvent?: boolean;
    }): void;
    focusItem(itemOrIndex: number | T, options?: {
        emitChangeEvent?: boolean;
    }): void;
    private _updateActiveItemIndex;
    private _setTypeAhead;
    private _findNextAvailableItemIndex;
    private _findPreviousAvailableItemIndex;
    /**
     * If the item is already expanded, we collapse the item. Otherwise, we will focus the parent.
     */
    private _collapseCurrentItem;
    /**
     * If the item is already collapsed, we expand the item. Otherwise, we will focus the first child.
     */
    private _expandCurrentItem;
    private _isCurrentItemExpanded;
    private _isItemDisabled;
    /** For all items that are the same level as the current item, we expand those items. */
    private _expandAllItemsAtCurrentItemLevel;
    private _activateCurrentItem;
}
/**
 * @docs-private
 * @deprecated No longer used, will be removed.
 * @breaking-change 21.0.0
 */
declare function TREE_KEY_MANAGER_FACTORY<T extends TreeKeyManagerItem>(): TreeKeyManagerFactory<T>;
/** Injection token that determines the key manager to use. */
declare const TREE_KEY_MANAGER: InjectionToken<TreeKeyManagerFactory<any>>;
/**
 * @docs-private
 * @deprecated No longer used, will be removed.
 * @breaking-change 21.0.0
 */
declare const TREE_KEY_MANAGER_FACTORY_PROVIDER: {
    provide: InjectionToken<TreeKeyManagerFactory<any>>;
    useFactory: typeof TREE_KEY_MANAGER_FACTORY;
};

/**
 * Options for creating a ConfigurableFocusTrap.
 */
interface ConfigurableFocusTrapConfig {
    /**
     * Whether to defer the creation of FocusTrap elements to be done manually by the user.
     */
    defer: boolean;
}

/** The injection token used to specify the inert strategy. */
declare const FOCUS_TRAP_INERT_STRATEGY: InjectionToken<FocusTrapInertStrategy>;
/**
 * A strategy that dictates how FocusTrap should prevent elements
 * outside of the FocusTrap from being focused.
 */
interface FocusTrapInertStrategy {
    /** Makes all elements outside focusTrap unfocusable. */
    preventFocus(focusTrap: FocusTrap): void;
    /** Reverts elements made unfocusable by preventFocus to their previous state. */
    allowFocus(focusTrap: FocusTrap): void;
}

/**
 * A FocusTrap managed by FocusTrapManager.
 * Implemented by ConfigurableFocusTrap to avoid circular dependency.
 */
interface ManagedFocusTrap {
    _enable(): void;
    _disable(): void;
    focusInitialElementWhenReady(): Promise<boolean>;
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

/**
 * Class that allows for trapping focus within a DOM element.
 *
 * This class uses a strategy pattern that determines how it traps focus.
 * See FocusTrapInertStrategy.
 */
declare class ConfigurableFocusTrap extends FocusTrap implements ManagedFocusTrap {
    private _focusTrapManager;
    private _inertStrategy;
    /** Whether the FocusTrap is enabled. */
    get enabled(): boolean;
    set enabled(value: boolean);
    constructor(_element: HTMLElement, _checker: InteractivityChecker, _ngZone: NgZone, _document: Document, _focusTrapManager: FocusTrapManager, _inertStrategy: FocusTrapInertStrategy, config: ConfigurableFocusTrapConfig, injector?: Injector);
    /** Notifies the FocusTrapManager that this FocusTrap will be destroyed. */
    destroy(): void;
    /** @docs-private Implemented as part of ManagedFocusTrap. */
    _enable(): void;
    /** @docs-private Implemented as part of ManagedFocusTrap. */
    _disable(): void;
}

/** Factory that allows easy instantiation of configurable focus traps. */
declare class ConfigurableFocusTrapFactory {
    private _checker;
    private _ngZone;
    private _focusTrapManager;
    private _document;
    private _inertStrategy;
    private readonly _injector;
    constructor(...args: unknown[]);
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
    static ɵfac: i0.ɵɵFactoryDeclaration<ConfigurableFocusTrapFactory, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<ConfigurableFocusTrapFactory>;
}

/**
 * Lightweight FocusTrapInertStrategy that adds a document focus event
 * listener to redirect focus back inside the FocusTrap.
 */
declare class EventListenerFocusTrapInertStrategy implements FocusTrapInertStrategy {
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

/**
 * The input modalities detected by this service. Null is used if the input modality is unknown.
 */
type InputModality = 'keyboard' | 'mouse' | 'touch' | null;
/** Options to configure the behavior of the InputModalityDetector. */
interface InputModalityDetectorOptions {
    /** Keys to ignore when detecting keyboard input modality. */
    ignoreKeys?: number[];
}
/**
 * Injectable options for the InputModalityDetector. These are shallowly merged with the default
 * options.
 */
declare const INPUT_MODALITY_DETECTOR_OPTIONS: InjectionToken<InputModalityDetectorOptions>;
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
declare const INPUT_MODALITY_DETECTOR_DEFAULT_OPTIONS: InputModalityDetectorOptions;
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
declare class InputModalityDetector implements OnDestroy {
    private readonly _platform;
    private readonly _listenerCleanups;
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
    constructor(...args: unknown[]);
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<InputModalityDetector, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<InputModalityDetector>;
}

/** Gets whether an event could be a faked `mousedown` event dispatched by a screen reader. */
declare function isFakeMousedownFromScreenReader(event: MouseEvent): boolean;
/** Gets whether an event could be a faked `touchstart` event dispatched by a screen reader. */
declare function isFakeTouchstartFromScreenReader(event: TouchEvent): boolean;

/** Set of possible high-contrast mode backgrounds. */
declare enum HighContrastMode {
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
declare class HighContrastModeDetector implements OnDestroy {
    private _platform;
    /**
     * Figuring out the high contrast mode and adding the body classes can cause
     * some expensive layouts. This flag is used to ensure that we only do it once.
     */
    private _hasCheckedHighContrastMode;
    private _document;
    private _breakpointSubscription;
    constructor(...args: unknown[]);
    /** Gets the current high-contrast-mode for the page. */
    getHighContrastMode(): HighContrastMode;
    ngOnDestroy(): void;
    /** Applies CSS classes indicating high-contrast mode to document body (browser-only). */
    _applyBodyHighContrastModeCssClasses(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<HighContrastModeDetector, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<HighContrastModeDetector>;
}

/** Service that generates unique IDs for DOM nodes. */
declare class _IdGenerator {
    private _appId;
    /**
     * Generates a unique ID with a specific prefix.
     * @param prefix Prefix to add to the ID.
     */
    getId(prefix: string): string;
    static ɵfac: i0.ɵɵFactoryDeclaration<_IdGenerator, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<_IdGenerator>;
}

export { AriaDescriber, CDK_DESCRIBEDBY_HOST_ATTRIBUTE, CDK_DESCRIBEDBY_ID_PREFIX, ConfigurableFocusTrap, ConfigurableFocusTrapFactory, EventListenerFocusTrapInertStrategy, FOCUS_TRAP_INERT_STRATEGY, FocusTrap, HighContrastMode, HighContrastModeDetector, INPUT_MODALITY_DETECTOR_DEFAULT_OPTIONS, INPUT_MODALITY_DETECTOR_OPTIONS, InputModalityDetector, InteractivityChecker, MESSAGES_CONTAINER_ID, NOOP_TREE_KEY_MANAGER_FACTORY, NOOP_TREE_KEY_MANAGER_FACTORY_PROVIDER, NoopTreeKeyManager, TREE_KEY_MANAGER, TREE_KEY_MANAGER_FACTORY, TREE_KEY_MANAGER_FACTORY_PROVIDER, TreeKeyManager, TreeKeyManagerFactory, TreeKeyManagerItem, TreeKeyManagerOptions, TreeKeyManagerStrategy, _IdGenerator, addAriaReferencedId, getAriaReferenceIds, isFakeMousedownFromScreenReader, isFakeTouchstartFromScreenReader, removeAriaReferencedId };
export type { ConfigurableFocusTrapConfig, FocusTrapInertStrategy, InputModality, InputModalityDetectorOptions, RegisteredMessage };
