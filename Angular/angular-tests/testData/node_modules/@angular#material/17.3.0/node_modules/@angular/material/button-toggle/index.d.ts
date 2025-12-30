import { AfterContentInit } from '@angular/core';
import { AfterViewInit } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';
import { ControlValueAccessor } from '@angular/forms';
import { ElementRef } from '@angular/core';
import { EventEmitter } from '@angular/core';
import { FocusMonitor } from '@angular/cdk/a11y';
import * as i0 from '@angular/core';
import * as i1 from '@angular/material/core';
import { InjectionToken } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { OnInit } from '@angular/core';
import { QueryList } from '@angular/core';

declare namespace i2 {
    export {
        MAT_BUTTON_TOGGLE_GROUP_DEFAULT_OPTIONS_FACTORY,
        ToggleType,
        MatButtonToggleAppearance,
        MatButtonToggleDefaultOptions,
        MAT_BUTTON_TOGGLE_DEFAULT_OPTIONS,
        MAT_BUTTON_TOGGLE_GROUP,
        MAT_BUTTON_TOGGLE_GROUP_VALUE_ACCESSOR,
        MatButtonToggleChange,
        MatButtonToggleGroup,
        MatButtonToggle
    }
}

/**
 * Injection token that can be used to configure the
 * default options for all button toggles within an app.
 */
export declare const MAT_BUTTON_TOGGLE_DEFAULT_OPTIONS: InjectionToken<MatButtonToggleDefaultOptions>;

/**
 * Injection token that can be used to reference instances of `MatButtonToggleGroup`.
 * It serves as alternative token to the actual `MatButtonToggleGroup` class which
 * could cause unnecessary retention of the class and its component metadata.
 */
export declare const MAT_BUTTON_TOGGLE_GROUP: InjectionToken<MatButtonToggleGroup>;

export declare function MAT_BUTTON_TOGGLE_GROUP_DEFAULT_OPTIONS_FACTORY(): MatButtonToggleDefaultOptions;

/**
 * Provider Expression that allows mat-button-toggle-group to register as a ControlValueAccessor.
 * This allows it to support [(ngModel)].
 * @docs-private
 */
export declare const MAT_BUTTON_TOGGLE_GROUP_VALUE_ACCESSOR: any;

/** Single button inside of a toggle group. */
export declare class MatButtonToggle implements OnInit, AfterViewInit, OnDestroy {
    private _changeDetectorRef;
    private _elementRef;
    private _focusMonitor;
    private _checked;
    /**
     * Attached to the aria-label attribute of the host element. In most cases, aria-labelledby will
     * take precedence so this may be omitted.
     */
    ariaLabel: string;
    /**
     * Users can specify the `aria-labelledby` attribute which will be forwarded to the input element
     */
    ariaLabelledby: string | null;
    /** Underlying native `button` element. */
    _buttonElement: ElementRef<HTMLButtonElement>;
    /** The parent button toggle group (exclusive selection). Optional. */
    buttonToggleGroup: MatButtonToggleGroup;
    /** Unique ID for the underlying `button` element. */
    get buttonId(): string;
    /** The unique ID for this button toggle. */
    id: string;
    /** HTML's 'name' attribute used to group radios for unique selection. */
    name: string;
    /** MatButtonToggleGroup reads this to assign its own value. */
    value: any;
    /** Tabindex for the toggle. */
    tabIndex: number | null;
    /** Whether ripples are disabled on the button toggle. */
    disableRipple: boolean;
    /** The appearance style of the button. */
    get appearance(): MatButtonToggleAppearance;
    set appearance(value: MatButtonToggleAppearance);
    private _appearance;
    /** Whether the button is checked. */
    get checked(): boolean;
    set checked(value: boolean);
    /** Whether the button is disabled. */
    get disabled(): boolean;
    set disabled(value: boolean);
    private _disabled;
    /** Event emitted when the group value changes. */
    readonly change: EventEmitter<MatButtonToggleChange>;
    constructor(toggleGroup: MatButtonToggleGroup, _changeDetectorRef: ChangeDetectorRef, _elementRef: ElementRef<HTMLElement>, _focusMonitor: FocusMonitor, defaultTabIndex: string, defaultOptions?: MatButtonToggleDefaultOptions);
    ngOnInit(): void;
    ngAfterViewInit(): void;
    ngOnDestroy(): void;
    /** Focuses the button. */
    focus(options?: FocusOptions): void;
    /** Checks the button toggle due to an interaction with the underlying native button. */
    _onButtonClick(): void;
    /**
     * Marks the button toggle as needing checking for change detection.
     * This method is exposed because the parent button toggle group will directly
     * update bound properties of the radio button.
     */
    _markForCheck(): void;
    /** Gets the name that should be assigned to the inner DOM node. */
    _getButtonName(): string | null;
    /** Whether the toggle is in single selection mode. */
    private _isSingleSelector;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatButtonToggle, [{ optional: true; }, null, null, null, { attribute: "tabindex"; }, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatButtonToggle, "mat-button-toggle", ["matButtonToggle"], { "ariaLabel": { "alias": "aria-label"; "required": false; }; "ariaLabelledby": { "alias": "aria-labelledby"; "required": false; }; "id": { "alias": "id"; "required": false; }; "name": { "alias": "name"; "required": false; }; "value": { "alias": "value"; "required": false; }; "tabIndex": { "alias": "tabIndex"; "required": false; }; "disableRipple": { "alias": "disableRipple"; "required": false; }; "appearance": { "alias": "appearance"; "required": false; }; "checked": { "alias": "checked"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; }, { "change": "change"; }, never, ["*"], true, never>;
    static ngAcceptInputType_disableRipple: unknown;
    static ngAcceptInputType_checked: unknown;
    static ngAcceptInputType_disabled: unknown;
}

/** Possible appearance styles for the button toggle. */
export declare type MatButtonToggleAppearance = 'legacy' | 'standard';

/** Change event object emitted by button toggle. */
export declare class MatButtonToggleChange {
    /** The button toggle that emits the event. */
    source: MatButtonToggle;
    /** The value assigned to the button toggle. */
    value: any;
    constructor(
    /** The button toggle that emits the event. */
    source: MatButtonToggle, 
    /** The value assigned to the button toggle. */
    value: any);
}

/**
 * Represents the default options for the button toggle that can be configured
 * using the `MAT_BUTTON_TOGGLE_DEFAULT_OPTIONS` injection token.
 */
export declare interface MatButtonToggleDefaultOptions {
    /**
     * Default appearance to be used by button toggles. Can be overridden by explicitly
     * setting an appearance on a button toggle or group.
     */
    appearance?: MatButtonToggleAppearance;
    /** Whetehr icon indicators should be hidden for single-selection button toggle groups. */
    hideSingleSelectionIndicator?: boolean;
    /** Whether icon indicators should be hidden for multiple-selection button toggle groups. */
    hideMultipleSelectionIndicator?: boolean;
}

/** Exclusive selection button toggle group that behaves like a radio-button group. */
export declare class MatButtonToggleGroup implements ControlValueAccessor, OnInit, AfterContentInit {
    private _changeDetector;
    private _multiple;
    private _disabled;
    private _selectionModel;
    /**
     * Reference to the raw value that the consumer tried to assign. The real
     * value will exclude any values from this one that don't correspond to a
     * toggle. Useful for the cases where the value is assigned before the toggles
     * have been initialized or at the same that they're being swapped out.
     */
    private _rawValue;
    /**
     * The method to be called in order to update ngModel.
     * Now `ngModel` binding is not supported in multiple selection mode.
     */
    _controlValueAccessorChangeFn: (value: any) => void;
    /** onTouch function registered via registerOnTouch (ControlValueAccessor). */
    _onTouched: () => any;
    /** Child button toggle buttons. */
    _buttonToggles: QueryList<MatButtonToggle>;
    /** The appearance for all the buttons in the group. */
    appearance: MatButtonToggleAppearance;
    /** `name` attribute for the underlying `input` element. */
    get name(): string;
    set name(value: string);
    private _name;
    /** Whether the toggle group is vertical. */
    vertical: boolean;
    /** Value of the toggle group. */
    get value(): any;
    set value(newValue: any);
    /**
     * Event that emits whenever the value of the group changes.
     * Used to facilitate two-way data binding.
     * @docs-private
     */
    readonly valueChange: EventEmitter<any>;
    /** Selected button toggles in the group. */
    get selected(): MatButtonToggle | MatButtonToggle[];
    /** Whether multiple button toggles can be selected. */
    get multiple(): boolean;
    set multiple(value: boolean);
    /** Whether multiple button toggle group is disabled. */
    get disabled(): boolean;
    set disabled(value: boolean);
    /** Event emitted when the group's value changes. */
    readonly change: EventEmitter<MatButtonToggleChange>;
    /** Whether checkmark indicator for single-selection button toggle groups is hidden. */
    get hideSingleSelectionIndicator(): boolean;
    set hideSingleSelectionIndicator(value: boolean);
    private _hideSingleSelectionIndicator;
    /** Whether checkmark indicator for multiple-selection button toggle groups is hidden. */
    get hideMultipleSelectionIndicator(): boolean;
    set hideMultipleSelectionIndicator(value: boolean);
    private _hideMultipleSelectionIndicator;
    constructor(_changeDetector: ChangeDetectorRef, defaultOptions?: MatButtonToggleDefaultOptions);
    ngOnInit(): void;
    ngAfterContentInit(): void;
    /**
     * Sets the model value. Implemented as part of ControlValueAccessor.
     * @param value Value to be set to the model.
     */
    writeValue(value: any): void;
    registerOnChange(fn: (value: any) => void): void;
    registerOnTouched(fn: any): void;
    setDisabledState(isDisabled: boolean): void;
    /** Dispatch change event with current selection and group value. */
    _emitChangeEvent(toggle: MatButtonToggle): void;
    /**
     * Syncs a button toggle's selected state with the model value.
     * @param toggle Toggle to be synced.
     * @param select Whether the toggle should be selected.
     * @param isUserInput Whether the change was a result of a user interaction.
     * @param deferEvents Whether to defer emitting the change events.
     */
    _syncButtonToggle(toggle: MatButtonToggle, select: boolean, isUserInput?: boolean, deferEvents?: boolean): void;
    /** Checks whether a button toggle is selected. */
    _isSelected(toggle: MatButtonToggle): boolean;
    /** Determines whether a button toggle should be checked on init. */
    _isPrechecked(toggle: MatButtonToggle): boolean;
    /** Updates the selection state of the toggles in the group based on a value. */
    private _setSelectionByValue;
    /** Clears the selected toggles. */
    private _clearSelection;
    /** Selects a value if there's a toggle that corresponds to it. */
    private _selectValue;
    /** Syncs up the group's value with the model and emits the change event. */
    private _updateModelValue;
    /** Marks all of the child button toggles to be checked. */
    private _markButtonsForCheck;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatButtonToggleGroup, [null, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatButtonToggleGroup, "mat-button-toggle-group", ["matButtonToggleGroup"], { "appearance": { "alias": "appearance"; "required": false; }; "name": { "alias": "name"; "required": false; }; "vertical": { "alias": "vertical"; "required": false; }; "value": { "alias": "value"; "required": false; }; "multiple": { "alias": "multiple"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; "hideSingleSelectionIndicator": { "alias": "hideSingleSelectionIndicator"; "required": false; }; "hideMultipleSelectionIndicator": { "alias": "hideMultipleSelectionIndicator"; "required": false; }; }, { "valueChange": "valueChange"; "change": "change"; }, ["_buttonToggles"], never, true, never>;
    static ngAcceptInputType_vertical: unknown;
    static ngAcceptInputType_multiple: unknown;
    static ngAcceptInputType_disabled: unknown;
    static ngAcceptInputType_hideSingleSelectionIndicator: unknown;
    static ngAcceptInputType_hideMultipleSelectionIndicator: unknown;
}

export declare class MatButtonToggleModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatButtonToggleModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatButtonToggleModule, never, [typeof i1.MatCommonModule, typeof i1.MatRippleModule, typeof i2.MatButtonToggleGroup, typeof i2.MatButtonToggle], [typeof i1.MatCommonModule, typeof i2.MatButtonToggleGroup, typeof i2.MatButtonToggle]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatButtonToggleModule>;
}

/**
 * @deprecated No longer used.
 * @breaking-change 11.0.0
 */
export declare type ToggleType = 'checkbox' | 'radio';

export { }
