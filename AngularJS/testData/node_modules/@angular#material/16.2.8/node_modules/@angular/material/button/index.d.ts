import { _AbstractConstructor } from '@angular/material/core';
import { AfterViewInit } from '@angular/core';
import { BooleanInput } from '@angular/cdk/coercion';
import { CanColor } from '@angular/material/core';
import { CanDisable } from '@angular/material/core';
import { CanDisableRipple } from '@angular/material/core';
import { _Constructor } from '@angular/material/core';
import { ElementRef } from '@angular/core';
import { FocusOrigin } from '@angular/cdk/a11y';
import * as i0 from '@angular/core';
import * as i4 from '@angular/material/core';
import { InjectionToken } from '@angular/core';
import { MatRipple } from '@angular/material/core';
import { MatRippleLoader } from '@angular/material/core';
import { NgZone } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { OnInit } from '@angular/core';
import { Platform } from '@angular/cdk/platform';
import { ThemePalette } from '@angular/material/core';

declare namespace i1 {
    export {
        MatButton,
        MatAnchor
    }
}

declare namespace i2 {
    export {
        MatIconButton,
        MatIconAnchor
    }
}

declare namespace i3 {
    export {
        MAT_FAB_DEFAULT_OPTIONS_FACTORY,
        MatFabDefaultOptions,
        MAT_FAB_DEFAULT_OPTIONS,
        MatFabButton,
        MatMiniFabButton,
        MatFabAnchor,
        MatMiniFabAnchor
    }
}

/** Injection token to be used to override the default options for FAB. */
export declare const MAT_FAB_DEFAULT_OPTIONS: InjectionToken<MatFabDefaultOptions>;

/** @docs-private */
export declare function MAT_FAB_DEFAULT_OPTIONS_FACTORY(): MatFabDefaultOptions;

/**
 * Material Design button component for anchor elements. Anchor elements are used to provide
 * links for the user to navigate across different routes or pages.
 * See https://material.io/components/buttons
 *
 * The `MatAnchor` class applies to native anchor elements and captures the appearances for
 * "text button", "outlined button", and "contained button" per the Material Design
 * specification. `MatAnchor` additionally captures an additional "flat" appearance, which matches
 * "contained" but without elevation.
 */
export declare class MatAnchor extends MatAnchorBase {
    constructor(elementRef: ElementRef, platform: Platform, ngZone: NgZone, animationMode?: string);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatAnchor, [null, null, null, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatAnchor, "a[mat-button], a[mat-raised-button], a[mat-flat-button], a[mat-stroked-button]", ["matButton", "matAnchor"], { "disabled": { "alias": "disabled"; "required": false; }; "disableRipple": { "alias": "disableRipple"; "required": false; }; "color": { "alias": "color"; "required": false; }; "tabIndex": { "alias": "tabIndex"; "required": false; }; }, {}, never, [".material-icons:not([iconPositionEnd]), mat-icon:not([iconPositionEnd]), [matButtonIcon]:not([iconPositionEnd])", "*", ".material-icons[iconPositionEnd], mat-icon[iconPositionEnd], [matButtonIcon][iconPositionEnd]"], false, never>;
}

/**
 * Anchor button base.
 */
declare class MatAnchorBase extends MatButtonBase implements OnInit, OnDestroy {
    tabIndex: number;
    constructor(elementRef: ElementRef, platform: Platform, ngZone: NgZone, animationMode?: string);
    ngOnInit(): void;
    ngOnDestroy(): void;
    _haltDisabledEvents: (event: Event) => void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatAnchorBase, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatAnchorBase, never, never, {}, {}, never, never, false, never>;
}

/**
 * Material Design button component. Users interact with a button to perform an action.
 * See https://material.io/components/buttons
 *
 * The `MatButton` class applies to native button elements and captures the appearances for
 * "text button", "outlined button", and "contained button" per the Material Design
 * specification. `MatButton` additionally captures an additional "flat" appearance, which matches
 * "contained" but without elevation.
 */
export declare class MatButton extends MatButtonBase {
    constructor(elementRef: ElementRef, platform: Platform, ngZone: NgZone, animationMode?: string);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatButton, [null, null, null, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatButton, "    button[mat-button], button[mat-raised-button], button[mat-flat-button],    button[mat-stroked-button]  ", ["matButton"], { "disabled": { "alias": "disabled"; "required": false; }; "disableRipple": { "alias": "disableRipple"; "required": false; }; "color": { "alias": "color"; "required": false; }; }, {}, never, [".material-icons:not([iconPositionEnd]), mat-icon:not([iconPositionEnd]), [matButtonIcon]:not([iconPositionEnd])", "*", ".material-icons[iconPositionEnd], mat-icon[iconPositionEnd], [matButtonIcon][iconPositionEnd]"], false, never>;
}

/** Base class for all buttons.  */
declare class MatButtonBase extends _MatButtonMixin implements CanDisable, CanColor, CanDisableRipple, AfterViewInit, OnDestroy {
    _platform: Platform;
    _ngZone: NgZone;
    _animationMode?: string | undefined;
    private readonly _focusMonitor;
    /**
     * Handles the lazy creation of the MatButton ripple.
     * Used to improve initial load time of large applications.
     */
    _rippleLoader: MatRippleLoader;
    /** Whether this button is a FAB. Used to apply the correct class on the ripple. */
    _isFab: boolean;
    /**
     * Reference to the MatRipple instance of the button.
     * @deprecated Considered an implementation detail. To be removed.
     * @breaking-change 17.0.0
     */
    get ripple(): MatRipple;
    set ripple(v: MatRipple);
    /** Whether the ripple effect is disabled or not. */
    get disableRipple(): boolean;
    set disableRipple(value: any);
    private _disableRipple;
    get disabled(): boolean;
    set disabled(value: any);
    private _disabled;
    constructor(elementRef: ElementRef, _platform: Platform, _ngZone: NgZone, _animationMode?: string | undefined);
    ngAfterViewInit(): void;
    ngOnDestroy(): void;
    /** Focuses the button. */
    focus(_origin?: FocusOrigin, options?: FocusOptions): void;
    /** Gets whether the button has one of the given attributes. */
    private _hasHostAttributes;
    private _updateRippleDisabled;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatButtonBase, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatButtonBase, never, never, {}, {}, never, never, false, never>;
}

/** @docs-private */
declare const _MatButtonMixin: _Constructor<CanColor> & _AbstractConstructor<CanColor> & _Constructor<CanDisable> & _AbstractConstructor<CanDisable> & _Constructor<CanDisableRipple> & _AbstractConstructor<CanDisableRipple> & {
    new (_elementRef: ElementRef): {
        _elementRef: ElementRef;
    };
};

export declare class MatButtonModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatButtonModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatButtonModule, [typeof i1.MatAnchor, typeof i1.MatButton, typeof i2.MatIconAnchor, typeof i3.MatMiniFabAnchor, typeof i3.MatMiniFabButton, typeof i2.MatIconButton, typeof i3.MatFabAnchor, typeof i3.MatFabButton], [typeof i4.MatCommonModule, typeof i4.MatRippleModule], [typeof i1.MatAnchor, typeof i1.MatButton, typeof i2.MatIconAnchor, typeof i2.MatIconButton, typeof i3.MatMiniFabAnchor, typeof i3.MatMiniFabButton, typeof i3.MatFabAnchor, typeof i3.MatFabButton, typeof i4.MatCommonModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatButtonModule>;
}

/**
 * Material Design floating action button (FAB) component for anchor elements. Anchor elements
 * are used to provide links for the user to navigate across different routes or pages.
 * See https://material.io/components/buttons-floating-action-button/
 *
 * The `MatFabAnchor` class has two appearances: normal and extended.
 */
export declare class MatFabAnchor extends MatAnchor {
    private _options?;
    _isFab: boolean;
    get extended(): boolean;
    set extended(value: BooleanInput);
    private _extended;
    constructor(elementRef: ElementRef, platform: Platform, ngZone: NgZone, animationMode?: string, _options?: MatFabDefaultOptions | undefined);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatFabAnchor, [null, null, null, { optional: true; }, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatFabAnchor, "a[mat-fab]", ["matButton", "matAnchor"], { "disabled": { "alias": "disabled"; "required": false; }; "disableRipple": { "alias": "disableRipple"; "required": false; }; "color": { "alias": "color"; "required": false; }; "tabIndex": { "alias": "tabIndex"; "required": false; }; "extended": { "alias": "extended"; "required": false; }; }, {}, never, [".material-icons:not([iconPositionEnd]), mat-icon:not([iconPositionEnd]), [matButtonIcon]:not([iconPositionEnd])", "*", ".material-icons[iconPositionEnd], mat-icon[iconPositionEnd], [matButtonIcon][iconPositionEnd]"], false, never>;
}

/**
 * Material Design floating action button (FAB) component. These buttons represent the primary
 * or most common action for users to interact with.
 * See https://material.io/components/buttons-floating-action-button/
 *
 * The `MatFabButton` class has two appearances: normal and extended.
 */
export declare class MatFabButton extends MatButtonBase {
    private _options?;
    _isFab: boolean;
    get extended(): boolean;
    set extended(value: BooleanInput);
    private _extended;
    constructor(elementRef: ElementRef, platform: Platform, ngZone: NgZone, animationMode?: string, _options?: MatFabDefaultOptions | undefined);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatFabButton, [null, null, null, { optional: true; }, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatFabButton, "button[mat-fab]", ["matButton"], { "disabled": { "alias": "disabled"; "required": false; }; "disableRipple": { "alias": "disableRipple"; "required": false; }; "color": { "alias": "color"; "required": false; }; "tabIndex": { "alias": "tabIndex"; "required": false; }; "extended": { "alias": "extended"; "required": false; }; }, {}, never, [".material-icons:not([iconPositionEnd]), mat-icon:not([iconPositionEnd]), [matButtonIcon]:not([iconPositionEnd])", "*", ".material-icons[iconPositionEnd], mat-icon[iconPositionEnd], [matButtonIcon][iconPositionEnd]"], false, never>;
}

/** Default FAB options that can be overridden. */
export declare interface MatFabDefaultOptions {
    color?: ThemePalette;
}

/**
 * Material Design icon button component for anchor elements. This button displays a single
 * interaction icon that allows users to navigate across different routes or pages.
 * See https://material.io/develop/web/components/buttons/icon-buttons/
 */
export declare class MatIconAnchor extends MatAnchorBase {
    constructor(elementRef: ElementRef, platform: Platform, ngZone: NgZone, animationMode?: string);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatIconAnchor, [null, null, null, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatIconAnchor, "a[mat-icon-button]", ["matButton", "matAnchor"], { "disabled": { "alias": "disabled"; "required": false; }; "disableRipple": { "alias": "disableRipple"; "required": false; }; "color": { "alias": "color"; "required": false; }; "tabIndex": { "alias": "tabIndex"; "required": false; }; }, {}, never, [".material-icons:not([iconPositionEnd]), mat-icon:not([iconPositionEnd]), [matButtonIcon]:not([iconPositionEnd])", "*", ".material-icons[iconPositionEnd], mat-icon[iconPositionEnd], [matButtonIcon][iconPositionEnd]"], false, never>;
}

/**
 * Material Design icon button component. This type of button displays a single interactive icon for
 * users to perform an action.
 * See https://material.io/develop/web/components/buttons/icon-buttons/
 */
export declare class MatIconButton extends MatButtonBase {
    constructor(elementRef: ElementRef, platform: Platform, ngZone: NgZone, animationMode?: string);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatIconButton, [null, null, null, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatIconButton, "button[mat-icon-button]", ["matButton"], { "disabled": { "alias": "disabled"; "required": false; }; "disableRipple": { "alias": "disableRipple"; "required": false; }; "color": { "alias": "color"; "required": false; }; }, {}, never, ["*"], false, never>;
}

/**
 * Material Design mini floating action button (FAB) component for anchor elements. Anchor elements
 * are used to provide links for the user to navigate across different routes or pages.
 * See https://material.io/components/buttons-floating-action-button/
 */
export declare class MatMiniFabAnchor extends MatAnchor {
    private _options?;
    _isFab: boolean;
    constructor(elementRef: ElementRef, platform: Platform, ngZone: NgZone, animationMode?: string, _options?: MatFabDefaultOptions | undefined);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatMiniFabAnchor, [null, null, null, { optional: true; }, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatMiniFabAnchor, "a[mat-mini-fab]", ["matButton", "matAnchor"], { "disabled": { "alias": "disabled"; "required": false; }; "disableRipple": { "alias": "disableRipple"; "required": false; }; "color": { "alias": "color"; "required": false; }; "tabIndex": { "alias": "tabIndex"; "required": false; }; }, {}, never, [".material-icons:not([iconPositionEnd]), mat-icon:not([iconPositionEnd]), [matButtonIcon]:not([iconPositionEnd])", "*", ".material-icons[iconPositionEnd], mat-icon[iconPositionEnd], [matButtonIcon][iconPositionEnd]"], false, never>;
}

/**
 * Material Design mini floating action button (FAB) component. These buttons represent the primary
 * or most common action for users to interact with.
 * See https://material.io/components/buttons-floating-action-button/
 */
export declare class MatMiniFabButton extends MatButtonBase {
    private _options?;
    _isFab: boolean;
    constructor(elementRef: ElementRef, platform: Platform, ngZone: NgZone, animationMode?: string, _options?: MatFabDefaultOptions | undefined);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatMiniFabButton, [null, null, null, { optional: true; }, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatMiniFabButton, "button[mat-mini-fab]", ["matButton"], { "disabled": { "alias": "disabled"; "required": false; }; "disableRipple": { "alias": "disableRipple"; "required": false; }; "color": { "alias": "color"; "required": false; }; }, {}, never, [".material-icons:not([iconPositionEnd]), mat-icon:not([iconPositionEnd]), [matButtonIcon]:not([iconPositionEnd])", "*", ".material-icons[iconPositionEnd], mat-icon[iconPositionEnd], [matButtonIcon][iconPositionEnd]"], false, never>;
}

export { }
