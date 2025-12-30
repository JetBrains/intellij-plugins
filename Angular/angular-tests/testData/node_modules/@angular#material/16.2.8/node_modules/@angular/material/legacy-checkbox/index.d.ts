import { AfterViewInit } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';
import { ElementRef } from '@angular/core';
import { FocusMonitor } from '@angular/cdk/a11y';
import { FocusOrigin } from '@angular/cdk/a11y';
import * as i0 from '@angular/core';
import * as i2 from '@angular/material/core';
import * as i3 from '@angular/cdk/observers';
import * as i4 from '@angular/material/checkbox';
import { TransitionCheckState as LegacyTransitionCheckState } from '@angular/material/checkbox';
import { MAT_CHECKBOX_DEFAULT_OPTIONS as MAT_LEGACY_CHECKBOX_DEFAULT_OPTIONS } from '@angular/material/checkbox';
import { MAT_CHECKBOX_DEFAULT_OPTIONS_FACTORY as MAT_LEGACY_CHECKBOX_DEFAULT_OPTIONS_FACTORY } from '@angular/material/checkbox';
import { MAT_CHECKBOX_REQUIRED_VALIDATOR as MAT_LEGACY_CHECKBOX_REQUIRED_VALIDATOR } from '@angular/material/checkbox';
import { _MatCheckboxBase } from '@angular/material/checkbox';
import { MatCheckboxClickAction as MatLegacyCheckboxClickAction } from '@angular/material/checkbox';
import { MatCheckboxDefaultOptions as MatLegacyCheckboxDefaultOptions } from '@angular/material/checkbox';
import { MatCheckboxRequiredValidator as MatLegacyCheckboxRequiredValidator } from '@angular/material/checkbox';
import { _MatCheckboxRequiredValidatorModule as _MatLegacyCheckboxRequiredValidatorModule } from '@angular/material/checkbox';
import { NgZone } from '@angular/core';
import { OnDestroy } from '@angular/core';

declare namespace i1 {
    export {
        MatLegacyCheckboxChange,
        MAT_LEGACY_CHECKBOX_CONTROL_VALUE_ACCESSOR,
        MatLegacyCheckbox
    }
}

export { LegacyTransitionCheckState }

/**
 * Provider Expression that allows mat-checkbox to register as a ControlValueAccessor.
 * This allows it to support [(ngModel)].
 * @docs-private
 * @deprecated Use `MAT_CHECKBOX_CONTROL_VALUE_ACCESSOR` from `@angular/material/checkbox` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare const MAT_LEGACY_CHECKBOX_CONTROL_VALUE_ACCESSOR: any;

export { MAT_LEGACY_CHECKBOX_DEFAULT_OPTIONS }

export { MAT_LEGACY_CHECKBOX_DEFAULT_OPTIONS_FACTORY }

export { MAT_LEGACY_CHECKBOX_REQUIRED_VALIDATOR }

/**
 * A material design checkbox component. Supports all of the functionality of an HTML5 checkbox,
 * and exposes a similar API. A checkbox can be either checked, unchecked, indeterminate, or
 * disabled. Note that all additional accessibility attributes are taken care of by the component,
 * so there is no need to provide them yourself. However, if you want to omit a label and still
 * have the checkbox be accessible, you may supply an [aria-label] input.
 * See: https://material.io/design/components/selection-controls.html
 * @deprecated Use `MatCheckbox` from `@angular/material/checkbox` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyCheckbox extends _MatCheckboxBase<MatLegacyCheckboxChange> implements AfterViewInit, OnDestroy {
    private _focusMonitor;
    protected _animationClasses: {
        uncheckedToChecked: string;
        uncheckedToIndeterminate: string;
        checkedToUnchecked: string;
        checkedToIndeterminate: string;
        indeterminateToChecked: string;
        indeterminateToUnchecked: string;
    };
    constructor(elementRef: ElementRef<HTMLElement>, changeDetectorRef: ChangeDetectorRef, _focusMonitor: FocusMonitor, ngZone: NgZone, tabIndex: string, animationMode?: string, options?: MatLegacyCheckboxDefaultOptions);
    protected _createChangeEvent(isChecked: boolean): MatLegacyCheckboxChange;
    protected _getAnimationTargetElement(): any;
    ngAfterViewInit(): void;
    ngOnDestroy(): void;
    /**
     * Event handler for checkbox input element.
     * Toggles checked state if element is not disabled.
     * Do not toggle on (change) event since IE doesn't fire change event when
     *   indeterminate checkbox is clicked.
     * @param event
     */
    _onInputClick(event: Event): void;
    /** Focuses the checkbox. */
    focus(origin?: FocusOrigin, options?: FocusOptions): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyCheckbox, [null, null, null, null, { attribute: "tabindex"; }, { optional: true; }, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyCheckbox, "mat-checkbox", ["matCheckbox"], { "disableRipple": { "alias": "disableRipple"; "required": false; }; "color": { "alias": "color"; "required": false; }; "tabIndex": { "alias": "tabIndex"; "required": false; }; }, {}, never, ["*"], false, never>;
}

/**
 * Change event object emitted by a checkbox.
 * @deprecated Use `MatCheckboxChange` from `@angular/material/checkbox` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyCheckboxChange {
    /** The source checkbox of the event. */
    source: MatLegacyCheckbox;
    /** The new `checked` value of the checkbox. */
    checked: boolean;
}

export { MatLegacyCheckboxClickAction }

export { MatLegacyCheckboxDefaultOptions }

/**
 * @deprecated Use `MatCheckboxModule` from `@angular/material/checkbox` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyCheckboxModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyCheckboxModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatLegacyCheckboxModule, [typeof i1.MatLegacyCheckbox], [typeof i2.MatRippleModule, typeof i2.MatCommonModule, typeof i3.ObserversModule, typeof i4._MatCheckboxRequiredValidatorModule], [typeof i1.MatLegacyCheckbox, typeof i2.MatCommonModule, typeof i4._MatCheckboxRequiredValidatorModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatLegacyCheckboxModule>;
}

export { MatLegacyCheckboxRequiredValidator }

export { _MatLegacyCheckboxRequiredValidatorModule }

export { }
