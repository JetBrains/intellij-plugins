/** @deprecated use `element.addEventListener` instead */
export declare const on: (element: HTMLElement | Document | Window, event: string, handler: EventListenerOrEventListenerObject, useCapture?: boolean) => void;
/** @deprecated use `element.addEventListener` instead */
export declare const off: (element: HTMLElement | Document | Window, event: string, handler: EventListenerOrEventListenerObject, useCapture?: boolean) => void;
/** @deprecated use `element.addEventListener` instead */
export declare const once: (el: HTMLElement, event: string, fn: EventListener) => void;
export declare const composeEventHandlers: <E>(theirsHandler?: ((event: E) => boolean | void) | undefined, oursHandler?: ((event: E) => void) | undefined, { checkForDefaultPrevented }?: {
    checkForDefaultPrevented?: boolean | undefined;
}) => (event: E) => void;
declare type WhenMouseHandler = (e: PointerEvent) => any;
export declare const whenMouse: (handler: WhenMouseHandler) => WhenMouseHandler;
export {};
