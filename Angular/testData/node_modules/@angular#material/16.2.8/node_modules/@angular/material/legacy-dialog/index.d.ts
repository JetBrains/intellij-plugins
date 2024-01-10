import { AnimationEvent as AnimationEvent_2 } from '@angular/animations';
import { ChangeDetectorRef } from '@angular/core';
import { _closeDialogVia as _closeLegacyDialogVia } from '@angular/material/dialog';
import { ElementRef } from '@angular/core';
import { FocusMonitor } from '@angular/cdk/a11y';
import { FocusTrapFactory } from '@angular/cdk/a11y';
import * as i0 from '@angular/core';
import * as i3 from '@angular/cdk/dialog';
import * as i4 from '@angular/cdk/overlay';
import * as i5 from '@angular/cdk/portal';
import * as i6 from '@angular/material/core';
import { InjectionToken } from '@angular/core';
import { Injector } from '@angular/core';
import { InteractivityChecker } from '@angular/cdk/a11y';
import { AutoFocusTarget as LegacyAutoFocusTarget } from '@angular/material/dialog';
import { DialogPosition as LegacyDialogPosition } from '@angular/material/dialog';
import { DialogRole as LegacyDialogRole } from '@angular/material/dialog';
import { Location as Location_2 } from '@angular/common';
import { MAT_DIALOG_SCROLL_STRATEGY_FACTORY as MAT_LEGACY_DIALOG_SCROLL_STRATEGY_FACTORY } from '@angular/material/dialog';
import { MatDialogConfig } from '@angular/material/dialog';
import { MatDialogRef } from '@angular/material/dialog';
import { matDialogAnimations as matLegacyDialogAnimations } from '@angular/material/dialog';
import { _MatDialogBase as _MatLegacyDialogBase } from '@angular/material/dialog';
import { _MatDialogContainerBase as _MatLegacyDialogContainerBase } from '@angular/material/dialog';
import { MatDialogState as MatLegacyDialogState } from '@angular/material/dialog';
import { NgZone } from '@angular/core';
import { OnChanges } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { OnInit } from '@angular/core';
import { Overlay } from '@angular/cdk/overlay';
import { OverlayContainer } from '@angular/cdk/overlay';
import { OverlayRef } from '@angular/cdk/overlay';
import { ScrollStrategy } from '@angular/cdk/overlay';
import { SimpleChanges } from '@angular/core';

export { _closeLegacyDialogVia }

declare namespace i1 {
    export {
        MatLegacyDialogContainer
    }
}

declare namespace i2 {
    export {
        MatLegacyDialogClose,
        MatLegacyDialogTitle,
        MatLegacyDialogContent,
        MatLegacyDialogActions
    }
}

export { LegacyAutoFocusTarget }

export { LegacyDialogPosition }

export { LegacyDialogRole }

/**
 * Injection token that can be used to access the data that was passed in to a dialog.
 * @deprecated Use `MAT_DIALOG_DATA` from `@angular/material/dialog` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare const MAT_LEGACY_DIALOG_DATA: InjectionToken<any>;

/**
 * Injection token that can be used to specify default dialog options.
 * @deprecated Use `MAT_DIALOG_DEFAULT_OPTIONS` from `@angular/material/dialog` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare const MAT_LEGACY_DIALOG_DEFAULT_OPTIONS: InjectionToken<MatLegacyDialogConfig<any>>;

/**
 * Injection token that determines the scroll handling while the dialog is open.
 * @deprecated Use `MAT_DIALOG_SCROLL_STRATEGY` from `@angular/material/dialog` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare const MAT_LEGACY_DIALOG_SCROLL_STRATEGY: InjectionToken<() => ScrollStrategy>;

export { MAT_LEGACY_DIALOG_SCROLL_STRATEGY_FACTORY }

/**
 * @docs-private
 * @deprecated Use `MAT_DIALOG_SCROLL_STRATEGY_PROVIDER` from `@angular/material/dialog` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare const MAT_LEGACY_DIALOG_SCROLL_STRATEGY_PROVIDER: {
    provide: InjectionToken<() => ScrollStrategy>;
    deps: (typeof Overlay)[];
    useFactory: typeof MAT_LEGACY_DIALOG_SCROLL_STRATEGY_PROVIDER_FACTORY;
};

/**
 * @docs-private
 * @deprecated Use `MAT_DIALOG_SCROLL_STRATEGY_PROVIDER_FACTORY` from `@angular/material/dialog` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare function MAT_LEGACY_DIALOG_SCROLL_STRATEGY_PROVIDER_FACTORY(overlay: Overlay): () => ScrollStrategy;

/**
 * Service to open Material Design modal dialogs.
 * @deprecated Use `MatDialog` from `@angular/material/dialog` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyDialog extends _MatLegacyDialogBase<MatLegacyDialogContainer> {
    protected dialogConfigClass: typeof MatLegacyDialogConfig;
    constructor(overlay: Overlay, injector: Injector, 
    /**
     * @deprecated `_location` parameter to be removed.
     * @breaking-change 10.0.0
     */
    _location: Location_2, defaultOptions: MatLegacyDialogConfig, scrollStrategy: any, parentDialog: MatLegacyDialog, 
    /**
     * @deprecated No longer used. To be removed.
     * @breaking-change 15.0.0
     */
    overlayContainer: OverlayContainer, 
    /**
     * @deprecated No longer used. To be removed.
     * @breaking-change 14.0.0
     */
    animationMode?: 'NoopAnimations' | 'BrowserAnimations');
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyDialog, [null, null, { optional: true; }, { optional: true; }, null, { optional: true; skipSelf: true; }, null, { optional: true; }]>;
    static ɵprov: i0.ɵɵInjectableDeclaration<MatLegacyDialog>;
}

/**
 * Container for the bottom action buttons in a dialog.
 * Stays fixed to the bottom when scrolling.
 * @deprecated Use `MatDialogActions` from `@angular/material/dialog` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyDialogActions {
    /**
     * Horizontal alignment of action buttons.
     */
    align?: 'start' | 'center' | 'end';
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyDialogActions, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyDialogActions, "[mat-dialog-actions], mat-dialog-actions, [matDialogActions]", never, { "align": { "alias": "align"; "required": false; }; }, {}, never, never, false, never>;
}

export { matLegacyDialogAnimations }

export { _MatLegacyDialogBase }

/**
 * Button that will close the current dialog.
 * @deprecated Use `MatDialogClose` from `@angular/material/dialog` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyDialogClose implements OnInit, OnChanges {
    /**
     * Reference to the containing dialog.
     * @deprecated `dialogRef` property to become private.
     * @breaking-change 13.0.0
     */
    dialogRef: MatLegacyDialogRef<any>;
    private _elementRef;
    private _dialog;
    /** Screen reader label for the button. */
    ariaLabel: string;
    /** Default to "button" to prevents accidental form submits. */
    type: 'submit' | 'button' | 'reset';
    /** Dialog close input. */
    dialogResult: any;
    _matDialogClose: any;
    constructor(
    /**
     * Reference to the containing dialog.
     * @deprecated `dialogRef` property to become private.
     * @breaking-change 13.0.0
     */
    dialogRef: MatLegacyDialogRef<any>, _elementRef: ElementRef<HTMLElement>, _dialog: MatLegacyDialog);
    ngOnInit(): void;
    ngOnChanges(changes: SimpleChanges): void;
    _onButtonClick(event: MouseEvent): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyDialogClose, [{ optional: true; }, null, null]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyDialogClose, "[mat-dialog-close], [matDialogClose]", ["matDialogClose"], { "ariaLabel": { "alias": "aria-label"; "required": false; }; "type": { "alias": "type"; "required": false; }; "dialogResult": { "alias": "mat-dialog-close"; "required": false; }; "_matDialogClose": { "alias": "matDialogClose"; "required": false; }; }, {}, never, never, false, never>;
}

/**
 * @deprecated Use `MatDialogConfig` from `@angular/material/dialog` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyDialogConfig<D = any> extends MatDialogConfig<D> {
    /** Duration of the enter animation. Has to be a valid CSS value (e.g. 100ms). */
    enterAnimationDuration?: string;
    /** Duration of the exit animation. Has to be a valid CSS value (e.g. 50ms). */
    exitAnimationDuration?: string;
}

/**
 * Internal component that wraps user-provided dialog content.
 * Animation is based on https://material.io/guidelines/motion/choreography.html.
 * @docs-private
 * @deprecated Use `MatDialogContainer` from `@angular/material/dialog` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyDialogContainer extends _MatLegacyDialogContainerBase {
    private _changeDetectorRef;
    /** State of the dialog animation. */
    _state: 'void' | 'enter' | 'exit';
    /** Callback, invoked whenever an animation on the host completes. */
    _onAnimationDone({ toState, totalTime }: AnimationEvent_2): void;
    /** Callback, invoked when an animation on the host starts. */
    _onAnimationStart({ toState, totalTime }: AnimationEvent_2): void;
    /** Starts the dialog exit animation. */
    _startExitAnimation(): void;
    constructor(elementRef: ElementRef, focusTrapFactory: FocusTrapFactory, document: any, dialogConfig: MatLegacyDialogConfig, checker: InteractivityChecker, ngZone: NgZone, overlayRef: OverlayRef, _changeDetectorRef: ChangeDetectorRef, focusMonitor?: FocusMonitor);
    _getAnimationState(): {
        value: "void" | "enter" | "exit";
        params: {
            enterAnimationDuration: string | number;
            exitAnimationDuration: string | number;
        };
    };
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyDialogContainer, [null, null, { optional: true; }, null, null, null, null, null, null]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyDialogContainer, "mat-dialog-container", never, {}, {}, never, never, false, never>;
}

export { _MatLegacyDialogContainerBase }

/**
 * Scrollable content container of a dialog.
 * @deprecated Use `MatDialogContent` from `@angular/material/dialog` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyDialogContent {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyDialogContent, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyDialogContent, "[mat-dialog-content], mat-dialog-content, [matDialogContent]", never, {}, {}, never, never, false, never>;
}

/**
 * @deprecated Use `MatDialogModule` from `@angular/material/dialog` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyDialogModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyDialogModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatLegacyDialogModule, [typeof i1.MatLegacyDialogContainer, typeof i2.MatLegacyDialogClose, typeof i2.MatLegacyDialogTitle, typeof i2.MatLegacyDialogActions, typeof i2.MatLegacyDialogContent], [typeof i3.DialogModule, typeof i4.OverlayModule, typeof i5.PortalModule, typeof i6.MatCommonModule], [typeof i1.MatLegacyDialogContainer, typeof i2.MatLegacyDialogClose, typeof i2.MatLegacyDialogTitle, typeof i2.MatLegacyDialogContent, typeof i2.MatLegacyDialogActions, typeof i6.MatCommonModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatLegacyDialogModule>;
}

/**
 * Reference to a dialog opened via the MatDialog service.
 * @deprecated Use `MatDialogRef` from `@angular/material/dialog` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyDialogRef<T, R = any> extends MatDialogRef<T, R> {
}

export { MatLegacyDialogState }

/**
 * Title of a dialog element. Stays fixed to the top of the dialog when scrolling.
 * @deprecated Use `MatDialogTitle` from `@angular/material/dialog` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyDialogTitle implements OnInit, OnDestroy {
    private _dialogRef;
    private _elementRef;
    private _dialog;
    /** Unique id for the dialog title. If none is supplied, it will be auto-generated. */
    id: string;
    constructor(_dialogRef: MatLegacyDialogRef<any>, _elementRef: ElementRef<HTMLElement>, _dialog: MatLegacyDialog);
    ngOnInit(): void;
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyDialogTitle, [{ optional: true; }, null, null]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyDialogTitle, "[mat-dialog-title], [matDialogTitle]", ["matDialogTitle"], { "id": { "alias": "id"; "required": false; }; }, {}, never, never, false, never>;
}

export { }
