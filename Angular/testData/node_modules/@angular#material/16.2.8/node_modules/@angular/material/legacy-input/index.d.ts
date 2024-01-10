import { getMatInputUnsupportedTypeError as getMatLegacyInputUnsupportedTypeError } from '@angular/material/input';
import * as i0 from '@angular/core';
import * as i2 from '@angular/cdk/text-field';
import * as i3 from '@angular/material/legacy-form-field';
import * as i4 from '@angular/material/core';
import { MAT_INPUT_VALUE_ACCESSOR as MAT_LEGACY_INPUT_VALUE_ACCESSOR } from '@angular/material/input';
import { MatInput } from '@angular/material/input';

export { getMatLegacyInputUnsupportedTypeError }

declare namespace i1 {
    export {
        MatLegacyInput
    }
}

export { MAT_LEGACY_INPUT_VALUE_ACCESSOR }

/**
 * Directive that allows a native input to work inside a `MatFormField`.
 * @deprecated Use `MatInput` from `@angular/material/input` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyInput extends MatInput {
    private _legacyFormField;
    protected _getPlaceholder(): string | null;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyInput, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyInput, "input[matInput], textarea[matInput], select[matNativeControl],      input[matNativeControl], textarea[matNativeControl]", ["matInput"], {}, {}, never, never, false, never>;
}

/**
 * @deprecated Use `MatInputModule` from `@angular/material/input` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyInputModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyInputModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatLegacyInputModule, [typeof i1.MatLegacyInput], [typeof i2.TextFieldModule, typeof i3.MatLegacyFormFieldModule, typeof i4.MatCommonModule], [typeof i2.TextFieldModule, typeof i3.MatLegacyFormFieldModule, typeof i1.MatLegacyInput]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatLegacyInputModule>;
}

export { }
