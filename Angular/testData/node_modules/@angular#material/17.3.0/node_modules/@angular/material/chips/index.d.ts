import { AfterContentInit } from '@angular/core';
import { AfterViewInit } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';
import { ControlValueAccessor } from '@angular/forms';
import { Directionality } from '@angular/cdk/bidi';
import { DoCheck } from '@angular/core';
import { ElementRef } from '@angular/core';
import { ErrorStateMatcher } from '@angular/material/core';
import { EventEmitter } from '@angular/core';
import { FocusKeyManager } from '@angular/cdk/a11y';
import { FocusMonitor } from '@angular/cdk/a11y';
import { FormGroupDirective } from '@angular/forms';
import * as i0 from '@angular/core';
import * as i1 from '@angular/material/core';
import { InjectionToken } from '@angular/core';
import { MatFormField } from '@angular/material/form-field';
import { MatFormFieldControl } from '@angular/material/form-field';
import { MatRipple } from '@angular/material/core';
import { MatRippleLoader } from '@angular/material/core';
import { NgControl } from '@angular/forms';
import { NgForm } from '@angular/forms';
import { NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { OnChanges } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { OnInit } from '@angular/core';
import { QueryList } from '@angular/core';
import { RippleGlobalOptions } from '@angular/material/core';
import { Subject } from 'rxjs';

declare namespace i10 {
    export {
        MatChipEditedEvent,
        MatChipRow
    }
}

declare namespace i11 {
    export {
        MatChipSet
    }
}

declare namespace i2 {
    export {
        MatChipAction
    }
}

declare namespace i3 {
    export {
        MatChipEvent,
        MatChip
    }
}

declare namespace i4 {
    export {
        MatChipAvatar,
        MatChipTrailingIcon,
        MatChipRemove
    }
}

declare namespace i5 {
    export {
        MatChipEditInput
    }
}

declare namespace i6 {
    export {
        MatChipGridChange,
        MatChipGrid
    }
}

declare namespace i7 {
    export {
        MatChipInputEvent,
        MatChipInput
    }
}

declare namespace i8 {
    export {
        MatChipListboxChange,
        MAT_CHIP_LISTBOX_CONTROL_VALUE_ACCESSOR,
        MatChipListbox
    }
}

declare namespace i9 {
    export {
        MatChipSelectionChange,
        MatChipOption
    }
}

/**
 * Injection token used to avoid a circular dependency between the `MatChip` and `MatChipAction`.
 */
export declare const MAT_CHIP: InjectionToken<unknown>;

/**
 * Injection token that can be used to reference instances of `MatChipAvatar`. It serves as
 * alternative token to the actual `MatChipAvatar` class which could cause unnecessary
 * retention of the class and its directive metadata.
 */
export declare const MAT_CHIP_AVATAR: InjectionToken<unknown>;

/**
 * Provider Expression that allows mat-chip-listbox to register as a ControlValueAccessor.
 * This allows it to support [(ngModel)].
 * @docs-private
 */
export declare const MAT_CHIP_LISTBOX_CONTROL_VALUE_ACCESSOR: any;

/**
 * Injection token that can be used to reference instances of `MatChipRemove`. It serves as
 * alternative token to the actual `MatChipRemove` class which could cause unnecessary
 * retention of the class and its directive metadata.
 */
export declare const MAT_CHIP_REMOVE: InjectionToken<unknown>;

/**
 * Injection token that can be used to reference instances of `MatChipTrailingIcon`. It serves as
 * alternative token to the actual `MatChipTrailingIcon` class which could cause unnecessary
 * retention of the class and its directive metadata.
 */
export declare const MAT_CHIP_TRAILING_ICON: InjectionToken<unknown>;

/** Injection token to be used to override the default options for the chips module. */
export declare const MAT_CHIPS_DEFAULT_OPTIONS: InjectionToken<MatChipsDefaultOptions>;

/**
 * Material design styled Chip base component. Used inside the MatChipSet component.
 *
 * Extended by MatChipOption and MatChipRow for different interaction patterns.
 */
export declare class MatChip implements OnInit, AfterViewInit, AfterContentInit, DoCheck, OnDestroy {
    _changeDetectorRef: ChangeDetectorRef;
    _elementRef: ElementRef<HTMLElement>;
    protected _ngZone: NgZone;
    private _focusMonitor;
    private _globalRippleOptions?;
    protected _document: Document;
    /** Emits when the chip is focused. */
    readonly _onFocus: Subject<MatChipEvent>;
    /** Emits when the chip is blurred. */
    readonly _onBlur: Subject<MatChipEvent>;
    /** Whether this chip is a basic (unstyled) chip. */
    _isBasicChip: boolean;
    /** Role for the root of the chip. */
    role: string | null;
    /** Whether the chip has focus. */
    private _hasFocusInternal;
    /** Whether moving focus into the chip is pending. */
    private _pendingFocus;
    /** Subscription to changes in the chip's actions. */
    private _actionChanges;
    /** Whether animations for the chip are enabled. */
    _animationsDisabled: boolean;
    /** All avatars present in the chip. */
    protected _allLeadingIcons: QueryList<MatChipAvatar>;
    /** All trailing icons present in the chip. */
    protected _allTrailingIcons: QueryList<MatChipTrailingIcon>;
    /** All remove icons present in the chip. */
    protected _allRemoveIcons: QueryList<MatChipRemove>;
    _hasFocus(): boolean;
    /** A unique id for the chip. If none is supplied, it will be auto-generated. */
    id: string;
    /** ARIA label for the content of the chip. */
    ariaLabel: string | null;
    /** ARIA description for the content of the chip. */
    ariaDescription: string | null;
    /** Id of a span that contains this chip's aria description. */
    _ariaDescriptionId: string;
    private _textElement;
    /**
     * The value of the chip. Defaults to the content inside
     * the `mat-mdc-chip-action-label` element.
     */
    get value(): any;
    set value(value: any);
    protected _value: any;
    /** Theme color palette of the chip. */
    color?: string | null;
    /**
     * Determines whether or not the chip displays the remove styling and emits (removed) events.
     */
    removable: boolean;
    /**
     * Colors the chip for emphasis as if it were selected.
     */
    highlighted: boolean;
    /** Whether the ripple effect is disabled or not. */
    disableRipple: boolean;
    /** Whether the chip is disabled. */
    disabled: boolean;
    /** Tab index of the chip. */
    tabIndex: number;
    /** Emitted when a chip is to be removed. */
    readonly removed: EventEmitter<MatChipEvent>;
    /** Emitted when the chip is destroyed. */
    readonly destroyed: EventEmitter<MatChipEvent>;
    /** The unstyled chip selector for this component. */
    protected basicChipAttrName: string;
    /** The chip's leading icon. */
    leadingIcon: MatChipAvatar;
    /** The chip's trailing icon. */
    trailingIcon: MatChipTrailingIcon;
    /** The chip's trailing remove icon. */
    removeIcon: MatChipRemove;
    /**
     * Reference to the MatRipple instance of the chip.
     * @deprecated Considered an implementation detail. To be removed.
     * @breaking-change 17.0.0
     */
    get ripple(): MatRipple;
    set ripple(v: MatRipple);
    /** Action receiving the primary set of user interactions. */
    primaryAction: MatChipAction;
    /**
     * Handles the lazy creation of the MatChip ripple.
     * Used to improve initial load time of large applications.
     */
    _rippleLoader: MatRippleLoader;
    constructor(_changeDetectorRef: ChangeDetectorRef, _elementRef: ElementRef<HTMLElement>, _ngZone: NgZone, _focusMonitor: FocusMonitor, _document: any, animationMode?: string, _globalRippleOptions?: RippleGlobalOptions | undefined, tabIndex?: string);
    ngOnInit(): void;
    ngAfterViewInit(): void;
    ngAfterContentInit(): void;
    ngDoCheck(): void;
    ngOnDestroy(): void;
    /**
     * Allows for programmatic removal of the chip.
     *
     * Informs any listeners of the removal request. Does not remove the chip from the DOM.
     */
    remove(): void;
    /** Whether or not the ripple should be disabled. */
    _isRippleDisabled(): boolean;
    /** Returns whether the chip has a trailing icon. */
    _hasTrailingIcon(): boolean;
    /** Handles keyboard events on the chip. */
    _handleKeydown(event: KeyboardEvent): void;
    /** Allows for programmatic focusing of the chip. */
    focus(): void;
    /** Gets the action that contains a specific target node. */
    _getSourceAction(target: Node): MatChipAction | undefined;
    /** Gets all of the actions within the chip. */
    _getActions(): MatChipAction[];
    /** Handles interactions with the primary action of the chip. */
    _handlePrimaryActionInteraction(): void;
    /** Gets the tabindex of the chip. */
    _getTabIndex(): number | null;
    /** Starts the focus monitoring process on the chip. */
    private _monitorFocus;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatChip, [null, null, null, null, null, { optional: true; }, { optional: true; }, { attribute: "tabindex"; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatChip, "mat-basic-chip, [mat-basic-chip], mat-chip, [mat-chip]", ["matChip"], { "role": { "alias": "role"; "required": false; }; "id": { "alias": "id"; "required": false; }; "ariaLabel": { "alias": "aria-label"; "required": false; }; "ariaDescription": { "alias": "aria-description"; "required": false; }; "value": { "alias": "value"; "required": false; }; "color": { "alias": "color"; "required": false; }; "removable": { "alias": "removable"; "required": false; }; "highlighted": { "alias": "highlighted"; "required": false; }; "disableRipple": { "alias": "disableRipple"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; "tabIndex": { "alias": "tabIndex"; "required": false; }; }, { "removed": "removed"; "destroyed": "destroyed"; }, ["leadingIcon", "trailingIcon", "removeIcon", "_allLeadingIcons", "_allTrailingIcons", "_allRemoveIcons"], ["mat-chip-avatar, [matChipAvatar]", "*", "mat-chip-trailing-icon,[matChipRemove],[matChipTrailingIcon]"], true, never>;
    static ngAcceptInputType_removable: unknown;
    static ngAcceptInputType_highlighted: unknown;
    static ngAcceptInputType_disableRipple: unknown;
    static ngAcceptInputType_disabled: unknown;
    static ngAcceptInputType_tabIndex: unknown;
}

/**
 * Section within a chip.
 * @docs-private
 */
declare class MatChipAction {
    _elementRef: ElementRef<HTMLElement>;
    protected _parentChip: {
        _handlePrimaryActionInteraction(): void;
        remove(): void;
        disabled: boolean;
        _isEditing?: boolean;
    };
    /** Whether the action is interactive. */
    isInteractive: boolean;
    /** Whether this is the primary action in the chip. */
    _isPrimary: boolean;
    /** Whether the action is disabled. */
    get disabled(): boolean;
    set disabled(value: boolean);
    private _disabled;
    /** Tab index of the action. */
    tabIndex: number;
    /**
     * Private API to allow focusing this chip when it is disabled.
     */
    private _allowFocusWhenDisabled;
    /**
     * Determine the value of the disabled attribute for this chip action.
     */
    protected _getDisabledAttribute(): string | null;
    /**
     * Determine the value of the tabindex attribute for this chip action.
     */
    protected _getTabindex(): string | null;
    constructor(_elementRef: ElementRef<HTMLElement>, _parentChip: {
        _handlePrimaryActionInteraction(): void;
        remove(): void;
        disabled: boolean;
        _isEditing?: boolean;
    });
    focus(): void;
    _handleClick(event: MouseEvent): void;
    _handleKeydown(event: KeyboardEvent): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatChipAction, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatChipAction, "[matChipAction]", never, { "isInteractive": { "alias": "isInteractive"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; "tabIndex": { "alias": "tabIndex"; "required": false; }; "_allowFocusWhenDisabled": { "alias": "_allowFocusWhenDisabled"; "required": false; }; }, {}, never, never, true, never>;
    static ngAcceptInputType_disabled: unknown;
    static ngAcceptInputType_tabIndex: unknown;
}

/** Avatar image within a chip. */
export declare class MatChipAvatar {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatChipAvatar, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatChipAvatar, "mat-chip-avatar, [matChipAvatar]", never, {}, {}, never, never, true, never>;
}

/** Represents an event fired on an individual `mat-chip` when it is edited. */
export declare interface MatChipEditedEvent extends MatChipEvent {
    /** The final edit value. */
    value: string;
}

/**
 * A directive that makes a span editable and exposes functions to modify and retrieve the
 * element's contents.
 */
export declare class MatChipEditInput {
    private readonly _elementRef;
    private readonly _document;
    constructor(_elementRef: ElementRef, _document: any);
    initialize(initialValue: string): void;
    getNativeElement(): HTMLElement;
    setValue(value: string): void;
    getValue(): string;
    private _moveCursorToEndOfInput;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatChipEditInput, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatChipEditInput, "span[matChipEditInput]", never, {}, {}, never, never, true, never>;
}

/** Represents an event fired on an individual `mat-chip`. */
export declare interface MatChipEvent {
    /** The chip the event was fired on. */
    chip: MatChip;
}

/**
 * An extension of the MatChipSet component used with MatChipRow chips and
 * the matChipInputFor directive.
 */
export declare class MatChipGrid extends MatChipSet implements AfterContentInit, AfterViewInit, ControlValueAccessor, DoCheck, MatFormFieldControl<any>, OnDestroy {
    ngControl: NgControl;
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    readonly controlType: string;
    /** The chip input to add more chips */
    protected _chipInput: MatChipTextControl;
    protected _defaultRole: string;
    private _errorStateTracker;
    /**
     * List of element ids to propagate to the chipInput's aria-describedby attribute.
     */
    private _ariaDescribedbyIds;
    /**
     * Function when touched. Set as part of ControlValueAccessor implementation.
     * @docs-private
     */
    _onTouched: () => void;
    /**
     * Function when changed. Set as part of ControlValueAccessor implementation.
     * @docs-private
     */
    _onChange: (value: any) => void;
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    get disabled(): boolean;
    set disabled(value: boolean);
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    get id(): string;
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    get empty(): boolean;
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    get placeholder(): string;
    set placeholder(value: string);
    protected _placeholder: string;
    /** Whether any chips or the matChipInput inside of this chip-grid has focus. */
    get focused(): boolean;
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    get required(): boolean;
    set required(value: boolean);
    protected _required: boolean | undefined;
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    get shouldLabelFloat(): boolean;
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    get value(): any;
    set value(value: any);
    protected _value: any[];
    /** An object used to control when error messages are shown. */
    get errorStateMatcher(): ErrorStateMatcher;
    set errorStateMatcher(value: ErrorStateMatcher);
    /** Combined stream of all of the child chips' blur events. */
    get chipBlurChanges(): Observable<MatChipEvent>;
    /** Emits when the chip grid value has been changed by the user. */
    readonly change: EventEmitter<MatChipGridChange>;
    /**
     * Emits whenever the raw value of the chip-grid changes. This is here primarily
     * to facilitate the two-way binding for the `value` input.
     * @docs-private
     */
    readonly valueChange: EventEmitter<any>;
    _chips: QueryList<MatChipRow>;
    /**
     * Emits whenever the component state changes and should cause the parent
     * form-field to update. Implemented as part of `MatFormFieldControl`.
     * @docs-private
     */
    readonly stateChanges: Subject<void>;
    /** Whether the chip grid is in an error state. */
    get errorState(): boolean;
    set errorState(value: boolean);
    constructor(elementRef: ElementRef, changeDetectorRef: ChangeDetectorRef, dir: Directionality, parentForm: NgForm, parentFormGroup: FormGroupDirective, defaultErrorStateMatcher: ErrorStateMatcher, ngControl: NgControl);
    ngAfterContentInit(): void;
    ngAfterViewInit(): void;
    ngDoCheck(): void;
    ngOnDestroy(): void;
    /** Associates an HTML input element with this chip grid. */
    registerInput(inputElement: MatChipTextControl): void;
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    onContainerClick(event: MouseEvent): void;
    /**
     * Focuses the first chip in this chip grid, or the associated input when there
     * are no eligible chips.
     */
    focus(): void;
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    setDescribedByIds(ids: string[]): void;
    /**
     * Implemented as part of ControlValueAccessor.
     * @docs-private
     */
    writeValue(value: any): void;
    /**
     * Implemented as part of ControlValueAccessor.
     * @docs-private
     */
    registerOnChange(fn: (value: any) => void): void;
    /**
     * Implemented as part of ControlValueAccessor.
     * @docs-private
     */
    registerOnTouched(fn: () => void): void;
    /**
     * Implemented as part of ControlValueAccessor.
     * @docs-private
     */
    setDisabledState(isDisabled: boolean): void;
    /** Refreshes the error state of the chip grid. */
    updateErrorState(): void;
    /** When blurred, mark the field as touched when focus moved outside the chip grid. */
    _blur(): void;
    /**
     * Removes the `tabindex` from the chip grid and resets it back afterwards, allowing the
     * user to tab out of it. This prevents the grid from capturing focus and redirecting
     * it back to the first chip, creating a focus trap, if it user tries to tab away.
     */
    protected _allowFocusEscape(): void;
    /** Handles custom keyboard events. */
    _handleKeydown(event: KeyboardEvent): void;
    _focusLastChip(): void;
    /** Emits change event to set the model value. */
    private _propagateChanges;
    /** Mark the field as touched */
    private _markAsTouched;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatChipGrid, [null, null, { optional: true; }, { optional: true; }, { optional: true; }, null, { optional: true; self: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatChipGrid, "mat-chip-grid", never, { "disabled": { "alias": "disabled"; "required": false; }; "placeholder": { "alias": "placeholder"; "required": false; }; "required": { "alias": "required"; "required": false; }; "value": { "alias": "value"; "required": false; }; "errorStateMatcher": { "alias": "errorStateMatcher"; "required": false; }; }, { "change": "change"; "valueChange": "valueChange"; }, ["_chips"], ["*"], true, never>;
    static ngAcceptInputType_disabled: unknown;
    static ngAcceptInputType_required: unknown;
}

/** Change event object that is emitted when the chip grid value has changed. */
export declare class MatChipGridChange {
    /** Chip grid that emitted the event. */
    source: MatChipGrid;
    /** Value of the chip grid when the event was emitted. */
    value: any;
    constructor(
    /** Chip grid that emitted the event. */
    source: MatChipGrid, 
    /** Value of the chip grid when the event was emitted. */
    value: any);
}

/**
 * Directive that adds chip-specific behaviors to an input element inside `<mat-form-field>`.
 * May be placed inside or outside of a `<mat-chip-grid>`.
 */
export declare class MatChipInput implements MatChipTextControl, AfterContentInit, OnChanges, OnDestroy {
    protected _elementRef: ElementRef<HTMLInputElement>;
    /** Used to prevent focus moving to chips while user is holding backspace */
    private _focusLastChipOnBackspace;
    /** Whether the control is focused. */
    focused: boolean;
    /** Register input for chip list */
    get chipGrid(): MatChipGrid;
    set chipGrid(value: MatChipGrid);
    private _chipGrid;
    /**
     * Whether or not the chipEnd event will be emitted when the input is blurred.
     */
    addOnBlur: boolean;
    /**
     * The list of key codes that will trigger a chipEnd event.
     *
     * Defaults to `[ENTER]`.
     */
    separatorKeyCodes: readonly number[] | ReadonlySet<number>;
    /** Emitted when a chip is to be added. */
    readonly chipEnd: EventEmitter<MatChipInputEvent>;
    /** The input's placeholder text. */
    placeholder: string;
    /** Unique id for the input. */
    id: string;
    /** Whether the input is disabled. */
    get disabled(): boolean;
    set disabled(value: boolean);
    private _disabled;
    /** Whether the input is empty. */
    get empty(): boolean;
    /** The native input element to which this directive is attached. */
    readonly inputElement: HTMLInputElement;
    constructor(_elementRef: ElementRef<HTMLInputElement>, defaultOptions: MatChipsDefaultOptions, formField?: MatFormField);
    ngOnChanges(): void;
    ngOnDestroy(): void;
    ngAfterContentInit(): void;
    /** Utility method to make host definition/tests more clear. */
    _keydown(event?: KeyboardEvent): void;
    /**
     * Pass events to the keyboard manager. Available here for tests.
     */
    _keyup(event: KeyboardEvent): void;
    /** Checks to see if the blur should emit the (chipEnd) event. */
    _blur(): void;
    _focus(): void;
    /** Checks to see if the (chipEnd) event needs to be emitted. */
    _emitChipEnd(event?: KeyboardEvent): void;
    _onInput(): void;
    /** Focuses the input. */
    focus(): void;
    /** Clears the input */
    clear(): void;
    setDescribedByIds(ids: string[]): void;
    /** Checks whether a keycode is one of the configured separators. */
    private _isSeparatorKey;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatChipInput, [null, null, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatChipInput, "input[matChipInputFor]", ["matChipInput", "matChipInputFor"], { "chipGrid": { "alias": "matChipInputFor"; "required": false; }; "addOnBlur": { "alias": "matChipInputAddOnBlur"; "required": false; }; "separatorKeyCodes": { "alias": "matChipInputSeparatorKeyCodes"; "required": false; }; "placeholder": { "alias": "placeholder"; "required": false; }; "id": { "alias": "id"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; }, { "chipEnd": "matChipInputTokenEnd"; }, never, never, true, never>;
    static ngAcceptInputType_addOnBlur: unknown;
    static ngAcceptInputType_disabled: unknown;
}

/** Represents an input event on a `matChipInput`. */
export declare interface MatChipInputEvent {
    /**
     * The native `<input>` element that the event is being fired for.
     * @deprecated Use `MatChipInputEvent#chipInput.inputElement` instead.
     * @breaking-change 13.0.0 This property will be removed.
     */
    input: HTMLInputElement;
    /** The value of the input. */
    value: string;
    /** Reference to the chip input that emitted the event. */
    chipInput: MatChipInput;
}

/**
 * An extension of the MatChipSet component that supports chip selection.
 * Used with MatChipOption chips.
 */
export declare class MatChipListbox extends MatChipSet implements AfterContentInit, OnDestroy, ControlValueAccessor {
    /**
     * Function when touched. Set as part of ControlValueAccessor implementation.
     * @docs-private
     */
    _onTouched: () => void;
    /**
     * Function when changed. Set as part of ControlValueAccessor implementation.
     * @docs-private
     */
    _onChange: (value: any) => void;
    protected _defaultRole: string;
    /** Value that was assigned before the listbox was initialized. */
    private _pendingInitialValue;
    /** Default chip options. */
    private _defaultOptions;
    /** Whether the user should be allowed to select multiple chips. */
    get multiple(): boolean;
    set multiple(value: boolean);
    private _multiple;
    /** The array of selected chips inside the chip listbox. */
    get selected(): MatChipOption[] | MatChipOption;
    /** Orientation of the chip list. */
    ariaOrientation: 'horizontal' | 'vertical';
    /**
     * Whether or not this chip listbox is selectable.
     *
     * When a chip listbox is not selectable, the selected states for all
     * the chips inside the chip listbox are always ignored.
     */
    get selectable(): boolean;
    set selectable(value: boolean);
    protected _selectable: boolean;
    /**
     * A function to compare the option values with the selected values. The first argument
     * is a value from an option. The second is a value from the selection. A boolean
     * should be returned.
     */
    compareWith: (o1: any, o2: any) => boolean;
    /** Whether this chip listbox is required. */
    required: boolean;
    /** Whether checkmark indicator for single-selection options is hidden. */
    get hideSingleSelectionIndicator(): boolean;
    set hideSingleSelectionIndicator(value: boolean);
    private _hideSingleSelectionIndicator;
    /** Combined stream of all of the child chips' selection change events. */
    get chipSelectionChanges(): Observable<MatChipSelectionChange>;
    /** Combined stream of all of the child chips' blur events. */
    get chipBlurChanges(): Observable<MatChipEvent>;
    /** The value of the listbox, which is the combined value of the selected chips. */
    get value(): any;
    set value(value: any);
    protected _value: any;
    /** Event emitted when the selected chip listbox value has been changed by the user. */
    readonly change: EventEmitter<MatChipListboxChange>;
    _chips: QueryList<MatChipOption>;
    ngAfterContentInit(): void;
    /**
     * Focuses the first selected chip in this chip listbox, or the first non-disabled chip when there
     * are no selected chips.
     */
    focus(): void;
    /**
     * Implemented as part of ControlValueAccessor.
     * @docs-private
     */
    writeValue(value: any): void;
    /**
     * Implemented as part of ControlValueAccessor.
     * @docs-private
     */
    registerOnChange(fn: (value: any) => void): void;
    /**
     * Implemented as part of ControlValueAccessor.
     * @docs-private
     */
    registerOnTouched(fn: () => void): void;
    /**
     * Implemented as part of ControlValueAccessor.
     * @docs-private
     */
    setDisabledState(isDisabled: boolean): void;
    /** Selects all chips with value. */
    _setSelectionByValue(value: any, isUserInput?: boolean): void;
    /** When blurred, marks the field as touched when focus moved outside the chip listbox. */
    _blur(): void;
    _keydown(event: KeyboardEvent): void;
    /** Marks the field as touched */
    private _markAsTouched;
    /** Emits change event to set the model value. */
    private _propagateChanges;
    /**
     * Deselects every chip in the listbox.
     * @param skip Chip that should not be deselected.
     */
    private _clearSelection;
    /**
     * Finds and selects the chip based on its value.
     * @returns Chip that has the corresponding value.
     */
    private _selectValue;
    /** Syncs the chip-listbox selection state with the individual chips. */
    private _syncListboxProperties;
    /** Returns the first selected chip in this listbox, or undefined if no chips are selected. */
    private _getFirstSelectedChip;
    /**
     * Determines if key manager should avoid putting a given chip action in the tab index. Skip
     * non-interactive actions since the user can't do anything with them.
     */
    protected _skipPredicate(action: MatChipAction): boolean;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatChipListbox, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatChipListbox, "mat-chip-listbox", never, { "multiple": { "alias": "multiple"; "required": false; }; "ariaOrientation": { "alias": "aria-orientation"; "required": false; }; "selectable": { "alias": "selectable"; "required": false; }; "compareWith": { "alias": "compareWith"; "required": false; }; "required": { "alias": "required"; "required": false; }; "hideSingleSelectionIndicator": { "alias": "hideSingleSelectionIndicator"; "required": false; }; "value": { "alias": "value"; "required": false; }; }, { "change": "change"; }, ["_chips"], ["*"], true, never>;
    static ngAcceptInputType_multiple: unknown;
    static ngAcceptInputType_selectable: unknown;
    static ngAcceptInputType_required: unknown;
    static ngAcceptInputType_hideSingleSelectionIndicator: unknown;
}

/** Change event object that is emitted when the chip listbox value has changed. */
export declare class MatChipListboxChange {
    /** Chip listbox that emitted the event. */
    source: MatChipListbox;
    /** Value of the chip listbox when the event was emitted. */
    value: any;
    constructor(
    /** Chip listbox that emitted the event. */
    source: MatChipListbox, 
    /** Value of the chip listbox when the event was emitted. */
    value: any);
}

/**
 * An extension of the MatChip component that supports chip selection. Used with MatChipListbox.
 *
 * Unlike other chips, the user can focus on disabled chip options inside a MatChipListbox. The
 * user cannot click disabled chips.
 */
export declare class MatChipOption extends MatChip implements OnInit {
    /** Default chip options. */
    private _defaultOptions;
    /** Whether the chip list is selectable. */
    chipListSelectable: boolean;
    /** Whether the chip list is in multi-selection mode. */
    _chipListMultiple: boolean;
    /** Whether the chip list hides single-selection indicator. */
    _chipListHideSingleSelectionIndicator: boolean;
    /**
     * Whether or not the chip is selectable.
     *
     * When a chip is not selectable, changes to its selected state are always
     * ignored. By default an option chip is selectable, and it becomes
     * non-selectable if its parent chip list is not selectable.
     */
    get selectable(): boolean;
    set selectable(value: boolean);
    protected _selectable: boolean;
    /** Whether the chip is selected. */
    get selected(): boolean;
    set selected(value: boolean);
    private _selected;
    /**
     * The ARIA selected applied to the chip. Conforms to WAI ARIA best practices for listbox
     * interaction patterns.
     *
     * From [WAI ARIA Listbox authoring practices guide](
     * https://www.w3.org/WAI/ARIA/apg/patterns/listbox/):
     *  "If any options are selected, each selected option has either aria-selected or aria-checked
     *  set to true. All options that are selectable but not selected have either aria-selected or
     *  aria-checked set to false."
     *
     * Set `aria-selected="false"` on not-selected listbox options that are selectable to fix
     * VoiceOver reading every option as "selected" (#25736).
     */
    get ariaSelected(): string | null;
    /** The unstyled chip selector for this component. */
    protected basicChipAttrName: string;
    /** Emitted when the chip is selected or deselected. */
    readonly selectionChange: EventEmitter<MatChipSelectionChange>;
    ngOnInit(): void;
    /** Selects the chip. */
    select(): void;
    /** Deselects the chip. */
    deselect(): void;
    /** Selects this chip and emits userInputSelection event */
    selectViaInteraction(): void;
    /** Toggles the current selected state of this chip. */
    toggleSelected(isUserInput?: boolean): boolean;
    _handlePrimaryActionInteraction(): void;
    _hasLeadingGraphic(): boolean;
    _setSelectedState(isSelected: boolean, isUserInput: boolean, emitEvent: boolean): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatChipOption, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatChipOption, "mat-basic-chip-option, [mat-basic-chip-option], mat-chip-option, [mat-chip-option]", never, { "selectable": { "alias": "selectable"; "required": false; }; "selected": { "alias": "selected"; "required": false; }; }, { "selectionChange": "selectionChange"; }, never, ["mat-chip-avatar, [matChipAvatar]", "*", "mat-chip-trailing-icon,[matChipRemove],[matChipTrailingIcon]"], true, never>;
    static ngAcceptInputType_selectable: unknown;
    static ngAcceptInputType_selected: unknown;
}

/**
 * Directive to remove the parent chip when the trailing icon is clicked or
 * when the ENTER key is pressed on it.
 *
 * Recommended for use with the Material Design "cancel" icon
 * available at https://material.io/icons/#ic_cancel.
 *
 * Example:
 *
 * ```
 * <mat-chip>
 *   <mat-icon matChipRemove>cancel</mat-icon>
 * </mat-chip>
 * ```
 */
export declare class MatChipRemove extends MatChipAction {
    _isPrimary: boolean;
    _handleClick(event: MouseEvent): void;
    _handleKeydown(event: KeyboardEvent): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatChipRemove, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatChipRemove, "[matChipRemove]", never, {}, {}, never, never, true, never>;
}

/**
 * An extension of the MatChip component used with MatChipGrid and
 * the matChipInputFor directive.
 */
export declare class MatChipRow extends MatChip implements AfterViewInit {
    protected basicChipAttrName: string;
    /**
     * The editing action has to be triggered in a timeout. While we're waiting on it, a blur
     * event might occur which will interrupt the editing. This flag is used to avoid interruptions
     * while the editing action is being initialized.
     */
    private _editStartPending;
    editable: boolean;
    /** Emitted when the chip is edited. */
    readonly edited: EventEmitter<MatChipEditedEvent>;
    /** The default chip edit input that is used if none is projected into this chip row. */
    defaultEditInput?: MatChipEditInput;
    /** The projected chip edit input. */
    contentEditInput?: MatChipEditInput;
    _isEditing: boolean;
    constructor(changeDetectorRef: ChangeDetectorRef, elementRef: ElementRef, ngZone: NgZone, focusMonitor: FocusMonitor, _document: any, animationMode?: string, globalRippleOptions?: RippleGlobalOptions, tabIndex?: string);
    _hasTrailingIcon(): boolean;
    /** Sends focus to the first gridcell when the user clicks anywhere inside the chip. */
    _handleFocus(): void;
    _handleKeydown(event: KeyboardEvent): void;
    _handleDoubleclick(event: MouseEvent): void;
    private _startEditing;
    private _onEditFinish;
    _isRippleDisabled(): boolean;
    /**
     * Gets the projected chip edit input, or the default input if none is projected in. One of these
     * two values is guaranteed to be defined.
     */
    private _getEditInput;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatChipRow, [null, null, null, null, null, { optional: true; }, { optional: true; }, { attribute: "tabindex"; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatChipRow, "mat-chip-row, [mat-chip-row], mat-basic-chip-row, [mat-basic-chip-row]", never, { "editable": { "alias": "editable"; "required": false; }; }, { "edited": "edited"; }, ["contentEditInput"], ["mat-chip-avatar, [matChipAvatar]", "[matChipEditInput]", "*", "mat-chip-trailing-icon,[matChipRemove],[matChipTrailingIcon]"], true, never>;
}

/** Default options, for the chips module, that can be overridden. */
export declare interface MatChipsDefaultOptions {
    /** The list of key codes that will trigger a chipEnd event. */
    separatorKeyCodes: readonly number[] | ReadonlySet<number>;
    /** Wheter icon indicators should be hidden for single-selection. */
    hideSingleSelectionIndicator?: boolean;
}

/** Event object emitted by MatChipOption when selected or deselected. */
export declare class MatChipSelectionChange {
    /** Reference to the chip that emitted the event. */
    source: MatChipOption;
    /** Whether the chip that emitted the event is selected. */
    selected: boolean;
    /** Whether the selection change was a result of a user interaction. */
    isUserInput: boolean;
    constructor(
    /** Reference to the chip that emitted the event. */
    source: MatChipOption, 
    /** Whether the chip that emitted the event is selected. */
    selected: boolean, 
    /** Whether the selection change was a result of a user interaction. */
    isUserInput?: boolean);
}

/**
 * Basic container component for the MatChip component.
 *
 * Extended by MatChipListbox and MatChipGrid for different interaction patterns.
 */
export declare class MatChipSet implements AfterViewInit, OnDestroy {
    protected _elementRef: ElementRef<HTMLElement>;
    protected _changeDetectorRef: ChangeDetectorRef;
    private _dir;
    /** Index of the last destroyed chip that had focus. */
    private _lastDestroyedFocusedChipIndex;
    /** Used to manage focus within the chip list. */
    protected _keyManager: FocusKeyManager<MatChipAction>;
    /** Subject that emits when the component has been destroyed. */
    protected _destroyed: Subject<void>;
    /** Role to use if it hasn't been overwritten by the user. */
    protected _defaultRole: string;
    /** Combined stream of all of the child chips' focus events. */
    get chipFocusChanges(): Observable<MatChipEvent>;
    /** Combined stream of all of the child chips' destroy events. */
    get chipDestroyedChanges(): Observable<MatChipEvent>;
    /** Combined stream of all of the child chips' remove events. */
    get chipRemovedChanges(): Observable<MatChipEvent>;
    /** Whether the chip set is disabled. */
    get disabled(): boolean;
    set disabled(value: boolean);
    protected _disabled: boolean;
    /** Whether the chip list contains chips or not. */
    get empty(): boolean;
    /** The ARIA role applied to the chip set. */
    get role(): string | null;
    /** Tabindex of the chip set. */
    tabIndex: number;
    set role(value: string | null);
    private _explicitRole;
    /** Whether any of the chips inside of this chip-set has focus. */
    get focused(): boolean;
    /** The chips that are part of this chip set. */
    _chips: QueryList<MatChip>;
    /** Flat list of all the actions contained within the chips. */
    _chipActions: QueryList<MatChipAction>;
    constructor(_elementRef: ElementRef<HTMLElement>, _changeDetectorRef: ChangeDetectorRef, _dir: Directionality);
    ngAfterViewInit(): void;
    ngOnDestroy(): void;
    /** Checks whether any of the chips is focused. */
    protected _hasFocusedChip(): boolean;
    /** Syncs the chip-set's state with the individual chips. */
    protected _syncChipsState(): void;
    /** Dummy method for subclasses to override. Base chip set cannot be focused. */
    focus(): void;
    /** Handles keyboard events on the chip set. */
    _handleKeydown(event: KeyboardEvent): void;
    /**
     * Utility to ensure all indexes are valid.
     *
     * @param index The index to be checked.
     * @returns True if the index is valid for our list of chips.
     */
    protected _isValidIndex(index: number): boolean;
    /**
     * Removes the `tabindex` from the chip set and resets it back afterwards, allowing the
     * user to tab out of it. This prevents the set from capturing focus and redirecting
     * it back to the first chip, creating a focus trap, if it user tries to tab away.
     */
    protected _allowFocusEscape(): void;
    /**
     * Gets a stream of events from all the chips within the set.
     * The stream will automatically incorporate any newly-added chips.
     */
    protected _getChipStream<T, C extends MatChip = MatChip>(mappingFunction: (chip: C) => Observable<T>): Observable<T>;
    /** Checks whether an event comes from inside a chip element. */
    protected _originatesFromChip(event: Event): boolean;
    /** Sets up the chip set's focus management logic. */
    private _setUpFocusManagement;
    /**
     * Determines if key manager should avoid putting a given chip action in the tab index. Skip
     * non-interactive and disabled actions since the user can't do anything with them.
     */
    protected _skipPredicate(action: MatChipAction): boolean;
    /** Listens to changes in the chip set and syncs up the state of the individual chips. */
    private _trackChipSetChanges;
    /** Starts tracking the destroyed chips in order to capture the focused one. */
    private _trackDestroyedFocusedChip;
    /**
     * Finds the next appropriate chip to move focus to,
     * if the currently-focused chip is destroyed.
     */
    private _redirectDestroyedChipFocus;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatChipSet, [null, null, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatChipSet, "mat-chip-set", never, { "disabled": { "alias": "disabled"; "required": false; }; "role": { "alias": "role"; "required": false; }; "tabIndex": { "alias": "tabIndex"; "required": false; }; }, {}, ["_chips"], ["*"], true, never>;
    static ngAcceptInputType_disabled: unknown;
    static ngAcceptInputType_tabIndex: unknown;
}

export declare class MatChipsModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatChipsModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatChipsModule, never, [typeof i1.MatCommonModule, typeof i1.MatRippleModule, typeof i2.MatChipAction, typeof i3.MatChip, typeof i4.MatChipAvatar, typeof i5.MatChipEditInput, typeof i6.MatChipGrid, typeof i7.MatChipInput, typeof i8.MatChipListbox, typeof i9.MatChipOption, typeof i4.MatChipRemove, typeof i10.MatChipRow, typeof i11.MatChipSet, typeof i4.MatChipTrailingIcon], [typeof i1.MatCommonModule, typeof i3.MatChip, typeof i4.MatChipAvatar, typeof i5.MatChipEditInput, typeof i6.MatChipGrid, typeof i7.MatChipInput, typeof i8.MatChipListbox, typeof i9.MatChipOption, typeof i4.MatChipRemove, typeof i10.MatChipRow, typeof i11.MatChipSet, typeof i4.MatChipTrailingIcon]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatChipsModule>;
}


/** Interface for a text control that is used to drive interaction with a mat-chip-list. */
export declare interface MatChipTextControl {
    /** Unique identifier for the text control. */
    id: string;
    /** The text control's placeholder text. */
    placeholder: string;
    /** Whether the text control has browser focus. */
    focused: boolean;
    /** Whether the text control is empty. */
    empty: boolean;
    /** Focuses the text control. */
    focus(): void;
    /** Sets the list of ids the input is described by. */
    setDescribedByIds(ids: string[]): void;
}

/** Non-interactive trailing icon in a chip. */
export declare class MatChipTrailingIcon extends MatChipAction {
    /**
     * MDC considers all trailing actions as a remove icon,
     * but we support non-interactive trailing icons.
     */
    isInteractive: boolean;
    _isPrimary: boolean;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatChipTrailingIcon, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatChipTrailingIcon, "mat-chip-trailing-icon, [matChipTrailingIcon]", never, {}, {}, never, never, true, never>;
}

export { }
