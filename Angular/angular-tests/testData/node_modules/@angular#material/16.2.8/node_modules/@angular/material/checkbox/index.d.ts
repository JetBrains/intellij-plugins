import { _AbstractConstructor } from '@angular/material/core';
import { AfterViewInit } from '@angular/core';
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
import { FocusableOption } from '@angular/cdk/a11y';
import { FocusOrigin } from '@angular/cdk/a11y';
import { HasTabIndex } from '@angular/material/core';
import * as i0 from '@angular/core';
import * as i3 from '@angular/material/core';
import { InjectionToken } from '@angular/core';
import { MatRipple } from '@angular/material/core';
import { NgZone } from '@angular/core';
import { Provider } from '@angular/core';
import { ThemePalette } from '@angular/material/core';

declare namespace i1 {
    export {
        MAT_CHECKBOX_REQUIRED_VALIDATOR,
        MatCheckboxRequiredValidator
    }
}

declare namespace i2 {
    export {
        TransitionCheckState,
        MAT_CHECKBOX_CONTROL_VALUE_ACCESSOR,
        MatCheckboxChange,
        _MatCheckboxBase,
        MatCheckbox
    }
}

export declare const MAT_CHECKBOX_CONTROL_VALUE_ACCESSOR: any;

/** Injection token to be used to override the default options for `mat-checkbox`. */
export declare const MAT_CHECKBOX_DEFAULT_OPTIONS: InjectionToken<MatCheckboxDefaultOptions>;

/** @docs-private */
export declare function MAT_CHECKBOX_DEFAULT_OPTIONS_FACTORY(): MatCheckboxDefaultOptions;

export declare const MAT_CHECKBOX_REQUIRED_VALIDATOR: Provider;

export declare class MatCheckbox extends _MatCheckboxBase<MatCheckboxChange> implements ControlValueAccessor, CanColor, CanDisable {
    protected _animationClasses: {
        uncheckedToChecked: string;
        uncheckedToIndeterminate: string;
        checkedToUnchecked: string;
        checkedToIndeterminate: string;
        indeterminateToChecked: string;
        indeterminateToUnchecked: string;
    };
    constructor(elementRef: ElementRef<HTMLElement>, changeDetectorRef: ChangeDetectorRef, ngZone: NgZone, tabIndex: string, animationMode?: string, options?: MatCheckboxDefaultOptions);
    /** Focuses the checkbox. */
    focus(): void;
    protected _createChangeEvent(isChecked: boolean): MatCheckboxChange;
    protected _getAnimationTargetElement(): HTMLInputElement;
    _onInputClick(): void;
    _onTouchTargetClick(): void;
    /**
     *  Prevent click events that come from the `<label/>` element from bubbling. This prevents the
     *  click handler on the host from triggering twice when clicking on the `<label/>` element. After
     *  the click event on the `<label/>` propagates, the browsers dispatches click on the associated
     *  `<input/>`. By preventing clicks on the label by bubbling, we ensure only one click event
     *  bubbles when the label is clicked.
     */
    _preventBubblingFromLabel(event: MouseEvent): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatCheckbox, [null, null, null, { attribute: "tabindex"; }, { optional: true; }, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatCheckbox, "mat-checkbox", ["matCheckbox"], { "disableRipple": { "alias": "disableRipple"; "required": false; }; "color": { "alias": "color"; "required": false; }; "tabIndex": { "alias": "tabIndex"; "required": false; }; }, {}, never, ["*"], false, never>;
}

export declare abstract class _MatCheckboxBase<E> extends _MatCheckboxMixinBase implements AfterViewInit, ControlValueAccessor, CanColor, CanDisable, HasTabIndex, CanDisableRipple, FocusableOption {
    protected _changeDetectorRef: ChangeDetectorRef;
    protected _ngZone: NgZone;
    _animationMode?: string | undefined;
    protected _options?: MatCheckboxDefaultOptions | undefined;
    /** Focuses the checkbox. */
    abstract focus(origin?: FocusOrigin): void;
    /** Creates the change event that will be emitted by the checkbox. */
    protected abstract _createChangeEvent(isChecked: boolean): E;
    /** Gets the element on which to add the animation CSS classes. */
    protected abstract _getAnimationTargetElement(): HTMLElement | null;
    /** CSS classes to add when transitioning between the different checkbox states. */
    protected abstract _animationClasses: {
        uncheckedToChecked: string;
        uncheckedToIndeterminate: string;
        checkedToUnchecked: string;
        checkedToIndeterminate: string;
        indeterminateToChecked: string;
        indeterminateToUnchecked: string;
    };
    /**
     * Attached to the aria-label attribute of the host element. In most cases, aria-labelledby will
     * take precedence so this may be omitted.
     */
    ariaLabel: string;
    /**
     * Users can specify the `aria-labelledby` attribute which will be forwarded to the input element
     */
    ariaLabelledby: string | null;
    /** The 'aria-describedby' attribute is read after the element's label and field type. */
    ariaDescribedby: string;
    private _uniqueId;
    /** A unique id for the checkbox input. If none is supplied, it will be auto-generated. */
    id: string;
    /** Returns the unique id for the visual hidden input. */
    get inputId(): string;
    /** Whether the checkbox is required. */
    get required(): boolean;
    set required(value: BooleanInput);
    private _required;
    /** Whether the label should appear after or before the checkbox. Defaults to 'after' */
    labelPosition: 'before' | 'after';
    /** Name value will be applied to the input element if present */
    name: string | null;
    /** Event emitted when the checkbox's `checked` value changes. */
    readonly change: EventEmitter<E>;
    /** Event emitted when the checkbox's `indeterminate` value changes. */
    readonly indeterminateChange: EventEmitter<boolean>;
    /** The value attribute of the native input element */
    value: string;
    /** The native `<input type="checkbox">` element */
    _inputElement: ElementRef<HTMLInputElement>;
    /** The native `<label>` element */
    _labelElement: ElementRef<HTMLInputElement>;
    /**
     * Reference to the MatRipple instance of the checkbox.
     * @deprecated Considered an implementation detail. To be removed.
     * @breaking-change 17.0.0
     */
    ripple: MatRipple;
    /**
     * Called when the checkbox is blurred. Needed to properly implement ControlValueAccessor.
     * @docs-private
     */
    _onTouched: () => any;
    private _currentAnimationClass;
    private _currentCheckState;
    private _controlValueAccessorChangeFn;
    constructor(idPrefix: string, elementRef: ElementRef<HTMLElement>, _changeDetectorRef: ChangeDetectorRef, _ngZone: NgZone, tabIndex: string, _animationMode?: string | undefined, _options?: MatCheckboxDefaultOptions | undefined);
    ngAfterViewInit(): void;
    /** Whether the checkbox is checked. */
    get checked(): boolean;
    set checked(value: BooleanInput);
    private _checked;
    /**
     * Whether the checkbox is disabled. This fully overrides the implementation provided by
     * mixinDisabled, but the mixin is still required because mixinTabIndex requires it.
     */
    get disabled(): boolean;
    set disabled(value: BooleanInput);
    private _disabled;
    /**
     * Whether the checkbox is indeterminate. This is also known as "mixed" mode and can be used to
     * represent a checkbox with three states, e.g. a checkbox that represents a nested list of
     * checkable items. Note that whenever checkbox is manually clicked, indeterminate is immediately
     * set to false.
     */
    get indeterminate(): boolean;
    set indeterminate(value: BooleanInput);
    private _indeterminate;
    _isRippleDisabled(): boolean;
    /** Method being called whenever the label text changes. */
    _onLabelTextChange(): void;
    writeValue(value: any): void;
    registerOnChange(fn: (value: any) => void): void;
    registerOnTouched(fn: any): void;
    setDisabledState(isDisabled: boolean): void;
    private _transitionCheckState;
    private _emitChangeEvent;
    /** Toggles the `checked` state of the checkbox. */
    toggle(): void;
    protected _handleInputClick(): void;
    _onInteractionEvent(event: Event): void;
    _onBlur(): void;
    private _getAnimationClassForCheckStateTransition;
    /**
     * Syncs the indeterminate value with the checkbox DOM node.
     *
     * We sync `indeterminate` directly on the DOM node, because in Ivy the check for whether a
     * property is supported on an element boils down to `if (propName in element)`. Domino's
     * HTMLInputElement doesn't have an `indeterminate` property so Ivy will warn during
     * server-side rendering.
     */
    private _syncIndeterminate;
    static ɵfac: i0.ɵɵFactoryDeclaration<_MatCheckboxBase<any>, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<_MatCheckboxBase<any>, never, never, { "ariaLabel": { "alias": "aria-label"; "required": false; }; "ariaLabelledby": { "alias": "aria-labelledby"; "required": false; }; "ariaDescribedby": { "alias": "aria-describedby"; "required": false; }; "id": { "alias": "id"; "required": false; }; "required": { "alias": "required"; "required": false; }; "labelPosition": { "alias": "labelPosition"; "required": false; }; "name": { "alias": "name"; "required": false; }; "value": { "alias": "value"; "required": false; }; "checked": { "alias": "checked"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; "indeterminate": { "alias": "indeterminate"; "required": false; }; }, { "change": "change"; "indeterminateChange": "indeterminateChange"; }, never, never, false, never>;
}

/** Change event object emitted by checkbox. */
export declare class MatCheckboxChange {
    /** The source checkbox of the event. */
    source: MatCheckbox;
    /** The new `checked` value of the checkbox. */
    checked: boolean;
}

/**
 * Checkbox click action when user click on input element.
 * noop: Do not toggle checked or indeterminate.
 * check: Only toggle checked status, ignore indeterminate.
 * check-indeterminate: Toggle checked status, set indeterminate to false. Default behavior.
 * undefined: Same as `check-indeterminate`.
 */
export declare type MatCheckboxClickAction = 'noop' | 'check' | 'check-indeterminate' | undefined;

/** Default `mat-checkbox` options that can be overridden. */
export declare interface MatCheckboxDefaultOptions {
    /** Default theme color palette to be used for checkboxes. */
    color?: ThemePalette;
    /** Default checkbox click action for checkboxes. */
    clickAction?: MatCheckboxClickAction;
}

/** @docs-private */
declare const _MatCheckboxMixinBase: _Constructor<HasTabIndex> & _AbstractConstructor<HasTabIndex> & _Constructor<CanColor> & _AbstractConstructor<CanColor> & _Constructor<CanDisableRipple> & _AbstractConstructor<CanDisableRipple> & _Constructor<CanDisable> & _AbstractConstructor<CanDisable> & {
    new (_elementRef: ElementRef): {
        _elementRef: ElementRef;
    };
};

export declare class MatCheckboxModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatCheckboxModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatCheckboxModule, [typeof i2.MatCheckbox], [typeof i3.MatCommonModule, typeof i3.MatRippleModule, typeof _MatCheckboxRequiredValidatorModule], [typeof i2.MatCheckbox, typeof i3.MatCommonModule, typeof _MatCheckboxRequiredValidatorModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatCheckboxModule>;
}

/**
 * Validator for Material checkbox's required attribute in template-driven checkbox.
 * Current CheckboxRequiredValidator only work with `input type=checkbox` and does not
 * work with `mat-checkbox`.
 */
export declare class MatCheckboxRequiredValidator extends CheckboxRequiredValidator {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatCheckboxRequiredValidator, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatCheckboxRequiredValidator, "mat-checkbox[required][formControlName],             mat-checkbox[required][formControl], mat-checkbox[required][ngModel]", never, {}, {}, never, never, false, never>;
}

/** This module is used by both original and MDC-based checkbox implementations. */
export declare class _MatCheckboxRequiredValidatorModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<_MatCheckboxRequiredValidatorModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<_MatCheckboxRequiredValidatorModule, [typeof i1.MatCheckboxRequiredValidator], never, [typeof i1.MatCheckboxRequiredValidator]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<_MatCheckboxRequiredValidatorModule>;
}

/**
 * Represents the different states that require custom transitions between them.
 * @docs-private
 */
export declare const enum TransitionCheckState {
    /** The initial state of the component before any user interaction. */
    Init = 0,
    /** The state representing the component when it's becoming checked. */
    Checked = 1,
    /** The state representing the component when it's becoming unchecked. */
    Unchecked = 2,
    /** The state representing the component when it's becoming indeterminate. */
    Indeterminate = 3
}

export { }
