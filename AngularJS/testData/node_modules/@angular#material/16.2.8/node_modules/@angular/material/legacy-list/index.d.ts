import { _AbstractConstructor } from '@angular/material/core';
import { AfterContentInit } from '@angular/core';
import { BooleanInput } from '@angular/cdk/coercion';
import { CanDisable } from '@angular/material/core';
import { CanDisableRipple } from '@angular/material/core';
import { ChangeDetectorRef } from '@angular/core';
import { _Constructor } from '@angular/material/core';
import { ControlValueAccessor } from '@angular/forms';
import { ElementRef } from '@angular/core';
import { EventEmitter } from '@angular/core';
import { FocusableOption } from '@angular/cdk/a11y';
import { FocusKeyManager } from '@angular/cdk/a11y';
import { FocusMonitor } from '@angular/cdk/a11y';
import * as i0 from '@angular/core';
import * as i3 from '@angular/material/core';
import * as i4 from '@angular/common';
import * as i5 from '@angular/material/divider';
import { MAT_LIST as MAT_LEGACY_LIST } from '@angular/material/list';
import { MAT_NAV_LIST as MAT_LEGACY_NAV_LIST } from '@angular/material/list';
import { MatLine } from '@angular/material/core';
import { OnChanges } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { OnInit } from '@angular/core';
import { QueryList } from '@angular/core';
import { SelectionModel } from '@angular/cdk/collections';
import { SimpleChanges } from '@angular/core';
import { Subject } from 'rxjs';
import { ThemePalette } from '@angular/material/core';

declare namespace i1 {
    export {
        MatLegacyNavList,
        MatLegacyList,
        MatLegacyListAvatarCssMatStyler,
        MatLegacyListIconCssMatStyler,
        MatLegacyListSubheaderCssMatStyler,
        MatLegacyListItem
    }
}

declare namespace i2 {
    export {
        MAT_LEGACY_SELECTION_LIST_VALUE_ACCESSOR,
        MatLegacySelectionListChange,
        MatLegacyListOptionCheckboxPosition,
        MatLegacyListOption,
        MatLegacySelectionList
    }
}

export { MAT_LEGACY_LIST }

export { MAT_LEGACY_NAV_LIST }

/**
 * @docs-private
 * @deprecated Use `MAT_SELECTION_LIST_VALUE_ACCESSOR` from `@angular/material/list` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare const MAT_LEGACY_SELECTION_LIST_VALUE_ACCESSOR: any;

/**
 * @deprecated Use `MatList` from `@angular/material/list` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyList extends _MatListBase implements CanDisable, CanDisableRipple, OnChanges, OnDestroy {
    private _elementRef;
    /** Emits when the state of the list changes. */
    readonly _stateChanges: Subject<void>;
    constructor(_elementRef: ElementRef<HTMLElement>);
    _getListType(): 'list' | 'action-list' | null;
    ngOnChanges(): void;
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyList, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyList, "mat-list, mat-action-list", ["matList"], { "disableRipple": { "alias": "disableRipple"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; }, {}, never, ["*"], false, never>;
}

/**
 * Directive whose purpose is to add the mat- CSS styling to this selector.
 * @docs-private
 * @deprecated Use `MatListAvatarCssMatStyler` from `@angular/material/list` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyListAvatarCssMatStyler {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyListAvatarCssMatStyler, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyListAvatarCssMatStyler, "[mat-list-avatar], [matListAvatar]", never, {}, {}, never, never, false, never>;
}

/**
 * Directive whose purpose is to add the mat- CSS styling to this selector.
 * @docs-private
 * @deprecated Use `MatListIconCssMatStyler` from `@angular/material/list` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyListIconCssMatStyler {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyListIconCssMatStyler, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyListIconCssMatStyler, "[mat-list-icon], [matListIcon]", never, {}, {}, never, never, false, never>;
}

/**
 * An item within a Material Design list.
 * @deprecated Use `MatListItem` from `@angular/material/list` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyListItem extends _MatListItemMixinBase implements AfterContentInit, CanDisableRipple, OnDestroy {
    private _element;
    private _isInteractiveList;
    private _list?;
    private readonly _destroyed;
    _lines: QueryList<MatLine>;
    _avatar: MatLegacyListAvatarCssMatStyler;
    _icon: MatLegacyListIconCssMatStyler;
    constructor(_element: ElementRef<HTMLElement>, _changeDetectorRef: ChangeDetectorRef, navList?: MatLegacyNavList, list?: MatLegacyList);
    /** Whether the option is disabled. */
    get disabled(): boolean;
    set disabled(value: BooleanInput);
    private _disabled;
    ngAfterContentInit(): void;
    ngOnDestroy(): void;
    /** Whether this list item should show a ripple effect when clicked. */
    _isRippleDisabled(): boolean;
    /** Retrieves the DOM element of the component host. */
    _getHostElement(): HTMLElement;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyListItem, [null, null, { optional: true; }, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyListItem, "mat-list-item, a[mat-list-item], button[mat-list-item]", ["matListItem"], { "disableRipple": { "alias": "disableRipple"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; }, {}, ["_avatar", "_icon", "_lines"], ["[mat-list-avatar], [mat-list-icon], [matListAvatar], [matListIcon]", "[mat-line], [matLine]", "*"], false, never>;
}

/**
 * @deprecated Use `MatListModule` from `@angular/material/list` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyListModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyListModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatLegacyListModule, [typeof i1.MatLegacyList, typeof i1.MatLegacyNavList, typeof i1.MatLegacyListItem, typeof i1.MatLegacyListAvatarCssMatStyler, typeof i1.MatLegacyListIconCssMatStyler, typeof i1.MatLegacyListSubheaderCssMatStyler, typeof i2.MatLegacySelectionList, typeof i2.MatLegacyListOption], [typeof i3.MatLineModule, typeof i3.MatRippleModule, typeof i3.MatCommonModule, typeof i3.MatPseudoCheckboxModule, typeof i4.CommonModule], [typeof i1.MatLegacyList, typeof i1.MatLegacyNavList, typeof i1.MatLegacyListItem, typeof i1.MatLegacyListAvatarCssMatStyler, typeof i3.MatLineModule, typeof i3.MatCommonModule, typeof i1.MatLegacyListIconCssMatStyler, typeof i1.MatLegacyListSubheaderCssMatStyler, typeof i3.MatPseudoCheckboxModule, typeof i2.MatLegacySelectionList, typeof i2.MatLegacyListOption, typeof i5.MatDividerModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatLegacyListModule>;
}

/**
 * Component for list-options of selection-list. Each list-option can automatically
 * generate a checkbox and can put current item into the selectionModel of selection-list
 * if the current item is selected.
 * @deprecated Use `MatListOption` from `@angular/material/list` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyListOption extends _MatListOptionBase implements AfterContentInit, OnDestroy, OnInit, FocusableOption, CanDisableRipple {
    private _element;
    private _changeDetector;
    /** @docs-private */
    selectionList: MatLegacySelectionList;
    private _selected;
    private _disabled;
    private _hasFocus;
    _avatar: MatLegacyListAvatarCssMatStyler;
    _icon: MatLegacyListIconCssMatStyler;
    _lines: QueryList<MatLine>;
    /**
     * Emits when the selected state of the option has changed.
     * Use to facilitate two-data binding to the `selected` property.
     * @docs-private
     */
    readonly selectedChange: EventEmitter<boolean>;
    /** DOM element containing the item's text. */
    _text: ElementRef;
    /** Whether the label should appear before or after the checkbox. Defaults to 'after' */
    checkboxPosition: MatLegacyListOptionCheckboxPosition;
    /** Theme color of the list option. This sets the color of the checkbox. */
    get color(): ThemePalette;
    set color(newValue: ThemePalette);
    private _color;
    /**
     * This is set to true after the first OnChanges cycle so we don't clear the value of `selected`
     * in the first cycle.
     */
    private _inputsInitialized;
    /** Value of the option */
    get value(): any;
    set value(newValue: any);
    private _value;
    /** Whether the option is disabled. */
    get disabled(): boolean;
    set disabled(value: BooleanInput);
    /** Whether the option is selected. */
    get selected(): boolean;
    set selected(value: BooleanInput);
    constructor(_element: ElementRef<HTMLElement>, _changeDetector: ChangeDetectorRef, 
    /** @docs-private */
    selectionList: MatLegacySelectionList);
    ngOnInit(): void;
    ngAfterContentInit(): void;
    ngOnDestroy(): void;
    /** Toggles the selection state of the option. */
    toggle(): void;
    /** Allows for programmatic focusing of the option. */
    focus(): void;
    /**
     * Returns the list item's text label. Implemented as a part of the FocusKeyManager.
     * @docs-private
     */
    getLabel(): any;
    /** Whether this list item should show a ripple effect when clicked. */
    _isRippleDisabled(): boolean;
    _handleClick(): void;
    _handleFocus(): void;
    _handleBlur(): void;
    /** Retrieves the DOM element of the component host. */
    _getHostElement(): HTMLElement;
    /** Sets the selected state of the option. Returns whether the value has changed. */
    _setSelected(selected: boolean): boolean;
    /**
     * Notifies Angular that the option needs to be checked in the next change detection run. Mainly
     * used to trigger an update of the list option if the disabled state of the selection list
     * changed.
     */
    _markForCheck(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyListOption, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyListOption, "mat-list-option", ["matListOption"], { "disableRipple": { "alias": "disableRipple"; "required": false; }; "checkboxPosition": { "alias": "checkboxPosition"; "required": false; }; "color": { "alias": "color"; "required": false; }; "value": { "alias": "value"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; "selected": { "alias": "selected"; "required": false; }; }, { "selectedChange": "selectedChange"; }, ["_avatar", "_icon", "_lines"], ["*", "[mat-list-avatar], [mat-list-icon], [matListAvatar], [matListIcon]"], false, never>;
}

/**
 * Type describing possible positions of a checkbox in a list option
 * with respect to the list item's text.
 * @deprecated Use `MatListOptionTogglePosition` from `@angular/material/list` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare type MatLegacyListOptionCheckboxPosition = 'before' | 'after';

/**
 * Directive whose purpose is to add the mat- CSS styling to this selector.
 * @docs-private
 * @deprecated Use `MatListSubheaderCssMatStyler` from `@angular/material/list` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyListSubheaderCssMatStyler {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyListSubheaderCssMatStyler, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyListSubheaderCssMatStyler, "[mat-subheader], [matSubheader]", never, {}, {}, never, never, false, never>;
}

/**
 * @deprecated Use `MatNavList` from `@angular/material/list` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyNavList extends _MatListBase implements CanDisable, CanDisableRipple, OnChanges, OnDestroy {
    /** Emits when the state of the list changes. */
    readonly _stateChanges: Subject<void>;
    ngOnChanges(): void;
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyNavList, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyNavList, "mat-nav-list", ["matNavList"], { "disableRipple": { "alias": "disableRipple"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; }, {}, never, ["*"], false, never>;
}

/**
 * Material Design list component where each item is a selectable option. Behaves as a listbox.
 * @deprecated Use `MatSelectionList` from `@angular/material/list` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacySelectionList extends _MatSelectionListBase implements CanDisableRipple, AfterContentInit, ControlValueAccessor, OnDestroy, OnChanges {
    private _element;
    private _changeDetector;
    private _focusMonitor;
    private _multiple;
    private _contentInitialized;
    /** The FocusKeyManager which handles focus. */
    _keyManager: FocusKeyManager<MatLegacyListOption>;
    /** The option components contained within this selection-list. */
    options: QueryList<MatLegacyListOption>;
    /** Emits a change event whenever the selected state of an option changes. */
    readonly selectionChange: EventEmitter<MatLegacySelectionListChange>;
    /** Theme color of the selection list. This sets the checkbox color for all list options. */
    color: ThemePalette;
    /**
     * Function used for comparing an option against the selected value when determining which
     * options should appear as selected. The first argument is the value of an options. The second
     * one is a value from the selected value. A boolean must be returned.
     */
    compareWith: (o1: any, o2: any) => boolean;
    /** Whether the selection list is disabled. */
    get disabled(): boolean;
    set disabled(value: BooleanInput);
    private _disabled;
    /** Whether selection is limited to one or multiple items (default multiple). */
    get multiple(): boolean;
    set multiple(value: BooleanInput);
    /** The currently selected options. */
    selectedOptions: SelectionModel<MatLegacyListOption>;
    /** The tabindex of the selection list. */
    _tabIndex: number;
    /** View to model callback that should be called whenever the selected options change. */
    private _onChange;
    /** Keeps track of the currently-selected value. */
    _value: string[] | null;
    /** Emits when the list has been destroyed. */
    private readonly _destroyed;
    /** View to model callback that should be called if the list or its options lost focus. */
    _onTouched: () => void;
    /** Whether the list has been destroyed. */
    private _isDestroyed;
    constructor(_element: ElementRef<HTMLElement>, _changeDetector: ChangeDetectorRef, _focusMonitor: FocusMonitor);
    ngAfterContentInit(): void;
    ngOnChanges(changes: SimpleChanges): void;
    ngOnDestroy(): void;
    /** Focuses the selection list. */
    focus(options?: FocusOptions): void;
    /** Selects all of the options. Returns the options that changed as a result. */
    selectAll(): MatLegacyListOption[];
    /** Deselects all of the options. Returns the options that changed as a result. */
    deselectAll(): MatLegacyListOption[];
    /** Sets the focused option of the selection-list. */
    _setFocusedOption(option: MatLegacyListOption): void;
    /**
     * Removes an option from the selection list and updates the active item.
     * @returns Currently-active item.
     */
    _removeOptionFromList(option: MatLegacyListOption): MatLegacyListOption | null;
    /** Passes relevant key presses to our key manager. */
    _keydown(event: KeyboardEvent): void;
    /** Reports a value change to the ControlValueAccessor */
    _reportValueChange(): void;
    /** Emits a change event if the selected state of an option changed. */
    _emitChangeEvent(options: MatLegacyListOption[]): void;
    /** Implemented as part of ControlValueAccessor. */
    writeValue(values: string[]): void;
    /** Implemented as a part of ControlValueAccessor. */
    setDisabledState(isDisabled: boolean): void;
    /** Implemented as part of ControlValueAccessor. */
    registerOnChange(fn: (value: any) => void): void;
    /** Implemented as part of ControlValueAccessor. */
    registerOnTouched(fn: () => void): void;
    /** Sets the selected options based on the specified values. */
    private _setOptionsFromValues;
    /** Returns the values of the selected options. */
    private _getSelectedOptionValues;
    /** Toggles the state of the currently focused option if enabled. */
    private _toggleFocusedOption;
    /**
     * Sets the selected state on all of the options
     * and emits an event if anything changed.
     */
    private _setAllOptionsSelected;
    /**
     * Utility to ensure all indexes are valid.
     * @param index The index to be checked.
     * @returns True if the index is valid for our list of options.
     */
    private _isValidIndex;
    /** Returns the index of the specified list option. */
    private _getOptionIndex;
    /** Marks all the options to be checked in the next change detection run. */
    private _markOptionsForCheck;
    /**
     * Removes the tabindex from the selection list and resets it back afterwards, allowing the user
     * to tab out of it. This prevents the list from capturing focus and redirecting it back within
     * the list, creating a focus trap if it user tries to tab away.
     */
    private _allowFocusEscape;
    /** Updates the tabindex based upon if the selection list is empty. */
    private _updateTabIndex;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacySelectionList, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacySelectionList, "mat-selection-list", ["matSelectionList"], { "disableRipple": { "alias": "disableRipple"; "required": false; }; "color": { "alias": "color"; "required": false; }; "compareWith": { "alias": "compareWith"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; "multiple": { "alias": "multiple"; "required": false; }; }, { "selectionChange": "selectionChange"; }, ["options"], ["*"], false, never>;
}

/**
 * Change event that is being fired whenever the selected state of an option changes.
 * @deprecated Use `MatSelectionListChange` from `@angular/material/list` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacySelectionListChange {
    /** Reference to the selection list that emitted the event. */
    source: MatLegacySelectionList;
    /** Reference to the options that have been changed. */
    options: MatLegacyListOption[];
    constructor(
    /** Reference to the selection list that emitted the event. */
    source: MatLegacySelectionList, 
    /** Reference to the options that have been changed. */
    options: MatLegacyListOption[]);
}

/** @docs-private */
declare const _MatListBase: _Constructor<CanDisable> & _AbstractConstructor<CanDisable> & _Constructor<CanDisableRipple> & _AbstractConstructor<CanDisableRipple> & {
    new (): {};
};

/** @docs-private */
declare const _MatListItemMixinBase: _Constructor<CanDisableRipple> & _AbstractConstructor<CanDisableRipple> & {
    new (): {};
};

declare const _MatListOptionBase: _Constructor<CanDisableRipple> & _AbstractConstructor<CanDisableRipple> & {
    new (): {};
};

declare const _MatSelectionListBase: _Constructor<CanDisableRipple> & _AbstractConstructor<CanDisableRipple> & {
    new (): {};
};

export { }
