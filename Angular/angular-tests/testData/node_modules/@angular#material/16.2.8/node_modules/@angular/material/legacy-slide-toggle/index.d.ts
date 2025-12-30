import { ChangeDetectorRef } from '@angular/core';
import { ControlValueAccessor } from '@angular/forms';
import { ElementRef } from '@angular/core';
import { FocusMonitor } from '@angular/cdk/a11y';
import { FocusOrigin } from '@angular/cdk/a11y';
import * as i0 from '@angular/core';
import * as i2 from '@angular/material/slide-toggle';
import * as i3 from '@angular/material/core';
import * as i4 from '@angular/cdk/observers';
import { InjectionToken } from '@angular/core';
import { MAT_SLIDE_TOGGLE_REQUIRED_VALIDATOR as MAT_LEGACY_SLIDE_TOGGLE_REQUIRED_VALIDATOR } from '@angular/material/slide-toggle';
import { _MatSlideToggleBase as _MatLegacySlideToggleBase } from '@angular/material/slide-toggle';
import { MatSlideToggleRequiredValidator as MatLegacySlideToggleRequiredValidator } from '@angular/material/slide-toggle';
import { _MatSlideToggleRequiredValidatorModule as _MatLegacySlideToggleRequiredValidatorModule } from '@angular/material/slide-toggle';
import { ThemePalette } from '@angular/material/core';
import { Type } from '@angular/core';

declare namespace i1 {
    export {
        MAT_LEGACY_SLIDE_TOGGLE_VALUE_ACCESSOR,
        MatLegacySlideToggleChange,
        MatLegacySlideToggle
    }
}

/**
 * Injection token to be used to override the default options for `mat-slide-toggle`
 * @deprecated Use `MAT_SLIDE_TOGGLE_DEFAULT_OPTIONS` from `@angular/material/slide-toggle` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare const MAT_LEGACY_SLIDE_TOGGLE_DEFAULT_OPTIONS: InjectionToken<MatLegacySlideToggleDefaultOptions>;

export { MAT_LEGACY_SLIDE_TOGGLE_REQUIRED_VALIDATOR }

/**
 * @docs-private
 * @deprecated Use `MAT_SLIDE_TOGGLE_VALUE_ACCESSOR` from `@angular/material/slide-toggle` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare const MAT_LEGACY_SLIDE_TOGGLE_VALUE_ACCESSOR: {
    provide: InjectionToken<readonly ControlValueAccessor[]>;
    useExisting: Type<any>;
    multi: boolean;
};

/**
 * Represents a slidable "switch" toggle that can be moved between on and off.
 * @deprecated Use `MatSlideToggle` from `@angular/material/slide-toggle` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacySlideToggle extends _MatLegacySlideToggleBase<MatLegacySlideToggleChange> {
    /** Reference to the underlying input element. */
    _inputElement: ElementRef<HTMLInputElement>;
    constructor(elementRef: ElementRef, focusMonitor: FocusMonitor, changeDetectorRef: ChangeDetectorRef, tabIndex: string, defaults: MatLegacySlideToggleDefaultOptions, animationMode?: string);
    protected _createChangeEvent(isChecked: boolean): MatLegacySlideToggleChange;
    /** Method being called whenever the underlying input emits a change event. */
    _onChangeEvent(event: Event): void;
    /** Method being called whenever the slide-toggle has been clicked. */
    _onInputClick(event: Event): void;
    /** Focuses the slide-toggle. */
    focus(options?: FocusOptions, origin?: FocusOrigin): void;
    /** Method being called whenever the label text changes. */
    _onLabelTextChange(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacySlideToggle, [null, null, null, { attribute: "tabindex"; }, null, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacySlideToggle, "mat-slide-toggle", ["matSlideToggle"], { "disabled": { "alias": "disabled"; "required": false; }; "disableRipple": { "alias": "disableRipple"; "required": false; }; "color": { "alias": "color"; "required": false; }; "tabIndex": { "alias": "tabIndex"; "required": false; }; }, {}, never, ["*"], false, never>;
}

export { _MatLegacySlideToggleBase }

/**
 * Change event object emitted by a slide toggle.
 * @deprecated Use `MatSlideToggleChange` from `@angular/material/slide-toggle` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacySlideToggleChange {
    /** The source slide toggle of the event. */
    source: MatLegacySlideToggle;
    /** The new `checked` value of the slide toggle. */
    checked: boolean;
    constructor(
    /** The source slide toggle of the event. */
    source: MatLegacySlideToggle, 
    /** The new `checked` value of the slide toggle. */
    checked: boolean);
}

/**
 * Default `mat-slide-toggle` options that can be overridden.
 * @deprecated Use `MatSlideToggleDefaultOptions` from `@angular/material/slide-toggle` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare interface MatLegacySlideToggleDefaultOptions {
    /** Whether toggle action triggers value changes in slide toggle. */
    disableToggleValue?: boolean;
    /** Default color for slide toggles. */
    color?: ThemePalette;
}

/**
 * @deprecated Use `MatSlideToggleModule` from `@angular/material/slide-toggle` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacySlideToggleModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacySlideToggleModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatLegacySlideToggleModule, [typeof i1.MatLegacySlideToggle], [typeof i2._MatSlideToggleRequiredValidatorModule, typeof i3.MatRippleModule, typeof i3.MatCommonModule, typeof i4.ObserversModule], [typeof i2._MatSlideToggleRequiredValidatorModule, typeof i1.MatLegacySlideToggle, typeof i3.MatCommonModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatLegacySlideToggleModule>;
}

export { MatLegacySlideToggleRequiredValidator }

export { _MatLegacySlideToggleRequiredValidatorModule }

export { }
