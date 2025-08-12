import type { KeyboardPlugin, KeyboardResizeOptions } from '@capacitor/keyboard';
export declare enum KeyboardResize {
    /**
     * Only the `body` HTML element will be resized.
     * Relative units are not affected, because the viewport does not change.
     *
     * @since 1.0.0
     */
    Body = "body",
    /**
     * Only the `ion-app` HTML element will be resized.
     * Use it only for Ionic Framework apps.
     *
     * @since 1.0.0
     */
    Ionic = "ionic",
    /**
     * The whole native Web View will be resized when the keyboard shows/hides.
     * This affects the `vh` relative unit.
     *
     * @since 1.0.0
     */
    Native = "native",
    /**
     * Neither the app nor the Web View are resized.
     *
     * @since 1.0.0
     */
    None = "none"
}
export declare const Keyboard: {
    getEngine(): KeyboardPlugin | undefined;
    getResizeMode(): Promise<KeyboardResizeOptions | undefined>;
};
