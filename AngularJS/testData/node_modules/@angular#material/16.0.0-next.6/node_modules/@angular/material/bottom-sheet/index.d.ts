import { AnimationEvent as AnimationEvent_2 } from '@angular/animations';
import { AnimationTriggerMetadata } from '@angular/animations';
import { BreakpointObserver } from '@angular/cdk/layout';
import { CdkDialogContainer } from '@angular/cdk/dialog';
import { ChangeDetectorRef } from '@angular/core';
import { ComponentType } from '@angular/cdk/portal';
import { DialogConfig } from '@angular/cdk/dialog';
import { DialogRef } from '@angular/cdk/dialog';
import { Direction } from '@angular/cdk/bidi';
import { ElementRef } from '@angular/core';
import { EventEmitter } from '@angular/core';
import { FocusMonitor } from '@angular/cdk/a11y';
import { FocusTrapFactory } from '@angular/cdk/a11y';
import * as i0 from '@angular/core';
import * as i2 from '@angular/cdk/dialog';
import * as i3 from '@angular/material/core';
import * as i4 from '@angular/cdk/portal';
import { InjectionToken } from '@angular/core';
import { Injector } from '@angular/core';
import { InteractivityChecker } from '@angular/cdk/a11y';
import { NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { OnDestroy } from '@angular/core';
import { Overlay } from '@angular/cdk/overlay';
import { OverlayRef } from '@angular/cdk/overlay';
import { ScrollStrategy } from '@angular/cdk/overlay';
import { TemplateRef } from '@angular/core';
import { ViewContainerRef } from '@angular/core';

/** Options for where to set focus to automatically on dialog open */
export declare type AutoFocusTarget = 'dialog' | 'first-tabbable' | 'first-heading';

declare namespace i1 {
    export {
        MatBottomSheetContainer
    }
}

/** Injection token that can be used to access the data that was passed in to a bottom sheet. */
export declare const MAT_BOTTOM_SHEET_DATA: InjectionToken<any>;

/** Injection token that can be used to specify default bottom sheet options. */
export declare const MAT_BOTTOM_SHEET_DEFAULT_OPTIONS: InjectionToken<MatBottomSheetConfig<any>>;

/**
 * Service to trigger Material Design bottom sheets.
 */
export declare class MatBottomSheet implements OnDestroy {
    private _overlay;
    private _parentBottomSheet;
    private _defaultOptions?;
    private _bottomSheetRefAtThisLevel;
    private _dialog;
    /** Reference to the currently opened bottom sheet. */
    get _openedBottomSheetRef(): MatBottomSheetRef<any> | null;
    set _openedBottomSheetRef(value: MatBottomSheetRef<any> | null);
    constructor(_overlay: Overlay, injector: Injector, _parentBottomSheet: MatBottomSheet, _defaultOptions?: MatBottomSheetConfig<any> | undefined);
    /**
     * Opens a bottom sheet containing the given component.
     * @param component Type of the component to load into the bottom sheet.
     * @param config Extra configuration options.
     * @returns Reference to the newly-opened bottom sheet.
     */
    open<T, D = any, R = any>(component: ComponentType<T>, config?: MatBottomSheetConfig<D>): MatBottomSheetRef<T, R>;
    /**
     * Opens a bottom sheet containing the given template.
     * @param template TemplateRef to instantiate as the bottom sheet content.
     * @param config Extra configuration options.
     * @returns Reference to the newly-opened bottom sheet.
     */
    open<T, D = any, R = any>(template: TemplateRef<T>, config?: MatBottomSheetConfig<D>): MatBottomSheetRef<T, R>;
    /**
     * Dismisses the currently-visible bottom sheet.
     * @param result Data to pass to the bottom sheet instance.
     */
    dismiss<R = any>(result?: R): void;
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatBottomSheet, [null, null, { optional: true; skipSelf: true; }, { optional: true; }]>;
    static ɵprov: i0.ɵɵInjectableDeclaration<MatBottomSheet>;
}

/** Animations used by the Material bottom sheet. */
export declare const matBottomSheetAnimations: {
    readonly bottomSheetState: AnimationTriggerMetadata;
};

/**
 * Configuration used when opening a bottom sheet.
 */
export declare class MatBottomSheetConfig<D = any> {
    /** The view container to place the overlay for the bottom sheet into. */
    viewContainerRef?: ViewContainerRef;
    /** Extra CSS classes to be added to the bottom sheet container. */
    panelClass?: string | string[];
    /** Text layout direction for the bottom sheet. */
    direction?: Direction;
    /** Data being injected into the child component. */
    data?: D | null;
    /** Whether the bottom sheet has a backdrop. */
    hasBackdrop?: boolean;
    /** Custom class for the backdrop. */
    backdropClass?: string;
    /** Whether the user can use escape or clicking outside to close the bottom sheet. */
    disableClose?: boolean;
    /** Aria label to assign to the bottom sheet element. */
    ariaLabel?: string | null;
    /** Whether this is a modal bottom sheet. Used to set the `aria-modal` attribute. */
    ariaModal?: boolean;
    /**
     * Whether the bottom sheet should close when the user goes backwards/forwards in history.
     * Note that this usually doesn't include clicking on links (unless the user is using
     * the `HashLocationStrategy`).
     */
    closeOnNavigation?: boolean;
    /**
     * Where the bottom sheet should focus on open.
     * @breaking-change 14.0.0 Remove boolean option from autoFocus. Use string or
     * AutoFocusTarget instead.
     */
    autoFocus?: AutoFocusTarget | string | boolean;
    /**
     * Whether the bottom sheet should restore focus to the
     * previously-focused element, after it's closed.
     */
    restoreFocus?: boolean;
    /** Scroll strategy to be used for the bottom sheet. */
    scrollStrategy?: ScrollStrategy;
}

/**
 * Internal component that wraps user-provided bottom sheet content.
 * @docs-private
 */
export declare class MatBottomSheetContainer extends CdkDialogContainer implements OnDestroy {
    private _changeDetectorRef;
    private _breakpointSubscription;
    /** The state of the bottom sheet animations. */
    _animationState: 'void' | 'visible' | 'hidden';
    /** Emits whenever the state of the animation changes. */
    _animationStateChanged: EventEmitter<AnimationEvent_2>;
    /** Whether the component has been destroyed. */
    private _destroyed;
    constructor(elementRef: ElementRef, focusTrapFactory: FocusTrapFactory, document: any, config: DialogConfig, checker: InteractivityChecker, ngZone: NgZone, overlayRef: OverlayRef, breakpointObserver: BreakpointObserver, _changeDetectorRef: ChangeDetectorRef, focusMonitor?: FocusMonitor);
    /** Begin animation of bottom sheet entrance into view. */
    enter(): void;
    /** Begin animation of the bottom sheet exiting from view. */
    exit(): void;
    ngOnDestroy(): void;
    _onAnimationDone(event: AnimationEvent_2): void;
    _onAnimationStart(event: AnimationEvent_2): void;
    protected _captureInitialFocus(): void;
    private _toggleClass;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatBottomSheetContainer, [null, null, { optional: true; }, null, null, null, null, null, null, null]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatBottomSheetContainer, "mat-bottom-sheet-container", never, {}, {}, never, never, false, never>;
}

export declare class MatBottomSheetModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatBottomSheetModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatBottomSheetModule, [typeof i1.MatBottomSheetContainer], [typeof i2.DialogModule, typeof i3.MatCommonModule, typeof i4.PortalModule], [typeof i1.MatBottomSheetContainer, typeof i3.MatCommonModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatBottomSheetModule>;
}

/**
 * Reference to a bottom sheet dispatched from the bottom sheet service.
 */
export declare class MatBottomSheetRef<T = any, R = any> {
    private _ref;
    /** Instance of the component making up the content of the bottom sheet. */
    get instance(): T;
    /**
     * Instance of the component into which the bottom sheet content is projected.
     * @docs-private
     */
    containerInstance: MatBottomSheetContainer;
    /** Whether the user is allowed to close the bottom sheet. */
    disableClose: boolean | undefined;
    /** Subject for notifying the user that the bottom sheet has opened and appeared. */
    private readonly _afterOpened;
    /** Result to be passed down to the `afterDismissed` stream. */
    private _result;
    /** Handle to the timeout that's running as a fallback in case the exit animation doesn't fire. */
    private _closeFallbackTimeout;
    constructor(_ref: DialogRef<R, T>, config: MatBottomSheetConfig, containerInstance: MatBottomSheetContainer);
    /**
     * Dismisses the bottom sheet.
     * @param result Data to be passed back to the bottom sheet opener.
     */
    dismiss(result?: R): void;
    /** Gets an observable that is notified when the bottom sheet is finished closing. */
    afterDismissed(): Observable<R | undefined>;
    /** Gets an observable that is notified when the bottom sheet has opened and appeared. */
    afterOpened(): Observable<void>;
    /**
     * Gets an observable that emits when the overlay's backdrop has been clicked.
     */
    backdropClick(): Observable<MouseEvent>;
    /**
     * Gets an observable that emits when keydown events are targeted on the overlay.
     */
    keydownEvents(): Observable<KeyboardEvent>;
}

export { }
