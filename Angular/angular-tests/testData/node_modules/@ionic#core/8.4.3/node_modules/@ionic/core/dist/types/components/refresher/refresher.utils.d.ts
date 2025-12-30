type RefresherAnimationType = 'scale' | 'translate';
export declare const getRefresherAnimationType: (contentEl: HTMLElement) => RefresherAnimationType;
export declare const createPullingAnimation: (type: RefresherAnimationType, pullingSpinner: HTMLElement, refresherEl: HTMLElement) => import("../../interface").Animation;
export declare const createSnapBackAnimation: (pullingRefresherIcon: HTMLElement) => import("../../interface").Animation;
export declare const setSpinnerOpacity: (spinner: HTMLElement, opacity: number) => void;
export declare const handleScrollWhilePulling: (ticks: NodeListOf<SVGElement>, numTicks: number, pullAmount: number) => void;
export declare const handleScrollWhileRefreshing: (spinner: HTMLElement, lastVelocityY: number) => void;
export declare const translateElement: (el?: HTMLElement, value?: string, duration?: number) => Promise<unknown>;
/**
 * In order to use the native iOS refresher the device must support rubber band scrolling.
 * As part of this, we need to exclude Desktop Safari because it has a slightly different rubber band effect that is not compatible with the native refresher in Ionic.
 *
 * We also need to be careful not to include devices that spoof their user agent.
 * For example, when using iOS emulation in Chrome the user agent will be spoofed such that
 * navigator.maxTouchPointer > 0. To work around this,
 * we check to see if the apple-pay-logo is supported as a named image which is only
 * true on Apple devices.
 *
 * We previously checked referencEl.style.webkitOverflowScrolling to explicitly check
 * for rubber band support. However, this property was removed on iPadOS and it's possible
 * that this will be removed on iOS in the future too.
 *
 */
export declare const supportsRubberBandScrolling: () => boolean;
export declare const shouldUseNativeRefresher: (referenceEl: HTMLIonRefresherElement, mode: string) => Promise<boolean>;
export {};
