import { AbstractControl, ValidationErrors, Validator } from '@angular/forms';
import { SbbDatepickerElement } from '@sbb-esta/lyne-elements/datepicker/datepicker.js';
import { type Observable } from 'rxjs';
import '@sbb-esta/lyne-elements/date-input.js';
import * as i0 from "@angular/core";
declare const SbbDateInput_base: import("@sbb-esta/lyne-elements/core/mixins.js").AbstractConstructor<import("@sbb-esta/lyne-angular/core").SbbControlValueAccessorMixinType> & {
    new (): {};
};
export declare class SbbDateInput<T = Date> extends SbbDateInput_base implements Validator {
    #private;
    protected validatorOnChange: () => void;
    set value(value: string);
    get value(): string;
    set valueAsDate(value: T | null);
    get valueAsDate(): T | null;
    set min(value: T | null);
    get min(): T | null;
    set max(value: T | null);
    get max(): T | null;
    set dateFilter(value: (date: T | null) => boolean);
    get dateFilter(): (date: T | null) => boolean;
    set weekdayStyle(value: 'short' | 'none');
    get weekdayStyle(): 'short' | 'none';
    set readOnly(value: boolean);
    get readOnly(): boolean;
    set disabled(value: boolean);
    get disabled(): boolean;
    set placeholder(value: string);
    get placeholder(): string;
    set required(value: boolean);
    get required(): boolean;
    set name(value: string);
    get name(): string;
    protected _input: (typeof this)['input'];
    input: Observable<InputEvent>;
    protected _change: (typeof this)['change'];
    change: Observable<Event>;
    get type(): string;
    get form(): HTMLFormElement | null;
    get validity(): ValidityState;
    get validationMessage(): string;
    get willValidate(): boolean;
    /** The form control validator for whether the input parses. */
    private _parseValidator;
    /** The form control validator for the min date. */
    private _minValidator;
    /** The form control validator for the max date. */
    private _maxValidator;
    /** The form control validator for the date filter. */
    private _filterValidator;
    /** The combined form control validator for this input. */
    private _validator;
    writeValue(value: any): void;
    registerOnValidatorChange?(fn: () => void): void;
    validate(control: AbstractControl): ValidationErrors | null;
    focus(options: FocusOptions): void;
    checkValidity(): boolean;
    reportValidity(): boolean;
    setCustomValidity(message: string): void;
    get datepicker(): SbbDatepickerElement<T> | null;
    static ɵfac: i0.ɵɵFactoryDeclaration<SbbDateInput<any>, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<SbbDateInput<any>, "sbb-date-input", ["sbbDateInput"], { "value": { "alias": "value"; "required": false; }; "valueAsDate": { "alias": "valueAsDate"; "required": false; }; "min": { "alias": "min"; "required": false; }; "max": { "alias": "max"; "required": false; }; "dateFilter": { "alias": "dateFilter"; "required": false; }; "weekdayStyle": { "alias": "weekdayStyle"; "required": false; }; "readOnly": { "alias": "readOnly"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; "placeholder": { "alias": "placeholder"; "required": false; }; "required": { "alias": "required"; "required": false; }; "name": { "alias": "name"; "required": false; }; }, { "_input": "input"; "_change": "change"; }, never, never, true, never>;
    static ngAcceptInputType_readOnly: unknown;
    static ngAcceptInputType_disabled: unknown;
    static ngAcceptInputType_required: unknown;
}
export {};
