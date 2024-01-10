import { _AbstractConstructor } from '@angular/material/core';
import { CanColor } from '@angular/material/core';
import { ChangeDetectorRef } from '@angular/core';
import { _Constructor } from '@angular/material/core';
import { ElementRef } from '@angular/core';
import * as i0 from '@angular/core';
import * as i2 from '@angular/material/core';
import * as i3 from '@angular/common';
import { ProgressSpinnerMode as LegacyProgressSpinnerMode } from '@angular/material/progress-spinner';
import { MAT_PROGRESS_SPINNER_DEFAULT_OPTIONS as MAT_LEGACY_PROGRESS_SPINNER_DEFAULT_OPTIONS } from '@angular/material/progress-spinner';
import { MAT_PROGRESS_SPINNER_DEFAULT_OPTIONS_FACTORY as MAT_LEGACY_PROGRESS_SPINNER_DEFAULT_OPTIONS_FACTORY } from '@angular/material/progress-spinner';
import { MatProgressSpinnerDefaultOptions as MatLegacyProgressSpinnerDefaultOptions } from '@angular/material/progress-spinner';
import { NgZone } from '@angular/core';
import { NumberInput } from '@angular/cdk/coercion';
import { OnDestroy } from '@angular/core';
import { OnInit } from '@angular/core';
import { Platform } from '@angular/cdk/platform';
import { ViewportRuler } from '@angular/cdk/scrolling';

declare namespace i1 {
    export {
        MatLegacyProgressSpinner
    }
}

export { LegacyProgressSpinnerMode }

export { MAT_LEGACY_PROGRESS_SPINNER_DEFAULT_OPTIONS }

export { MAT_LEGACY_PROGRESS_SPINNER_DEFAULT_OPTIONS_FACTORY }

/**
 * `<mat-progress-spinner>` component.
 * @deprecated Use `MatProgressSpinner` from `@angular/material/progress-spinner` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyProgressSpinner extends _MatProgressSpinnerBase implements OnInit, OnDestroy, CanColor {
    private _document;
    private _nonce?;
    private _diameter;
    private _value;
    private _strokeWidth;
    private _resizeSubscription;
    /**
     * Element to which we should add the generated style tags for the indeterminate animation.
     * For most elements this is the document, but for the ones in the Shadow DOM we need to
     * use the shadow root.
     */
    private _styleRoot;
    /**
     * Tracks diameters of existing instances to de-dupe generated styles (default d = 100).
     * We need to keep track of which elements the diameters were attached to, because for
     * elements in the Shadow DOM the style tags are attached to the shadow root, rather
     * than the document head.
     */
    private static _diameters;
    /** Whether the _mat-animation-noopable class should be applied, disabling animations.  */
    _noopAnimations: boolean;
    /** A string that is used for setting the spinner animation-name CSS property */
    _spinnerAnimationLabel: string;
    /** The diameter of the progress spinner (will set width and height of svg). */
    get diameter(): number;
    set diameter(size: NumberInput);
    /** Stroke width of the progress spinner. */
    get strokeWidth(): number;
    set strokeWidth(value: NumberInput);
    /** Mode of the progress circle */
    mode: LegacyProgressSpinnerMode;
    /** Value of the progress circle. */
    get value(): number;
    set value(newValue: NumberInput);
    constructor(elementRef: ElementRef<HTMLElement>, _platform: Platform, _document: any, animationMode: string, defaults?: MatLegacyProgressSpinnerDefaultOptions, 
    /**
     * @deprecated `changeDetectorRef`, `viewportRuler` and `ngZone`
     * parameters to become required.
     * @breaking-change 14.0.0
     */
    changeDetectorRef?: ChangeDetectorRef, viewportRuler?: ViewportRuler, ngZone?: NgZone, _nonce?: string | null | undefined);
    ngOnInit(): void;
    ngOnDestroy(): void;
    /** The radius of the spinner, adjusted for stroke width. */
    _getCircleRadius(): number;
    /** The view box of the spinner's svg element. */
    _getViewBox(): string;
    /** The stroke circumference of the svg circle. */
    _getStrokeCircumference(): number;
    /** The dash offset of the svg circle. */
    _getStrokeDashOffset(): number | null;
    /** Stroke width of the circle in percent. */
    _getCircleStrokeWidth(): number;
    /** Gets the `transform-origin` for the inner circle element. */
    _getCircleTransformOrigin(svg: HTMLElement): string;
    /** Dynamically generates a style tag containing the correct animation for this diameter. */
    private _attachStyleNode;
    /** Generates animation styles adjusted for the spinner's diameter. */
    private _getAnimationText;
    /** Returns the circle diameter formatted for use with the animation-name CSS property. */
    private _getSpinnerAnimationLabel;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyProgressSpinner, [null, null, { optional: true; }, { optional: true; }, null, null, null, null, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyProgressSpinner, "mat-progress-spinner, mat-spinner", ["matProgressSpinner"], { "color": { "alias": "color"; "required": false; }; "diameter": { "alias": "diameter"; "required": false; }; "strokeWidth": { "alias": "strokeWidth"; "required": false; }; "mode": { "alias": "mode"; "required": false; }; "value": { "alias": "value"; "required": false; }; }, {}, never, never, false, never>;
}

export { MatLegacyProgressSpinnerDefaultOptions }

/**
 * @deprecated Use `MatProgressSpinnerModule` from `@angular/material/progress-spinner` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyProgressSpinnerModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyProgressSpinnerModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatLegacyProgressSpinnerModule, [typeof i1.MatLegacyProgressSpinner], [typeof i2.MatCommonModule, typeof i3.CommonModule], [typeof i1.MatLegacyProgressSpinner, typeof i2.MatCommonModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatLegacyProgressSpinnerModule>;
}

/**
 * @deprecated Import Progress Spinner instead. Note that the
 *    `mat-spinner` selector isn't deprecated.
 * @breaking-change 8.0.0
 */
export declare const MatLegacySpinner: typeof MatLegacyProgressSpinner;

/** @docs-private */
declare const _MatProgressSpinnerBase: _Constructor<CanColor> & _AbstractConstructor<CanColor> & {
    new (_elementRef: ElementRef): {
        _elementRef: ElementRef;
    };
};

export { }
