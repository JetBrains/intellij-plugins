/**
 * Creates a controller that tracks and reacts to opening or closing the keyboard.
 *
 * @internal
 * @param keyboardChangeCallback A function to call when the keyboard opens or closes.
 */
export declare const createKeyboardController: (keyboardChangeCallback?: (keyboardOpen: boolean, resizePromise?: Promise<void>) => void) => Promise<KeyboardController>;
export type KeyboardController = {
    init: () => void;
    destroy: () => void;
    isKeyboardVisible: () => boolean;
};
