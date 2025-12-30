import * as i0 from '@angular/core';
import { ViewContainerRef, Injector, StaticProvider, Type, OnDestroy, ElementRef, NgZone, ChangeDetectorRef, ComponentRef, EmbeddedViewRef, TemplateRef, InjectionToken } from '@angular/core';
import { FocusOrigin } from '../focus-monitor.d-BBkiOKUH.js';
import { FocusTrapFactory, A11yModule } from '../a11y-module.d-DrV0SO0k.js';
import { BasePortalOutlet, CdkPortalOutlet, ComponentPortal, TemplatePortal, DomPortal, ComponentType, PortalModule } from '../portal-directives.d-C698lRc2.js';
export { CdkPortal as ɵɵCdkPortal, PortalHostDirective as ɵɵPortalHostDirective, TemplatePortalDirective as ɵɵTemplatePortalDirective } from '../portal-directives.d-C698lRc2.js';
import { Direction } from '../bidi-module.d-BSI86Zrk.js';
import { PositionStrategy, ScrollStrategy, OverlayRef, OverlayModule } from '../overlay-module.d-CSrPj90C.js';
import { Observable, Subject } from 'rxjs';
import * as _angular_cdk_portal from '@angular/cdk/portal';
import { Overlay } from '../overlay.d-CPV_bcvH.js';
import '../observers/index.js';
import '../number-property.d-BzBQchZ2.js';
import '../scrolling-module.d-CUKr8D_p.js';
import '../data-source.d-DAIyaEMO.js';
import '@angular/common';
import '../scrolling/index.js';
import '../platform.d-cnFZCLss.js';
import '../style-loader.d-DbvWk0ty.js';

/** Options for where to set focus to automatically on dialog open */
type AutoFocusTarget = 'dialog' | 'first-tabbable' | 'first-heading';
/** Valid ARIA roles for a dialog. */
type DialogRole = 'dialog' | 'alertdialog';
/** Configuration for opening a modal dialog. */
declare class DialogConfig<D = unknown, R = unknown, C extends BasePortalOutlet = BasePortalOutlet> {
    /**
     * Where the attached component should live in Angular's *logical* component tree.
     * This affects what is available for injection and the change detection order for the
     * component instantiated inside of the dialog. This does not affect where the dialog
     * content will be rendered.
     */
    viewContainerRef?: ViewContainerRef;
    /**
     * Injector used for the instantiation of the component to be attached. If provided,
     * takes precedence over the injector indirectly provided by `ViewContainerRef`.
     */
    injector?: Injector;
    /** ID for the dialog. If omitted, a unique one will be generated. */
    id?: string;
    /** The ARIA role of the dialog element. */
    role?: DialogRole;
    /** Optional CSS class or classes applied to the overlay panel. */
    panelClass?: string | string[];
    /** Whether the dialog has a backdrop. */
    hasBackdrop?: boolean;
    /** Optional CSS class or classes applied to the overlay backdrop. */
    backdropClass?: string | string[];
    /** Whether the dialog closes with the escape key or pointer events outside the panel element. */
    disableClose?: boolean;
    /** Width of the dialog. */
    width?: string;
    /** Height of the dialog. */
    height?: string;
    /** Min-width of the dialog. If a number is provided, assumes pixel units. */
    minWidth?: number | string;
    /** Min-height of the dialog. If a number is provided, assumes pixel units. */
    minHeight?: number | string;
    /** Max-width of the dialog. If a number is provided, assumes pixel units. Defaults to 80vw. */
    maxWidth?: number | string;
    /** Max-height of the dialog. If a number is provided, assumes pixel units. */
    maxHeight?: number | string;
    /** Strategy to use when positioning the dialog. Defaults to centering it on the page. */
    positionStrategy?: PositionStrategy;
    /** Data being injected into the child component. */
    data?: D | null;
    /** Layout direction for the dialog's content. */
    direction?: Direction;
    /** ID of the element that describes the dialog. */
    ariaDescribedBy?: string | null;
    /** ID of the element that labels the dialog. */
    ariaLabelledBy?: string | null;
    /** Dialog label applied via `aria-label` */
    ariaLabel?: string | null;
    /**
     * Whether this is a modal dialog. Used to set the `aria-modal` attribute. Off by default,
     * because it can interfere with other overlay-based components (e.g. `mat-select`) and because
     * it is redundant since the dialog marks all outside content as `aria-hidden` anyway.
     */
    ariaModal?: boolean;
    /**
     * Where the dialog should focus on open.
     * @breaking-change 14.0.0 Remove boolean option from autoFocus. Use string or
     * AutoFocusTarget instead.
     */
    autoFocus?: AutoFocusTarget | string | boolean;
    /**
     * Whether the dialog should restore focus to the previously-focused element upon closing.
     * Has the following behavior based on the type that is passed in:
     * - `boolean` - when true, will return focus to the element that was focused before the dialog
     *    was opened, otherwise won't restore focus at all.
     * - `string` - focus will be restored to the first element that matches the CSS selector.
     * - `HTMLElement` - focus will be restored to the specific element.
     */
    restoreFocus?: boolean | string | HTMLElement;
    /**
     * Scroll strategy to be used for the dialog. This determines how
     * the dialog responds to scrolling underneath the panel element.
     */
    scrollStrategy?: ScrollStrategy;
    /**
     * Whether the dialog should close when the user navigates backwards or forwards through browser
     * history. This does not apply to navigation via anchor element unless using URL-hash based
     * routing (`HashLocationStrategy` in the Angular router).
     */
    closeOnNavigation?: boolean;
    /**
     * Whether the dialog should close when the dialog service is destroyed. This is useful if
     * another service is wrapping the dialog and is managing the destruction instead.
     */
    closeOnDestroy?: boolean;
    /**
     * Whether the dialog should close when the underlying overlay is detached. This is useful if
     * another service is wrapping the dialog and is managing the destruction instead. E.g. an
     * external detachment can happen as a result of a scroll strategy triggering it or when the
     * browser location changes.
     */
    closeOnOverlayDetachments?: boolean;
    /**
     * Alternate `ComponentFactoryResolver` to use when resolving the associated component.
     * @deprecated No longer used. Will be removed.
     * @breaking-change 20.0.0
     */
    componentFactoryResolver?: unknown;
    /**
     * Providers that will be exposed to the contents of the dialog. Can also
     * be provided as a function in order to generate the providers lazily.
     */
    providers?: StaticProvider[] | ((dialogRef: R, config: DialogConfig<D, R, C>, container: C) => StaticProvider[]);
    /**
     * Component into which the dialog content will be rendered. Defaults to `CdkDialogContainer`.
     * A configuration object can be passed in to customize the providers that will be exposed
     * to the dialog container.
     */
    container?: Type<C> | {
        type: Type<C>;
        providers: (config: DialogConfig<D, R, C>) => StaticProvider[];
    };
    /**
     * Context that will be passed to template-based dialogs.
     * A function can be passed in to resolve the context lazily.
     */
    templateContext?: Record<string, any> | (() => Record<string, any>);
}

declare function throwDialogContentAlreadyAttachedError(): void;
/**
 * Internal component that wraps user-provided dialog content.
 * @docs-private
 */
declare class CdkDialogContainer<C extends DialogConfig = DialogConfig> extends BasePortalOutlet implements OnDestroy {
    protected _elementRef: ElementRef<HTMLElement>;
    protected _focusTrapFactory: FocusTrapFactory;
    readonly _config: C;
    private _interactivityChecker;
    protected _ngZone: NgZone;
    private _overlayRef;
    private _focusMonitor;
    private _renderer;
    private _platform;
    protected _document: Document;
    /** The portal outlet inside of this container into which the dialog content will be loaded. */
    _portalOutlet: CdkPortalOutlet;
    /** The class that traps and manages focus within the dialog. */
    private _focusTrap;
    /** Element that was focused before the dialog was opened. Save this to restore upon close. */
    private _elementFocusedBeforeDialogWasOpened;
    /**
     * Type of interaction that led to the dialog being closed. This is used to determine
     * whether the focus style will be applied when returning focus to its original location
     * after the dialog is closed.
     */
    _closeInteractionType: FocusOrigin | null;
    /**
     * Queue of the IDs of the dialog's label element, based on their definition order. The first
     * ID will be used as the `aria-labelledby` value. We use a queue here to handle the case
     * where there are two or more titles in the DOM at a time and the first one is destroyed while
     * the rest are present.
     */
    _ariaLabelledByQueue: string[];
    protected readonly _changeDetectorRef: ChangeDetectorRef;
    private _injector;
    private _isDestroyed;
    constructor(...args: unknown[]);
    _addAriaLabelledBy(id: string): void;
    _removeAriaLabelledBy(id: string): void;
    protected _contentAttached(): void;
    /**
     * Can be used by child classes to customize the initial focus
     * capturing behavior (e.g. if it's tied to an animation).
     */
    protected _captureInitialFocus(): void;
    ngOnDestroy(): void;
    /**
     * Attach a ComponentPortal as content to this dialog container.
     * @param portal Portal to be attached as the dialog content.
     */
    attachComponentPortal<T>(portal: ComponentPortal<T>): ComponentRef<T>;
    /**
     * Attach a TemplatePortal as content to this dialog container.
     * @param portal Portal to be attached as the dialog content.
     */
    attachTemplatePortal<T>(portal: TemplatePortal<T>): EmbeddedViewRef<T>;
    /**
     * Attaches a DOM portal to the dialog container.
     * @param portal Portal to be attached.
     * @deprecated To be turned into a method.
     * @breaking-change 10.0.0
     */
    attachDomPortal: (portal: DomPortal) => void;
    /** Captures focus if it isn't already inside the dialog. */
    _recaptureFocus(): void;
    /**
     * Focuses the provided element. If the element is not focusable, it will add a tabIndex
     * attribute to forcefully focus it. The attribute is removed after focus is moved.
     * @param element The element to focus.
     */
    private _forceFocus;
    /**
     * Focuses the first element that matches the given selector within the focus trap.
     * @param selector The CSS selector for the element to set focus to.
     */
    private _focusByCssSelector;
    /**
     * Moves the focus inside the focus trap. When autoFocus is not set to 'dialog', if focus
     * cannot be moved then focus will go to the dialog container.
     */
    protected _trapFocus(options?: FocusOptions): void;
    /** Restores focus to the element that was focused before the dialog opened. */
    private _restoreFocus;
    /** Focuses the dialog container. */
    private _focusDialogContainer;
    /** Returns whether focus is inside the dialog. */
    private _containsFocus;
    /** Sets up the focus trap. */
    private _initializeFocusTrap;
    /** Sets up the listener that handles clicks on the dialog backdrop. */
    private _handleBackdropClicks;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkDialogContainer<any>, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<CdkDialogContainer<any>, "cdk-dialog-container", never, {}, {}, never, never, true, never>;
}

/** Additional options that can be passed in when closing a dialog. */
interface DialogCloseOptions {
    /** Focus original to use when restoring focus. */
    focusOrigin?: FocusOrigin;
}
/**
 * Reference to a dialog opened via the Dialog service.
 */
declare class DialogRef<R = unknown, C = unknown> {
    readonly overlayRef: OverlayRef;
    readonly config: DialogConfig<any, DialogRef<R, C>, BasePortalOutlet>;
    /**
     * Instance of component opened into the dialog. Will be
     * null when the dialog is opened using a `TemplateRef`.
     */
    readonly componentInstance: C | null;
    /**
     * `ComponentRef` of the component opened into the dialog. Will be
     * null when the dialog is opened using a `TemplateRef`.
     */
    readonly componentRef: ComponentRef<C> | null;
    /** Instance of the container that is rendering out the dialog content. */
    readonly containerInstance: BasePortalOutlet & {
        _closeInteractionType?: FocusOrigin;
    };
    /** Whether the user is allowed to close the dialog. */
    disableClose: boolean | undefined;
    /** Emits when the dialog has been closed. */
    readonly closed: Observable<R | undefined>;
    /** Emits when the backdrop of the dialog is clicked. */
    readonly backdropClick: Observable<MouseEvent>;
    /** Emits when on keyboard events within the dialog. */
    readonly keydownEvents: Observable<KeyboardEvent>;
    /** Emits on pointer events that happen outside of the dialog. */
    readonly outsidePointerEvents: Observable<MouseEvent>;
    /** Unique ID for the dialog. */
    readonly id: string;
    /** Subscription to external detachments of the dialog. */
    private _detachSubscription;
    constructor(overlayRef: OverlayRef, config: DialogConfig<any, DialogRef<R, C>, BasePortalOutlet>);
    /**
     * Close the dialog.
     * @param result Optional result to return to the dialog opener.
     * @param options Additional options to customize the closing behavior.
     */
    close(result?: R, options?: DialogCloseOptions): void;
    /** Updates the position of the dialog based on the current position strategy. */
    updatePosition(): this;
    /**
     * Updates the dialog's width and height.
     * @param width New width of the dialog.
     * @param height New height of the dialog.
     */
    updateSize(width?: string | number, height?: string | number): this;
    /** Add a CSS class or an array of classes to the overlay pane. */
    addPanelClass(classes: string | string[]): this;
    /** Remove a CSS class or an array of classes from the overlay pane. */
    removePanelClass(classes: string | string[]): this;
}

declare class Dialog implements OnDestroy {
    private _overlay;
    private _injector;
    private _defaultOptions;
    private _parentDialog;
    private _overlayContainer;
    private _idGenerator;
    private _openDialogsAtThisLevel;
    private readonly _afterAllClosedAtThisLevel;
    private readonly _afterOpenedAtThisLevel;
    private _ariaHiddenElements;
    private _scrollStrategy;
    /** Keeps track of the currently-open dialogs. */
    get openDialogs(): readonly DialogRef<any, any>[];
    /** Stream that emits when a dialog has been opened. */
    get afterOpened(): Subject<DialogRef<any, any>>;
    /**
     * Stream that emits when all open dialog have finished closing.
     * Will emit on subscribe if there are no open dialogs to begin with.
     */
    readonly afterAllClosed: Observable<void>;
    constructor(...args: unknown[]);
    /**
     * Opens a modal dialog containing the given component.
     * @param component Type of the component to load into the dialog.
     * @param config Extra configuration options.
     * @returns Reference to the newly-opened dialog.
     */
    open<R = unknown, D = unknown, C = unknown>(component: ComponentType<C>, config?: DialogConfig<D, DialogRef<R, C>>): DialogRef<R, C>;
    /**
     * Opens a modal dialog containing the given template.
     * @param template TemplateRef to instantiate as the dialog content.
     * @param config Extra configuration options.
     * @returns Reference to the newly-opened dialog.
     */
    open<R = unknown, D = unknown, C = unknown>(template: TemplateRef<C>, config?: DialogConfig<D, DialogRef<R, C>>): DialogRef<R, C>;
    open<R = unknown, D = unknown, C = unknown>(componentOrTemplateRef: ComponentType<C> | TemplateRef<C>, config?: DialogConfig<D, DialogRef<R, C>>): DialogRef<R, C>;
    /**
     * Closes all of the currently-open dialogs.
     */
    closeAll(): void;
    /**
     * Finds an open dialog by its id.
     * @param id ID to use when looking up the dialog.
     */
    getDialogById<R, C>(id: string): DialogRef<R, C> | undefined;
    ngOnDestroy(): void;
    /**
     * Creates an overlay config from a dialog config.
     * @param config The dialog configuration.
     * @returns The overlay configuration.
     */
    private _getOverlayConfig;
    /**
     * Attaches a dialog container to a dialog's already-created overlay.
     * @param overlay Reference to the dialog's underlying overlay.
     * @param config The dialog configuration.
     * @returns A promise resolving to a ComponentRef for the attached container.
     */
    private _attachContainer;
    /**
     * Attaches the user-provided component to the already-created dialog container.
     * @param componentOrTemplateRef The type of component being loaded into the dialog,
     *     or a TemplateRef to instantiate as the content.
     * @param dialogRef Reference to the dialog being opened.
     * @param dialogContainer Component that is going to wrap the dialog content.
     * @param config Configuration used to open the dialog.
     */
    private _attachDialogContent;
    /**
     * Creates a custom injector to be used inside the dialog. This allows a component loaded inside
     * of a dialog to close itself and, optionally, to return a value.
     * @param config Config object that is used to construct the dialog.
     * @param dialogRef Reference to the dialog being opened.
     * @param dialogContainer Component that is going to wrap the dialog content.
     * @param fallbackInjector Injector to use as a fallback when a lookup fails in the custom
     * dialog injector, if the user didn't provide a custom one.
     * @returns The custom injector that can be used inside the dialog.
     */
    private _createInjector;
    /**
     * Removes a dialog from the array of open dialogs.
     * @param dialogRef Dialog to be removed.
     * @param emitEvent Whether to emit an event if this is the last dialog.
     */
    private _removeOpenDialog;
    /** Hides all of the content that isn't an overlay from assistive technology. */
    private _hideNonDialogContentFromAssistiveTechnology;
    private _getAfterAllClosed;
    static ɵfac: i0.ɵɵFactoryDeclaration<Dialog, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<Dialog>;
}

declare class DialogModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<DialogModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<DialogModule, never, [typeof OverlayModule, typeof PortalModule, typeof A11yModule, typeof CdkDialogContainer], [typeof PortalModule, typeof CdkDialogContainer]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<DialogModule>;
}

/** Injection token for the Dialog's ScrollStrategy. */
declare const DIALOG_SCROLL_STRATEGY: InjectionToken<() => ScrollStrategy>;
/** Injection token for the Dialog's Data. */
declare const DIALOG_DATA: InjectionToken<any>;
/** Injection token that can be used to provide default options for the dialog module. */
declare const DEFAULT_DIALOG_CONFIG: InjectionToken<DialogConfig<unknown, unknown, _angular_cdk_portal.BasePortalOutlet>>;
/**
 * @docs-private
 * @deprecated No longer used. To be removed.
 * @breaking-change 19.0.0
 */
declare function DIALOG_SCROLL_STRATEGY_PROVIDER_FACTORY(overlay: Overlay): () => ScrollStrategy;
/**
 * @docs-private
 * @deprecated No longer used. To be removed.
 * @breaking-change 19.0.0
 */
declare const DIALOG_SCROLL_STRATEGY_PROVIDER: {
    provide: InjectionToken<() => ScrollStrategy>;
    deps: (typeof Overlay)[];
    useFactory: typeof DIALOG_SCROLL_STRATEGY_PROVIDER_FACTORY;
};

export { CdkDialogContainer, DEFAULT_DIALOG_CONFIG, DIALOG_DATA, DIALOG_SCROLL_STRATEGY, DIALOG_SCROLL_STRATEGY_PROVIDER, DIALOG_SCROLL_STRATEGY_PROVIDER_FACTORY, Dialog, DialogConfig, DialogModule, DialogRef, throwDialogContentAlreadyAttachedError, CdkPortalOutlet as ɵɵCdkPortalOutlet };
export type { AutoFocusTarget, DialogCloseOptions, DialogRole };
