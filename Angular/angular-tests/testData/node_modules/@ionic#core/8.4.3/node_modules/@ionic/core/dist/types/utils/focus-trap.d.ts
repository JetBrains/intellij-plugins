/**
 * This query string selects elements that
 * are eligible to receive focus. We select
 * interactive elements that meet the following
 * criteria:
 * 1. Element does not have a negative tabindex
 * 2. Element does not have `hidden`
 * 3. Element does not have `disabled` for non-Ionic components.
 * 4. Element does not have `disabled` or `disabled="true"` for Ionic components.
 * Note: We need this distinction because `disabled="false"` is
 * valid usage for the disabled property on ion-button.
 */
export declare const focusableQueryString = "[tabindex]:not([tabindex^=\"-\"]):not([hidden]):not([disabled]), input:not([type=hidden]):not([tabindex^=\"-\"]):not([hidden]):not([disabled]), textarea:not([tabindex^=\"-\"]):not([hidden]):not([disabled]), button:not([tabindex^=\"-\"]):not([hidden]):not([disabled]), select:not([tabindex^=\"-\"]):not([hidden]):not([disabled]), ion-checkbox:not([tabindex^=\"-\"]):not([hidden]):not([disabled]), ion-radio:not([tabindex^=\"-\"]):not([hidden]):not([disabled]), .ion-focusable:not([tabindex^=\"-\"]):not([hidden]):not([disabled]), .ion-focusable[disabled=\"false\"]:not([tabindex^=\"-\"]):not([hidden])";
/**
 * Focuses the first descendant in a context
 * that can receive focus. If none exists,
 * a fallback element will be focused.
 * This fallback is typically an ancestor
 * container such as a menu or overlay so focus does not
 * leave the container we are trying to trap focus in.
 *
 * If no fallback is specified then we focus the container itself.
 */
export declare const focusFirstDescendant: <R extends HTMLElement, T extends HTMLElement>(ref: R, fallbackElement?: T) => void;
/**
 * Focuses the last descendant in a context
 * that can receive focus. If none exists,
 * a fallback element will be focused.
 * This fallback is typically an ancestor
 * container such as a menu or overlay so focus does not
 * leave the container we are trying to trap focus in.
 *
 * If no fallback is specified then we focus the container itself.
 */
export declare const focusLastDescendant: <R extends HTMLElement, T extends HTMLElement>(ref: R, fallbackElement?: T) => void;
