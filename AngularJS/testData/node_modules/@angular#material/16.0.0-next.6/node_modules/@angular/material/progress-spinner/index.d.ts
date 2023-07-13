import { _AbstractConstructor } from '@angular/material/core';
import { CanColor } from '@angular/material/core';
import { _Constructor } from '@angular/material/core';
import { ElementRef } from '@angular/core';
import * as i0 from '@angular/core';
import * as i2 from '@angular/common';
import * as i3 from '@angular/material/core';
import { InjectionToken } from '@angular/core';
import { NumberInput } from '@angular/cdk/coercion';
import { ThemePalette } from '@angular/material/core';

declare namespace i1 {
    export {
        MAT_PROGRESS_SPINNER_DEFAULT_OPTIONS_FACTORY,
        ProgressSpinnerMode,
        MatProgressSpinnerDefaultOptions,
        MAT_PROGRESS_SPINNER_DEFAULT_OPTIONS,
        MatProgressSpinner,
        MatSpinner
    }
}

/** Injection token to be used to override the default options for `mat-progress-spinner`. */
export declare const MAT_PROGRESS_SPINNER_DEFAULT_OPTIONS: InjectionToken<MatProgressSpinnerDefaultOptions>;

/** @docs-private */
export declare function MAT_PROGRESS_SPINNER_DEFAULT_OPTIONS_FACTORY(): MatProgressSpinnerDefaultOptions;

export declare class MatProgressSpinner extends _MatProgressSpinnerBase implements CanColor {
    /** Whether the _mat-animation-noopable class should be applied, disabling animations.  */
    _noopAnimations: boolean;
    /** The element of the determinate spinner. */
    _determinateCircle: ElementRef<HTMLElement>;
    constructor(elementRef: ElementRef<HTMLElement>, animationMode: string, defaults?: MatProgressSpinnerDefaultOptions);
    /**
     * Mode of the progress bar.
     *
     * Input must be one of these values: determinate, indeterminate, buffer, query, defaults to
     * 'determinate'.
     * Mirrored to mode attribute.
     */
    mode: ProgressSpinnerMode;
    /** Value of the progress bar. Defaults to zero. Mirrored to aria-valuenow. */
    get value(): number;
    set value(v: NumberInput);
    private _value;
    /** The diameter of the progress spinner (will set width and height of svg). */
    get diameter(): number;
    set diameter(size: NumberInput);
    private _diameter;
    /** Stroke width of the progress spinner. */
    get strokeWidth(): number;
    set strokeWidth(value: NumberInput);
    private _strokeWidth;
    /** The radius of the spinner, adjusted for stroke width. */
    _circleRadius(): number;
    /** The view box of the spinner's svg element. */
    _viewBox(): string;
    /** The stroke circumference of the svg circle. */
    _strokeCircumference(): number;
    /** The dash offset of the svg circle. */
    _strokeDashOffset(): number | null;
    /** Stroke width of the circle in percent. */
    _circleStrokeWidth(): number;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatProgressSpinner, [null, { optional: true; }, null]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatProgressSpinner, "mat-progress-spinner, mat-spinner", ["matProgressSpinner"], { "color": "color"; "mode": "mode"; "value": "value"; "diameter": "diameter"; "strokeWidth": "strokeWidth"; }, {}, never, never, false, never>;
}

declare const _MatProgressSpinnerBase: _Constructor<CanColor> & _AbstractConstructor<CanColor> & {
    new (_elementRef: ElementRef): {
        _elementRef: ElementRef;
    };
};

/** Default `mat-progress-spinner` options that can be overridden. */
export declare interface MatProgressSpinnerDefaultOptions {
    /** Default color of the spinner. */
    color?: ThemePalette;
    /** Diameter of the spinner. */
    diameter?: number;
    /** Width of the spinner's stroke. */
    strokeWidth?: number;
    /**
     * Whether the animations should be force to be enabled, ignoring if the current environment is
     * using NoopAnimationsModule.
     */
    _forceAnimations?: boolean;
}

export declare class MatProgressSpinnerModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatProgressSpinnerModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatProgressSpinnerModule, [typeof i1.MatProgressSpinner, typeof i1.MatProgressSpinner], [typeof i2.CommonModule], [typeof i1.MatProgressSpinner, typeof i1.MatProgressSpinner, typeof i3.MatCommonModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatProgressSpinnerModule>;
}

/**
 * @deprecated Import Progress Spinner instead. Note that the
 *    `mat-spinner` selector isn't deprecated.
 * @breaking-change 16.0.0
 */
export declare const MatSpinner: typeof MatProgressSpinner;

/** Possible mode for a progress spinner. */
export declare type ProgressSpinnerMode = 'determinate' | 'indeterminate';

export { }
