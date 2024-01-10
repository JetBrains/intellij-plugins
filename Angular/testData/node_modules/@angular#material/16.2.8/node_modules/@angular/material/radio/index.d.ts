import { _AbstractConstructor } from '@angular/material/core';
import { AfterContentInit } from '@angular/core';
import { AfterViewInit } from '@angular/core';
import { BooleanInput } from '@angular/cdk/coercion';
import { CanDisableRipple } from '@angular/material/core';
import { ChangeDetectorRef } from '@angular/core';
import { _Constructor } from '@angular/material/core';
import { ControlValueAccessor } from '@angular/forms';
import { DoCheck } from '@angular/core';
import { ElementRef } from '@angular/core';
import { EventEmitter } from '@angular/core';
import { FocusMonitor } from '@angular/cdk/a11y';
import { FocusOrigin } from '@angular/cdk/a11y';
import { HasTabIndex } from '@angular/material/core';
import * as i0 from '@angular/core';
import * as i2 from '@angular/material/core';
import * as i3 from '@angular/common';
import { InjectionToken } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { OnInit } from '@angular/core';
import { QueryList } from '@angular/core';
import { ThemePalette } from '@angular/material/core';
import { UniqueSelectionDispatcher } from '@angular/cdk/collections';

declare namespace i1 {
    export {
        MAT_RADIO_DEFAULT_OPTIONS_FACTORY,
        MatRadioChange,
        MAT_RADIO_GROUP_CONTROL_VALUE_ACCESSOR,
        MAT_RADIO_GROUP,
        MatRadioDefaultOptions,
        MAT_RADIO_DEFAULT_OPTIONS,
        _MatRadioGroupBase,
        _MatRadioButtonBase,
        MatRadioGroup,
        MatRadioButton
    }
}

export declare const MAT_RADIO_DEFAULT_OPTIONS: InjectionToken<MatRadioDefaultOptions>;

export declare function MAT_RADIO_DEFAULT_OPTIONS_FACTORY(): MatRadioDefaultOptions;

/**
 * Injection token that can be used to inject instances of `MatRadioGroup`. It serves as
 * alternative token to the actual `MatRadioGroup` class which could cause unnecessary
 * retention of the class and its component metadata.
 */
export declare const MAT_RADIO_GROUP: InjectionToken<_MatRadioGroupBase<_MatRadioButtonBase>>;

/**
 * Provider Expression that allows mat-radio-group to register as a ControlValueAccessor. This
 * allows it to support [(ngModel)] and ngControl.
 * @docs-private
 */
export declare const MAT_RADIO_GROUP_CONTROL_VALUE_ACCESSOR: any;

export declare class MatRadioButton extends _MatRadioButtonBase {
    constructor(radioGroup: MatRadioGroup, elementRef: ElementRef, _changeDetector: ChangeDetectorRef, _focusMonitor: FocusMonitor, _radioDispatcher: UniqueSelectionDispatcher, animationMode?: string, _providerOverride?: MatRadioDefaultOptions, tabIndex?: string);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatRadioButton, [{ optional: true; }, null, null, null, null, { optional: true; }, { optional: true; }, { attribute: "tabindex"; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatRadioButton, "mat-radio-button", ["matRadioButton"], { "disableRipple": { "alias": "disableRipple"; "required": false; }; "tabIndex": { "alias": "tabIndex"; "required": false; }; }, {}, never, ["*"], false, never>;
}

/** @docs-private */
declare abstract class MatRadioButtonBase {
    _elementRef: ElementRef;
    abstract disabled: boolean;
    constructor(_elementRef: ElementRef);
}

/**
 * Base class with all of the `MatRadioButton` functionality.
 * @docs-private
 */
export declare abstract class _MatRadioButtonBase extends _MatRadioButtonMixinBase implements OnInit, AfterViewInit, DoCheck, OnDestroy, CanDisableRipple, HasTabIndex {
    protected _changeDetector: ChangeDetectorRef;
    private _focusMonitor;
    private _radioDispatcher;
    private _providerOverride?;
    private _uniqueId;
    /** The unique ID for the radio button. */
    id: string;
    /** Analog to HTML 'name' attribute used to group radios for unique selection. */
    name: string;
    /** Used to set the 'aria-label' attribute on the underlying input element. */
    ariaLabel: string;
    /** The 'aria-labelledby' attribute takes precedence as the element's text alternative. */
    ariaLabelledby: string;
    /** The 'aria-describedby' attribute is read after the element's label and field type. */
    ariaDescribedby: string;
    /** Whether this radio button is checked. */
    get checked(): boolean;
    set checked(value: BooleanInput);
    /** The value of this radio button. */
    get value(): any;
    set value(value: any);
    /** Whether the label should appear after or before the radio button. Defaults to 'after' */
    get labelPosition(): 'before' | 'after';
    set labelPosition(value: 'before' | 'after');
    private _labelPosition;
    /** Whether the radio button is disabled. */
    get disabled(): boolean;
    set disabled(value: BooleanInput);
    /** Whether the radio button is required. */
    get required(): boolean;
    set required(value: BooleanInput);
    /** Theme color of the radio button. */
    get color(): ThemePalette;
    set color(newValue: ThemePalette);
    private _color;
    /**
     * Event emitted when the checked state of this radio button changes.
     * Change events are only emitted when the value changes due to user interaction with
     * the radio button (the same behavior as `<input type-"radio">`).
     */
    readonly change: EventEmitter<MatRadioChange>;
    /** The parent radio group. May or may not be present. */
    radioGroup: _MatRadioGroupBase<_MatRadioButtonBase>;
    /** ID of the native input element inside `<mat-radio-button>` */
    get inputId(): string;
    /** Whether this radio is checked. */
    private _checked;
    /** Whether this radio is disabled. */
    private _disabled;
    /** Whether this radio is required. */
    private _required;
    /** Value assigned to this radio. */
    private _value;
    /** Unregister function for _radioDispatcher */
    private _removeUniqueSelectionListener;
    /** Previous value of the input's tabindex. */
    private _previousTabIndex;
    /** The native `<input type=radio>` element */
    _inputElement: ElementRef<HTMLInputElement>;
    /** Whether animations are disabled. */
    _noopAnimations: boolean;
    constructor(radioGroup: _MatRadioGroupBase<_MatRadioButtonBase>, elementRef: ElementRef, _changeDetector: ChangeDetectorRef, _focusMonitor: FocusMonitor, _radioDispatcher: UniqueSelectionDispatcher, animationMode?: string, _providerOverride?: MatRadioDefaultOptions | undefined, tabIndex?: string);
    /** Focuses the radio button. */
    focus(options?: FocusOptions, origin?: FocusOrigin): void;
    /**
     * Marks the radio button as needing checking for change detection.
     * This method is exposed because the parent radio group will directly
     * update bound properties of the radio button.
     */
    _markForCheck(): void;
    ngOnInit(): void;
    ngDoCheck(): void;
    ngAfterViewInit(): void;
    ngOnDestroy(): void;
    /** Dispatch change event with current value. */
    private _emitChangeEvent;
    _isRippleDisabled(): boolean;
    _onInputClick(event: Event): void;
    /** Triggered when the radio button receives an interaction from the user. */
    _onInputInteraction(event: Event): void;
    /** Triggered when the user clicks on the touch target. */
    _onTouchTargetClick(event: Event): void;
    /** Sets the disabled state and marks for check if a change occurred. */
    protected _setDisabled(value: boolean): void;
    /** Gets the tabindex for the underlying input element. */
    private _updateTabIndex;
    static ɵfac: i0.ɵɵFactoryDeclaration<_MatRadioButtonBase, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<_MatRadioButtonBase, never, never, { "id": { "alias": "id"; "required": false; }; "name": { "alias": "name"; "required": false; }; "ariaLabel": { "alias": "aria-label"; "required": false; }; "ariaLabelledby": { "alias": "aria-labelledby"; "required": false; }; "ariaDescribedby": { "alias": "aria-describedby"; "required": false; }; "checked": { "alias": "checked"; "required": false; }; "value": { "alias": "value"; "required": false; }; "labelPosition": { "alias": "labelPosition"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; "required": { "alias": "required"; "required": false; }; "color": { "alias": "color"; "required": false; }; }, { "change": "change"; }, never, never, false, never>;
}

declare const _MatRadioButtonMixinBase: _Constructor<CanDisableRipple> & _AbstractConstructor<CanDisableRipple> & _Constructor<HasTabIndex> & _AbstractConstructor<HasTabIndex> & typeof MatRadioButtonBase;

/** Change event object emitted by radio button and radio group. */
export declare class MatRadioChange {
    /** The radio button that emits the change event. */
    source: _MatRadioButtonBase;
    /** The value of the radio button. */
    value: any;
    constructor(
    /** The radio button that emits the change event. */
    source: _MatRadioButtonBase, 
    /** The value of the radio button. */
    value: any);
}

export declare interface MatRadioDefaultOptions {
    color: ThemePalette;
}

/**
 * A group of radio buttons. May contain one or more `<mat-radio-button>` elements.
 */
export declare class MatRadioGroup extends _MatRadioGroupBase<MatRadioButton> {
    /** Child radio buttons. */
    _radios: QueryList<MatRadioButton>;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatRadioGroup, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatRadioGroup, "mat-radio-group", ["matRadioGroup"], {}, {}, ["_radios"], never, false, never>;
}

/**
 * Base class with all of the `MatRadioGroup` functionality.
 * @docs-private
 */
export declare abstract class _MatRadioGroupBase<T extends _MatRadioButtonBase> implements AfterContentInit, OnDestroy, ControlValueAccessor {
    private _changeDetector;
    /** Selected value for the radio group. */
    private _value;
    /** The HTML name attribute applied to radio buttons in this group. */
    private _name;
    /** The currently selected radio button. Should match value. */
    private _selected;
    /** Whether the `value` has been set to its initial value. */
    private _isInitialized;
    /** Whether the labels should appear after or before the radio-buttons. Defaults to 'after' */
    private _labelPosition;
    /** Whether the radio group is disabled. */
    private _disabled;
    /** Whether the radio group is required. */
    private _required;
    /** Subscription to changes in amount of radio buttons. */
    private _buttonChanges;
    /** The method to be called in order to update ngModel */
    _controlValueAccessorChangeFn: (value: any) => void;
    /**
     * onTouch function registered via registerOnTouch (ControlValueAccessor).
     * @docs-private
     */
    onTouched: () => any;
    /**
     * Event emitted when the group value changes.
     * Change events are only emitted when the value changes due to user interaction with
     * a radio button (the same behavior as `<input type-"radio">`).
     */
    readonly change: EventEmitter<MatRadioChange>;
    /** Child radio buttons. */
    abstract _radios: QueryList<T>;
    /** Theme color for all of the radio buttons in the group. */
    color: ThemePalette;
    /** Name of the radio button group. All radio buttons inside this group will use this name. */
    get name(): string;
    set name(value: string);
    /** Whether the labels should appear after or before the radio-buttons. Defaults to 'after' */
    get labelPosition(): 'before' | 'after';
    set labelPosition(v: 'before' | 'after');
    /**
     * Value for the radio-group. Should equal the value of the selected radio button if there is
     * a corresponding radio button with a matching value. If there is not such a corresponding
     * radio button, this value persists to be applied in case a new radio button is added with a
     * matching value.
     */
    get value(): any;
    set value(newValue: any);
    _checkSelectedRadioButton(): void;
    /**
     * The currently selected radio button. If set to a new radio button, the radio group value
     * will be updated to match the new selected button.
     */
    get selected(): T | null;
    set selected(selected: T | null);
    /** Whether the radio group is disabled */
    get disabled(): boolean;
    set disabled(value: BooleanInput);
    /** Whether the radio group is required */
    get required(): boolean;
    set required(value: BooleanInput);
    constructor(_changeDetector: ChangeDetectorRef);
    /**
     * Initialize properties once content children are available.
     * This allows us to propagate relevant attributes to associated buttons.
     */
    ngAfterContentInit(): void;
    ngOnDestroy(): void;
    /**
     * Mark this group as being "touched" (for ngModel). Meant to be called by the contained
     * radio buttons upon their blur.
     */
    _touch(): void;
    private _updateRadioButtonNames;
    /** Updates the `selected` radio button from the internal _value state. */
    private _updateSelectedRadioFromValue;
    /** Dispatch change event with current selection and group value. */
    _emitChangeEvent(): void;
    _markRadiosForCheck(): void;
    /**
     * Sets the model value. Implemented as part of ControlValueAccessor.
     * @param value
     */
    writeValue(value: any): void;
    /**
     * Registers a callback to be triggered when the model value changes.
     * Implemented as part of ControlValueAccessor.
     * @param fn Callback to be registered.
     */
    registerOnChange(fn: (value: any) => void): void;
    /**
     * Registers a callback to be triggered when the control is touched.
     * Implemented as part of ControlValueAccessor.
     * @param fn Callback to be registered.
     */
    registerOnTouched(fn: any): void;
    /**
     * Sets the disabled state of the control. Implemented as a part of ControlValueAccessor.
     * @param isDisabled Whether the control should be disabled.
     */
    setDisabledState(isDisabled: boolean): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<_MatRadioGroupBase<any>, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<_MatRadioGroupBase<any>, never, never, { "color": { "alias": "color"; "required": false; }; "name": { "alias": "name"; "required": false; }; "labelPosition": { "alias": "labelPosition"; "required": false; }; "value": { "alias": "value"; "required": false; }; "selected": { "alias": "selected"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; "required": { "alias": "required"; "required": false; }; }, { "change": "change"; }, never, never, false, never>;
}

export declare class MatRadioModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatRadioModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatRadioModule, [typeof i1.MatRadioGroup, typeof i1.MatRadioButton], [typeof i2.MatCommonModule, typeof i3.CommonModule, typeof i2.MatRippleModule], [typeof i2.MatCommonModule, typeof i1.MatRadioGroup, typeof i1.MatRadioButton]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatRadioModule>;
}

export { }
