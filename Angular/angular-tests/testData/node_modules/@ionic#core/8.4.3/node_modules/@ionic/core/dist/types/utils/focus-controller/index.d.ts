/**
 * The focus controller allows us to manage focus within a view so assistive
 * technologies can inform users of changes to the navigation state. Traditional
 * native apps have a way of informing assistive technology about a navigation
 * state change. Mobile browsers have this too, but only when doing a full page
 * load. In a single page app we do not do that, so we need to build this
 * integration ourselves.
 */
export declare const createFocusController: () => FocusController;
export type FocusController = {
    saveViewFocus: (referenceEl?: HTMLElement) => void;
    setViewFocus: (referenceEl: HTMLElement) => void;
};
