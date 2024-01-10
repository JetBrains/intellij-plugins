import { getMatAutocompleteMissingPanelError as getMatLegacyAutocompleteMissingPanelError } from '@angular/material/autocomplete';
import * as i0 from '@angular/core';
import * as i4 from '@angular/cdk/overlay';
import * as i5 from '@angular/material/legacy-core';
import * as i6 from '@angular/material/core';
import * as i7 from '@angular/common';
import * as i8 from '@angular/cdk/scrolling';
import { MAT_AUTOCOMPLETE_DEFAULT_OPTIONS as MAT_LEGACY_AUTOCOMPLETE_DEFAULT_OPTIONS } from '@angular/material/autocomplete';
import { MAT_AUTOCOMPLETE_DEFAULT_OPTIONS_FACTORY as MAT_LEGACY_AUTOCOMPLETE_DEFAULT_OPTIONS_FACTORY } from '@angular/material/autocomplete';
import { MAT_AUTOCOMPLETE_SCROLL_STRATEGY as MAT_LEGACY_AUTOCOMPLETE_SCROLL_STRATEGY } from '@angular/material/autocomplete';
import { MAT_AUTOCOMPLETE_SCROLL_STRATEGY_FACTORY as MAT_LEGACY_AUTOCOMPLETE_SCROLL_STRATEGY_FACTORY } from '@angular/material/autocomplete';
import { MAT_AUTOCOMPLETE_SCROLL_STRATEGY_FACTORY_PROVIDER as MAT_LEGACY_AUTOCOMPLETE_SCROLL_STRATEGY_FACTORY_PROVIDER } from '@angular/material/autocomplete';
import { _MatAutocompleteBase } from '@angular/material/autocomplete';
import { _MatAutocompleteOriginBase } from '@angular/material/autocomplete';
import { _MatAutocompleteTriggerBase } from '@angular/material/autocomplete';
import { MatAutocompleteActivatedEvent as MatLegacyAutocompleteActivatedEvent } from '@angular/material/autocomplete';
import { MatAutocompleteDefaultOptions as MatLegacyAutocompleteDefaultOptions } from '@angular/material/autocomplete';
import { MatAutocompleteSelectedEvent as MatLegacyAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatLegacyOptgroup } from '@angular/material/legacy-core';
import { MatLegacyOption } from '@angular/material/legacy-core';
import { QueryList } from '@angular/core';

export { getMatLegacyAutocompleteMissingPanelError }

declare namespace i1 {
    export {
        MatLegacyAutocomplete
    }
}

declare namespace i2 {
    export {
        MAT_LEGACY_AUTOCOMPLETE_VALUE_ACCESSOR,
        MatLegacyAutocompleteTrigger
    }
}

declare namespace i3 {
    export {
        MatLegacyAutocompleteOrigin
    }
}

export { MAT_LEGACY_AUTOCOMPLETE_DEFAULT_OPTIONS }

export { MAT_LEGACY_AUTOCOMPLETE_DEFAULT_OPTIONS_FACTORY }

export { MAT_LEGACY_AUTOCOMPLETE_SCROLL_STRATEGY }

export { MAT_LEGACY_AUTOCOMPLETE_SCROLL_STRATEGY_FACTORY }

export { MAT_LEGACY_AUTOCOMPLETE_SCROLL_STRATEGY_FACTORY_PROVIDER }

/**
 * Provider that allows the autocomplete to register as a ControlValueAccessor.
 * @docs-private
 * @deprecated Use `MAT_AUTOCOMPLETE_VALUE_ACCESSOR` from `@angular/material/autocomplete` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare const MAT_LEGACY_AUTOCOMPLETE_VALUE_ACCESSOR: any;

/**
 * @deprecated Use `MatAutocomplete` from `@angular/material/autocomplete` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyAutocomplete extends _MatAutocompleteBase {
    /** Reference to all option groups within the autocomplete. */
    optionGroups: QueryList<MatLegacyOptgroup>;
    /** Reference to all options within the autocomplete. */
    options: QueryList<MatLegacyOption>;
    protected _visibleClass: string;
    protected _hiddenClass: string;
    _animationDone: null;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyAutocomplete, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyAutocomplete, "mat-autocomplete", ["matAutocomplete"], { "disableRipple": { "alias": "disableRipple"; "required": false; }; }, {}, ["optionGroups", "options"], ["*"], false, never>;
}

export { MatLegacyAutocompleteActivatedEvent }

export { MatLegacyAutocompleteDefaultOptions }

/**
 * @deprecated Use `MatAutocompleteModule` from `@angular/material/autocomplete` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyAutocompleteModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyAutocompleteModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatLegacyAutocompleteModule, [typeof i1.MatLegacyAutocomplete, typeof i2.MatLegacyAutocompleteTrigger, typeof i3.MatLegacyAutocompleteOrigin], [typeof i4.OverlayModule, typeof i5.MatLegacyOptionModule, typeof i6.MatCommonModule, typeof i7.CommonModule], [typeof i1.MatLegacyAutocomplete, typeof i2.MatLegacyAutocompleteTrigger, typeof i3.MatLegacyAutocompleteOrigin, typeof i8.CdkScrollableModule, typeof i5.MatLegacyOptionModule, typeof i6.MatCommonModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatLegacyAutocompleteModule>;
}

/**
 * Directive applied to an element to make it usable
 * as a connection point for an autocomplete panel.
 * @deprecated Use `MatAutocompleteOrigin` from `@angular/material/autocomplete` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyAutocompleteOrigin extends _MatAutocompleteOriginBase {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyAutocompleteOrigin, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyAutocompleteOrigin, "[matAutocompleteOrigin]", ["matAutocompleteOrigin"], {}, {}, never, never, false, never>;
}

export { MatLegacyAutocompleteSelectedEvent }

/**
 * @deprecated Use `MatAutocompleteTrigger` from `@angular/material/autocomplete` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyAutocompleteTrigger extends _MatAutocompleteTriggerBase {
    protected _aboveClass: string;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyAutocompleteTrigger, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyAutocompleteTrigger, "input[matAutocomplete], textarea[matAutocomplete]", ["matAutocompleteTrigger"], {}, {}, never, never, false, never>;
}

export { }
