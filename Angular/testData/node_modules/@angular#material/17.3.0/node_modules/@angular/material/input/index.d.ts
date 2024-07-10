import { AfterViewInit } from '@angular/core';
import { AutofillMonitor } from '@angular/cdk/text-field';
import { BooleanInput } from '@angular/cdk/coercion';
import { DoCheck } from '@angular/core';
import { ElementRef } from '@angular/core';
import { ErrorStateMatcher } from '@angular/material/core';
import { FormGroupDirective } from '@angular/forms';
import * as i0 from '@angular/core';
import * as i1 from '@angular/material/core';
import * as i2 from '@angular/material/form-field';
import * as i4 from '@angular/cdk/text-field';
import { InjectionToken } from '@angular/core';
import { MatError } from '@angular/material/form-field';
import { MatFormField } from '@angular/material/form-field';
import { MatFormFieldControl } from '@angular/material/form-field';
import { MatHint } from '@angular/material/form-field';
import { MatLabel } from '@angular/material/form-field';
import { MatPrefix } from '@angular/material/form-field';
import { MatSuffix } from '@angular/material/form-field';
import { NgControl } from '@angular/forms';
import { NgForm } from '@angular/forms';
import { NgZone } from '@angular/core';
import { OnChanges } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { Platform } from '@angular/cdk/platform';
import { Subject } from 'rxjs';


/** @docs-private */
export declare function getMatInputUnsupportedTypeError(type: string): Error;

declare namespace i3 {
    export {
        MatInput
    }
}

/**
 * This token is used to inject the object whose value should be set into `MatInput`. If none is
 * provided, the native `HTMLInputElement` is used. Directives like `MatDatepickerInput` can provide
 * themselves for this token, in order to make `MatInput` delegate the getting and setting of the
 * value to them.
 */
export declare const MAT_INPUT_VALUE_ACCESSOR: InjectionToken<{
    value: any;
}>;

export { MatError }

export { MatFormField }

export { MatHint }

export declare class MatInput implements MatFormFieldControl<any>, OnChanges, OnDestroy, AfterViewInit, DoCheck {
    protected _elementRef: ElementRef<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>;
    protected _platform: Platform;
    ngControl: NgControl;
    private _autofillMonitor;
    protected _formField?: MatFormField | undefined;
    protected _uid: string;
    protected _previousNativeValue: any;
    private _inputValueAccessor;
    private _previousPlaceholder;
    private _errorStateTracker;
    /** Whether the component is being rendered on the server. */
    readonly _isServer: boolean;
    /** Whether the component is a native html select. */
    readonly _isNativeSelect: boolean;
    /** Whether the component is a textarea. */
    readonly _isTextarea: boolean;
    /** Whether the input is inside of a form field. */
    readonly _isInFormField: boolean;
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    focused: boolean;
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    readonly stateChanges: Subject<void>;
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    controlType: string;
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    autofilled: boolean;
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    get disabled(): boolean;
    set disabled(value: BooleanInput);
    protected _disabled: boolean;
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    get id(): string;
    set id(value: string);
    protected _id: string;
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    placeholder: string;
    /**
     * Name of the input.
     * @docs-private
     */
    name: string;
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    get required(): boolean;
    set required(value: BooleanInput);
    protected _required: boolean | undefined;
    /** Input type of the element. */
    get type(): string;
    set type(value: string);
    protected _type: string;
    /** An object used to control when error messages are shown. */
    get errorStateMatcher(): ErrorStateMatcher;
    set errorStateMatcher(value: ErrorStateMatcher);
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    userAriaDescribedBy: string;
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    get value(): string;
    set value(value: any);
    /** Whether the element is readonly. */
    get readonly(): boolean;
    set readonly(value: BooleanInput);
    private _readonly;
    /** Whether the input is in an error state. */
    get errorState(): boolean;
    set errorState(value: boolean);
    protected _neverEmptyInputTypes: string[];
    constructor(_elementRef: ElementRef<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>, _platform: Platform, ngControl: NgControl, parentForm: NgForm, parentFormGroup: FormGroupDirective, defaultErrorStateMatcher: ErrorStateMatcher, inputValueAccessor: any, _autofillMonitor: AutofillMonitor, ngZone: NgZone, _formField?: MatFormField | undefined);
    ngAfterViewInit(): void;
    ngOnChanges(): void;
    ngOnDestroy(): void;
    ngDoCheck(): void;
    /** Focuses the input. */
    focus(options?: FocusOptions): void;
    /** Refreshes the error state of the input. */
    updateErrorState(): void;
    /** Callback for the cases where the focused state of the input changes. */
    _focusChanged(isFocused: boolean): void;
    _onInput(): void;
    /** Does some manual dirty checking on the native input `value` property. */
    protected _dirtyCheckNativeValue(): void;
    /** Does some manual dirty checking on the native input `placeholder` attribute. */
    private _dirtyCheckPlaceholder;
    /** Gets the current placeholder of the form field. */
    protected _getPlaceholder(): string | null;
    /** Make sure the input is a supported type. */
    protected _validateType(): void;
    /** Checks whether the input type is one of the types that are never empty. */
    protected _isNeverEmpty(): boolean;
    /** Checks whether the input is invalid based on the native validation. */
    protected _isBadInput(): boolean;
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    get empty(): boolean;
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    get shouldLabelFloat(): boolean;
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    setDescribedByIds(ids: string[]): void;
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    onContainerClick(): void;
    /** Whether the form control is a native select that is displayed inline. */
    _isInlineSelect(): boolean;
    private _iOSKeyupListener;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatInput, [null, null, { optional: true; self: true; }, { optional: true; }, { optional: true; }, null, { optional: true; self: true; }, null, null, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatInput, "input[matInput], textarea[matInput], select[matNativeControl],      input[matNativeControl], textarea[matNativeControl]", ["matInput"], { "disabled": { "alias": "disabled"; "required": false; }; "id": { "alias": "id"; "required": false; }; "placeholder": { "alias": "placeholder"; "required": false; }; "name": { "alias": "name"; "required": false; }; "required": { "alias": "required"; "required": false; }; "type": { "alias": "type"; "required": false; }; "errorStateMatcher": { "alias": "errorStateMatcher"; "required": false; }; "userAriaDescribedBy": { "alias": "aria-describedby"; "required": false; }; "value": { "alias": "value"; "required": false; }; "readonly": { "alias": "readonly"; "required": false; }; }, {}, never, never, true, never>;
}

export declare class MatInputModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatInputModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatInputModule, never, [typeof i1.MatCommonModule, typeof i2.MatFormFieldModule, typeof i3.MatInput], [typeof i3.MatInput, typeof i2.MatFormFieldModule, typeof i4.TextFieldModule, typeof i1.MatCommonModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatInputModule>;
}

export { MatLabel }

export { MatPrefix }

export { MatSuffix }

export { }
