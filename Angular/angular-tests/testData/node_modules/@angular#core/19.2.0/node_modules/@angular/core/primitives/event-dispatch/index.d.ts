/**
 * @license Angular v19.2.0
 * (c) 2010-2024 Google LLC. https://angular.io/
 * License: MIT
 */



/**
 * Records information about the action that should handle a given `Event`.
 */
declare interface ActionInfo {
    name: string;
    element: Element;
}

declare type ActionInfoInternal = [name: string, element: Element];


export declare const Attribute: {
    /**
     * The jsaction attribute defines a mapping of a DOM event to a
     * generic event (aka jsaction), to which the actual event handlers
     * that implement the behavior of the application are bound. The
     * value is a semicolon separated list of colon separated pairs of
     * an optional DOM event name and a jsaction name. If the optional
     * DOM event name is omitted, 'click' is assumed. The jsaction names
     * are dot separated pairs of a namespace and a simple jsaction
     * name.
     *
     * See grammar in README.md for expected syntax in the attribute value.
     */
    JSACTION: "jsaction";
};

/**
 * Creates an `EarlyJsactionData`, adds events to it, and populates it on a nested object on
 * the window.
 */
export declare function bootstrapAppScopedEarlyEventContract(container: HTMLElement, appId: string, bubbleEventTypes: string[], captureEventTypes: string[], dataContainer?: EarlyJsactionDataContainer): void;

/** Clear the early event contract. */
export declare function clearAppScopedEarlyEventContract(appId: string, dataContainer?: EarlyJsactionDataContainer): void;

/** Clones an `EventInfo` */
declare function cloneEventInfo(eventInfo: EventInfo): EventInfo;

/**
 * Utility function for creating an `EventInfo`.
 *
 * This should be used in compilation units that are less sensitive to code
 * size.
 */
declare function createEventInfo({ eventType, event, targetElement, container, timestamp, action, isReplay, a11yClickKey, }: {
    eventType: string;
    event: Event;
    targetElement: Element;
    container: Element;
    timestamp: number;
    action?: ActionInfo;
    isReplay?: boolean;
    a11yClickKey?: boolean;
}): EventInfo;

/**
 * Utility function for creating an `EventInfo`.
 *
 * This can be used from code-size sensitive compilation units, as taking
 * parameters vs. an `Object` literal reduces code size.
 */
declare function createEventInfoFromParameters(eventType: string, event: Event, targetElement: Element, container: Element, timestamp: number, action?: ActionInfoInternal, isReplay?: boolean, a11yClickKey?: boolean): EventInfo;

/** A function that is called to handle events captured by the EventContract. */
declare type Dispatcher = (eventInfo: eventInfoLib.EventInfo, globalDispatch?: boolean) => void;

/**
 * Defines the early jsaction data types.
 */
declare interface EarlyJsactionData {
    /** List used to keep track of the early JSAction event types. */
    et: string[];
    /** List used to keep track of the early JSAction capture event types. */
    etc: string[];
    /** Early JSAction handler for all events. */
    h: (event: Event) => void;
    /** Dispatcher handler. Initializes to populating `q`. */
    d: (eventInfo: EventInfo) => void;
    /** List used to push `EventInfo` objects if the dispatcher is not registered. */
    q: EventInfo[];
    /** Container for listening to events. */
    c: HTMLElement;
}

export declare interface EarlyJsactionDataContainer {
    _ejsa?: EarlyJsactionData;
    _ejsas?: {
        [appId: string]: EarlyJsactionData | undefined;
    };
}

/**
 * EventContract intercepts events in the bubbling phase at the
 * boundary of a container element, and maps them to generic actions
 * which are specified using the custom jsaction attribute in
 * HTML. Behavior of the application is then specified in terms of
 * handler for such actions, cf. jsaction.Dispatcher in dispatcher.js.
 *
 * This has several benefits: (1) No DOM event handlers need to be
 * registered on the specific elements in the UI. (2) The set of
 * events that the application has to handle can be specified in terms
 * of the semantics of the application, rather than in terms of DOM
 * events. (3) Invocation of handlers can be delayed and handlers can
 * be delay loaded in a generic way.
 */
export declare class EventContract implements UnrenamedEventContract {
    static MOUSE_SPECIAL_SUPPORT: boolean;
    private containerManager;
    /**
     * The DOM events which this contract covers. Used to prevent double
     * registration of event types. The value of the map is the
     * internally created DOM event handler function that handles the
     * DOM events. See addEvent().
     *
     */
    private eventHandlers;
    private browserEventTypeToExtraEventTypes;
    /**
     * The dispatcher function. Events are passed to this function for
     * handling once it was set using the registerDispatcher() method. This is
     * done because the function is passed from another jsbinary, so passing the
     * instance and invoking the method here would require to leave the method
     * unobfuscated.
     */
    private dispatcher;
    /**
     * The list of suspended `EventInfo` that will be dispatched
     * as soon as the `Dispatcher` is registered.
     */
    private queuedEventInfos;
    constructor(containerManager: EventContractContainerManager);
    private handleEvent;
    /**
     * Handle an `EventInfo`.
     */
    private handleEventInfo;
    /**
     * Enables jsaction handlers to be called for the event type given by
     * name.
     *
     * If the event is already registered, this does nothing.
     *
     * @param prefixedEventType If supplied, this event is used in
     *     the actual browser event registration instead of the name that is
     *     exposed to jsaction. Use this if you e.g. want users to be able
     *     to subscribe to jsaction="transitionEnd:foo" while the underlying
     *     event is webkitTransitionEnd in one browser and mozTransitionEnd
     *     in another.
     *
     * @param passive A boolean value that, if `true`, indicates that the event
     *     handler will never call `preventDefault()`.
     */
    addEvent(eventType: string, prefixedEventType?: string, passive?: boolean): void;
    /**
     * Gets the queued early events and replay them using the appropriate handler
     * in the provided event contract. Once all the events are replayed, it cleans
     * up the early contract.
     */
    replayEarlyEvents(earlyJsactionData?: EarlyJsactionData | undefined): void;
    /**
     * Replays all the early `EventInfo` objects, dispatching them through the normal
     * `EventContract` flow.
     */
    replayEarlyEventInfos(earlyEventInfos: eventInfoLib.EventInfo[]): void;
    /**
     * Returns all JSAction event types that have been registered for a given
     * browser event type.
     */
    private getEventTypesForBrowserEventType;
    /**
     * Returns the event handler function for a given event type.
     */
    handler(eventType: string): EventHandler | undefined;
    /**
     * Cleans up the event contract. This resets all of the `EventContract`'s
     * internal state. Users are responsible for not using this `EventContract`
     * after it has been cleaned up.
     */
    cleanUp(): void;
    /**
     * Register a dispatcher function. Event info of each event mapped to
     * a jsaction is passed for handling to this callback. The queued
     * events are passed as well to the dispatcher for later replaying
     * once the dispatcher is registered. Clears the event queue to null.
     *
     * @param dispatcher The dispatcher function.
     * @param restriction
     */
    registerDispatcher(dispatcher: Dispatcher, restriction: Restriction): void;
    /**
     * Unrenamed alias for registerDispatcher. Necessary for any codebases that
     * split the `EventContract` and `Dispatcher` code into different compilation
     * units.
     */
    ecrd(dispatcher: Dispatcher, restriction: Restriction): void;
}

/**
 * A class representing a container node and all the event handlers
 * installed on it. Used so that handlers can be cleaned up if the
 * container is removed from the contract.
 */
export declare class EventContractContainer implements EventContractContainerManager {
    readonly element: Element;
    /**
     * Array of event handlers and their corresponding event types that are
     * installed on this container.
     *
     */
    private handlerInfos;
    /**
     * @param element The container Element.
     */
    constructor(element: Element);
    /**
     * Installs the provided installer on the element owned by this container,
     * and maintains a reference to resulting handler in order to remove it
     * later if desired.
     */
    addEventListener(eventType: string, getHandler: (element: Element) => (event: Event) => void, passive?: boolean): void;
    /**
     * Removes all the handlers installed on this container.
     */
    cleanUp(): void;
}


/**
 * An `EventContractContainerManager` provides the common interface for managing
 * containers.
 */
declare interface EventContractContainerManager {
    addEventListener(eventType: string, getHandler: (element: Element) => (event: Event) => void, passive?: boolean): void;
    cleanUp(): void;
}

/**
 * A dispatcher that uses browser-based `Event` semantics, for example bubbling, `stopPropagation`,
 * `currentTarget`, etc.
 */
export declare class EventDispatcher {
    private readonly dispatchDelegate;
    private readonly clickModSupport;
    private readonly actionResolver;
    private readonly dispatcher;
    constructor(dispatchDelegate: (event: Event, actionName: string) => void, clickModSupport?: boolean);
    /**
     * The entrypoint for the `EventContract` dispatch.
     */
    dispatch(eventInfo: EventInfo): void;
    /** Internal method that does basic disaptching. */
    private dispatchToDelegate;
}

/**
 * A function that handles an event dispatched from the browser.
 *
 * eventType: May differ from `event.type` if JSAction uses a
 * short-hand name or is patching over an non-bubbling event with a bubbling
 * variant.
 * event: The native browser event.
 * container: The container for this dispatch.
 */
declare type EventHandler = (eventType: string, event: Event, container: Element) => void;

/**
 * Records information for later handling of events. This type is
 * shared, and instances of it are passed, between the eventcontract
 * and the dispatcher jsbinary. Therefore, the fields of this type are
 * referenced by string literals rather than property literals
 * throughout the code.
 *
 * 'targetElement' is the element the action occurred on, 'actionElement'
 * is the element that has the jsaction handler.
 *
 * A null 'actionElement' identifies an EventInfo instance that didn't match a
 * jsaction attribute.  This allows us to execute global event handlers with the
 * appropriate event type (including a11y clicks and custom events).
 * The declare portion of this interface creates a set of externs that make sure
 * renaming doesn't happen for EventInfo. This is important since EventInfo
 * is shared across multiple binaries.
 */
declare interface EventInfo {
    eventType: string;
    event: Event;
    targetElement: Element;
    /** The element that is the container for this Event. */
    eic: Element;
    timeStamp: number;
    /**
     * The action parsed from the JSAction element.
     */
    eia?: ActionInfoInternal;
    /**
     * Whether this `Event` is a replay event, meaning no dispatcher was
     * installed when this `Event` was originally dispatched.
     */
    eirp?: boolean;
    /**
     * Whether this `Event` represents a `keydown` event that should be processed
     * as a `click`. Only used when a11y click events is on.
     */
    eiack?: boolean;
    /** Whether action resolution has already run on this `EventInfo`. */
    eir?: boolean;
}

declare namespace eventInfoLib {
    export {
        getEventType,
        setEventType,
        getEvent,
        setEvent,
        getTargetElement,
        setTargetElement,
        getContainer,
        setContainer,
        getTimestamp,
        setTimestamp,
        getAction,
        setAction,
        unsetAction,
        getActionName,
        getActionElement,
        getIsReplay,
        setIsReplay,
        getA11yClickKey,
        setA11yClickKey,
        getResolved,
        setResolved,
        cloneEventInfo,
        createEventInfoFromParameters,
        createEventInfo,
        ActionInfo,
        EventInfo,
        EventInfoWrapper
    }
}

/**
 * Utility class around an `EventInfo`.
 *
 * This should be used in compilation units that are less sensitive to code
 * size.
 */
export declare class EventInfoWrapper {
    readonly eventInfo: EventInfo;
    constructor(eventInfo: EventInfo);
    getEventType(): string;
    setEventType(eventType: string): void;
    getEvent(): Event;
    setEvent(event: Event): void;
    getTargetElement(): Element;
    setTargetElement(targetElement: Element): void;
    getContainer(): Element;
    setContainer(container: Element): void;
    getTimestamp(): number;
    setTimestamp(timestamp: number): void;
    getAction(): {
        name: string;
        element: Element;
    } | undefined;
    setAction(action: ActionInfo | undefined): void;
    getIsReplay(): boolean | undefined;
    setIsReplay(replay: boolean): void;
    getResolved(): boolean | undefined;
    setResolved(resolved: boolean): void;
    clone(): EventInfoWrapper;
}

/** Extra event phases beyond what the browser provides. */
export declare const EventPhase: {
    REPLAY: number;
};

/** Added for readability when accessing stable property names. */
declare function getA11yClickKey(eventInfo: EventInfo): boolean | undefined;

/** Added for readability when accessing stable property names. */
declare function getAction(eventInfo: EventInfo): ActionInfoInternal | undefined;

/**
 * Reads the jsaction parser cache for the given DOM element. If no cache is yet present,
 * creates an empty one.
 */
export declare function getActionCache(element: Element): {
    [key: string]: string | undefined;
};

/** Added for readability when accessing stable property names. */
declare function getActionElement(actionInfo: ActionInfoInternal): Element;

/** Added for readability when accessing stable property names. */
declare function getActionName(actionInfo: ActionInfoInternal): string;

/** Get the queued `EventInfo` objects that were dispatched before a dispatcher was registered. */
export declare function getAppScopedQueuedEventInfos(appId: string, dataContainer?: EarlyJsactionDataContainer): EventInfo[];

/** Added for readability when accessing stable property names. */
declare function getContainer(eventInfo: EventInfo): Element;

/** Added for readability when accessing stable property names. */
declare function getEvent(eventInfo: EventInfo): Event;

/** Added for readability when accessing stable property names. */
declare function getEventType(eventInfo: EventInfo): string;

/** Added for readability when accessing stable property names. */
declare function getIsReplay(eventInfo: EventInfo): boolean | undefined;

/** Added for readability when accessing stable property names. */
declare function getResolved(eventInfo: EventInfo): boolean | undefined;

/** Added for readability when accessing stable property names. */
declare function getTargetElement(eventInfo: EventInfo): Element;

/** Added for readability when accessing stable property names. */
declare function getTimestamp(eventInfo: EventInfo): number;

/**
 * Whether or not an event type should be registered in the capture phase.
 * @param eventType
 * @returns bool
 */
export declare const isCaptureEventType: (eventType: string) => boolean;

/**
 * Whether or not an event type is registered in the early contract.
 */
export declare const isEarlyEventType: (eventType: string) => boolean;

/**
 * Registers a dispatcher function on the `EarlyJsactionData` present on the nested object on the
 * window.
 */
export declare function registerAppScopedDispatcher(restriction: Restriction, appId: string, dispatcher: (eventInfo: EventInfo) => void, dataContainer?: EarlyJsactionDataContainer): void;

/**
 * Registers deferred functionality for an EventContract and a Jsaction
 * Dispatcher.
 */
export declare function registerDispatcher(eventContract: UnrenamedEventContract, dispatcher: EventDispatcher): void;

/** Removes all event listener handlers. */
export declare function removeAllAppScopedEventListeners(appId: string, dataContainer?: EarlyJsactionDataContainer): void;


/**
 * @fileoverview An enum to control who can call certain jsaction APIs.
 */
declare enum Restriction {
    I_AM_THE_JSACTION_FRAMEWORK = 0
}

/** Added for readability when accessing stable property names. */
declare function setA11yClickKey(eventInfo: EventInfo, a11yClickKey: boolean): void;

/** Added for readability when accessing stable property names. */
declare function setAction(eventInfo: EventInfo, actionName: string, actionElement: Element): void;

/** Added for readability when accessing stable property names. */
declare function setContainer(eventInfo: EventInfo, container: Element): void;

/** Added for readability when accessing stable property names. */
declare function setEvent(eventInfo: EventInfo, event: Event): void;

/** Added for readability when accessing stable property names. */
declare function setEventType(eventInfo: EventInfo, eventType: string): void;

/** Added for readability when accessing stable property names. */
declare function setIsReplay(eventInfo: EventInfo, replay: boolean): void;

/** Added for readability when accessing stable property names. */
declare function setResolved(eventInfo: EventInfo, resolved: boolean): void;

/** Added for readability when accessing stable property names. */
declare function setTargetElement(eventInfo: EventInfo, targetElement: Element): void;

/** Added for readability when accessing stable property names. */
declare function setTimestamp(eventInfo: EventInfo, timestamp: number): void;

/**
 * The API of an EventContract that is safe to call from any compilation unit.
 */
declare interface UnrenamedEventContract {
    ecrd(dispatcher: Dispatcher, restriction: Restriction): void;
}

/** Added for readability when accessing stable property names. */
declare function unsetAction(eventInfo: EventInfo): void;

export { }
