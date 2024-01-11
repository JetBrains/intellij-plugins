import { ElementDimensions } from '@angular/cdk/testing';
import { EventData } from '@angular/cdk/testing';
import { HarnessEnvironment } from '@angular/cdk/testing';
import { HarnessLoader } from '@angular/cdk/testing';
import { ModifierKeys } from '@angular/cdk/testing';
import { TestElement } from '@angular/cdk/testing';
import { TestKey } from '@angular/cdk/testing';
import { TextOptions } from '@angular/cdk/testing';
import * as webdriver from 'selenium-webdriver';

/** A `TestElement` implementation for WebDriver. */
export declare class SeleniumWebDriverElement implements TestElement {
    readonly element: () => webdriver.WebElement;
    private _stabilize;
    constructor(element: () => webdriver.WebElement, _stabilize: () => Promise<void>);
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
     * and attempts to add the string to the Element's value.
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
    setInputValue(newValue: string): Promise<void>;
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
    /** Gets the webdriver action sequence. */
    private _actions;
    /** Executes a function in the browser. */
    private _executeScript;
    /** Dispatches all the events that are part of a click event sequence. */
    private _dispatchClickEventSequence;
}

/** A `HarnessEnvironment` implementation for WebDriver. */
export declare class SeleniumWebDriverHarnessEnvironment extends HarnessEnvironment<() => webdriver.WebElement> {
    /** The options for this environment. */
    private _options;
    /** Environment stabilization callback passed to the created test elements. */
    private _stabilizeCallback;
    protected constructor(rawRootElement: () => webdriver.WebElement, options?: WebDriverHarnessEnvironmentOptions);
    /** Gets the ElementFinder corresponding to the given TestElement. */
    static getNativeElement(el: TestElement): webdriver.WebElement;
    /** Creates a `HarnessLoader` rooted at the document root. */
    static loader(driver: webdriver.WebDriver, options?: WebDriverHarnessEnvironmentOptions): HarnessLoader;
    /**
     * Flushes change detection and async tasks captured in the Angular zone.
     * In most cases it should not be necessary to call this manually. However, there may be some edge
     * cases where it is needed to fully flush animation events.
     */
    forceStabilize(): Promise<void>;
    /** @docs-private */
    waitForTasksOutsideAngular(): Promise<void>;
    /** Gets the root element for the document. */
    protected getDocumentRoot(): () => webdriver.WebElement;
    /** Creates a `TestElement` from a raw element. */
    protected createTestElement(element: () => webdriver.WebElement): TestElement;
    /** Creates a `HarnessLoader` rooted at the given raw element. */
    protected createEnvironment(element: () => webdriver.WebElement): HarnessEnvironment<() => webdriver.WebElement>;
    /**
     * Gets a list of all elements matching the given selector under this environment's root element.
     */
    protected getAllRawElements(selector: string): Promise<(() => webdriver.WebElement)[]>;
}

/** Waits for angular to be ready after the page load. */
export declare function waitForAngularReady(wd: webdriver.WebDriver): Promise<void>;

/** Options to configure the environment. */
export declare interface WebDriverHarnessEnvironmentOptions {
    /** The query function used to find DOM elements. */
    queryFn: (selector: string, root: () => webdriver.WebElement) => Promise<webdriver.WebElement[]>;
}

export { }
