import { AnimationTriggerMetadata } from '@angular/animations';
import { CdkDialogContainer } from '@angular/cdk/dialog';
import { ComponentFactoryResolver } from '@angular/core';
import { ComponentPortal } from '@angular/cdk/portal';
import { ComponentRef } from '@angular/core';
import { ComponentType } from '@angular/cdk/overlay';
import { Dialog } from '@angular/cdk/dialog';
import { DialogRef } from '@angular/cdk/dialog';
import { Direction } from '@angular/cdk/bidi';
import { ElementRef } from '@angular/core';
import { EventEmitter } from '@angular/core';
import { FocusMonitor } from '@angular/cdk/a11y';
import { FocusOrigin } from '@angular/cdk/a11y';
import { FocusTrapFactory } from '@angular/cdk/a11y';
import * as i0 from '@angular/core';
import * as i1 from '@angular/cdk/dialog';
import * as i2 from '@angular/cdk/overlay';
import * as i3 from '@angular/cdk/portal';
import * as i4 from '@angular/material/core';
import { InjectionToken } from '@angular/core';
import { Injector } from '@angular/core';
import { InteractivityChecker } from '@angular/cdk/a11y';
import { Location as Location_2 } from '@angular/common';
import { NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { OnChanges } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { OnInit } from '@angular/core';
import { Overlay } from '@angular/cdk/overlay';
import { OverlayContainer } from '@angular/cdk/overlay';
import { OverlayRef } from '@angular/cdk/overlay';
import { ScrollStrategy } from '@angular/cdk/overlay';
import { SimpleChanges } from '@angular/core';
import { Subject } from 'rxjs';
import { TemplateRef } from '@angular/core';
import { ViewContainerRef } from '@angular/core';

/** Options for where to set focus to automatically on dialog open */
export declare type AutoFocusTarget = 'dialog' | 'first-tabbable' | 'first-heading';

/** Duration of the closing animation in milliseconds. */
declare const CLOSE_ANIMATION_DURATION = 75;

/**
 * Closes the dialog with the specified interaction type. This is currently not part of
 * `MatDialogRef` as that would conflict with custom dialog ref mocks provided in tests.
 * More details. See: https://github.com/angular/components/pull/9257#issuecomment-651342226.
 */
export declare function _closeDialogVia<R>(ref: MatDialogRef<R>, interactionType: FocusOrigin, result?: R): void;

/**
 * Default parameters for the animation for backwards compatibility.
 * @docs-private
 */
export declare const _defaultParams: {
    params: {
        enterAnimationDuration: string;
        exitAnimationDuration: string;
    };
};

/** Possible overrides for a dialog's position. */
export declare interface DialogPosition {
    /** Override for the dialog's top position. */
    top?: string;
    /** Override for the dialog's bottom position. */
    bottom?: string;
    /** Override for the dialog's left position. */
    left?: string;
    /** Override for the dialog's right position. */
    right?: string;
}

/** Valid ARIA roles for a dialog element. */
export declare type DialogRole = 'dialog' | 'alertdialog';

declare namespace i5 {
    export {
        OPEN_ANIMATION_DURATION,
        CLOSE_ANIMATION_DURATION,
        MatDialogContainer
    }
}

declare namespace i6 {
    export {
        MatDialogClose,
        MatDialogLayoutSection,
        MatDialogTitle,
        MatDialogContent,
        MatDialogActions
    }
}

/** Event that captures the state of dialog container animations. */
declare interface LegacyDialogAnimationEvent {
    state: 'opened' | 'opening' | 'closing' | 'closed';
    totalTime: number;
}

/** Injection token that can be used to access the data that was passed in to a dialog. */
export declare const MAT_DIALOG_DATA: InjectionToken<any>;

/** Injection token that can be used to specify default dialog options. */
export declare const MAT_DIALOG_DEFAULT_OPTIONS: InjectionToken<MatDialogConfig<any>>;

/** Injection token that determines the scroll handling while the dialog is open. */
export declare const MAT_DIALOG_SCROLL_STRATEGY: InjectionToken<() => ScrollStrategy>;

/**
 * @docs-private
 * @deprecated No longer used. To be removed.
 * @breaking-change 19.0.0
 */
export declare const MAT_DIALOG_SCROLL_STRATEGY_PROVIDER: {
    provide: InjectionToken<() => ScrollStrategy>;
    deps: (typeof Overlay)[];
    useFactory: typeof MAT_DIALOG_SCROLL_STRATEGY_PROVIDER_FACTORY;
};

/**
 * @docs-private
 * @deprecated No longer used. To be removed.
 * @breaking-change 19.0.0
 */
export declare function MAT_DIALOG_SCROLL_STRATEGY_PROVIDER_FACTORY(overlay: Overlay): () => ScrollStrategy;

/**
 * Service to open Material Design modal dialogs.
 */
export declare class MatDialog implements OnDestroy {
    private _overlay;
    private _defaultOptions;
    private _scrollStrategy;
    private _parentDialog;
    private readonly _openDialogsAtThisLevel;
    private readonly _afterAllClosedAtThisLevel;
    private readonly _afterOpenedAtThisLevel;
    protected _dialog: Dialog;
    protected dialogConfigClass: typeof MatDialogConfig;
    private readonly _dialogRefConstructor;
    private readonly _dialogContainerType;
    private readonly _dialogDataToken;
    /** Keeps track of the currently-open dialogs. */
    get openDialogs(): MatDialogRef<any>[];
    /** Stream that emits when a dialog has been opened. */
    get afterOpened(): Subject<MatDialogRef<any>>;
    private _getAfterAllClosed;
    /**
     * Stream that emits when all open dialog have finished closing.
     * Will emit on subscribe if there are no open dialogs to begin with.
     */
    readonly afterAllClosed: Observable<void>;
    constructor(_overlay: Overlay, injector: Injector, 
    /**
     * @deprecated `_location` parameter to be removed.
     * @breaking-change 10.0.0
     */
    location: Location_2, _defaultOptions: MatDialogConfig, _scrollStrategy: any, _parentDialog: MatDialog, 
    /**
     * @deprecated No longer used. To be removed.
     * @breaking-change 15.0.0
     */
    _overlayContainer: OverlayContainer, 
    /**
     * @deprecated No longer used. To be removed.
     * @breaking-change 14.0.0
     */
    _animationMode?: 'NoopAnimations' | 'BrowserAnimations');
    /**
     * Opens a modal dialog containing the given component.
     * @param component Type of the component to load into the dialog.
     * @param config Extra configuration options.
     * @returns Reference to the newly-opened dialog.
     */
    open<T, D = any, R = any>(component: ComponentType<T>, config?: MatDialogConfig<D>): MatDialogRef<T, R>;
    /**
     * Opens a modal dialog containing the given template.
     * @param template TemplateRef to instantiate as the dialog content.
     * @param config Extra configuration options.
     * @returns Reference to the newly-opened dialog.
     */
    open<T, D = any, R = any>(template: TemplateRef<T>, config?: MatDialogConfig<D>): MatDialogRef<T, R>;
    open<T, D = any, R = any>(template: ComponentType<T> | TemplateRef<T>, config?: MatDialogConfig<D>): MatDialogRef<T, R>;
    /**
     * Closes all of the currently-open dialogs.
     */
    closeAll(): void;
    /**
     * Finds an open dialog by its id.
     * @param id ID to use when looking up the dialog.
     */
    getDialogById(id: string): MatDialogRef<any> | undefined;
    ngOnDestroy(): void;
    private _closeDialogs;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDialog, [null, null, { optional: true; }, { optional: true; }, null, { optional: true; skipSelf: true; }, null, { optional: true; }]>;
    static ɵprov: i0.ɵɵInjectableDeclaration<MatDialog>;
}

/**
 * Container for the bottom action buttons in a dialog.
 * Stays fixed to the bottom when scrolling.
 */
export declare class MatDialogActions extends MatDialogLayoutSection {
    /**
     * Horizontal alignment of action buttons.
     */
    align?: 'start' | 'center' | 'end';
    protected _onAdd(): void;
    protected _onRemove(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDialogActions, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatDialogActions, "[mat-dialog-actions], mat-dialog-actions, [matDialogActions]", never, { "align": { "alias": "align"; "required": false; }; }, {}, never, never, true, never>;
}

/**
 * Animations used by MatDialog.
 * @docs-private
 */
export declare const matDialogAnimations: {
    readonly dialogContainer: AnimationTriggerMetadata;
};

/**
 * Button that will close the current dialog.
 */
export declare class MatDialogClose implements OnInit, OnChanges {
    dialogRef: MatDialogRef<any>;
    private _elementRef;
    private _dialog;
    /** Screen-reader label for the button. */
    ariaLabel: string;
    /** Default to "button" to prevents accidental form submits. */
    type: 'submit' | 'button' | 'reset';
    /** Dialog close input. */
    dialogResult: any;
    _matDialogClose: any;
    constructor(dialogRef: MatDialogRef<any>, _elementRef: ElementRef<HTMLElement>, _dialog: MatDialog);
    ngOnInit(): void;
    ngOnChanges(changes: SimpleChanges): void;
    _onButtonClick(event: MouseEvent): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDialogClose, [{ optional: true; }, null, null]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatDialogClose, "[mat-dialog-close], [matDialogClose]", ["matDialogClose"], { "ariaLabel": { "alias": "aria-label"; "required": false; }; "type": { "alias": "type"; "required": false; }; "dialogResult": { "alias": "mat-dialog-close"; "required": false; }; "_matDialogClose": { "alias": "matDialogClose"; "required": false; }; }, {}, never, never, true, never>;
}

/**
 * Configuration for opening a modal dialog with the MatDialog service.
 */
export declare class MatDialogConfig<D = any> {
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
    /** Custom class for the overlay pane. */
    panelClass?: string | string[];
    /** Whether the dialog has a backdrop. */
    hasBackdrop?: boolean;
    /** Custom class for the backdrop. */
    backdropClass?: string | string[];
    /** Whether the user can use escape or clicking on the backdrop to close the modal. */
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
    /** Position overrides. */
    position?: DialogPosition;
    /** Data being injected into the child component. */
    data?: D | null;
    /** Layout direction for the dialog's content. */
    direction?: Direction;
    /** ID of the element that describes the dialog. */
    ariaDescribedBy?: string | null;
    /** ID of the element that labels the dialog. */
    ariaLabelledBy?: string | null;
    /** Aria label to assign to the dialog element. */
    ariaLabel?: string | null;
    /** Whether this is a modal dialog. Used to set the `aria-modal` attribute. */
    ariaModal?: boolean;
    /**
     * Where the dialog should focus on open.
     * @breaking-change 14.0.0 Remove boolean option from autoFocus. Use string or
     * AutoFocusTarget instead.
     */
    autoFocus?: AutoFocusTarget | string | boolean;
    /**
     * Whether the dialog should restore focus to the
     * previously-focused element, after it's closed.
     */
    restoreFocus?: boolean;
    /** Whether to wait for the opening animation to finish before trapping focus. */
    delayFocusTrap?: boolean;
    /** Scroll strategy to be used for the dialog. */
    scrollStrategy?: ScrollStrategy;
    /**
     * Whether the dialog should close when the user goes backwards/forwards in history.
     * Note that this usually doesn't include clicking on links (unless the user is using
     * the `HashLocationStrategy`).
     */
    closeOnNavigation?: boolean;
    /** Alternate `ComponentFactoryResolver` to use when resolving the associated component. */
    componentFactoryResolver?: ComponentFactoryResolver;
    /**
     * Duration of the enter animation in ms.
     * Should be a number, string type is deprecated.
     * @breaking-change 17.0.0 Remove string signature.
     */
    enterAnimationDuration?: string | number;
    /**
     * Duration of the exit animation in ms.
     * Should be a number, string type is deprecated.
     * @breaking-change 17.0.0 Remove string signature.
     */
    exitAnimationDuration?: string | number;
}

export declare class MatDialogContainer extends CdkDialogContainer<MatDialogConfig> implements OnDestroy {
    private _animationMode?;
    /** Emits when an animation state changes. */
    _animationStateChanged: EventEmitter<LegacyDialogAnimationEvent>;
    /** Whether animations are enabled. */
    _animationsEnabled: boolean;
    /** Number of actions projected in the dialog. */
    protected _actionSectionCount: number;
    /** Host element of the dialog container component. */
    private _hostElement;
    /** Duration of the dialog open animation. */
    private _enterAnimationDuration;
    /** Duration of the dialog close animation. */
    private _exitAnimationDuration;
    /** Current timer for dialog animations. */
    private _animationTimer;
    constructor(elementRef: ElementRef, focusTrapFactory: FocusTrapFactory, _document: any, dialogConfig: MatDialogConfig, interactivityChecker: InteractivityChecker, ngZone: NgZone, overlayRef: OverlayRef, _animationMode?: string | undefined, focusMonitor?: FocusMonitor);
    protected _contentAttached(): void;
    /** Starts the dialog open animation if enabled. */
    private _startOpenAnimation;
    /**
     * Starts the exit animation of the dialog if enabled. This method is
     * called by the dialog ref.
     */
    _startExitAnimation(): void;
    /**
     * Updates the number action sections.
     * @param delta Increase/decrease in the number of sections.
     */
    _updateActionSectionCount(delta: number): void;
    /**
     * Completes the dialog open by clearing potential animation classes, trapping
     * focus and emitting an opened event.
     */
    private _finishDialogOpen;
    /**
     * Completes the dialog close by clearing potential animation classes, restoring
     * focus and emitting a closed event.
     */
    private _finishDialogClose;
    /** Clears all dialog animation classes. */
    private _clearAnimationClasses;
    private _waitForAnimationToComplete;
    /** Runs a callback in `requestAnimationFrame`, if available. */
    private _requestAnimationFrame;
    protected _captureInitialFocus(): void;
    /**
     * Callback for when the open dialog animation has finished. Intended to
     * be called by sub-classes that use different animation implementations.
     */
    protected _openAnimationDone(totalTime: number): void;
    ngOnDestroy(): void;
    attachComponentPortal<T>(portal: ComponentPortal<T>): ComponentRef<T>;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDialogContainer, [null, null, { optional: true; }, null, null, null, null, { optional: true; }, null]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatDialogContainer, "mat-dialog-container", never, {}, {}, never, never, true, never>;
}

/**
 * Scrollable content container of a dialog.
 */
export declare class MatDialogContent {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDialogContent, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatDialogContent, "[mat-dialog-content], mat-dialog-content, [matDialogContent]", never, {}, {}, never, never, true, never>;
}

declare abstract class MatDialogLayoutSection implements OnInit, OnDestroy {
    protected _dialogRef: MatDialogRef<any>;
    private _elementRef;
    private _dialog;
    constructor(_dialogRef: MatDialogRef<any>, _elementRef: ElementRef<HTMLElement>, _dialog: MatDialog);
    protected abstract _onAdd(): void;
    protected abstract _onRemove(): void;
    ngOnInit(): void;
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDialogLayoutSection, [{ optional: true; }, null, null]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatDialogLayoutSection, never, never, {}, {}, never, never, true, never>;
}

export declare class MatDialogModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDialogModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatDialogModule, never, [typeof i1.DialogModule, typeof i2.OverlayModule, typeof i3.PortalModule, typeof i4.MatCommonModule, typeof i5.MatDialogContainer, typeof i6.MatDialogClose, typeof i6.MatDialogTitle, typeof i6.MatDialogActions, typeof i6.MatDialogContent], [typeof i4.MatCommonModule, typeof i5.MatDialogContainer, typeof i6.MatDialogClose, typeof i6.MatDialogTitle, typeof i6.MatDialogActions, typeof i6.MatDialogContent]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatDialogModule>;
}

/**
 * Reference to a dialog opened via the MatDialog service.
 */
export declare class MatDialogRef<T, R = any> {
    private _ref;
    _containerInstance: MatDialogContainer;
    /** The instance of component opened into the dialog. */
    componentInstance: T;
    /**
     * `ComponentRef` of the component opened into the dialog. Will be
     * null when the dialog is opened using a `TemplateRef`.
     */
    readonly componentRef: ComponentRef<T> | null;
    /** Whether the user is allowed to close the dialog. */
    disableClose: boolean | undefined;
    /** Unique ID for the dialog. */
    id: string;
    /** Subject for notifying the user that the dialog has finished opening. */
    private readonly _afterOpened;
    /** Subject for notifying the user that the dialog has started closing. */
    private readonly _beforeClosed;
    /** Result to be passed to afterClosed. */
    private _result;
    /** Handle to the timeout that's running as a fallback in case the exit animation doesn't fire. */
    private _closeFallbackTimeout;
    /** Current state of the dialog. */
    private _state;
    /** Interaction that caused the dialog to close. */
    private _closeInteractionType;
    constructor(_ref: DialogRef<R, T>, config: MatDialogConfig, _containerInstance: MatDialogContainer);
    /**
     * Close the dialog.
     * @param dialogResult Optional result to return to the dialog opener.
     */
    close(dialogResult?: R): void;
    /**
     * Gets an observable that is notified when the dialog is finished opening.
     */
    afterOpened(): Observable<void>;
    /**
     * Gets an observable that is notified when the dialog is finished closing.
     */
    afterClosed(): Observable<R | undefined>;
    /**
     * Gets an observable that is notified when the dialog has started closing.
     */
    beforeClosed(): Observable<R | undefined>;
    /**
     * Gets an observable that emits when the overlay's backdrop has been clicked.
     */
    backdropClick(): Observable<MouseEvent>;
    /**
     * Gets an observable that emits when keydown events are targeted on the overlay.
     */
    keydownEvents(): Observable<KeyboardEvent>;
    /**
     * Updates the dialog's position.
     * @param position New dialog position.
     */
    updatePosition(position?: DialogPosition): this;
    /**
     * Updates the dialog's width and height.
     * @param width New width of the dialog.
     * @param height New height of the dialog.
     */
    updateSize(width?: string, height?: string): this;
    /** Add a CSS class or an array of classes to the overlay pane. */
    addPanelClass(classes: string | string[]): this;
    /** Remove a CSS class or an array of classes from the overlay pane. */
    removePanelClass(classes: string | string[]): this;
    /** Gets the current state of the dialog's lifecycle. */
    getState(): MatDialogState;
    /**
     * Finishes the dialog close by updating the state of the dialog
     * and disposing the overlay.
     */
    private _finishDialogClose;
}

export declare enum MatDialogState {
    OPEN = 0,
    CLOSING = 1,
    CLOSED = 2
}

/**
 * Title of a dialog element. Stays fixed to the top of the dialog when scrolling.
 */
export declare class MatDialogTitle extends MatDialogLayoutSection {
    id: string;
    protected _onAdd(): void;
    protected _onRemove(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDialogTitle, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatDialogTitle, "[mat-dialog-title], [matDialogTitle]", ["matDialogTitle"], { "id": { "alias": "id"; "required": false; }; }, {}, never, never, true, never>;
}

/** Duration of the opening animation in milliseconds. */
declare const OPEN_ANIMATION_DURATION = 150;

export { }
