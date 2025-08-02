import type { ActionSheetOptions, AlertOptions, Animation, AnimationBuilder, FrameworkDelegate, HTMLIonOverlayElement, IonicConfig, LoadingOptions, ModalOptions, OverlayInterface, PickerOptions, PopoverOptions, ToastOptions } from '../interface';
export declare const activeAnimations: WeakMap<OverlayInterface, Animation[]>;
export declare const alertController: {
    create(options: AlertOptions): Promise<HTMLIonAlertElement>;
    dismiss(data?: any, role?: string, id?: string): Promise<boolean>;
    getTop(): Promise<HTMLIonAlertElement | undefined>;
};
export declare const actionSheetController: {
    create(options: ActionSheetOptions): Promise<HTMLIonActionSheetElement>;
    dismiss(data?: any, role?: string, id?: string): Promise<boolean>;
    getTop(): Promise<HTMLIonActionSheetElement | undefined>;
};
export declare const loadingController: {
    create(options: LoadingOptions): Promise<HTMLIonLoadingElement>;
    dismiss(data?: any, role?: string, id?: string): Promise<boolean>;
    getTop(): Promise<HTMLIonLoadingElement | undefined>;
};
export declare const modalController: {
    create(options: ModalOptions<import("../interface").ComponentRef>): Promise<HTMLIonModalElement>;
    dismiss(data?: any, role?: string, id?: string): Promise<boolean>;
    getTop(): Promise<HTMLIonModalElement | undefined>;
};
/**
 * @deprecated Use the inline ion-picker component instead.
 */
export declare const pickerController: {
    create(options: PickerOptions): Promise<HTMLIonPickerLegacyElement>;
    dismiss(data?: any, role?: string, id?: string): Promise<boolean>;
    getTop(): Promise<HTMLIonPickerLegacyElement | undefined>;
};
export declare const popoverController: {
    create(options: PopoverOptions<import("../interface").ComponentRef>): Promise<HTMLIonPopoverElement>;
    dismiss(data?: any, role?: string, id?: string): Promise<boolean>;
    getTop(): Promise<HTMLIonPopoverElement | undefined>;
};
export declare const toastController: {
    create(options: ToastOptions): Promise<HTMLIonToastElement>;
    dismiss(data?: any, role?: string, id?: string): Promise<boolean>;
    getTop(): Promise<HTMLIonToastElement | undefined>;
};
/**
 * Prepares the overlay element to be presented.
 */
export declare const prepareOverlay: <T extends HTMLIonOverlayElement>(el: T) => void;
/**
 * Assigns an incrementing id to an overlay element, that does not
 * already have an id assigned to it.
 *
 * Used to track unique instances of an overlay element.
 */
export declare const setOverlayId: <T extends HTMLIonOverlayElement>(el: T) => string;
export declare const createOverlay: <T extends HTMLIonOverlayElement>(tagName: string, opts: object | undefined) => Promise<T>;
export declare const dismissOverlay: (doc: Document, data: any, role: string | undefined, overlayTag: string, id?: string) => Promise<boolean>;
/**
 * Returns a list of all overlays in the DOM even if they are not presented.
 */
export declare const getOverlays: (doc: Document, selector?: string) => HTMLIonOverlayElement[];
/**
 * Returns a presented overlay element.
 * @param doc The document to find the element within.
 * @param overlayTag The selector for the overlay, defaults to Ionic overlay components.
 * @param id The unique identifier for the overlay instance.
 * @returns The overlay element or `undefined` if no overlay element is found.
 */
export declare const getPresentedOverlay: (doc: Document, overlayTag?: string, id?: string) => HTMLIonOverlayElement | undefined;
/**
 * When an overlay is presented, the main
 * focus is the overlay not the page content.
 * We need to remove the page content from the
 * accessibility tree otherwise when
 * users use "read screen from top" gestures with
 * TalkBack and VoiceOver, the screen reader will begin
 * to read the content underneath the overlay.
 *
 * We need a container where all page components
 * exist that is separate from where the overlays
 * are added in the DOM. For most apps, this element
 * is the top most ion-router-outlet. In the event
 * that devs are not using a router,
 * they will need to add the "ion-view-container-root"
 * id to the element that contains all of their views.
 *
 * TODO: If Framework supports having multiple top
 * level router outlets we would need to update this.
 * Example: One outlet for side menu and one outlet
 * for main content.
 */
export declare const setRootAriaHidden: (hidden?: boolean) => void;
export declare const present: <OverlayPresentOptions>(overlay: OverlayInterface, name: keyof IonicConfig, iosEnterAnimation: AnimationBuilder, mdEnterAnimation: AnimationBuilder, opts?: OverlayPresentOptions) => Promise<void>;
export declare const dismiss: <OverlayDismissOptions>(overlay: OverlayInterface, data: any | undefined, role: string | undefined, name: keyof IonicConfig, iosLeaveAnimation: AnimationBuilder, mdLeaveAnimation: AnimationBuilder, opts?: OverlayDismissOptions) => Promise<boolean>;
export declare const eventMethod: <T>(element: HTMLElement, eventName: string) => Promise<T>;
export declare const onceEvent: (element: HTMLElement, eventName: string, callback: (ev: Event) => void) => void;
export declare const isCancel: (role: string | undefined) => boolean;
/**
 * Calls a developer provided method while avoiding
 * Angular Zones. Since the handler is provided by
 * the developer, we should throw any errors
 * received so that developer-provided bug
 * tracking software can log it.
 */
export declare const safeCall: (handler: any, arg?: any) => any;
export declare const BACKDROP = "backdrop";
export declare const GESTURE = "gesture";
export declare const OVERLAY_GESTURE_PRIORITY = 39;
/**
 * Creates a delegate controller.
 *
 * Requires that the component has the following properties:
 * - `el: HTMLElement`
 * - `hasController: boolean`
 * - `delegate?: FrameworkDelegate`
 *
 * @param ref The component class instance.
 */
export declare const createDelegateController: (ref: {
    el: HTMLElement;
    hasController: boolean;
    delegate?: FrameworkDelegate;
}) => {
    attachViewToDom: (component?: any) => Promise<HTMLElement | null>;
    removeViewFromDom: () => void;
};
/**
 * Constructs a trigger interaction for an overlay.
 * Presents an overlay when the trigger is clicked.
 *
 * Usage:
 * ```ts
 * triggerController = createTriggerController();
 * triggerController.addClickListener(el, trigger);
 * ```
 */
export declare const createTriggerController: () => {
    addClickListener: (el: HTMLIonOverlayElement, trigger: string) => void;
    removeClickListener: () => void;
};
export declare const FOCUS_TRAP_DISABLE_CLASS = "ion-disable-focus-trap";
