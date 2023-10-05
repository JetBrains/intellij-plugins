import { _AbstractConstructor } from '@angular/material/core';
import { AfterViewInit } from '@angular/core';
import { CanColor } from '@angular/material/core';
import { ChangeDetectorRef } from '@angular/core';
import { _Constructor } from '@angular/material/core';
import { ElementRef } from '@angular/core';
import { EventEmitter } from '@angular/core';
import * as i0 from '@angular/core';
import * as i2 from '@angular/common';
import * as i3 from '@angular/material/core';
import { ProgressAnimationEnd as LegacyProgressAnimationEnd } from '@angular/material/progress-bar';
import { ProgressBarMode as LegacyProgressBarMode } from '@angular/material/progress-bar';
import { MAT_PROGRESS_BAR_DEFAULT_OPTIONS as MAT_LEGACY_PROGRESS_BAR_DEFAULT_OPTIONS } from '@angular/material/progress-bar';
import { MAT_PROGRESS_BAR_LOCATION as MAT_LEGACY_PROGRESS_BAR_LOCATION } from '@angular/material/progress-bar';
import { MAT_PROGRESS_BAR_LOCATION_FACTORY as MAT_LEGACY_PROGRESS_BAR_LOCATION_FACTORY } from '@angular/material/progress-bar';
import { MatProgressBarDefaultOptions as MatLegacyProgressBarDefaultOptions } from '@angular/material/progress-bar';
import { MatProgressBarLocation as MatLegacyProgressBarLocation } from '@angular/material/progress-bar';
import { NgZone } from '@angular/core';
import { NumberInput } from '@angular/cdk/coercion';
import { OnDestroy } from '@angular/core';

declare namespace i1 {
    export {
        MatLegacyProgressBar
    }
}

export { LegacyProgressAnimationEnd }

export { LegacyProgressBarMode }

export { MAT_LEGACY_PROGRESS_BAR_DEFAULT_OPTIONS }

export { MAT_LEGACY_PROGRESS_BAR_LOCATION }

export { MAT_LEGACY_PROGRESS_BAR_LOCATION_FACTORY }

/**
 * `<mat-progress-bar>` component.
 * @deprecated Use `MatProgressBar` from `@angular/material/progress-bar` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyProgressBar extends _MatProgressBarBase implements CanColor, AfterViewInit, OnDestroy {
    private _ngZone;
    _animationMode?: string | undefined;
    /**
     * @deprecated `_changeDetectorRef` parameter to be made required.
     * @breaking-change 11.0.0
     */
    private _changeDetectorRef?;
    constructor(elementRef: ElementRef, _ngZone: NgZone, _animationMode?: string | undefined, 
    /**
     * @deprecated `location` parameter to be made required.
     * @breaking-change 8.0.0
     */
    location?: MatLegacyProgressBarLocation, defaults?: MatLegacyProgressBarDefaultOptions, 
    /**
     * @deprecated `_changeDetectorRef` parameter to be made required.
     * @breaking-change 11.0.0
     */
    _changeDetectorRef?: ChangeDetectorRef | undefined);
    /** Flag that indicates whether NoopAnimations mode is set to true. */
    _isNoopAnimation: boolean;
    /** Value of the progress bar. Defaults to zero. Mirrored to aria-valuenow. */
    get value(): number;
    set value(v: NumberInput);
    private _value;
    /** Buffer value of the progress bar. Defaults to zero. */
    get bufferValue(): number;
    set bufferValue(v: number);
    private _bufferValue;
    _primaryValueBar: ElementRef;
    /**
     * Event emitted when animation of the primary progress bar completes. This event will not
     * be emitted when animations are disabled, nor will it be emitted for modes with continuous
     * animations (indeterminate and query).
     */
    readonly animationEnd: EventEmitter<LegacyProgressAnimationEnd>;
    /** Reference to animation end subscription to be unsubscribed on destroy. */
    private _animationEndSubscription;
    /**
     * Mode of the progress bar.
     *
     * Input must be one of these values: determinate, indeterminate, buffer, query, defaults to
     * 'determinate'.
     * Mirrored to mode attribute.
     */
    mode: LegacyProgressBarMode;
    /** ID of the progress bar. */
    progressbarId: string;
    /** Attribute to be used for the `fill` attribute on the internal `rect` element. */
    _rectangleFillValue: string;
    /** Gets the current transform value for the progress bar's primary indicator. */
    _primaryTransform(): {
        transform: string;
    };
    /**
     * Gets the current transform value for the progress bar's buffer indicator. Only used if the
     * progress mode is set to buffer, otherwise returns an undefined, causing no transformation.
     */
    _bufferTransform(): {
        transform: string;
    } | null;
    ngAfterViewInit(): void;
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyProgressBar, [null, null, { optional: true; }, { optional: true; }, { optional: true; }, null]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyProgressBar, "mat-progress-bar", ["matProgressBar"], { "color": { "alias": "color"; "required": false; }; "value": { "alias": "value"; "required": false; }; "bufferValue": { "alias": "bufferValue"; "required": false; }; "mode": { "alias": "mode"; "required": false; }; }, { "animationEnd": "animationEnd"; }, never, never, false, never>;
}

export { MatLegacyProgressBarDefaultOptions }

export { MatLegacyProgressBarLocation }

/**
 * @deprecated Use `MatProgressBarModule` from `@angular/material/progress-bar` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyProgressBarModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyProgressBarModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatLegacyProgressBarModule, [typeof i1.MatLegacyProgressBar], [typeof i2.CommonModule, typeof i3.MatCommonModule], [typeof i1.MatLegacyProgressBar, typeof i3.MatCommonModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatLegacyProgressBarModule>;
}

/** @docs-private */
declare const _MatProgressBarBase: _Constructor<CanColor> & _AbstractConstructor<CanColor> & {
    new (_elementRef: ElementRef): {
        _elementRef: ElementRef;
    };
};

export { }
