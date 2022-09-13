import type { HTMLElementCustomized } from '../typescript';
export declare type ResizableElement = HTMLElementCustomized<{
    __resizeListeners__?: Array<(...args: unknown[]) => unknown>;
    __ro__?: ResizeObserver;
}>;
/** @deprecated use `useResizeObserver` or `useElementSize` in vueuse */
export declare const addResizeListener: (element: ResizableElement, fn: (...args: unknown[]) => unknown) => void;
/** @deprecated use `useResizeObserver` or `useElementSize` in vueuse */
export declare const removeResizeListener: (element: ResizableElement, fn: (...args: unknown[]) => unknown) => void;
