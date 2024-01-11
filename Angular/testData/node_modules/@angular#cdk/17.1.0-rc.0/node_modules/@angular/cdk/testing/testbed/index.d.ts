import { ComponentFixture } from '@angular/core/testing';
import { ComponentHarness } from '@angular/cdk/testing';
import { ComponentHarnessConstructor } from '@angular/cdk/testing';
import { ElementDimensions } from '@angular/cdk/testing';
import { EventData } from '@angular/cdk/testing';
import { HarnessEnvironment } from '@angular/cdk/testing';
import { HarnessLoader } from '@angular/cdk/testing';
import { ModifierKeys } from '@angular/cdk/testing';
import { TestElement } from '@angular/cdk/testing';
import { TestKey } from '@angular/cdk/testing';
import { TextOptions } from '@angular/cdk/testing';

/** A `HarnessEnvironment` implementation for Angular's Testbed. */
export declare class TestbedHarnessEnvironment extends HarnessEnvironment<Element> {
    private _fixture;
    /** Whether the environment has been destroyed. */
    private _destroyed;
    /** Observable that emits whenever the test task state changes. */
    private _taskState;
    /** The options for this environment. */
    private _options;
    /** Environment stabilization callback passed to the created test elements. */
    private _stabilizeCallback;
    protected constructor(rawRootElement: Element, _fixture: ComponentFixture<unknown>, options?: TestbedHarnessEnvironmentOptions);
    /** Creates a `HarnessLoader` rooted at the given fixture's root element. */
    static loader(fixture: ComponentFixture<unknown>, options?: TestbedHarnessEnvironmentOptions): HarnessLoader;
    /**
     * Creates a `HarnessLoader` at the document root. This can be used if harnesses are
     * located outside of a fixture (e.g. overlays appended to the document body).
     */
    static documentRootLoader(fixture: ComponentFixture<unknown>, options?: TestbedHarnessEnvironmentOptions): HarnessLoader;
    /** Gets the native DOM element corresponding to the given TestElement. */
    static getNativeElement(el: TestElement): Element;
    /**
     * Creates an instance of the given harness type, using the fixture's root element as the
     * harness's host element. This method should be used when creating a harness for the root element
     * of a fixture, as components do not have the correct selector when they are created as the root
     * of the fixture.
     */
    static harnessForFixture<T extends ComponentHarness>(fixture: ComponentFixture<unknown>, harnessType: ComponentHarnessConstructor<T>, options?: TestbedHarnessEnvironmentOptions): Promise<T>;
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
    /** Gets the root element for the document. */
    protected getDocumentRoot(): Element;
    /** Creates a `TestElement` from a raw element. */
    protected createTestElement(element: Element): TestElement;
    /** Creates a `HarnessLoader` rooted at the given raw element. */
    protected createEnvironment(element: Element): HarnessEnvironment<Element>;
    /**
     * Gets a list of all elements matching the given selector under this environment's root element.
     */
    protected getAllRawElements(selector: string): Promise<Element[]>;
}

/** Options to configure the environment. */
export declare interface TestbedHarnessEnvironmentOptions {
    /** The query function used to find DOM elements. */
    queryFn: (selector: string, root: Element) => Iterable<Element> | ArrayLike<Element>;
}

/** A `TestElement` implementation for unit tests. */
export declare class UnitTestElement implements TestElement {
    readonly element: Element;
    private _stabilize;
    constructor(element: Element, _stabilize: () => Promise<void>);
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
     * and attempts to add the string to the Element's value. Note that this cannot
     * reproduce native browser behavior for keyboard shortcuts such as Tab, Ctrl + A, etc.
     */
    sendKeys(...keys: (string | TestKey)[]): Promise<void>;
    /**
     * Sends the given string to the input as a series of key presses. Also fires input events
     * and attempts to add the string to the Element's value.
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
     */
    setContenteditableValue(value: string): Promise<void>;
    /** Gets the value for the given attribute from the element. */
    getAttribute(name: string): Promise<string | null>;
    /** Checks whether the element has the given class. */
    hasClass(name: string): Promise<boolean>;
    /** Gets the dimensions of the element. */
    getDimensions(): Promise<ElementDimensions>;
    /** Gets the value of a property of an element. */
    getProperty<T = any>(name: string): Promise<T>;
    /** Sets the value of a property of an input. */
    setInputValue(value: string): Promise<void>;
    /** Selects the options at the specified indexes inside of a native `select` element. */
    selectOptions(...optionIndexes: number[]): Promise<void>;
    /** Checks whether this element matches the given selector. */
    matchesSelector(selector: string): Promise<boolean>;
    /** Checks whether the element is focused. */
    isFocused(): Promise<boolean>;
    /**
     * Dispatches an event with a particular name.
     * @param name Name of the event to be dispatched.
     */
    dispatchEvent(name: string, data?: Record<string, EventData>): Promise<void>;
    /**
     * Dispatches a pointer event on the current element if the browser supports it.
     * @param name Name of the pointer event to be dispatched.
     * @param clientX Coordinate of the user's pointer along the X axis.
     * @param clientY Coordinate of the user's pointer along the Y axis.
     * @param button Mouse button that should be pressed when dispatching the event.
     */
    private _dispatchPointerEventIfSupported;
    /**
     * Dispatches all the events that are part of a mouse event sequence
     * and then emits a given primary event at the end, if speciifed.
     */
    private _dispatchMouseEventSequence;
}

export { }
