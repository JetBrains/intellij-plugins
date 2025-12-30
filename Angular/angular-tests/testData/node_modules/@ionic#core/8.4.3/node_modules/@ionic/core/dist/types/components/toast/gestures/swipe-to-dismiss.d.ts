import type { ToastAnimationPosition } from '../toast-interface';
/**
 * Create a gesture that allows the Toast
 * to be swiped to dismiss.
 * @param el - The Toast element
 * @param toastPosition - The last computed position of the Toast. This is computed in the "present" method.
 * @param onDismiss - A callback to fire when the Toast was swiped to dismiss.
 */
export declare const createSwipeToDismissGesture: (el: HTMLIonToastElement, toastPosition: ToastAnimationPosition, onDismiss: () => void) => import("@utils/gesture").Gesture;
