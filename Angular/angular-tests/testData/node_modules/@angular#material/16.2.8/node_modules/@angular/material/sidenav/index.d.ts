import { AfterContentChecked } from '@angular/core';
import { AfterContentInit } from '@angular/core';
import { AfterViewInit } from '@angular/core';
import { AnimationEvent as AnimationEvent_2 } from '@angular/animations';
import { AnimationTriggerMetadata } from '@angular/animations';
import { BooleanInput } from '@angular/cdk/coercion';
import { CdkScrollable } from '@angular/cdk/scrolling';
import { ChangeDetectorRef } from '@angular/core';
import { Directionality } from '@angular/cdk/bidi';
import { DoCheck } from '@angular/core';
import { ElementRef } from '@angular/core';
import { EventEmitter } from '@angular/core';
import { FocusMonitor } from '@angular/cdk/a11y';
import { FocusOrigin } from '@angular/cdk/a11y';
import { FocusTrapFactory } from '@angular/cdk/a11y';
import * as i0 from '@angular/core';
import * as i3 from '@angular/common';
import * as i4 from '@angular/material/core';
import * as i5 from '@angular/cdk/scrolling';
import { InjectionToken } from '@angular/core';
import { InteractivityChecker } from '@angular/cdk/a11y';
import { NgZone } from '@angular/core';
import { NumberInput } from '@angular/cdk/coercion';
import { Observable } from 'rxjs';
import { OnDestroy } from '@angular/core';
import { Platform } from '@angular/cdk/platform';
import { QueryList } from '@angular/core';
import { ScrollDispatcher } from '@angular/cdk/scrolling';
import { Subject } from 'rxjs';
import { ViewportRuler } from '@angular/cdk/scrolling';

/** Options for where to set focus to automatically on dialog open */
declare type AutoFocusTarget = 'dialog' | 'first-tabbable' | 'first-heading';

declare namespace i1 {
    export {
        throwMatDuplicatedDrawerError,
        MAT_DRAWER_DEFAULT_AUTOSIZE_FACTORY,
        AutoFocusTarget,
        MatDrawerToggleResult,
        MatDrawerMode,
        MAT_DRAWER_DEFAULT_AUTOSIZE,
        MAT_DRAWER_CONTAINER,
        MatDrawerContent,
        MatDrawer,
        MatDrawerContainer
    }
}

declare namespace i2 {
    export {
        MatSidenavContent,
        MatSidenav,
        MatSidenavContainer
    }
}

/**
 * Used to provide a drawer container to a drawer while avoiding circular references.
 * @docs-private
 */
declare const MAT_DRAWER_CONTAINER: InjectionToken<unknown>;

/** Configures whether drawers should use auto sizing by default. */
export declare const MAT_DRAWER_DEFAULT_AUTOSIZE: InjectionToken<boolean>;

/** @docs-private */
export declare function MAT_DRAWER_DEFAULT_AUTOSIZE_FACTORY(): boolean;

/**
 * This component corresponds to a drawer that can be opened on the drawer container.
 */
export declare class MatDrawer implements AfterViewInit, AfterContentChecked, OnDestroy {
    private _elementRef;
    private _focusTrapFactory;
    private _focusMonitor;
    private _platform;
    private _ngZone;
    private readonly _interactivityChecker;
    private _doc;
    _container?: MatDrawerContainer | undefined;
    private _focusTrap;
    private _elementFocusedBeforeDrawerWasOpened;
    /** Whether the drawer is initialized. Used for disabling the initial animation. */
    private _enableAnimations;
    /** Whether the view of the component has been attached. */
    private _isAttached;
    /** Anchor node used to restore the drawer to its initial position. */
    private _anchor;
    /** The side that the drawer is attached to. */
    get position(): 'start' | 'end';
    set position(value: 'start' | 'end');
    private _position;
    /** Mode of the drawer; one of 'over', 'push' or 'side'. */
    get mode(): MatDrawerMode;
    set mode(value: MatDrawerMode);
    private _mode;
    /** Whether the drawer can be closed with the escape key or by clicking on the backdrop. */
    get disableClose(): boolean;
    set disableClose(value: BooleanInput);
    private _disableClose;
    /**
     * Whether the drawer should focus the first focusable element automatically when opened.
     * Defaults to false in when `mode` is set to `side`, otherwise defaults to `true`. If explicitly
     * enabled, focus will be moved into the sidenav in `side` mode as well.
     * @breaking-change 14.0.0 Remove boolean option from autoFocus. Use string or AutoFocusTarget
     * instead.
     */
    get autoFocus(): AutoFocusTarget | string | boolean;
    set autoFocus(value: AutoFocusTarget | string | BooleanInput);
    private _autoFocus;
    /**
     * Whether the drawer is opened. We overload this because we trigger an event when it
     * starts or end.
     */
    get opened(): boolean;
    set opened(value: BooleanInput);
    private _opened;
    /** How the sidenav was opened (keypress, mouse click etc.) */
    private _openedVia;
    /** Emits whenever the drawer has started animating. */
    readonly _animationStarted: Subject<AnimationEvent_2>;
    /** Emits whenever the drawer is done animating. */
    readonly _animationEnd: Subject<AnimationEvent_2>;
    /** Current state of the sidenav animation. */
    _animationState: 'open-instant' | 'open' | 'void';
    /** Event emitted when the drawer open state is changed. */
    readonly openedChange: EventEmitter<boolean>;
    /** Event emitted when the drawer has been opened. */
    readonly _openedStream: Observable<void>;
    /** Event emitted when the drawer has started opening. */
    readonly openedStart: Observable<void>;
    /** Event emitted when the drawer has been closed. */
    readonly _closedStream: Observable<void>;
    /** Event emitted when the drawer has started closing. */
    readonly closedStart: Observable<void>;
    /** Emits when the component is destroyed. */
    private readonly _destroyed;
    /** Event emitted when the drawer's position changes. */
    readonly onPositionChanged: EventEmitter<void>;
    /** Reference to the inner element that contains all the content. */
    _content: ElementRef<HTMLElement>;
    /**
     * An observable that emits when the drawer mode changes. This is used by the drawer container to
     * to know when to when the mode changes so it can adapt the margins on the content.
     */
    readonly _modeChanged: Subject<void>;
    constructor(_elementRef: ElementRef<HTMLElement>, _focusTrapFactory: FocusTrapFactory, _focusMonitor: FocusMonitor, _platform: Platform, _ngZone: NgZone, _interactivityChecker: InteractivityChecker, _doc: any, _container?: MatDrawerContainer | undefined);
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
     * Moves focus into the drawer. Note that this works even if
     * the focus trap is disabled in `side` mode.
     */
    private _takeFocus;
    /**
     * Restores focus to the element that was originally focused when the drawer opened.
     * If no element was focused at that time, the focus will be restored to the drawer.
     */
    private _restoreFocus;
    /** Whether focus is currently within the drawer. */
    private _isFocusWithinDrawer;
    ngAfterViewInit(): void;
    ngAfterContentChecked(): void;
    ngOnDestroy(): void;
    /**
     * Open the drawer.
     * @param openedVia Whether the drawer was opened by a key press, mouse click or programmatically.
     * Used for focus management after the sidenav is closed.
     */
    open(openedVia?: FocusOrigin): Promise<MatDrawerToggleResult>;
    /** Close the drawer. */
    close(): Promise<MatDrawerToggleResult>;
    /** Closes the drawer with context that the backdrop was clicked. */
    _closeViaBackdropClick(): Promise<MatDrawerToggleResult>;
    /**
     * Toggle this drawer.
     * @param isOpen Whether the drawer should be open.
     * @param openedVia Whether the drawer was opened by a key press, mouse click or programmatically.
     * Used for focus management after the sidenav is closed.
     */
    toggle(isOpen?: boolean, openedVia?: FocusOrigin): Promise<MatDrawerToggleResult>;
    /**
     * Toggles the opened state of the drawer.
     * @param isOpen Whether the drawer should open or close.
     * @param restoreFocus Whether focus should be restored on close.
     * @param focusOrigin Origin to use when restoring focus.
     */
    private _setOpen;
    _getWidth(): number;
    /** Updates the enabled state of the focus trap. */
    private _updateFocusTrapState;
    /**
     * Updates the position of the drawer in the DOM. We need to move the element around ourselves
     * when it's in the `end` position so that it comes after the content and the visual order
     * matches the tab order. We also need to be able to move it back to `start` if the sidenav
     * started off as `end` and was changed to `start`.
     */
    private _updatePositionInParent;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDrawer, [null, null, null, null, null, null, { optional: true; }, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatDrawer, "mat-drawer", ["matDrawer"], { "position": { "alias": "position"; "required": false; }; "mode": { "alias": "mode"; "required": false; }; "disableClose": { "alias": "disableClose"; "required": false; }; "autoFocus": { "alias": "autoFocus"; "required": false; }; "opened": { "alias": "opened"; "required": false; }; }, { "openedChange": "openedChange"; "_openedStream": "opened"; "openedStart": "openedStart"; "_closedStream": "closed"; "closedStart": "closedStart"; "onPositionChanged": "positionChanged"; }, never, ["*"], false, never>;
}

/**
 * Animations used by the Material drawers.
 * @docs-private
 */
export declare const matDrawerAnimations: {
    readonly transformDrawer: AnimationTriggerMetadata;
};

/**
 * `<mat-drawer-container>` component.
 *
 * This is the parent component to one or two `<mat-drawer>`s that validates the state internally
 * and coordinates the backdrop and content styling.
 */
export declare class MatDrawerContainer implements AfterContentInit, DoCheck, OnDestroy {
    private _dir;
    private _element;
    private _ngZone;
    private _changeDetectorRef;
    private _animationMode?;
    /** All drawers in the container. Includes drawers from inside nested containers. */
    _allDrawers: QueryList<MatDrawer>;
    /** Drawers that belong to this container. */
    _drawers: QueryList<MatDrawer>;
    _content: MatDrawerContent;
    _userContent: MatDrawerContent;
    /** The drawer child with the `start` position. */
    get start(): MatDrawer | null;
    /** The drawer child with the `end` position. */
    get end(): MatDrawer | null;
    /**
     * Whether to automatically resize the container whenever
     * the size of any of its drawers changes.
     *
     * **Use at your own risk!** Enabling this option can cause layout thrashing by measuring
     * the drawers on every change detection cycle. Can be configured globally via the
     * `MAT_DRAWER_DEFAULT_AUTOSIZE` token.
     */
    get autosize(): boolean;
    set autosize(value: BooleanInput);
    private _autosize;
    /**
     * Whether the drawer container should have a backdrop while one of the sidenavs is open.
     * If explicitly set to `true`, the backdrop will be enabled for drawers in the `side`
     * mode as well.
     */
    get hasBackdrop(): boolean;
    set hasBackdrop(value: BooleanInput);
    _backdropOverride: boolean | null;
    /** Event emitted when the drawer backdrop is clicked. */
    readonly backdropClick: EventEmitter<void>;
    /** The drawer at the start/end position, independent of direction. */
    private _start;
    private _end;
    /**
     * The drawer at the left/right. When direction changes, these will change as well.
     * They're used as aliases for the above to set the left/right style properly.
     * In LTR, _left == _start and _right == _end.
     * In RTL, _left == _end and _right == _start.
     */
    private _left;
    private _right;
    /** Emits when the component is destroyed. */
    private readonly _destroyed;
    /** Emits on every ngDoCheck. Used for debouncing reflows. */
    private readonly _doCheckSubject;
    /**
     * Margins to be applied to the content. These are used to push / shrink the drawer content when a
     * drawer is open. We use margin rather than transform even for push mode because transform breaks
     * fixed position elements inside of the transformed element.
     */
    _contentMargins: {
        left: number | null;
        right: number | null;
    };
    readonly _contentMarginChanges: Subject<{
        left: number | null;
        right: number | null;
    }>;
    /** Reference to the CdkScrollable instance that wraps the scrollable content. */
    get scrollable(): CdkScrollable;
    constructor(_dir: Directionality, _element: ElementRef<HTMLElement>, _ngZone: NgZone, _changeDetectorRef: ChangeDetectorRef, viewportRuler: ViewportRuler, defaultAutosize?: boolean, _animationMode?: string | undefined);
    ngAfterContentInit(): void;
    ngOnDestroy(): void;
    /** Calls `open` of both start and end drawers */
    open(): void;
    /** Calls `close` of both start and end drawers */
    close(): void;
    /**
     * Recalculates and updates the inline styles for the content. Note that this should be used
     * sparingly, because it causes a reflow.
     */
    updateContentMargins(): void;
    ngDoCheck(): void;
    /**
     * Subscribes to drawer events in order to set a class on the main container element when the
     * drawer is open and the backdrop is visible. This ensures any overflow on the container element
     * is properly hidden.
     */
    private _watchDrawerToggle;
    /**
     * Subscribes to drawer onPositionChanged event in order to
     * re-validate drawers when the position changes.
     */
    private _watchDrawerPosition;
    /** Subscribes to changes in drawer mode so we can run change detection. */
    private _watchDrawerMode;
    /** Toggles the 'mat-drawer-opened' class on the main 'mat-drawer-container' element. */
    private _setContainerClass;
    /** Validate the state of the drawer children components. */
    private _validateDrawers;
    /** Whether the container is being pushed to the side by one of the drawers. */
    private _isPushed;
    _onBackdropClicked(): void;
    _closeModalDrawersViaBackdrop(): void;
    _isShowingBackdrop(): boolean;
    private _isDrawerOpen;
    private _drawerHasBackdrop;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDrawerContainer, [{ optional: true; }, null, null, null, null, null, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatDrawerContainer, "mat-drawer-container", ["matDrawerContainer"], { "autosize": { "alias": "autosize"; "required": false; }; "hasBackdrop": { "alias": "hasBackdrop"; "required": false; }; }, { "backdropClick": "backdropClick"; }, ["_content", "_allDrawers"], ["mat-drawer", "mat-drawer-content", "*"], false, never>;
}

export declare class MatDrawerContent extends CdkScrollable implements AfterContentInit {
    private _changeDetectorRef;
    _container: MatDrawerContainer;
    constructor(_changeDetectorRef: ChangeDetectorRef, _container: MatDrawerContainer, elementRef: ElementRef<HTMLElement>, scrollDispatcher: ScrollDispatcher, ngZone: NgZone);
    ngAfterContentInit(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDrawerContent, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatDrawerContent, "mat-drawer-content", never, {}, {}, never, ["*"], false, never>;
}

/** Drawer and SideNav display modes. */
export declare type MatDrawerMode = 'over' | 'push' | 'side';

/** Result of the toggle promise that indicates the state of the drawer. */
export declare type MatDrawerToggleResult = 'open' | 'close';

export declare class MatSidenav extends MatDrawer {
    /** Whether the sidenav is fixed in the viewport. */
    get fixedInViewport(): boolean;
    set fixedInViewport(value: BooleanInput);
    private _fixedInViewport;
    /**
     * The gap between the top of the sidenav and the top of the viewport when the sidenav is in fixed
     * mode.
     */
    get fixedTopGap(): number;
    set fixedTopGap(value: NumberInput);
    private _fixedTopGap;
    /**
     * The gap between the bottom of the sidenav and the bottom of the viewport when the sidenav is in
     * fixed mode.
     */
    get fixedBottomGap(): number;
    set fixedBottomGap(value: NumberInput);
    private _fixedBottomGap;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatSidenav, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatSidenav, "mat-sidenav", ["matSidenav"], { "fixedInViewport": { "alias": "fixedInViewport"; "required": false; }; "fixedTopGap": { "alias": "fixedTopGap"; "required": false; }; "fixedBottomGap": { "alias": "fixedBottomGap"; "required": false; }; }, {}, never, ["*"], false, never>;
}

export declare class MatSidenavContainer extends MatDrawerContainer {
    _allDrawers: QueryList<MatSidenav>;
    _content: MatSidenavContent;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatSidenavContainer, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatSidenavContainer, "mat-sidenav-container", ["matSidenavContainer"], {}, {}, ["_content", "_allDrawers"], ["mat-sidenav", "mat-sidenav-content", "*"], false, never>;
}

export declare class MatSidenavContent extends MatDrawerContent {
    constructor(changeDetectorRef: ChangeDetectorRef, container: MatSidenavContainer, elementRef: ElementRef<HTMLElement>, scrollDispatcher: ScrollDispatcher, ngZone: NgZone);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatSidenavContent, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatSidenavContent, "mat-sidenav-content", never, {}, {}, never, ["*"], false, never>;
}

export declare class MatSidenavModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatSidenavModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatSidenavModule, [typeof i1.MatDrawer, typeof i1.MatDrawerContainer, typeof i1.MatDrawerContent, typeof i2.MatSidenav, typeof i2.MatSidenavContainer, typeof i2.MatSidenavContent], [typeof i3.CommonModule, typeof i4.MatCommonModule, typeof i5.CdkScrollableModule], [typeof i5.CdkScrollableModule, typeof i4.MatCommonModule, typeof i1.MatDrawer, typeof i1.MatDrawerContainer, typeof i1.MatDrawerContent, typeof i2.MatSidenav, typeof i2.MatSidenavContainer, typeof i2.MatSidenavContent]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatSidenavModule>;
}

/**
 * Throws an exception when two MatDrawer are matching the same position.
 * @docs-private
 */
export declare function throwMatDuplicatedDrawerError(position: string): void;

export { }
