import { ChangeDetectorRef } from '@angular/core';
import { ElementRef } from '@angular/core';
import { FocusMonitor } from '@angular/cdk/a11y';
import * as i0 from '@angular/core';
import * as i2 from '@angular/material/core';
import { MAT_RADIO_DEFAULT_OPTIONS as MAT_LEGACY_RADIO_DEFAULT_OPTIONS } from '@angular/material/radio';
import { MAT_RADIO_DEFAULT_OPTIONS_FACTORY as MAT_LEGACY_RADIO_DEFAULT_OPTIONS_FACTORY } from '@angular/material/radio';
import { MAT_RADIO_GROUP as MAT_LEGACY_RADIO_GROUP } from '@angular/material/radio';
import { MatRadioChange as MatLegacyRadioChange } from '@angular/material/radio';
import { MatRadioDefaultOptions as MatLegacyRadioDefaultOptions } from '@angular/material/radio';
import { _MatRadioButtonBase } from '@angular/material/radio';
import { _MatRadioGroupBase } from '@angular/material/radio';
import { QueryList } from '@angular/core';
import { UniqueSelectionDispatcher } from '@angular/cdk/collections';

declare namespace i1 {
    export {
        MAT_LEGACY_RADIO_GROUP_CONTROL_VALUE_ACCESSOR,
        MatLegacyRadioGroup,
        MatLegacyRadioButton
    }
}

export { MAT_LEGACY_RADIO_DEFAULT_OPTIONS }

export { MAT_LEGACY_RADIO_DEFAULT_OPTIONS_FACTORY }

export { MAT_LEGACY_RADIO_GROUP }

/**
 * Provider Expression that allows mat-radio-group to register as a ControlValueAccessor. This
 * allows it to support [(ngModel)] and ngControl.
 * @docs-private
 * @deprecated Use `MAT_RADIO_GROUP_CONTROL_VALUE_ACCESSOR` from `@angular/material/radio` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare const MAT_LEGACY_RADIO_GROUP_CONTROL_VALUE_ACCESSOR: any;

/**
 * A Material design radio-button. Typically placed inside of `<mat-radio-group>` elements.
 * @deprecated Use `MatRadioButton` from `@angular/material/radio` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyRadioButton extends _MatRadioButtonBase {
    constructor(radioGroup: MatLegacyRadioGroup, elementRef: ElementRef, changeDetector: ChangeDetectorRef, focusMonitor: FocusMonitor, radioDispatcher: UniqueSelectionDispatcher, animationMode?: string, providerOverride?: MatLegacyRadioDefaultOptions, tabIndex?: string);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyRadioButton, [{ optional: true; }, null, null, null, null, { optional: true; }, { optional: true; }, { attribute: "tabindex"; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyRadioButton, "mat-radio-button", ["matRadioButton"], { "disableRipple": { "alias": "disableRipple"; "required": false; }; "tabIndex": { "alias": "tabIndex"; "required": false; }; }, {}, never, ["*"], false, never>;
}

export { MatLegacyRadioChange }

export { MatLegacyRadioDefaultOptions }

/**
 * A group of radio buttons. May contain one or more `<mat-radio-button>` elements.
 * @deprecated Use `MatRadioGroup` from `@angular/material/radio` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyRadioGroup extends _MatRadioGroupBase<MatLegacyRadioButton> {
    _radios: QueryList<MatLegacyRadioButton>;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyRadioGroup, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyRadioGroup, "mat-radio-group", ["matRadioGroup"], {}, {}, ["_radios"], never, false, never>;
}

/**
 * @deprecated Use `MatRadioModule` from `@angular/material/radio` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyRadioModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyRadioModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatLegacyRadioModule, [typeof i1.MatLegacyRadioGroup, typeof i1.MatLegacyRadioButton], [typeof i2.MatRippleModule, typeof i2.MatCommonModule], [typeof i1.MatLegacyRadioGroup, typeof i1.MatLegacyRadioButton, typeof i2.MatCommonModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatLegacyRadioModule>;
}

export { }
