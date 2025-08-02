import type { ComponentInterface, EventEmitter } from '../../stencil-public-runtime';
import type { AnimationBuilder, Color, OverlayInterface, FrameworkDelegate } from '../../interface';
import type { OverlayEventDetail } from '../../utils/overlays-interface';
import type { IonicSafeString } from '../../utils/sanitization';
import type { ToastButton, ToastPosition, ToastLayout, ToastSwipeGestureDirection } from './toast-interface';
/**
 * @virtualProp {"ios" | "md"} mode - The mode determines which platform styles to use.
 *
 * @part button - Any button element that is displayed inside of the toast.
 * @part button cancel - Any button element with role "cancel" that is displayed inside of the toast.
 * @part container - The element that wraps all child elements.
 * @part header - The header text of the toast.
 * @part message - The body text of the toast.
 * @part icon - The icon that appears next to the toast content.
 */
export declare class Toast implements ComponentInterface, OverlayInterface {
    private readonly delegateController;
    private readonly lockController;
    private readonly triggerController;
    private customHTMLEnabled;
    private durationTimeout?;
    private gesture?;
    /**
     * Holds the position of the toast calculated in the present
     * animation, to be passed along to the dismiss animation so
     * we don't have to calculate the position twice.
     */
    private lastPresentedPosition?;
    presented: boolean;
    /**
     * When `true`, content inside of .toast-content
     * will have aria-hidden elements removed causing
     * screen readers to announce the remaining content.
     */
    revealContentToScreenReader: boolean;
    el: HTMLIonToastElement;
    /**
     * @internal
     */
    overlayIndex: number;
    /** @internal */
    delegate?: FrameworkDelegate;
    /** @internal */
    hasController: boolean;
    /**
     * The color to use from your application's color palette.
     * Default options are: `"primary"`, `"secondary"`, `"tertiary"`, `"success"`, `"warning"`, `"danger"`, `"light"`, `"medium"`, and `"dark"`.
     * For more information on colors, see [theming](/docs/theming/basics).
     */
    color?: Color;
    /**
     * Animation to use when the toast is presented.
     */
    enterAnimation?: AnimationBuilder;
    /**
     * Animation to use when the toast is dismissed.
     */
    leaveAnimation?: AnimationBuilder;
    /**
     * Additional classes to apply for custom CSS. If multiple classes are
     * provided they should be separated by spaces.
     */
    cssClass?: string | string[];
    /**
     * How many milliseconds to wait before hiding the toast. By default, it will show
     * until `dismiss()` is called.
     */
    duration: number;
    /**
     * Header to be shown in the toast.
     */
    header?: string;
    /**
     * Defines how the message and buttons are laid out in the toast.
     * 'baseline': The message and the buttons will appear on the same line.
     * Message text may wrap within the message container.
     * 'stacked': The buttons containers and message will stack on top
     * of each other. Use this if you have long text in your buttons.
     */
    layout: ToastLayout;
    /**
     * Message to be shown in the toast.
     * This property accepts custom HTML as a string.
     * Content is parsed as plaintext by default.
     * `innerHTMLTemplatesEnabled` must be set to `true` in the Ionic config
     * before custom HTML can be used.
     */
    message?: string | IonicSafeString;
    /**
     * If `true`, the keyboard will be automatically dismissed when the overlay is presented.
     */
    keyboardClose: boolean;
    /**
     * The starting position of the toast on the screen. Can be tweaked further
     * using the `positionAnchor` property.
     */
    position: ToastPosition;
    /**
     * The element to anchor the toast's position to. Can be set as a direct reference
     * or the ID of the element. With `position="bottom"`, the toast will sit above the
     * chosen element. With `position="top"`, the toast will sit below the chosen element.
     * With `position="middle"`, the value of `positionAnchor` is ignored.
     */
    positionAnchor?: HTMLElement | string;
    /**
     * An array of buttons for the toast.
     */
    buttons?: (ToastButton | string)[];
    /**
     * If `true`, the toast will be translucent.
     * Only applies when the mode is `"ios"` and the device supports
     * [`backdrop-filter`](https://developer.mozilla.org/en-US/docs/Web/CSS/backdrop-filter#Browser_compatibility).
     */
    translucent: boolean;
    /**
     * If `true`, the toast will animate.
     */
    animated: boolean;
    /**
     * The name of the icon to display, or the path to a valid SVG file. See `ion-icon`.
     * https://ionic.io/ionicons
     */
    icon?: string;
    /**
     * Additional attributes to pass to the toast.
     */
    htmlAttributes?: {
        [key: string]: any;
    };
    /**
     * If set to 'vertical', the Toast can be dismissed with
     * a swipe gesture. The swipe direction is determined by
     * the value of the `position` property:
     * `top`: The Toast can be swiped up to dismiss.
     * `bottom`: The Toast can be swiped down to dismiss.
     * `middle`: The Toast can be swiped up or down to dismiss.
     */
    swipeGesture?: ToastSwipeGestureDirection;
    swipeGestureChanged(): void;
    /**
     * If `true`, the toast will open. If `false`, the toast will close.
     * Use this if you need finer grained control over presentation, otherwise
     * just use the toastController or the `trigger` property.
     * Note: `isOpen` will not automatically be set back to `false` when
     * the toast dismisses. You will need to do that in your code.
     */
    isOpen: boolean;
    onIsOpenChange(newValue: boolean, oldValue: boolean): void;
    /**
     * An ID corresponding to the trigger element that
     * causes the toast to open when clicked.
     */
    trigger: string | undefined;
    triggerChanged(): void;
    /**
     * Emitted after the toast has presented.
     */
    didPresent: EventEmitter<void>;
    /**
     * Emitted before the toast has presented.
     */
    willPresent: EventEmitter<void>;
    /**
     * Emitted before the toast has dismissed.
     */
    willDismiss: EventEmitter<OverlayEventDetail>;
    /**
     * Emitted after the toast has dismissed.
     */
    didDismiss: EventEmitter<OverlayEventDetail>;
    /**
     * Emitted after the toast has presented.
     * Shorthand for ionToastWillDismiss.
     */
    didPresentShorthand: EventEmitter<void>;
    /**
     * Emitted before the toast has presented.
     * Shorthand for ionToastWillPresent.
     */
    willPresentShorthand: EventEmitter<void>;
    /**
     * Emitted before the toast has dismissed.
     * Shorthand for ionToastWillDismiss.
     */
    willDismissShorthand: EventEmitter<OverlayEventDetail>;
    /**
     * Emitted after the toast has dismissed.
     * Shorthand for ionToastDidDismiss.
     */
    didDismissShorthand: EventEmitter<OverlayEventDetail>;
    connectedCallback(): void;
    disconnectedCallback(): void;
    componentWillLoad(): void;
    componentDidLoad(): void;
    /**
     * Present the toast overlay after it has been created.
     */
    present(): Promise<void>;
    /**
     * Dismiss the toast overlay after it has been presented.
     *
     * @param data Any data to emit in the dismiss events.
     * @param role The role of the element that is dismissing the toast.
     * This can be useful in a button handler for determining which button was
     * clicked to dismiss the toast.
     * Some examples include: ``"cancel"`, `"destructive"`, "selected"`, and `"backdrop"`.
     *
     * This is a no-op if the overlay has not been presented yet. If you want
     * to remove an overlay from the DOM that was never presented, use the
     * [remove](https://developer.mozilla.org/en-US/docs/Web/API/Element/remove) method.
     */
    dismiss(data?: any, role?: string): Promise<boolean>;
    /**
     * Returns a promise that resolves when the toast did dismiss.
     */
    onDidDismiss<T = any>(): Promise<OverlayEventDetail<T>>;
    /**
     * Returns a promise that resolves when the toast will dismiss.
     */
    onWillDismiss<T = any>(): Promise<OverlayEventDetail<T>>;
    private getButtons;
    /**
     * Returns the element specified by the positionAnchor prop,
     * or undefined if prop's value is an ID string and the element
     * is not found in the DOM.
     */
    private getAnchorElement;
    private buttonClick;
    private callButtonHandler;
    private dispatchCancelHandler;
    /**
     * Create a new swipe gesture so Toast
     * can be swiped to dismiss.
     */
    private createSwipeGesture;
    /**
     * Destroy an existing swipe gesture
     * so Toast can no longer be swiped to dismiss.
     */
    private destroySwipeGesture;
    /**
     * Returns `true` if swipeGesture
     * is configured to a value that enables the swipe behavior.
     * Returns `false` otherwise.
     */
    private prefersSwipeGesture;
    renderButtons(buttons: ToastButton[], side: 'start' | 'end'): any;
    /**
     * Render the `message` property.
     * @param key - A key to give the element a stable identity. This is used to improve compatibility with screen readers.
     * @param ariaHidden - If "true" then content will be hidden from screen readers.
     */
    private renderToastMessage;
    /**
     * Render the `header` property.
     * @param key - A key to give the element a stable identity. This is used to improve compatibility with screen readers.
     * @param ariaHidden - If "true" then content will be hidden from screen readers.
     */
    private renderHeader;
    render(): any;
}
