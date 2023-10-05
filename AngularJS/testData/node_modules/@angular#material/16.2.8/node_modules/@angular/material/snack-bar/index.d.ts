import { AnimationEvent as AnimationEvent_2 } from '@angular/animations';
import { AnimationTriggerMetadata } from '@angular/animations';
import { AriaLivePoliteness } from '@angular/cdk/a11y';
import { BasePortalOutlet } from '@angular/cdk/portal';
import { BreakpointObserver } from '@angular/cdk/layout';
import { CdkPortalOutlet } from '@angular/cdk/portal';
import { ChangeDetectorRef } from '@angular/core';
import { ComponentPortal } from '@angular/cdk/portal';
import { ComponentRef } from '@angular/core';
import { ComponentType } from '@angular/cdk/overlay';
import { Direction } from '@angular/cdk/bidi';
import { DomPortal } from '@angular/cdk/portal';
import { ElementRef } from '@angular/core';
import { EmbeddedViewRef } from '@angular/core';
import * as i0 from '@angular/core';
import * as i4 from '@angular/cdk/overlay';
import * as i5 from '@angular/cdk/portal';
import * as i6 from '@angular/common';
import * as i7 from '@angular/material/button';
import * as i8 from '@angular/material/core';
import { InjectionToken } from '@angular/core';
import { Injector } from '@angular/core';
import { LiveAnnouncer } from '@angular/cdk/a11y';
import { NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { OnDestroy } from '@angular/core';
import { Overlay } from '@angular/cdk/overlay';
import { OverlayRef } from '@angular/cdk/overlay';
import { Platform } from '@angular/cdk/platform';
import { Subject } from 'rxjs';
import { TemplatePortal } from '@angular/cdk/portal';
import { TemplateRef } from '@angular/core';
import { Type } from '@angular/core';
import { ViewContainerRef } from '@angular/core';

declare namespace i1 {
    export {
        TextOnlySnackBar,
        SimpleSnackBar
    }
}

declare namespace i2 {
    export {
        _MatSnackBarContainerBase,
        MatSnackBarContainer
    }
}

declare namespace i3 {
    export {
        MatSnackBarLabel,
        MatSnackBarActions,
        MatSnackBarAction
    }
}

/** Injection token that can be used to access the data that was passed in to a snack bar. */
export declare const MAT_SNACK_BAR_DATA: InjectionToken<any>;

/** Injection token that can be used to specify default snack bar. */
export declare const MAT_SNACK_BAR_DEFAULT_OPTIONS: InjectionToken<MatSnackBarConfig<any>>;

/** @docs-private */
export declare function MAT_SNACK_BAR_DEFAULT_OPTIONS_FACTORY(): MatSnackBarConfig;

/**
 * Service to dispatch Material Design snack bar messages.
 */
export declare class MatSnackBar extends _MatSnackBarBase {
    protected simpleSnackBarComponent: typeof SimpleSnackBar;
    protected snackBarContainerComponent: typeof MatSnackBarContainer;
    protected handsetCssClass: string;
    constructor(overlay: Overlay, live: LiveAnnouncer, injector: Injector, breakpointObserver: BreakpointObserver, parentSnackBar: MatSnackBar, defaultConfig: MatSnackBarConfig);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatSnackBar, [null, null, null, null, { optional: true; skipSelf: true; }, null]>;
    static ɵprov: i0.ɵɵInjectableDeclaration<MatSnackBar>;
}

/** Directive that should be applied to each of the snack bar's action buttons. */
export declare class MatSnackBarAction {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatSnackBarAction, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatSnackBarAction, "[matSnackBarAction]", never, {}, {}, never, never, false, never>;
}

/** Directive that should be applied to the element containing the snack bar's action buttons. */
export declare class MatSnackBarActions {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatSnackBarActions, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatSnackBarActions, "[matSnackBarActions]", never, {}, {}, never, never, false, never>;
}

/**
 * Animations used by the Material snack bar.
 * @docs-private
 */
export declare const matSnackBarAnimations: {
    readonly snackBarState: AnimationTriggerMetadata;
};

export declare abstract class _MatSnackBarBase implements OnDestroy {
    private _overlay;
    private _live;
    private _injector;
    private _breakpointObserver;
    private _parentSnackBar;
    private _defaultConfig;
    /**
     * Reference to the current snack bar in the view *at this level* (in the Angular injector tree).
     * If there is a parent snack-bar service, all operations should delegate to that parent
     * via `_openedSnackBarRef`.
     */
    private _snackBarRefAtThisLevel;
    /** The component that should be rendered as the snack bar's simple component. */
    protected abstract simpleSnackBarComponent: Type<TextOnlySnackBar>;
    /** The container component that attaches the provided template or component. */
    protected abstract snackBarContainerComponent: Type<_MatSnackBarContainerBase>;
    /** The CSS class to apply for handset mode. */
    protected abstract handsetCssClass: string;
    /** Reference to the currently opened snackbar at *any* level. */
    get _openedSnackBarRef(): MatSnackBarRef<any> | null;
    set _openedSnackBarRef(value: MatSnackBarRef<any> | null);
    constructor(_overlay: Overlay, _live: LiveAnnouncer, _injector: Injector, _breakpointObserver: BreakpointObserver, _parentSnackBar: _MatSnackBarBase, _defaultConfig: MatSnackBarConfig);
    /**
     * Creates and dispatches a snack bar with a custom component for the content, removing any
     * currently opened snack bars.
     *
     * @param component Component to be instantiated.
     * @param config Extra configuration for the snack bar.
     */
    openFromComponent<T, D = any>(component: ComponentType<T>, config?: MatSnackBarConfig<D>): MatSnackBarRef<T>;
    /**
     * Creates and dispatches a snack bar with a custom template for the content, removing any
     * currently opened snack bars.
     *
     * @param template Template to be instantiated.
     * @param config Extra configuration for the snack bar.
     */
    openFromTemplate(template: TemplateRef<any>, config?: MatSnackBarConfig): MatSnackBarRef<EmbeddedViewRef<any>>;
    /**
     * Opens a snackbar with a message and an optional action.
     * @param message The message to show in the snackbar.
     * @param action The label for the snackbar action.
     * @param config Additional configuration options for the snackbar.
     */
    open(message: string, action?: string, config?: MatSnackBarConfig): MatSnackBarRef<TextOnlySnackBar>;
    /**
     * Dismisses the currently-visible snack bar.
     */
    dismiss(): void;
    ngOnDestroy(): void;
    /**
     * Attaches the snack bar container component to the overlay.
     */
    private _attachSnackBarContainer;
    /**
     * Places a new component or a template as the content of the snack bar container.
     */
    private _attach;
    /** Animates the old snack bar out and the new one in. */
    private _animateSnackBar;
    /**
     * Creates a new overlay and places it in the correct location.
     * @param config The user-specified snack bar config.
     */
    private _createOverlay;
    /**
     * Creates an injector to be used inside of a snack bar component.
     * @param config Config that was used to create the snack bar.
     * @param snackBarRef Reference to the snack bar.
     */
    private _createInjector;
    static ɵfac: i0.ɵɵFactoryDeclaration<_MatSnackBarBase, [null, null, null, null, { optional: true; skipSelf: true; }, null]>;
    static ɵprov: i0.ɵɵInjectableDeclaration<_MatSnackBarBase>;
}

/**
 * Configuration used when opening a snack-bar.
 */
export declare class MatSnackBarConfig<D = any> {
    /** The politeness level for the MatAriaLiveAnnouncer announcement. */
    politeness?: AriaLivePoliteness;
    /**
     * Message to be announced by the LiveAnnouncer. When opening a snackbar without a custom
     * component or template, the announcement message will default to the specified message.
     */
    announcementMessage?: string;
    /**
     * The view container that serves as the parent for the snackbar for the purposes of dependency
     * injection. Note: this does not affect where the snackbar is inserted in the DOM.
     */
    viewContainerRef?: ViewContainerRef;
    /** The length of time in milliseconds to wait before automatically dismissing the snack bar. */
    duration?: number;
    /** Extra CSS classes to be added to the snack bar container. */
    panelClass?: string | string[];
    /** Text layout direction for the snack bar. */
    direction?: Direction;
    /** Data being injected into the child component. */
    data?: D | null;
    /** The horizontal position to place the snack bar. */
    horizontalPosition?: MatSnackBarHorizontalPosition;
    /** The vertical position to place the snack bar. */
    verticalPosition?: MatSnackBarVerticalPosition;
}

/**
 * Internal component that wraps user-provided snack bar content.
 * @docs-private
 */
export declare class MatSnackBarContainer extends _MatSnackBarContainerBase {
    /**
     * Element that will have the `mdc-snackbar__label` class applied if the attached component
     * or template does not have it. This ensures that the appropriate structure, typography, and
     * color is applied to the attached view.
     */
    _label: ElementRef;
    /** Applies the correct CSS class to the label based on its content. */
    protected _afterPortalAttached(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatSnackBarContainer, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatSnackBarContainer, "mat-snack-bar-container", never, {}, {}, never, never, false, never>;
}

/**
 * Base class for snack bar containers.
 * @docs-private
 */
export declare abstract class _MatSnackBarContainerBase extends BasePortalOutlet implements OnDestroy {
    private _ngZone;
    protected _elementRef: ElementRef<HTMLElement>;
    private _changeDetectorRef;
    private _platform;
    /** The snack bar configuration. */
    snackBarConfig: MatSnackBarConfig;
    private _document;
    private _trackedModals;
    /** The number of milliseconds to wait before announcing the snack bar's content. */
    private readonly _announceDelay;
    /** The timeout for announcing the snack bar's content. */
    private _announceTimeoutId;
    /** Whether the component has been destroyed. */
    private _destroyed;
    /** The portal outlet inside of this container into which the snack bar content will be loaded. */
    _portalOutlet: CdkPortalOutlet;
    /** Subject for notifying that the snack bar has announced to screen readers. */
    readonly _onAnnounce: Subject<void>;
    /** Subject for notifying that the snack bar has exited from view. */
    readonly _onExit: Subject<void>;
    /** Subject for notifying that the snack bar has finished entering the view. */
    readonly _onEnter: Subject<void>;
    /** The state of the snack bar animations. */
    _animationState: string;
    /** aria-live value for the live region. */
    _live: AriaLivePoliteness;
    /**
     * Role of the live region. This is only for Firefox as there is a known issue where Firefox +
     * JAWS does not read out aria-live message.
     */
    _role?: 'status' | 'alert';
    /** Unique ID of the aria-live element. */
    readonly _liveElementId: string;
    constructor(_ngZone: NgZone, _elementRef: ElementRef<HTMLElement>, _changeDetectorRef: ChangeDetectorRef, _platform: Platform, 
    /** The snack bar configuration. */
    snackBarConfig: MatSnackBarConfig);
    /** Attach a component portal as content to this snack bar container. */
    attachComponentPortal<T>(portal: ComponentPortal<T>): ComponentRef<T>;
    /** Attach a template portal as content to this snack bar container. */
    attachTemplatePortal<C>(portal: TemplatePortal<C>): EmbeddedViewRef<C>;
    /**
     * Attaches a DOM portal to the snack bar container.
     * @deprecated To be turned into a method.
     * @breaking-change 10.0.0
     */
    attachDomPortal: (portal: DomPortal) => void;
    /** Handle end of animations, updating the state of the snackbar. */
    onAnimationEnd(event: AnimationEvent_2): void;
    /** Begin animation of snack bar entrance into view. */
    enter(): void;
    /** Begin animation of the snack bar exiting from view. */
    exit(): Observable<void>;
    /** Makes sure the exit callbacks have been invoked when the element is destroyed. */
    ngOnDestroy(): void;
    /**
     * Waits for the zone to settle before removing the element. Helps prevent
     * errors where we end up removing an element which is in the middle of an animation.
     */
    private _completeExit;
    /**
     * Called after the portal contents have been attached. Can be
     * used to modify the DOM once it's guaranteed to be in place.
     */
    protected _afterPortalAttached(): void;
    /**
     * Some browsers won't expose the accessibility node of the live element if there is an
     * `aria-modal` and the live element is outside of it. This method works around the issue by
     * pointing the `aria-owns` of all modals to the live element.
     */
    private _exposeToModals;
    /** Clears the references to the live element from any modals it was added to. */
    private _clearFromModals;
    /** Asserts that no content is already attached to the container. */
    private _assertNotAttached;
    /**
     * Starts a timeout to move the snack bar content to the live region so screen readers will
     * announce it.
     */
    private _screenReaderAnnounce;
    static ɵfac: i0.ɵɵFactoryDeclaration<_MatSnackBarContainerBase, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<_MatSnackBarContainerBase, never, never, {}, {}, never, never, false, never>;
}

/** Event that is emitted when a snack bar is dismissed. */
export declare interface MatSnackBarDismiss {
    /** Whether the snack bar was dismissed using the action button. */
    dismissedByAction: boolean;
}

/** Possible values for horizontalPosition on MatSnackBarConfig. */
export declare type MatSnackBarHorizontalPosition = 'start' | 'center' | 'end' | 'left' | 'right';

/** Directive that should be applied to the text element to be rendered in the snack bar. */
export declare class MatSnackBarLabel {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatSnackBarLabel, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatSnackBarLabel, "[matSnackBarLabel]", never, {}, {}, never, never, false, never>;
}

export declare class MatSnackBarModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatSnackBarModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatSnackBarModule, [typeof i1.SimpleSnackBar, typeof i2.MatSnackBarContainer, typeof i3.MatSnackBarLabel, typeof i3.MatSnackBarActions, typeof i3.MatSnackBarAction], [typeof i4.OverlayModule, typeof i5.PortalModule, typeof i6.CommonModule, typeof i7.MatButtonModule, typeof i8.MatCommonModule], [typeof i8.MatCommonModule, typeof i2.MatSnackBarContainer, typeof i3.MatSnackBarLabel, typeof i3.MatSnackBarActions, typeof i3.MatSnackBarAction]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatSnackBarModule>;
}

/**
 * Reference to a snack bar dispatched from the snack bar service.
 */
export declare class MatSnackBarRef<T> {
    private _overlayRef;
    /** The instance of the component making up the content of the snack bar. */
    instance: T;
    /**
     * The instance of the component making up the content of the snack bar.
     * @docs-private
     */
    containerInstance: _MatSnackBarContainerBase;
    /** Subject for notifying the user that the snack bar has been dismissed. */
    private readonly _afterDismissed;
    /** Subject for notifying the user that the snack bar has opened and appeared. */
    private readonly _afterOpened;
    /** Subject for notifying the user that the snack bar action was called. */
    private readonly _onAction;
    /**
     * Timeout ID for the duration setTimeout call. Used to clear the timeout if the snackbar is
     * dismissed before the duration passes.
     */
    private _durationTimeoutId;
    /** Whether the snack bar was dismissed using the action button. */
    private _dismissedByAction;
    constructor(containerInstance: _MatSnackBarContainerBase, _overlayRef: OverlayRef);
    /** Dismisses the snack bar. */
    dismiss(): void;
    /** Marks the snackbar action clicked. */
    dismissWithAction(): void;
    /**
     * Marks the snackbar action clicked.
     * @deprecated Use `dismissWithAction` instead.
     * @breaking-change 8.0.0
     */
    closeWithAction(): void;
    /** Dismisses the snack bar after some duration */
    _dismissAfter(duration: number): void;
    /** Marks the snackbar as opened */
    _open(): void;
    /** Cleans up the DOM after closing. */
    private _finishDismiss;
    /** Gets an observable that is notified when the snack bar is finished closing. */
    afterDismissed(): Observable<MatSnackBarDismiss>;
    /** Gets an observable that is notified when the snack bar has opened and appeared. */
    afterOpened(): Observable<void>;
    /** Gets an observable that is notified when the snack bar action is called. */
    onAction(): Observable<void>;
}

/** Possible values for verticalPosition on MatSnackBarConfig. */
export declare type MatSnackBarVerticalPosition = 'top' | 'bottom';

export declare class SimpleSnackBar implements TextOnlySnackBar {
    snackBarRef: MatSnackBarRef<SimpleSnackBar>;
    data: {
        message: string;
        action: string;
    };
    constructor(snackBarRef: MatSnackBarRef<SimpleSnackBar>, data: {
        message: string;
        action: string;
    });
    /** Performs the action on the snack bar. */
    action(): void;
    /** If the action button should be shown. */
    get hasAction(): boolean;
    static ɵfac: i0.ɵɵFactoryDeclaration<SimpleSnackBar, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<SimpleSnackBar, "simple-snack-bar", ["matSnackBar"], {}, {}, never, never, false, never>;
}

/**
 * Interface for a simple snack bar component that has a message and a single action.
 */
export declare interface TextOnlySnackBar {
    data: {
        message: string;
        action: string;
    };
    snackBarRef: MatSnackBarRef<TextOnlySnackBar>;
    action: () => void;
    hasAction: boolean;
}

export { }
