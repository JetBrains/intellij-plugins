import { AbstractControl } from '@angular/forms';
import { AfterContentInit } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';
import { CheckboxRequiredValidator } from '@angular/forms';
import { ControlValueAccessor } from '@angular/forms';
import { ElementRef } from '@angular/core';
import { EventEmitter } from '@angular/core';
import { FocusMonitor } from '@angular/cdk/a11y';
import * as i0 from '@angular/core';
import * as i3 from '@angular/material/core';
import { InjectionToken } from '@angular/core';
import { OnChanges } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { Provider } from '@angular/core';
import { SimpleChanges } from '@angular/core';
import { ThemePalette } from '@angular/material/core';
import { Type } from '@angular/core';
import { ValidationErrors } from '@angular/forms';
import { Validator } from '@angular/forms';

declare namespace i1 {
    export {
        MAT_SLIDE_TOGGLE_REQUIRED_VALIDATOR,
        MatSlideToggleRequiredValidator
    }
}

declare namespace i2 {
    export {
        MAT_SLIDE_TOGGLE_VALUE_ACCESSOR,
        MatSlideToggleChange,
        MatSlideToggle
    }
}

/** Injection token to be used to override the default options for `mat-slide-toggle`. */
export declare const MAT_SLIDE_TOGGLE_DEFAULT_OPTIONS: InjectionToken<MatSlideToggleDefaultOptions>;

/**
 * @deprecated No longer used, `MatCheckbox` implements required validation directly.
 * @breaking-change 19.0.0
 */
export declare const MAT_SLIDE_TOGGLE_REQUIRED_VALIDATOR: Provider;

/**
 * @deprecated Will stop being exported.
 * @breaking-change 19.0.0
 */
export declare const MAT_SLIDE_TOGGLE_VALUE_ACCESSOR: {
    provide: InjectionToken<readonly ControlValueAccessor[]>;
    useExisting: Type<any>;
    multi: boolean;
};

export declare class MatSlideToggle implements OnDestroy, AfterContentInit, OnChanges, ControlValueAccessor, Validator {
    private _elementRef;
    protected _focusMonitor: FocusMonitor;
    protected _changeDetectorRef: ChangeDetectorRef;
    defaults: MatSlideToggleDefaultOptions;
    private _onChange;
    private _onTouched;
    private _validatorOnChange;
    private _uniqueId;
    private _checked;
    private _createChangeEvent;
    /** Unique ID for the label element. */
    _labelId: string;
    /** Returns the unique id for the visual hidden button. */
    get buttonId(): string;
    /** Reference to the MDC switch element. */
    _switchElement: ElementRef<HTMLElement>;
    /** Focuses the slide-toggle. */
    focus(): void;
    /** Whether noop animations are enabled. */
    _noopAnimations: boolean;
    /** Whether the slide toggle is currently focused. */
    _focused: boolean;
    /** Name value will be applied to the input element if present. */
    name: string | null;
    /** A unique id for the slide-toggle input. If none is supplied, it will be auto-generated. */
    id: string;
    /** Whether the label should appear after or before the slide-toggle. Defaults to 'after'. */
    labelPosition: 'before' | 'after';
    /** Used to set the aria-label attribute on the underlying input element. */
    ariaLabel: string | null;
    /** Used to set the aria-labelledby attribute on the underlying input element. */
    ariaLabelledby: string | null;
    /** Used to set the aria-describedby attribute on the underlying input element. */
    ariaDescribedby: string;
    /** Whether the slide-toggle is required. */
    required: boolean;
    /** Palette color of slide toggle. */
    color: string | undefined;
    /** Whether the slide toggle is disabled. */
    disabled: boolean;
    /** Whether the slide toggle has a ripple. */
    disableRipple: boolean;
    /** Tabindex of slide toggle. */
    tabIndex: number;
    /** Whether the slide-toggle element is checked or not. */
    get checked(): boolean;
    set checked(value: boolean);
    /** Whether to hide the icon inside of the slide toggle. */
    hideIcon: boolean;
    /** An event will be dispatched each time the slide-toggle changes its value. */
    readonly change: EventEmitter<MatSlideToggleChange>;
    /**
     * An event will be dispatched each time the slide-toggle input is toggled.
     * This event is always emitted when the user toggles the slide toggle, but this does not mean
     * the slide toggle's value has changed.
     */
    readonly toggleChange: EventEmitter<void>;
    /** Returns the unique id for the visual hidden input. */
    get inputId(): string;
    constructor(_elementRef: ElementRef, _focusMonitor: FocusMonitor, _changeDetectorRef: ChangeDetectorRef, tabIndex: string, defaults: MatSlideToggleDefaultOptions, animationMode?: string);
    ngAfterContentInit(): void;
    ngOnChanges(changes: SimpleChanges): void;
    ngOnDestroy(): void;
    /** Implemented as part of ControlValueAccessor. */
    writeValue(value: any): void;
    /** Implemented as part of ControlValueAccessor. */
    registerOnChange(fn: any): void;
    /** Implemented as part of ControlValueAccessor. */
    registerOnTouched(fn: any): void;
    /** Implemented as a part of Validator. */
    validate(control: AbstractControl<boolean>): ValidationErrors | null;
    /** Implemented as a part of Validator. */
    registerOnValidatorChange(fn: () => void): void;
    /** Implemented as a part of ControlValueAccessor. */
    setDisabledState(isDisabled: boolean): void;
    /** Toggles the checked state of the slide-toggle. */
    toggle(): void;
    /**
     * Emits a change event on the `change` output. Also notifies the FormControl about the change.
     */
    protected _emitChangeEvent(): void;
    /** Method being called whenever the underlying button is clicked. */
    _handleClick(): void;
    _getAriaLabelledBy(): string | null;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatSlideToggle, [null, null, null, { attribute: "tabindex"; }, null, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatSlideToggle, "mat-slide-toggle", ["matSlideToggle"], { "name": { "alias": "name"; "required": false; }; "id": { "alias": "id"; "required": false; }; "labelPosition": { "alias": "labelPosition"; "required": false; }; "ariaLabel": { "alias": "aria-label"; "required": false; }; "ariaLabelledby": { "alias": "aria-labelledby"; "required": false; }; "ariaDescribedby": { "alias": "aria-describedby"; "required": false; }; "required": { "alias": "required"; "required": false; }; "color": { "alias": "color"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; "disableRipple": { "alias": "disableRipple"; "required": false; }; "tabIndex": { "alias": "tabIndex"; "required": false; }; "checked": { "alias": "checked"; "required": false; }; "hideIcon": { "alias": "hideIcon"; "required": false; }; }, { "change": "change"; "toggleChange": "toggleChange"; }, never, ["*"], true, never>;
    static ngAcceptInputType_required: unknown;
    static ngAcceptInputType_disabled: unknown;
    static ngAcceptInputType_disableRipple: unknown;
    static ngAcceptInputType_tabIndex: unknown;
    static ngAcceptInputType_checked: unknown;
    static ngAcceptInputType_hideIcon: unknown;
}

/** Change event object emitted by a slide toggle. */
export declare class MatSlideToggleChange {
    /** The source slide toggle of the event. */
    source: MatSlideToggle;
    /** The new `checked` value of the slide toggle. */
    checked: boolean;
    constructor(
    /** The source slide toggle of the event. */
    source: MatSlideToggle, 
    /** The new `checked` value of the slide toggle. */
    checked: boolean);
}

/** Default `mat-slide-toggle` options that can be overridden. */
export declare interface MatSlideToggleDefaultOptions {
    /** Whether toggle action triggers value changes in slide toggle. */
    disableToggleValue?: boolean;
    /** Default color for slide toggles. */
    color?: ThemePalette;
    /** Whether to hide the icon inside the slide toggle. */
    hideIcon?: boolean;
}

export declare class MatSlideToggleModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatSlideToggleModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatSlideToggleModule, never, [typeof i2.MatSlideToggle, typeof i3.MatCommonModule], [typeof i2.MatSlideToggle, typeof i3.MatCommonModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatSlideToggleModule>;
}

/**
 * Validator for Material slide-toggle components with the required attribute in a
 * template-driven form. The default validator for required form controls asserts
 * that the control value is not undefined but that is not appropriate for a slide-toggle
 * where the value is always defined.
 *
 * Required slide-toggle form controls are valid when checked.
 *
 * @deprecated No longer used, `MatCheckbox` implements required validation directly.
 * @breaking-change 19.0.0
 */
export declare class MatSlideToggleRequiredValidator extends CheckboxRequiredValidator {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatSlideToggleRequiredValidator, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatSlideToggleRequiredValidator, "mat-slide-toggle[required][formControlName],             mat-slide-toggle[required][formControl], mat-slide-toggle[required][ngModel]", never, {}, {}, never, never, true, never>;
}

/**
 * @deprecated No longer used, `MatSlideToggle` implements required validation directly.
 * @breaking-change 19.0.0
 */
export declare class _MatSlideToggleRequiredValidatorModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<_MatSlideToggleRequiredValidatorModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<_MatSlideToggleRequiredValidatorModule, never, [typeof i1.MatSlideToggleRequiredValidator], [typeof i1.MatSlideToggleRequiredValidator]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<_MatSlideToggleRequiredValidatorModule>;
}

export { }
