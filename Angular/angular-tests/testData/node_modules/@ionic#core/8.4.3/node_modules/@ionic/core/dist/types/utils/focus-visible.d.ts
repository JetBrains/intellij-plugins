export interface FocusVisibleUtility {
    destroy: () => void;
    setFocus: (elements: Element[]) => void;
}
export declare const startFocusVisible: (rootEl?: HTMLElement) => FocusVisibleUtility;
