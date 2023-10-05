import { _AbstractConstructor } from '@angular/material/core';
import { AfterViewInit } from '@angular/core';
import { CanColor } from '@angular/material/core';
import { CanDisable } from '@angular/material/core';
import { CanDisableRipple } from '@angular/material/core';
import { _Constructor } from '@angular/material/core';
import { ElementRef } from '@angular/core';
import { FocusableOption } from '@angular/cdk/a11y';
import { FocusMonitor } from '@angular/cdk/a11y';
import { FocusOrigin } from '@angular/cdk/a11y';
import * as i0 from '@angular/core';
import * as i2 from '@angular/material/core';
import { MatRipple } from '@angular/material/core';
import { NgZone } from '@angular/core';
import { OnDestroy } from '@angular/core';

declare namespace i1 {
    export {
        MatLegacyButton,
        MatLegacyAnchor
    }
}

declare const _MatButtonBase: _Constructor<CanColor> & _AbstractConstructor<CanColor> & _Constructor<CanDisable> & _AbstractConstructor<CanDisable> & _Constructor<CanDisableRipple> & _AbstractConstructor<CanDisableRipple> & {
    new (_elementRef: ElementRef): {
        _elementRef: ElementRef;
    };
};

/**
 * Material design anchor button.
 * @deprecated Use `MatAnchor` from `@angular/material/button` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyAnchor extends MatLegacyButton implements AfterViewInit, OnDestroy {
    /** @breaking-change 14.0.0 _ngZone will be required. */
    private _ngZone?;
    /** Tabindex of the button. */
    tabIndex: number;
    constructor(focusMonitor: FocusMonitor, elementRef: ElementRef, animationMode: string, 
    /** @breaking-change 14.0.0 _ngZone will be required. */
    _ngZone?: NgZone | undefined);
    ngAfterViewInit(): void;
    ngOnDestroy(): void;
    _haltDisabledEvents: (event: Event) => void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyAnchor, [null, null, { optional: true; }, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyAnchor, "a[mat-button], a[mat-raised-button], a[mat-icon-button], a[mat-fab],             a[mat-mini-fab], a[mat-stroked-button], a[mat-flat-button]", ["matButton", "matAnchor"], { "disabled": { "alias": "disabled"; "required": false; }; "disableRipple": { "alias": "disableRipple"; "required": false; }; "color": { "alias": "color"; "required": false; }; "tabIndex": { "alias": "tabIndex"; "required": false; }; }, {}, never, ["*"], false, never>;
}

/**
 * Material design button.
 * @deprecated Use `MatButton` from `@angular/material/button` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyButton extends _MatButtonBase implements AfterViewInit, OnDestroy, CanDisable, CanColor, CanDisableRipple, FocusableOption {
    private _focusMonitor;
    _animationMode: string;
    /** Whether the button is round. */
    readonly isRoundButton: boolean;
    /** Whether the button is icon button. */
    readonly isIconButton: boolean;
    /** Reference to the MatRipple instance of the button. */
    ripple: MatRipple;
    constructor(elementRef: ElementRef, _focusMonitor: FocusMonitor, _animationMode: string);
    ngAfterViewInit(): void;
    ngOnDestroy(): void;
    /** Focuses the button. */
    focus(origin?: FocusOrigin, options?: FocusOptions): void;
    _getHostElement(): any;
    _isRippleDisabled(): boolean;
    /** Gets whether the button has one of the given attributes. */
    _hasHostAttributes(...attributes: string[]): boolean;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyButton, [null, null, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyButton, "button[mat-button], button[mat-raised-button], button[mat-icon-button],             button[mat-fab], button[mat-mini-fab], button[mat-stroked-button],             button[mat-flat-button]", ["matButton"], { "disabled": { "alias": "disabled"; "required": false; }; "disableRipple": { "alias": "disableRipple"; "required": false; }; "color": { "alias": "color"; "required": false; }; }, {}, never, ["*"], false, never>;
}

/**
 * @deprecated Use `MatButtonModule` from `@angular/material/button` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyButtonModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyButtonModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatLegacyButtonModule, [typeof i1.MatLegacyButton, typeof i1.MatLegacyAnchor], [typeof i2.MatRippleModule, typeof i2.MatCommonModule], [typeof i1.MatLegacyButton, typeof i1.MatLegacyAnchor, typeof i2.MatCommonModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatLegacyButtonModule>;
}

export { }
