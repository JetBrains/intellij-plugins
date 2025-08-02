import type { NavOptions } from '../../components/nav/nav-interface';
import type { Animation } from '../animation/animation-interface';
export declare const transition: (opts: TransitionOptions) => Promise<TransitionResult>;
export declare const lifecycle: (el: HTMLElement | undefined, eventName: string) => void;
/**
 * Wait two request animation frame loops.
 * This allows the framework implementations enough time to mount
 * the user-defined contents. This is often needed when using inline
 * modals and popovers that accept user components. For popover,
 * the contents must be mounted for the popover to be sized correctly.
 * For modals, the contents must be mounted for iOS to run the
 * transition correctly.
 *
 * On Angular and React, a single raf is enough time, but for Vue
 * we need to wait two rafs. As a result we are using two rafs for
 * all frameworks to ensure contents are mounted.
 */
export declare const waitForMount: () => Promise<void>;
export declare const deepReady: (el: any | undefined) => Promise<void>;
export declare const setPageHidden: (el: HTMLElement, hidden: boolean) => void;
export declare const getIonPageElement: (element: HTMLElement) => Element;
export interface TransitionOptions extends NavOptions {
    progressCallback?: (ani: Animation | undefined) => void;
    baseEl: any;
    enteringEl: HTMLElement;
    leavingEl: HTMLElement | undefined;
}
export interface TransitionResult {
    hasCompleted: boolean;
    animation?: Animation;
}
