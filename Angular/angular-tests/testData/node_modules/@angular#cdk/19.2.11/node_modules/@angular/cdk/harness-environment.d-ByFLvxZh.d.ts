/**
 * Dimensions for element size and its position relative to the viewport.
 */
interface ElementDimensions {
    top: number;
    left: number;
    width: number;
    height: number;
}

/** Modifier keys that may be held while typing. */
interface ModifierKeys {
    control?: boolean;
    alt?: boolean;
    shift?: boolean;
    meta?: boolean;
}
/** Data that can be attached to a custom event dispatched from a `TestElement`. */
type EventData = string | number | boolean | Function | undefined | null | EventData[] | {
    [key: string]: EventData;
};
/** An enum of non-text keys that can be used with the `sendKeys` method. */
declare enum TestKey {
    BACKSPACE = 0,
    TAB = 1,
    ENTER = 2,
    SHIFT = 3,
    CONTROL = 4,
    ALT = 5,
    ESCAPE = 6,
    PAGE_UP = 7,
    PAGE_DOWN = 8,
    END = 9,
    HOME = 10,
    LEFT_ARROW = 11,
    UP_ARROW = 12,
    RIGHT_ARROW = 13,
    DOWN_ARROW = 14,
    INSERT = 15,
    DELETE = 16,
    F1 = 17,
    F2 = 18,
    F3 = 19,
    F4 = 20,
    F5 = 21,
    F6 = 22,
    F7 = 23,
    F8 = 24,
    F9 = 25,
    F10 = 26,
    F11 = 27,
    F12 = 28,
    META = 29,
    COMMA = 30
}
/**
 * This acts as a common interface for DOM elements across both unit and e2e tests. It is the
 * interface through which the ComponentHarness interacts with the component's DOM.
 */
interface TestElement {
    /** Blur the element. */
    blur(): Promise<void>;
    /** Clear the element's input (for input and textarea elements only). */
    clear(): Promise<void>;
    /**
     * Click the element at the default location for the current environment. If you need to guarantee
     * the element is clicked at a specific location, consider using `click('center')` or
     * `click(x, y)` instead.
     */
    click(modifiers?: ModifierKeys): Promise<void>;
    /** Click the element at the element's center. */
    click(location: 'center', modifiers?: ModifierKeys): Promise<void>;
    /**
     * Click the element at the specified coordinates relative to the top-left of the element.
     * @param relativeX Coordinate within the element, along the X-axis at which to click.
     * @param relativeY Coordinate within the element, along the Y-axis at which to click.
     * @param modifiers Modifier keys held while clicking
     */
    click(relativeX: number, relativeY: number, modifiers?: ModifierKeys): Promise<void>;
    /**
     * Right clicks on the element at the specified coordinates relative to the top-left of it.
     * @param relativeX Coordinate within the element, along the X-axis at which to click.
     * @param relativeY Coordinate within the element, along the Y-axis at which to click.
     * @param modifiers Modifier keys held while clicking
     */
    rightClick(relativeX: number, relativeY: number, modifiers?: ModifierKeys): Promise<void>;
    /** Focus the element. */
    focus(): Promise<void>;
    /** Get the computed value of the given CSS property for the element. */
    getCssValue(property: string): Promise<string>;
    /** Hovers the mouse over the element. */
    hover(): Promise<void>;
    /** Moves the mouse away from the element. */
    mouseAway(): Promise<void>;
    /**
     * Sends the given string to the input as a series of key presses. Also fires input events
     * and attempts to add the string to the Element's value. Note that some environments cannot
     * reproduce native browser behavior for keyboard shortcuts such as Tab, Ctrl + A, etc.
     * @throws An error if no keys have been specified.
     */
    sendKeys(...keys: (string | TestKey)[]): Promise<void>;
    /**
     * Sends the given string to the input as a series of key presses. Also fires input
     * events and attempts to add the string to the Element's value.
     * @throws An error if no keys have been specified.
     */
    sendKeys(modifiers: ModifierKeys, ...keys: (string | TestKey)[]): Promise<void>;
    /**
     * Gets the text from the element.
     * @param options Options that affect what text is included.
     */
    text(options?: TextOptions): Promise<string>;
    /**
     * Sets the value of a `contenteditable` element.
     * @param value Value to be set on the element.
     * @breaking-change 16.0.0 Will become a required method.
     */
    setContenteditableValue?(value: string): Promise<void>;
    /** Gets the value for the given attribute from the element. */
    getAttribute(name: string): Promise<string | null>;
    /** Checks whether the element has the given class. */
    hasClass(name: string): Promise<boolean>;
    /** Gets the dimensions of the element. */
    getDimensions(): Promise<ElementDimensions>;
    /** Gets the value of a property of an element. */
    getProperty<T = any>(name: string): Promise<T>;
    /** Checks whether this element matches the given selector. */
    matchesSelector(selector: string): Promise<boolean>;
    /** Checks whether the element is focused. */
    isFocused(): Promise<boolean>;
    /** Sets the value of a property of an input. */
    setInputValue(value: string): Promise<void>;
    /** Selects the options at the specified indexes inside of a native `select` element. */
    selectOptions(...optionIndexes: number[]): Promise<void>;
    /**
     * Dispatches an event with a particular name.
     * @param name Name of the event to be dispatched.
     */
    dispatchEvent(name: string, data?: Record<string, EventData>): Promise<void>;
}
interface TextOptions {
    /** Optional selector for elements to exclude. */
    exclude?: string;
}

/** An async function that returns a promise when called. */
type AsyncFactoryFn<T> = () => Promise<T>;
/** An async function that takes an item and returns a boolean promise */
type AsyncPredicate<T> = (item: T) => Promise<boolean>;
/** An async function that takes an item and an option value and returns a boolean promise. */
type AsyncOptionPredicate<T, O> = (item: T, option: O) => Promise<boolean>;
/**
 * A query for a `ComponentHarness`, which is expressed as either a `ComponentHarnessConstructor` or
 * a `HarnessPredicate`.
 */
type HarnessQuery<T extends ComponentHarness> = ComponentHarnessConstructor<T> | HarnessPredicate<T>;
/**
 * The result type obtained when searching using a particular list of queries. This type depends on
 * the particular items being queried.
 * - If one of the queries is for a `ComponentHarnessConstructor<C1>`, it means that the result
 *   might be a harness of type `C1`
 * - If one of the queries is for a `HarnessPredicate<C2>`, it means that the result might be a
 *   harness of type `C2`
 * - If one of the queries is for a `string`, it means that the result might be a `TestElement`.
 *
 * Since we don't know for sure which query will match, the result type if the union of the types
 * for all possible results.
 *
 * e.g.
 * The type:
 * `LocatorFnResult&lt;[
 *   ComponentHarnessConstructor&lt;MyHarness&gt;,
 *   HarnessPredicate&lt;MyOtherHarness&gt;,
 *   string
 * ]&gt;`
 * is equivalent to:
 * `MyHarness | MyOtherHarness | TestElement`.
 */
type LocatorFnResult<T extends (HarnessQuery<any> | string)[]> = {
    [I in keyof T]: T[I] extends new (...args: any[]) => infer C ? C : T[I] extends {
        harnessType: new (...args: any[]) => infer C;
    } ? C : T[I] extends string ? TestElement : never;
}[number];
/**
 * Interface used to load ComponentHarness objects. This interface is used by test authors to
 * instantiate `ComponentHarness`es.
 */
interface HarnessLoader {
    /**
     * Searches for an element with the given selector under the current instances's root element,
     * and returns a `HarnessLoader` rooted at the matching element. If multiple elements match the
     * selector, the first is used. If no elements match, an error is thrown.
     * @param selector The selector for the root element of the new `HarnessLoader`
     * @return A `HarnessLoader` rooted at the element matching the given selector.
     * @throws If a matching element can't be found.
     */
    getChildLoader(selector: string): Promise<HarnessLoader>;
    /**
     * Searches for all elements with the given selector under the current instances's root element,
     * and returns an array of `HarnessLoader`s, one for each matching element, rooted at that
     * element.
     * @param selector The selector for the root element of the new `HarnessLoader`
     * @return A list of `HarnessLoader`s, one for each matching element, rooted at that element.
     */
    getAllChildLoaders(selector: string): Promise<HarnessLoader[]>;
    /**
     * Searches for an instance of the component corresponding to the given harness type under the
     * `HarnessLoader`'s root element, and returns a `ComponentHarness` for that instance. If multiple
     * matching components are found, a harness for the first one is returned. If no matching
     * component is found, an error is thrown.
     * @param query A query for a harness to create
     * @return An instance of the given harness type
     * @throws If a matching component instance can't be found.
     */
    getHarness<T extends ComponentHarness>(query: HarnessQuery<T>): Promise<T>;
    /**
     * Searches for an instance of the component corresponding to the given harness type under the
     * `HarnessLoader`'s root element, and returns a `ComponentHarness` for that instance. If multiple
     * matching components are found, a harness for the first one is returned. If no matching
     * component is found, null is returned.
     * @param query A query for a harness to create
     * @return An instance of the given harness type (or null if not found).
     */
    getHarnessOrNull<T extends ComponentHarness>(query: HarnessQuery<T>): Promise<T | null>;
    /**
     * Searches for all instances of the component corresponding to the given harness type under the
     * `HarnessLoader`'s root element, and returns a list `ComponentHarness` for each instance.
     * @param query A query for a harness to create
     * @return A list instances of the given harness type.
     */
    getAllHarnesses<T extends ComponentHarness>(query: HarnessQuery<T>): Promise<T[]>;
    /**
     * Searches for an instance of the component corresponding to the given harness type under the
     * `HarnessLoader`'s root element, and returns a boolean indicating if any were found.
     * @param query A query for a harness to create
     * @return A boolean indicating if an instance was found.
     */
    hasHarness<T extends ComponentHarness>(query: HarnessQuery<T>): Promise<boolean>;
}
/**
 * Interface used to create asynchronous locator functions used find elements and component
 * harnesses. This interface is used by `ComponentHarness` authors to create locator functions for
 * their `ComponentHarness` subclass.
 */
interface LocatorFactory {
    /** Gets a locator factory rooted at the document root. */
    documentRootLocatorFactory(): LocatorFactory;
    /** The root element of this `LocatorFactory` as a `TestElement`. */
    rootElement: TestElement;
    /**
     * Creates an asynchronous locator function that can be used to find a `ComponentHarness` instance
     * or element under the root element of this `LocatorFactory`.
     * @param queries A list of queries specifying which harnesses and elements to search for:
     *   - A `string` searches for elements matching the CSS selector specified by the string.
     *   - A `ComponentHarness` constructor searches for `ComponentHarness` instances matching the
     *     given class.
     *   - A `HarnessPredicate` searches for `ComponentHarness` instances matching the given
     *     predicate.
     * @return An asynchronous locator function that searches for and returns a `Promise` for the
     *   first element or harness matching the given search criteria. Matches are ordered first by
     *   order in the DOM, and second by order in the queries list. If no matches are found, the
     *   `Promise` rejects. The type that the `Promise` resolves to is a union of all result types for
     *   each query.
     *
     * e.g. Given the following DOM: `<div id="d1" /><div id="d2" />`, and assuming
     * `DivHarness.hostSelector === 'div'`:
     * - `await lf.locatorFor(DivHarness, 'div')()` gets a `DivHarness` instance for `#d1`
     * - `await lf.locatorFor('div', DivHarness)()` gets a `TestElement` instance for `#d1`
     * - `await lf.locatorFor('span')()` throws because the `Promise` rejects.
     */
    locatorFor<T extends (HarnessQuery<any> | string)[]>(...queries: T): AsyncFactoryFn<LocatorFnResult<T>>;
    /**
     * Creates an asynchronous locator function that can be used to find a `ComponentHarness` instance
     * or element under the root element of this `LocatorFactory`.
     * @param queries A list of queries specifying which harnesses and elements to search for:
     *   - A `string` searches for elements matching the CSS selector specified by the string.
     *   - A `ComponentHarness` constructor searches for `ComponentHarness` instances matching the
     *     given class.
     *   - A `HarnessPredicate` searches for `ComponentHarness` instances matching the given
     *     predicate.
     * @return An asynchronous locator function that searches for and returns a `Promise` for the
     *   first element or harness matching the given search criteria. Matches are ordered first by
     *   order in the DOM, and second by order in the queries list. If no matches are found, the
     *   `Promise` is resolved with `null`. The type that the `Promise` resolves to is a union of all
     *   result types for each query or null.
     *
     * e.g. Given the following DOM: `<div id="d1" /><div id="d2" />`, and assuming
     * `DivHarness.hostSelector === 'div'`:
     * - `await lf.locatorForOptional(DivHarness, 'div')()` gets a `DivHarness` instance for `#d1`
     * - `await lf.locatorForOptional('div', DivHarness)()` gets a `TestElement` instance for `#d1`
     * - `await lf.locatorForOptional('span')()` gets `null`.
     */
    locatorForOptional<T extends (HarnessQuery<any> | string)[]>(...queries: T): AsyncFactoryFn<LocatorFnResult<T> | null>;
    /**
     * Creates an asynchronous locator function that can be used to find `ComponentHarness` instances
     * or elements under the root element of this `LocatorFactory`.
     * @param queries A list of queries specifying which harnesses and elements to search for:
     *   - A `string` searches for elements matching the CSS selector specified by the string.
     *   - A `ComponentHarness` constructor searches for `ComponentHarness` instances matching the
     *     given class.
     *   - A `HarnessPredicate` searches for `ComponentHarness` instances matching the given
     *     predicate.
     * @return An asynchronous locator function that searches for and returns a `Promise` for all
     *   elements and harnesses matching the given search criteria. Matches are ordered first by
     *   order in the DOM, and second by order in the queries list. If an element matches more than
     *   one `ComponentHarness` class, the locator gets an instance of each for the same element. If
     *   an element matches multiple `string` selectors, only one `TestElement` instance is returned
     *   for that element. The type that the `Promise` resolves to is an array where each element is
     *   the union of all result types for each query.
     *
     * e.g. Given the following DOM: `<div id="d1" /><div id="d2" />`, and assuming
     * `DivHarness.hostSelector === 'div'` and `IdIsD1Harness.hostSelector === '#d1'`:
     * - `await lf.locatorForAll(DivHarness, 'div')()` gets `[
     *     DivHarness, // for #d1
     *     TestElement, // for #d1
     *     DivHarness, // for #d2
     *     TestElement // for #d2
     *   ]`
     * - `await lf.locatorForAll('div', '#d1')()` gets `[
     *     TestElement, // for #d1
     *     TestElement // for #d2
     *   ]`
     * - `await lf.locatorForAll(DivHarness, IdIsD1Harness)()` gets `[
     *     DivHarness, // for #d1
     *     IdIsD1Harness, // for #d1
     *     DivHarness // for #d2
     *   ]`
     * - `await lf.locatorForAll('span')()` gets `[]`.
     */
    locatorForAll<T extends (HarnessQuery<any> | string)[]>(...queries: T): AsyncFactoryFn<LocatorFnResult<T>[]>;
    /** @return A `HarnessLoader` rooted at the root element of this `LocatorFactory`. */
    rootHarnessLoader(): Promise<HarnessLoader>;
    /**
     * Gets a `HarnessLoader` instance for an element under the root of this `LocatorFactory`.
     * @param selector The selector for the root element.
     * @return A `HarnessLoader` rooted at the first element matching the given selector.
     * @throws If no matching element is found for the given selector.
     */
    harnessLoaderFor(selector: string): Promise<HarnessLoader>;
    /**
     * Gets a `HarnessLoader` instance for an element under the root of this `LocatorFactory`
     * @param selector The selector for the root element.
     * @return A `HarnessLoader` rooted at the first element matching the given selector, or null if
     *     no matching element is found.
     */
    harnessLoaderForOptional(selector: string): Promise<HarnessLoader | null>;
    /**
     * Gets a list of `HarnessLoader` instances, one for each matching element.
     * @param selector The selector for the root element.
     * @return A list of `HarnessLoader`, one rooted at each element matching the given selector.
     */
    harnessLoaderForAll(selector: string): Promise<HarnessLoader[]>;
    /**
     * Flushes change detection and async tasks captured in the Angular zone.
     * In most cases it should not be necessary to call this manually. However, there may be some edge
     * cases where it is needed to fully flush animation events.
     */
    forceStabilize(): Promise<void>;
    /**
     * Waits for all scheduled or running async tasks to complete. This allows harness
     * authors to wait for async tasks outside of the Angular zone.
     */
    waitForTasksOutsideAngular(): Promise<void>;
}
/**
 * Base class for component harnesses that all component harness authors should extend. This base
 * component harness provides the basic ability to locate element and sub-component harness. It
 * should be inherited when defining user's own harness.
 */
declare abstract class ComponentHarness {
    protected readonly locatorFactory: LocatorFactory;
    constructor(locatorFactory: LocatorFactory);
    /** Gets a `Promise` for the `TestElement` representing the host element of the component. */
    host(): Promise<TestElement>;
    /**
     * Gets a `LocatorFactory` for the document root element. This factory can be used to create
     * locators for elements that a component creates outside of its own root element. (e.g. by
     * appending to document.body).
     */
    protected documentRootLocatorFactory(): LocatorFactory;
    /**
     * Creates an asynchronous locator function that can be used to find a `ComponentHarness` instance
     * or element under the host element of this `ComponentHarness`.
     * @param queries A list of queries specifying which harnesses and elements to search for:
     *   - A `string` searches for elements matching the CSS selector specified by the string.
     *   - A `ComponentHarness` constructor searches for `ComponentHarness` instances matching the
     *     given class.
     *   - A `HarnessPredicate` searches for `ComponentHarness` instances matching the given
     *     predicate.
     * @return An asynchronous locator function that searches for and returns a `Promise` for the
     *   first element or harness matching the given search criteria. Matches are ordered first by
     *   order in the DOM, and second by order in the queries list. If no matches are found, the
     *   `Promise` rejects. The type that the `Promise` resolves to is a union of all result types for
     *   each query.
     *
     * e.g. Given the following DOM: `<div id="d1" /><div id="d2" />`, and assuming
     * `DivHarness.hostSelector === 'div'`:
     * - `await ch.locatorFor(DivHarness, 'div')()` gets a `DivHarness` instance for `#d1`
     * - `await ch.locatorFor('div', DivHarness)()` gets a `TestElement` instance for `#d1`
     * - `await ch.locatorFor('span')()` throws because the `Promise` rejects.
     */
    protected locatorFor<T extends (HarnessQuery<any> | string)[]>(...queries: T): AsyncFactoryFn<LocatorFnResult<T>>;
    /**
     * Creates an asynchronous locator function that can be used to find a `ComponentHarness` instance
     * or element under the host element of this `ComponentHarness`.
     * @param queries A list of queries specifying which harnesses and elements to search for:
     *   - A `string` searches for elements matching the CSS selector specified by the string.
     *   - A `ComponentHarness` constructor searches for `ComponentHarness` instances matching the
     *     given class.
     *   - A `HarnessPredicate` searches for `ComponentHarness` instances matching the given
     *     predicate.
     * @return An asynchronous locator function that searches for and returns a `Promise` for the
     *   first element or harness matching the given search criteria. Matches are ordered first by
     *   order in the DOM, and second by order in the queries list. If no matches are found, the
     *   `Promise` is resolved with `null`. The type that the `Promise` resolves to is a union of all
     *   result types for each query or null.
     *
     * e.g. Given the following DOM: `<div id="d1" /><div id="d2" />`, and assuming
     * `DivHarness.hostSelector === 'div'`:
     * - `await ch.locatorForOptional(DivHarness, 'div')()` gets a `DivHarness` instance for `#d1`
     * - `await ch.locatorForOptional('div', DivHarness)()` gets a `TestElement` instance for `#d1`
     * - `await ch.locatorForOptional('span')()` gets `null`.
     */
    protected locatorForOptional<T extends (HarnessQuery<any> | string)[]>(...queries: T): AsyncFactoryFn<LocatorFnResult<T> | null>;
    /**
     * Creates an asynchronous locator function that can be used to find `ComponentHarness` instances
     * or elements under the host element of this `ComponentHarness`.
     * @param queries A list of queries specifying which harnesses and elements to search for:
     *   - A `string` searches for elements matching the CSS selector specified by the string.
     *   - A `ComponentHarness` constructor searches for `ComponentHarness` instances matching the
     *     given class.
     *   - A `HarnessPredicate` searches for `ComponentHarness` instances matching the given
     *     predicate.
     * @return An asynchronous locator function that searches for and returns a `Promise` for all
     *   elements and harnesses matching the given search criteria. Matches are ordered first by
     *   order in the DOM, and second by order in the queries list. If an element matches more than
     *   one `ComponentHarness` class, the locator gets an instance of each for the same element. If
     *   an element matches multiple `string` selectors, only one `TestElement` instance is returned
     *   for that element. The type that the `Promise` resolves to is an array where each element is
     *   the union of all result types for each query.
     *
     * e.g. Given the following DOM: `<div id="d1" /><div id="d2" />`, and assuming
     * `DivHarness.hostSelector === 'div'` and `IdIsD1Harness.hostSelector === '#d1'`:
     * - `await ch.locatorForAll(DivHarness, 'div')()` gets `[
     *     DivHarness, // for #d1
     *     TestElement, // for #d1
     *     DivHarness, // for #d2
     *     TestElement // for #d2
     *   ]`
     * - `await ch.locatorForAll('div', '#d1')()` gets `[
     *     TestElement, // for #d1
     *     TestElement // for #d2
     *   ]`
     * - `await ch.locatorForAll(DivHarness, IdIsD1Harness)()` gets `[
     *     DivHarness, // for #d1
     *     IdIsD1Harness, // for #d1
     *     DivHarness // for #d2
     *   ]`
     * - `await ch.locatorForAll('span')()` gets `[]`.
     */
    protected locatorForAll<T extends (HarnessQuery<any> | string)[]>(...queries: T): AsyncFactoryFn<LocatorFnResult<T>[]>;
    /**
     * Flushes change detection and async tasks in the Angular zone.
     * In most cases it should not be necessary to call this manually. However, there may be some edge
     * cases where it is needed to fully flush animation events.
     */
    protected forceStabilize(): Promise<void>;
    /**
     * Waits for all scheduled or running async tasks to complete. This allows harness
     * authors to wait for async tasks outside of the Angular zone.
     */
    protected waitForTasksOutsideAngular(): Promise<void>;
}
/**
 * Base class for component harnesses that authors should extend if they anticipate that consumers
 * of the harness may want to access other harnesses within the `<ng-content>` of the component.
 */
declare abstract class ContentContainerComponentHarness<S extends string = string> extends ComponentHarness implements HarnessLoader {
    getChildLoader(selector: S): Promise<HarnessLoader>;
    getAllChildLoaders(selector: S): Promise<HarnessLoader[]>;
    getHarness<T extends ComponentHarness>(query: HarnessQuery<T>): Promise<T>;
    getHarnessOrNull<T extends ComponentHarness>(query: HarnessQuery<T>): Promise<T | null>;
    getAllHarnesses<T extends ComponentHarness>(query: HarnessQuery<T>): Promise<T[]>;
    hasHarness<T extends ComponentHarness>(query: HarnessQuery<T>): Promise<boolean>;
    /**
     * Gets the root harness loader from which to start
     * searching for content contained by this harness.
     */
    protected getRootHarnessLoader(): Promise<HarnessLoader>;
}
/** Constructor for a ComponentHarness subclass. */
interface ComponentHarnessConstructor<T extends ComponentHarness> {
    new (locatorFactory: LocatorFactory): T;
    /**
     * `ComponentHarness` subclasses must specify a static `hostSelector` property that is used to
     * find the host element for the corresponding component. This property should match the selector
     * for the Angular component.
     */
    hostSelector: string;
}
/** A set of criteria that can be used to filter a list of `ComponentHarness` instances. */
interface BaseHarnessFilters {
    /** Only find instances whose host element matches the given selector. */
    selector?: string;
    /** Only find instances that are nested under an element with the given selector. */
    ancestor?: string;
}
/**
 * A class used to associate a ComponentHarness class with predicates functions that can be used to
 * filter instances of the class.
 */
declare class HarnessPredicate<T extends ComponentHarness> {
    harnessType: ComponentHarnessConstructor<T>;
    private _predicates;
    private _descriptions;
    private _ancestor;
    constructor(harnessType: ComponentHarnessConstructor<T>, options: BaseHarnessFilters);
    /**
     * Checks if the specified nullable string value matches the given pattern.
     * @param value The nullable string value to check, or a Promise resolving to the
     *   nullable string value.
     * @param pattern The pattern the value is expected to match. If `pattern` is a string,
     *   `value` is expected to match exactly. If `pattern` is a regex, a partial match is
     *   allowed. If `pattern` is `null`, the value is expected to be `null`.
     * @return Whether the value matches the pattern.
     */
    static stringMatches(value: string | null | Promise<string | null>, pattern: string | RegExp | null): Promise<boolean>;
    /**
     * Adds a predicate function to be run against candidate harnesses.
     * @param description A description of this predicate that may be used in error messages.
     * @param predicate An async predicate function.
     * @return this (for method chaining).
     */
    add(description: string, predicate: AsyncPredicate<T>): this;
    /**
     * Adds a predicate function that depends on an option value to be run against candidate
     * harnesses. If the option value is undefined, the predicate will be ignored.
     * @param name The name of the option (may be used in error messages).
     * @param option The option value.
     * @param predicate The predicate function to run if the option value is not undefined.
     * @return this (for method chaining).
     */
    addOption<O>(name: string, option: O | undefined, predicate: AsyncOptionPredicate<T, O>): this;
    /**
     * Filters a list of harnesses on this predicate.
     * @param harnesses The list of harnesses to filter.
     * @return A list of harnesses that satisfy this predicate.
     */
    filter(harnesses: T[]): Promise<T[]>;
    /**
     * Evaluates whether the given harness satisfies this predicate.
     * @param harness The harness to check
     * @return A promise that resolves to true if the harness satisfies this predicate,
     *   and resolves to false otherwise.
     */
    evaluate(harness: T): Promise<boolean>;
    /** Gets a description of this predicate for use in error messages. */
    getDescription(): string;
    /** Gets the selector used to find candidate elements. */
    getSelector(): string;
    /** Adds base options common to all harness types. */
    private _addBaseOptions;
}

/**
 * Base harness environment class that can be extended to allow `ComponentHarness`es to be used in
 * different test environments (e.g. testbed, protractor, etc.). This class implements the
 * functionality of both a `HarnessLoader` and `LocatorFactory`. This class is generic on the raw
 * element type, `E`, used by the particular test environment.
 */
declare abstract class HarnessEnvironment<E> implements HarnessLoader, LocatorFactory {
    protected rawRootElement: E;
    get rootElement(): TestElement;
    set rootElement(element: TestElement);
    private _rootElement;
    protected constructor(rawRootElement: E);
    documentRootLocatorFactory(): LocatorFactory;
    locatorFor<T extends (HarnessQuery<any> | string)[]>(...queries: T): AsyncFactoryFn<LocatorFnResult<T>>;
    locatorForOptional<T extends (HarnessQuery<any> | string)[]>(...queries: T): AsyncFactoryFn<LocatorFnResult<T> | null>;
    locatorForAll<T extends (HarnessQuery<any> | string)[]>(...queries: T): AsyncFactoryFn<LocatorFnResult<T>[]>;
    rootHarnessLoader(): Promise<HarnessLoader>;
    harnessLoaderFor(selector: string): Promise<HarnessLoader>;
    harnessLoaderForOptional(selector: string): Promise<HarnessLoader | null>;
    harnessLoaderForAll(selector: string): Promise<HarnessLoader[]>;
    getHarness<T extends ComponentHarness>(query: HarnessQuery<T>): Promise<T>;
    getHarnessOrNull<T extends ComponentHarness>(query: HarnessQuery<T>): Promise<T | null>;
    getAllHarnesses<T extends ComponentHarness>(query: HarnessQuery<T>): Promise<T[]>;
    hasHarness<T extends ComponentHarness>(query: HarnessQuery<T>): Promise<boolean>;
    getChildLoader(selector: string): Promise<HarnessLoader>;
    getAllChildLoaders(selector: string): Promise<HarnessLoader[]>;
    /** Creates a `ComponentHarness` for the given harness type with the given raw host element. */
    protected createComponentHarness<T extends ComponentHarness>(harnessType: ComponentHarnessConstructor<T>, element: E): T;
    abstract forceStabilize(): Promise<void>;
    abstract waitForTasksOutsideAngular(): Promise<void>;
    /** Gets the root element for the document. */
    protected abstract getDocumentRoot(): E;
    /** Creates a `TestElement` from a raw element. */
    protected abstract createTestElement(element: E): TestElement;
    /** Creates a `HarnessLoader` rooted at the given raw element. */
    protected abstract createEnvironment(element: E): HarnessEnvironment<E>;
    /**
     * Gets a list of all elements matching the given selector under this environment's root element.
     */
    protected abstract getAllRawElements(selector: string): Promise<E[]>;
    /**
     * Matches the given raw elements with the given list of element and harness queries to produce a
     * list of matched harnesses and test elements.
     */
    private _getAllHarnessesAndTestElements;
    /**
     * Check whether the given query matches the given element, if it does return the matched
     * `TestElement` or `ComponentHarness`, if it does not, return null. In cases where the caller
     * knows for sure that the query matches the element's selector, `skipSelectorCheck` can be used
     * to skip verification and optimize performance.
     */
    private _getQueryResultForElement;
}

export { ComponentHarness, ContentContainerComponentHarness, HarnessEnvironment, HarnessPredicate, TestKey };
export type { AsyncFactoryFn, AsyncOptionPredicate, AsyncPredicate, BaseHarnessFilters, ComponentHarnessConstructor, ElementDimensions, EventData, HarnessLoader, HarnessQuery, LocatorFactory, LocatorFnResult, ModifierKeys, TestElement, TextOptions };
