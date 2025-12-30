import type { Mode } from "../../../interface";
import type { ToastAnimationPosition, ToastPosition } from '../toast-interface';
/**
 * Calculate the CSS top and bottom position of the toast, to be used
 * as starting points for the animation keyframes.
 *
 * The default animations for both MD and iOS
 * use translateY, which calculates from the
 * top edge of the screen. This behavior impacts
 * how we compute the offset when a toast has
 * position='bottom' since we need to calculate from
 * the bottom edge of the screen instead.
 *
 * @param position The value of the toast's position prop.
 * @param positionAnchor The element the toast should be anchored to,
 * if applicable.
 * @param mode The toast component's mode (md, ios, etc).
 * @param toast A reference to the toast element itself.
 */
export declare function getAnimationPosition(position: ToastPosition, positionAnchor: HTMLElement | undefined, mode: Mode, toast: HTMLElement): ToastAnimationPosition;
/**
 * Returns the top offset required to place
 * the toast in the middle of the screen.
 * Only needed when position="toast".
 * @param toastHeight - The height of the ion-toast element
 * @param wrapperHeight - The height of the .toast-wrapper element
 * inside the toast's shadow root.
 */
export declare const getOffsetForMiddlePosition: (toastHeight: number, wrapperHeight: number) => number;
