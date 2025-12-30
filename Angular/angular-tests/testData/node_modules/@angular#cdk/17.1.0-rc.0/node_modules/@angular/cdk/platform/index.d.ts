import * as i0 from '@angular/core';

/** Gets the target of an event while accounting for Shadow DOM. */
export declare function _getEventTarget<T extends EventTarget>(event: Event): T | null;

/**
 * Gets the currently-focused element on the page while
 * also piercing through Shadow DOM boundaries.
 */
export declare function _getFocusedElementPierceShadowDom(): HTMLElement | null;

/**
 * Checks the type of RTL scroll axis used by this browser. As of time of writing, Chrome is NORMAL,
 * Firefox & Safari are NEGATED, and IE & Edge are INVERTED.
 */
export declare function getRtlScrollAxisType(): RtlScrollAxisType;

/** Gets the shadow root of an element, if supported and the element is inside the Shadow DOM. */
export declare function _getShadowRoot(element: HTMLElement): ShadowRoot | null;


/** @returns The input types supported by this browser. */
export declare function getSupportedInputTypes(): Set<string>;


/** Gets whether the code is currently running in a test environment. */
export declare function _isTestEnvironment(): boolean;

/**
 * Normalizes an `AddEventListener` object to something that can be passed
 * to `addEventListener` on any browser, no matter whether it supports the
 * `options` parameter.
 * @param options Object to be normalized.
 */
export declare function normalizePassiveListenerOptions(options: AddEventListenerOptions): AddEventListenerOptions | boolean;

/**
 * Service to detect the current platform by comparing the userAgent strings and
 * checking browser-specific global properties.
 */
export declare class Platform {
    private _platformId;
    /** Whether the Angular application is being rendered in the browser. */
    isBrowser: boolean;
    /** Whether the current browser is Microsoft Edge. */
    EDGE: boolean;
    /** Whether the current rendering engine is Microsoft Trident. */
    TRIDENT: boolean;
    /** Whether the current rendering engine is Blink. */
    BLINK: boolean;
    /** Whether the current rendering engine is WebKit. */
    WEBKIT: boolean;
    /** Whether the current platform is Apple iOS. */
    IOS: boolean;
    /** Whether the current browser is Firefox. */
    FIREFOX: boolean;
    /** Whether the current platform is Android. */
    ANDROID: boolean;
    /** Whether the current browser is Safari. */
    SAFARI: boolean;
    constructor(_platformId: Object);
    static ɵfac: i0.ɵɵFactoryDeclaration<Platform, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<Platform>;
}

export declare class PlatformModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<PlatformModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<PlatformModule, never, never, never>;
    static ɵinj: i0.ɵɵInjectorDeclaration<PlatformModule>;
}


/** The possible ways the browser may handle the horizontal scroll axis in RTL languages. */
export declare enum RtlScrollAxisType {
    /**
     * scrollLeft is 0 when scrolled all the way left and (scrollWidth - clientWidth) when scrolled
     * all the way right.
     */
    NORMAL = 0,
    /**
     * scrollLeft is -(scrollWidth - clientWidth) when scrolled all the way left and 0 when scrolled
     * all the way right.
     */
    NEGATED = 1,
    /**
     * scrollLeft is (scrollWidth - clientWidth) when scrolled all the way left and 0 when scrolled
     * all the way right.
     */
    INVERTED = 2
}


/**
 * Checks whether the user's browser supports passive event listeners.
 * See: https://github.com/WICG/EventListenerOptions/blob/gh-pages/explainer.md
 */
export declare function supportsPassiveEventListeners(): boolean;

/** Check whether the browser supports scroll behaviors. */
export declare function supportsScrollBehavior(): boolean;


/** Checks whether the user's browser support Shadow DOM. */
export declare function _supportsShadowDom(): boolean;

export { }
