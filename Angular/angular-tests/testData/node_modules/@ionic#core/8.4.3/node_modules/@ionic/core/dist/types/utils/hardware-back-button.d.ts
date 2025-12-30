export interface BackButtonEventDetail {
    register(priority: number, handler: (processNextHandler: () => void) => Promise<any> | void): void;
}
export type BackButtonEvent = CustomEvent<BackButtonEventDetail>;
/**
 * CloseWatcher is a newer API that lets
 * use detect the hardware back button event
 * in a web browser: https://caniuse.com/?search=closewatcher
 * However, not every browser supports it yet.
 *
 * This needs to be a function so that we can
 * check the config once it has been set.
 * Otherwise, this code would be evaluated the
 * moment this file is evaluated which could be
 * before the config is set.
 */
export declare const shouldUseCloseWatcher: () => any;
/**
 * When hardwareBackButton: false in config,
 * we need to make sure we also block the default
 * webview behavior. If we don't then it will be
 * possible for users to navigate backward while
 * an overlay is still open. Additionally, it will
 * give the appearance that the hardwareBackButton
 * config is not working as the page transition
 * will still happen.
 */
export declare const blockHardwareBackButton: () => void;
export declare const startHardwareBackButton: () => void;
export declare const OVERLAY_BACK_BUTTON_PRIORITY = 100;
export declare const MENU_BACK_BUTTON_PRIORITY = 99;
