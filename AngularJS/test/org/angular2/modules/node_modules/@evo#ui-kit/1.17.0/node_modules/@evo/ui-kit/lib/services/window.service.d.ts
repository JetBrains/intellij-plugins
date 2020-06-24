import { ClassProvider, FactoryProvider, InjectionToken } from '@angular/core';
export declare const WINDOW: InjectionToken<{}>;
export declare abstract class WindowRef {
    readonly nativeWindow: Window | Object;
}
export declare class BrowserWindowRef extends WindowRef {
    constructor();
    readonly nativeWindow: Window | Object;
}
export declare function windowFactory(browserWindowRef: BrowserWindowRef, platformId: Object): Window | Object;
export declare const WINDOW_PROVIDERS: (FactoryProvider | ClassProvider)[];
