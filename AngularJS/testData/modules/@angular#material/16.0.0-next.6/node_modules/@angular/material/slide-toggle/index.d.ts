import { _AbstractConstructor } from '@angular/material/core';
import { AfterContentInit } from '@angular/core';
import { BooleanInput } from '@angular/cdk/coercion';
import { CanColor } from '@angular/material/core';
import { CanDisable } from '@angular/material/core';
import { CanDisableRipple } from '@angular/material/core';
import { ChangeDetectorRef } from '@angular/core';
import { CheckboxRequiredValidator } from '@angular/forms';
import { _Constructor } from '@angular/material/core';
import { ControlValueAccessor } from '@angular/forms';
import { ElementRef } from '@angular/core';
import { EventEmitter } from '@angular/core';
import { FocusMonitor } from '@angular/cdk/a11y';
import { FocusOrigin } from '@angular/cdk/a11y';
import { HasTabIndex } from '@angular/material/core';
import * as i0 from '@angular/core';
import * as i3 from '@angular/material/core';
import * as i4 from '@angular/common';
import { InjectionToken } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { Provider } from '@angular/core';
import { ThemePalette } from '@angular/material/core';
import { Type } from '@angular/core';

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
        _MatSlideToggleBase,
        MatSlideToggle
    }
}

/** Injection token to be used to override the default options for `mat-slide-toggle`. */
export declare const MAT_SLIDE_TOGGLE_DEFAULT_OPTIONS: InjectionToken<MatSlideToggleDefaultOptions>;

export declare const MAT_SLIDE_TOGGLE_REQUIRED_VALIDATOR: Provider;

/** @docs-private */
export declare const MAT_SLIDE_TOGGLE_VALUE_ACCESSOR: {
    provide: InjectionToken<readonly ControlValueAccessor[]>;
    useExisting: Type<any>;
    multi: boolean;
};

export declare class MatSlideToggle extends _MatSlideToggleBase<MatSlideToggleChange> {
    /** Unique ID for the label element. */
    _labelId: string;
    /** Returns the unique id for the visual hidden button. */
    get buttonId(): string;
    /** Reference to the MDC switch element. */
    _switchElement: ElementRef<HTMLElement>;
    constructor(elementRef: ElementRef, focusMonitor: FocusMonitor, changeDetectorRef: ChangeDetectorRef, tabIndex: string, defaults: MatSlideToggleDefaultOptions, animationMode?: string);
    /** Method being called whenever the underlying button is clicked. */
    _handleClick(): void;
    /** Focuses the slide-toggle. */
    focus(): void;
    protected _createChangeEvent(isChecked: boolean): MatSlideToggleChange;
    _getAriaLabelledBy(): string | null;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatSlideToggle, [null, null, null, { attribute: "tabindex"; }, null, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatSlideToggle, "mat-slide-toggle", ["matSlideToggle"], { "disabled": "disabled"; "disableRipple": "disableRipple"; "color": "color"; "tabIndex": "tabIndex"; }, {}, never, ["*"], false, never>;
}

export declare abstract class _MatSlideToggleBase<T> extends _MatSlideToggleMixinBase implements OnDestroy, AfterContentInit, ControlValueAccessor, CanDisable, CanColor, HasTabIndex, CanDisableRipple {
    protected _focusMonitor: FocusMonitor;
    protected _changeDetectorRef: ChangeDetectorRef;
    defaults: MatSlideToggleDefaultOptions;
    protected _onChange: (_: any) => void;
    private _onTouched;
    protected _uniqueId: string;
    private _required;
    private _checked;
    protected abstract _createChangeEvent(isChecked: boolean): T;
    abstract focus(options?: FocusOptions, origin?: FocusOrigin): void;
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
    get required(): boolean;
    set required(value: BooleanInput);
    /** Whether the slide-toggle element is checked or not. */
    get checked(): boolean;
    set checked(value: BooleanInput);
    /** An event will be dispatched each time the slide-toggle changes its value. */
    readonly change: EventEmitter<T>;
    /**
     * An event will be dispatched each time the slide-toggle input is toggled.
     * This event is always emitted when the user toggles the slide toggle, but this does not mean
     * the slide toggle's value has changed.
     */
    readonly toggleChange: EventEmitter<void>;
    /** Returns the unique id for the visual hidden input. */
    get inputId(): string;
    constructor(elementRef: ElementRef, _focusMonitor: FocusMonitor, _changeDetectorRef: ChangeDetectorRef, tabIndex: string, defaults: MatSlideToggleDefaultOptions, animationMode: string | undefined, idPrefix: string);
    ngAfterContentInit(): void;
    ngOnDestroy(): void;
    /** Implemented as part of ControlValueAccessor. */
    writeValue(value: any): void;
    /** Implemented as part of ControlValueAccessor. */
    registerOnChange(fn: any): void;
    /** Implemented as part of ControlValueAccessor. */
    registerOnTouched(fn: any): void;
    /** Implemented as a part of ControlValueAccessor. */
    setDisabledState(isDisabled: boolean): void;
    /** Toggles the checked state of the slide-toggle. */
    toggle(): void;
    /**
     * Emits a change event on the `change` output. Also notifies the FormControl about the change.
     */
    protected _emitChangeEvent(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<_MatSlideToggleBase<any>, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<_MatSlideToggleBase<any>, never, never, { "name": "name"; "id": "id"; "labelPosition": "labelPosition"; "ariaLabel": "aria-label"; "ariaLabelledby": "aria-labelledby"; "ariaDescribedby": "aria-describedby"; "required": "required"; "checked": "checked"; }, { "change": "change"; "toggleChange": "toggleChange"; }, never, never, false, never>;
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
}

/** @docs-private */
declare const _MatSlideToggleMixinBase: _Constructor<HasTabIndex> & _AbstractConstructor<HasTabIndex> & _Constructor<CanColor> & _AbstractConstructor<CanColor> & _Constructor<CanDisableRipple> & _AbstractConstructor<CanDisableRipple> & _Constructor<CanDisable> & _AbstractConstructor<CanDisable> & {
    new (_elementRef: ElementRef): {
        _elementRef: ElementRef;
    };
};

export declare class MatSlideToggleModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatSlideToggleModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatSlideToggleModule, [typeof i2.MatSlideToggle], [typeof _MatSlideToggleRequiredValidatorModule, typeof i3.MatCommonModule, typeof i3.MatRippleModule, typeof i4.CommonModule], [typeof _MatSlideToggleRequiredValidatorModule, typeof i2.MatSlideToggle, typeof i3.MatCommonModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatSlideToggleModule>;
}

/**
 * Validator for Material slide-toggle components with the required attribute in a
 * template-driven form. The default validator for required form controls asserts
 * that the control value is not undefined but that is not appropriate for a slide-toggle
 * where the value is always defined.
 *
 * Required slide-toggle form controls are valid when checked.
 */
export declare class MatSlideToggleRequiredValidator extends CheckboxRequiredValidator {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatSlideToggleRequiredValidator, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatSlideToggleRequiredValidator, "mat-slide-toggle[required][formControlName],             mat-slide-toggle[required][formControl], mat-slide-toggle[required][ngModel]", never, {}, {}, never, never, false, never>;
}

/** This module is used by both original and MDC-based slide-toggle implementations. */
export declare class _MatSlideToggleRequiredValidatorModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<_MatSlideToggleRequiredValidatorModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<_MatSlideToggleRequiredValidatorModule, [typeof i1.MatSlideToggleRequiredValidator], never, [typeof i1.MatSlideToggleRequiredValidator]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<_MatSlideToggleRequiredValidatorModule>;
}

export { }
