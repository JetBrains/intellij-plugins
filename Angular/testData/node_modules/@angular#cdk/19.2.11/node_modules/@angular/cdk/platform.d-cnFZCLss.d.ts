import * as i0 from '@angular/core';

/**
 * Service to detect the current platform by comparing the userAgent strings and
 * checking browser-specific global properties.
 */
declare class Platform {
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
    /** Backwards-compatible constructor. */
    constructor(..._args: unknown[]);
    static ɵfac: i0.ɵɵFactoryDeclaration<Platform, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<Platform>;
}

export { Platform };
