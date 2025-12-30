export { Platform } from '../platform.d-cnFZCLss.js';
import * as i0 from '@angular/core';
import { Renderer2 } from '@angular/core';

declare class PlatformModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<PlatformModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<PlatformModule, never, never, never>;
    static ɵinj: i0.ɵɵInjectorDeclaration<PlatformModule>;
}

/** @returns The input types supported by this browser. */
declare function getSupportedInputTypes(): Set<string>;

/**
 * Checks whether the user's browser supports passive event listeners.
 * See: https://github.com/WICG/EventListenerOptions/blob/gh-pages/explainer.md
 */
declare function supportsPassiveEventListeners(): boolean;
/**
 * Normalizes an `AddEventListener` object to something that can be passed
 * to `addEventListener` on any browser, no matter whether it supports the
 * `options` parameter.
 * @param options Object to be normalized.
 */
declare function normalizePassiveListenerOptions(options: AddEventListenerOptions): AddEventListenerOptions | boolean;

/** The possible ways the browser may handle the horizontal scroll axis in RTL languages. */
declare enum RtlScrollAxisType {
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
/** Check whether the browser supports scroll behaviors. */
declare function supportsScrollBehavior(): boolean;
/**
 * Checks the type of RTL scroll axis used by this browser. As of time of writing, Chrome is NORMAL,
 * Firefox & Safari are NEGATED, and IE & Edge are INVERTED.
 */
declare function getRtlScrollAxisType(): RtlScrollAxisType;

/** Checks whether the user's browser support Shadow DOM. */
declare function _supportsShadowDom(): boolean;
/** Gets the shadow root of an element, if supported and the element is inside the Shadow DOM. */
declare function _getShadowRoot(element: HTMLElement): ShadowRoot | null;
/**
 * Gets the currently-focused element on the page while
 * also piercing through Shadow DOM boundaries.
 */
declare function _getFocusedElementPierceShadowDom(): HTMLElement | null;
/** Gets the target of an event while accounting for Shadow DOM. */
declare function _getEventTarget<T extends EventTarget>(event: Event): T | null;

/** Gets whether the code is currently running in a test environment. */
declare function _isTestEnvironment(): boolean;

/** Options when binding events manually. */
interface _ListenerOptions {
    capture?: boolean;
    once?: boolean;
    passive?: boolean;
}
/**
 * Binds an event listener with specific options in a backwards-compatible way.
 * This function is necessary, because `Renderer2.listen` only supports listener options
 * after 19.1 and during the v19 period we support any 19.x version.
 * @docs-private
 */
declare function _bindEventWithOptions(renderer: Renderer2, target: EventTarget, eventName: string, callback: (event: any) => boolean | void, options: _ListenerOptions): () => void;

export { PlatformModule, RtlScrollAxisType, _bindEventWithOptions, _getEventTarget, _getFocusedElementPierceShadowDom, _getShadowRoot, _isTestEnvironment, _supportsShadowDom, getRtlScrollAxisType, getSupportedInputTypes, normalizePassiveListenerOptions, supportsPassiveEventListeners, supportsScrollBehavior };
export type { _ListenerOptions };
