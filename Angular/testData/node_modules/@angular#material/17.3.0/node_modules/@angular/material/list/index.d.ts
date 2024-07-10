import { AfterViewInit } from '@angular/core';
import { BooleanInput } from '@angular/cdk/coercion';
import { ChangeDetectorRef } from '@angular/core';
import { ControlValueAccessor } from '@angular/forms';
import { ElementRef } from '@angular/core';
import { EventEmitter } from '@angular/core';
import * as i0 from '@angular/core';
import * as i1 from '@angular/cdk/observers';
import * as i11 from '@angular/material/divider';
import * as i2 from '@angular/common';
import * as i3 from '@angular/material/core';
import { InjectionToken } from '@angular/core';
import { NgZone } from '@angular/core';
import { OnChanges } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { OnInit } from '@angular/core';
import { Platform } from '@angular/cdk/platform';
import { QueryList } from '@angular/core';
import { RippleConfig } from '@angular/material/core';
import { RippleGlobalOptions } from '@angular/material/core';
import { RippleTarget } from '@angular/material/core';
import { SelectionModel } from '@angular/cdk/collections';
import { SimpleChanges } from '@angular/core';
import { ThemePalette } from '@angular/material/core';

declare namespace i10 {
    export {
        MatListItemTitle,
        MatListItemLine,
        MatListItemMeta,
        _MatListItemGraphicBase,
        MatListItemAvatar,
        MatListItemIcon
    }
}

declare namespace i4 {
    export {
        MAT_LIST,
        MatList,
        MatListItem
    }
}

declare namespace i5 {
    export {
        MatActionList
    }
}

declare namespace i6 {
    export {
        MAT_NAV_LIST,
        MatNavList
    }
}

declare namespace i7 {
    export {
        MAT_SELECTION_LIST_VALUE_ACCESSOR,
        MatSelectionListChange,
        MatSelectionList
    }
}

declare namespace i8 {
    export {
        SELECTION_LIST,
        SelectionList,
        MatListOption
    }
}

declare namespace i9 {
    export {
        MatListSubheaderCssMatStyler
    }
}

/**
 * Interface describing a list option. This is used to avoid circular
 * dependencies between the list-option and the styler directives.
 * @docs-private
 */
declare interface ListOption {
    _getTogglePosition(): MatListOptionTogglePosition;
}

/**
 * Injection token that can be used to inject instances of `MatList`. It serves as
 * alternative token to the actual `MatList` class which could cause unnecessary
 * retention of the class and its component metadata.
 */
export declare const MAT_LIST: InjectionToken<MatList>;

/** Injection token that can be used to provide the default options for the list module. */
export declare const MAT_LIST_CONFIG: InjectionToken<MatListConfig>;

/**
 * Injection token that can be used to inject instances of `MatNavList`. It serves as
 * alternative token to the actual `MatNavList` class which could cause unnecessary
 * retention of the class and its component metadata.
 */
export declare const MAT_NAV_LIST: InjectionToken<MatNavList>;

export declare const MAT_SELECTION_LIST_VALUE_ACCESSOR: any;

export declare class MatActionList extends MatListBase {
    _isNonInteractive: boolean;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatActionList, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatActionList, "mat-action-list", ["matActionList"], {}, {}, never, ["*"], true, never>;
}

export declare class MatList extends MatListBase {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatList, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatList, "mat-list", ["matList"], {}, {}, never, ["*"], true, never>;
}

declare abstract class MatListBase {
    _isNonInteractive: boolean;
    /** Whether ripples for all list items is disabled. */
    get disableRipple(): boolean;
    set disableRipple(value: BooleanInput);
    private _disableRipple;
    /**
     * Whether the entire list is disabled. When disabled, the list itself and each of its list items
     * are disabled.
     */
    get disabled(): boolean;
    set disabled(value: BooleanInput);
    private _disabled;
    protected _defaultOptions: MatListConfig | null;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatListBase, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatListBase, never, never, { "disableRipple": { "alias": "disableRipple"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; }, {}, never, never, true, never>;
}

/** Object that can be used to configure the default options for the list module. */
export declare interface MatListConfig {
    /** Wheter icon indicators should be hidden for single-selection. */
    hideSingleSelectionIndicator?: boolean;
}

export declare class MatListItem extends MatListItemBase {
    _lines: QueryList<MatListItemLine>;
    _titles: QueryList<MatListItemTitle>;
    _meta: QueryList<MatListItemMeta>;
    _unscopedContent: ElementRef<HTMLSpanElement>;
    _itemText: ElementRef<HTMLElement>;
    /** Indicates whether an item in a `<mat-nav-list>` is the currently active page. */
    get activated(): boolean;
    set activated(activated: boolean);
    _activated: boolean;
    constructor(element: ElementRef, ngZone: NgZone, listBase: MatListBase | null, platform: Platform, globalRippleOptions?: RippleGlobalOptions, animationMode?: string);
    /**
     * Determine the value of `aria-current`. Return 'page' if this item is an activated anchor tag.
     * Otherwise, return `null`. This method is safe to use with server-side rendering.
     */
    _getAriaCurrent(): string | null;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatListItem, [null, null, { optional: true; }, null, { optional: true; }, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatListItem, "mat-list-item, a[mat-list-item], button[mat-list-item]", ["matListItem"], { "activated": { "alias": "activated"; "required": false; }; }, {}, ["_lines", "_titles", "_meta"], ["[matListItemAvatar],[matListItemIcon]", "[matListItemTitle]", "[matListItemLine]", "*", "[matListItemMeta]", "mat-divider"], true, never>;
}

/**
 * Directive matching an optional avatar within a list item.
 *
 * List items can reserve space at the beginning of an item to display an avatar.
 */
export declare class MatListItemAvatar extends _MatListItemGraphicBase {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatListItemAvatar, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatListItemAvatar, "[matListItemAvatar]", never, {}, {}, never, never, true, never>;
}

declare abstract class MatListItemBase implements AfterViewInit, OnDestroy, RippleTarget {
    _elementRef: ElementRef<HTMLElement>;
    protected _ngZone: NgZone;
    private _listBase;
    private _platform;
    /** Query list matching list-item line elements. */
    abstract _lines: QueryList<MatListItemLine> | undefined;
    /** Query list matching list-item title elements. */
    abstract _titles: QueryList<MatListItemTitle> | undefined;
    /**
     * Element reference to the unscoped content in a list item.
     *
     * Unscoped content is user-projected text content in a list item that is
     * not part of an explicit line or title.
     */
    abstract _unscopedContent: ElementRef<HTMLSpanElement> | undefined;
    /** Host element for the list item. */
    _hostElement: HTMLElement;
    /** indicate whether the host element is a button or not */
    _isButtonElement: boolean;
    /** Whether animations are disabled. */
    _noopAnimations: boolean;
    _avatars: QueryList<never>;
    _icons: QueryList<never>;
    /**
     * The number of lines this list item should reserve space for. If not specified,
     * lines are inferred based on the projected content.
     *
     * Explicitly specifying the number of lines is useful if you want to acquire additional
     * space and enable the wrapping of text. The unscoped text content of a list item will
     * always be able to take up the remaining space of the item, unless it represents the title.
     *
     * A maximum of three lines is supported as per the Material Design specification.
     */
    set lines(lines: number | string | null);
    _explicitLines: number | null;
    /** Whether ripples for list items are disabled. */
    get disableRipple(): boolean;
    set disableRipple(value: BooleanInput);
    private _disableRipple;
    /** Whether the list-item is disabled. */
    get disabled(): boolean;
    set disabled(value: BooleanInput);
    private _disabled;
    private _subscriptions;
    private _rippleRenderer;
    /** Whether the list item has unscoped text content. */
    _hasUnscopedTextContent: boolean;
    /**
     * Implemented as part of `RippleTarget`.
     * @docs-private
     */
    rippleConfig: RippleConfig & RippleGlobalOptions;
    /**
     * Implemented as part of `RippleTarget`.
     * @docs-private
     */
    get rippleDisabled(): boolean;
    constructor(_elementRef: ElementRef<HTMLElement>, _ngZone: NgZone, _listBase: MatListBase | null, _platform: Platform, globalRippleOptions?: RippleGlobalOptions, animationMode?: string);
    ngAfterViewInit(): void;
    ngOnDestroy(): void;
    /** Whether the list item has icons or avatars. */
    _hasIconOrAvatar(): boolean;
    private _initInteractiveListItem;
    /**
     * Subscribes to changes in the projected title and lines. Triggers a
     * item lines update whenever a change occurs.
     */
    private _monitorProjectedLinesAndTitle;
    /**
     * Updates the lines of the list item. Based on the projected user content and optional
     * explicit lines setting, the visual appearance of the list item is determined.
     *
     * This method should be invoked whenever the projected user content changes, or
     * when the explicit lines have been updated.
     *
     * @param recheckUnscopedContent Whether the projected unscoped content should be re-checked.
     *   The unscoped content is not re-checked for every update as it is a rather expensive check
     *   for content that is expected to not change very often.
     */
    _updateItemLines(recheckUnscopedContent: boolean): void;
    /**
     * Infers the number of lines based on the projected user content. This is useful
     * if no explicit number of lines has been specified on the list item.
     *
     * The number of lines is inferred based on whether there is a title, the number of
     * additional lines (secondary/tertiary). An additional line is acquired if there is
     * unscoped text content.
     */
    private _inferLinesFromContent;
    /** Checks whether the list item has unscoped text content. */
    private _checkDomForUnscopedTextContent;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatListItemBase, [null, null, { optional: true; }, null, { optional: true; }, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatListItemBase, never, never, { "lines": { "alias": "lines"; "required": false; }; "disableRipple": { "alias": "disableRipple"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; }, {}, ["_avatars", "_icons"], never, true, never>;
}

/**
 * @docs-private
 *
 * MDC uses the very intuitively named classes `.mdc-list-item__start` and `.mat-list-item__end` to
 * position content such as icons or checkboxes/radios that comes either before or after the text
 * content respectively. This directive detects the placement of the checkbox/radio and applies the
 * correct MDC class to position the icon/avatar on the opposite side.
 */
export declare class _MatListItemGraphicBase {
    _listOption: ListOption;
    constructor(_listOption: ListOption);
    _isAlignedAtStart(): boolean;
    static ɵfac: i0.ɵɵFactoryDeclaration<_MatListItemGraphicBase, [{ optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<_MatListItemGraphicBase, never, never, {}, {}, never, never, true, never>;
}

/**
 * Directive matching an optional icon within a list item.
 *
 * List items can reserve space at the beginning of an item to display an icon.
 */
export declare class MatListItemIcon extends _MatListItemGraphicBase {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatListItemIcon, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatListItemIcon, "[matListItemIcon]", never, {}, {}, never, never, true, never>;
}

/**
 * Directive capturing a line in a list item. A list item usually consists of a
 * title and optional secondary or tertiary lines.
 *
 * Text content inside a line never wraps. There can be at maximum two lines per list item.
 */
export declare class MatListItemLine {
    _elementRef: ElementRef<HTMLElement>;
    constructor(_elementRef: ElementRef<HTMLElement>);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatListItemLine, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatListItemLine, "[matListItemLine]", never, {}, {}, never, never, true, never>;
}

/**
 * Directive matching an optional meta section for list items.
 *
 * List items can reserve space at the end of an item to display a control,
 * button or additional text content.
 */
export declare class MatListItemMeta {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatListItemMeta, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatListItemMeta, "[matListItemMeta]", never, {}, {}, never, never, true, never>;
}

/**
 * Directive capturing the title of a list item. A list item usually consists of a
 * title and optional secondary or tertiary lines.
 *
 * Text content for the title never wraps. There can only be a single title per list item.
 */
export declare class MatListItemTitle {
    _elementRef: ElementRef<HTMLElement>;
    constructor(_elementRef: ElementRef<HTMLElement>);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatListItemTitle, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatListItemTitle, "[matListItemTitle]", never, {}, {}, never, never, true, never>;
}

export declare class MatListModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatListModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatListModule, never, [typeof i1.ObserversModule, typeof i2.CommonModule, typeof i3.MatCommonModule, typeof i3.MatRippleModule, typeof i3.MatPseudoCheckboxModule, typeof i4.MatList, typeof i5.MatActionList, typeof i6.MatNavList, typeof i7.MatSelectionList, typeof i4.MatListItem, typeof i8.MatListOption, typeof i9.MatListSubheaderCssMatStyler, typeof i10.MatListItemAvatar, typeof i10.MatListItemIcon, typeof i10.MatListItemLine, typeof i10.MatListItemTitle, typeof i10.MatListItemMeta], [typeof i4.MatList, typeof i5.MatActionList, typeof i6.MatNavList, typeof i7.MatSelectionList, typeof i4.MatListItem, typeof i8.MatListOption, typeof i10.MatListItemAvatar, typeof i10.MatListItemIcon, typeof i9.MatListSubheaderCssMatStyler, typeof i11.MatDividerModule, typeof i10.MatListItemLine, typeof i10.MatListItemTitle, typeof i10.MatListItemMeta]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatListModule>;
}

export declare class MatListOption extends MatListItemBase implements ListOption, OnInit, OnDestroy {
    private _selectionList;
    private _changeDetectorRef;
    _lines: QueryList<MatListItemLine>;
    _titles: QueryList<MatListItemTitle>;
    _unscopedContent: ElementRef<HTMLSpanElement>;
    /**
     * Emits when the selected state of the option has changed.
     * Use to facilitate two-data binding to the `selected` property.
     * @docs-private
     */
    readonly selectedChange: EventEmitter<boolean>;
    /** Whether the label should appear before or after the checkbox/radio. Defaults to 'after' */
    togglePosition: MatListOptionTogglePosition;
    /**
     * Whether the label should appear before or after the checkbox/radio. Defaults to 'after'
     *
     * @deprecated Use `togglePosition` instead.
     * @breaking-change 17.0.0
     */
    get checkboxPosition(): MatListOptionTogglePosition;
    set checkboxPosition(value: MatListOptionTogglePosition);
    /** Theme color of the list option. This sets the color of the checkbox/radio. */
    get color(): ThemePalette;
    set color(newValue: ThemePalette);
    private _color;
    /** Value of the option */
    get value(): any;
    set value(newValue: any);
    private _value;
    /** Whether the option is selected. */
    get selected(): boolean;
    set selected(value: BooleanInput);
    private _selected;
    /**
     * This is set to true after the first OnChanges cycle so we don't
     * clear the value of `selected` in the first cycle.
     */
    private _inputsInitialized;
    constructor(elementRef: ElementRef<HTMLElement>, ngZone: NgZone, _selectionList: SelectionList, platform: Platform, _changeDetectorRef: ChangeDetectorRef, globalRippleOptions?: RippleGlobalOptions, animationMode?: string);
    ngOnInit(): void;
    ngOnDestroy(): void;
    /** Toggles the selection state of the option. */
    toggle(): void;
    /** Allows for programmatic focusing of the option. */
    focus(): void;
    /** Gets the text label of the list option. Used for the typeahead functionality in the list. */
    getLabel(): string;
    /** Whether a checkbox is shown at the given position. */
    _hasCheckboxAt(position: MatListOptionTogglePosition): boolean;
    /** Where a radio indicator is shown at the given position. */
    _hasRadioAt(position: MatListOptionTogglePosition): boolean;
    /** Whether icons or avatars are shown at the given position. */
    _hasIconsOrAvatarsAt(position: 'before' | 'after'): boolean;
    /** Gets whether the given type of element is projected at the specified position. */
    _hasProjected(type: 'icons' | 'avatars', position: 'before' | 'after'): boolean;
    _handleBlur(): void;
    /** Gets the current position of the checkbox/radio. */
    _getTogglePosition(): MatListOptionTogglePosition;
    /**
     * Sets the selected state of the option.
     * @returns Whether the value has changed.
     */
    _setSelected(selected: boolean): boolean;
    /**
     * Notifies Angular that the option needs to be checked in the next change detection run.
     * Mainly used to trigger an update of the list option if the disabled state of the selection
     * list changed.
     */
    _markForCheck(): void;
    /** Toggles the option's value based on a user interaction. */
    _toggleOnInteraction(): void;
    /** Sets the tabindex of the list option. */
    _setTabindex(value: number): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatListOption, [null, null, null, null, null, { optional: true; }, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatListOption, "mat-list-option", ["matListOption"], { "togglePosition": { "alias": "togglePosition"; "required": false; }; "checkboxPosition": { "alias": "checkboxPosition"; "required": false; }; "color": { "alias": "color"; "required": false; }; "value": { "alias": "value"; "required": false; }; "selected": { "alias": "selected"; "required": false; }; }, { "selectedChange": "selectedChange"; }, ["_lines", "_titles"], ["[matListItemAvatar],[matListItemIcon]", "[matListItemTitle]", "[matListItemLine]", "*", "mat-divider"], true, never>;
}

/**
 * Type describing possible positions of a checkbox or radio in a list option
 * with respect to the list item's text.
 */
declare type MatListOptionTogglePosition = 'before' | 'after';
export { MatListOptionTogglePosition as MatListOptionCheckboxPosition }
export { MatListOptionTogglePosition }

/**
 * Directive whose purpose is to add the mat- CSS styling to this selector.
 * @docs-private
 */
export declare class MatListSubheaderCssMatStyler {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatListSubheaderCssMatStyler, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatListSubheaderCssMatStyler, "[mat-subheader], [matSubheader]", never, {}, {}, never, never, true, never>;
}

export declare class MatNavList extends MatListBase {
    _isNonInteractive: boolean;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatNavList, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatNavList, "mat-nav-list", ["matNavList"], {}, {}, never, ["*"], true, never>;
}

export declare class MatSelectionList extends MatListBase implements SelectionList, ControlValueAccessor, AfterViewInit, OnChanges, OnDestroy {
    _element: ElementRef<HTMLElement>;
    private _ngZone;
    private _initialized;
    private _keyManager;
    /** Emits when the list has been destroyed. */
    private _destroyed;
    /** Whether the list has been destroyed. */
    private _isDestroyed;
    /** View to model callback that should be called whenever the selected options change. */
    private _onChange;
    _items: QueryList<MatListOption>;
    /** Emits a change event whenever the selected state of an option changes. */
    readonly selectionChange: EventEmitter<MatSelectionListChange>;
    /** Theme color of the selection list. This sets the checkbox color for all list options. */
    color: ThemePalette;
    /**
     * Function used for comparing an option against the selected value when determining which
     * options should appear as selected. The first argument is the value of an options. The second
     * one is a value from the selected value. A boolean must be returned.
     */
    compareWith: (o1: any, o2: any) => boolean;
    /** Whether selection is limited to one or multiple items (default multiple). */
    get multiple(): boolean;
    set multiple(value: BooleanInput);
    private _multiple;
    /** Whether radio indicator for all list items is hidden. */
    get hideSingleSelectionIndicator(): boolean;
    set hideSingleSelectionIndicator(value: BooleanInput);
    private _hideSingleSelectionIndicator;
    /** The currently selected options. */
    selectedOptions: SelectionModel<MatListOption>;
    /** Keeps track of the currently-selected value. */
    _value: string[] | null;
    /** View to model callback that should be called if the list or its options lost focus. */
    _onTouched: () => void;
    constructor(_element: ElementRef<HTMLElement>, _ngZone: NgZone);
    ngAfterViewInit(): void;
    ngOnChanges(changes: SimpleChanges): void;
    ngOnDestroy(): void;
    /** Focuses the selection list. */
    focus(options?: FocusOptions): void;
    /** Selects all of the options. Returns the options that changed as a result. */
    selectAll(): MatListOption[];
    /** Deselects all of the options. Returns the options that changed as a result. */
    deselectAll(): MatListOption[];
    /** Reports a value change to the ControlValueAccessor */
    _reportValueChange(): void;
    /** Emits a change event if the selected state of an option changed. */
    _emitChangeEvent(options: MatListOption[]): void;
    /** Implemented as part of ControlValueAccessor. */
    writeValue(values: string[]): void;
    /** Implemented as a part of ControlValueAccessor. */
    setDisabledState(isDisabled: boolean): void;
    /**
     * Whether the *entire* selection list is disabled. When true, each list item is also disabled
     * and each list item is removed from the tab order (has tabindex="-1").
     */
    get disabled(): boolean;
    set disabled(value: BooleanInput);
    private _selectionListDisabled;
    /** Implemented as part of ControlValueAccessor. */
    registerOnChange(fn: (value: any) => void): void;
    /** Implemented as part of ControlValueAccessor. */
    registerOnTouched(fn: () => void): void;
    /** Watches for changes in the selected state of the options and updates the list accordingly. */
    private _watchForSelectionChange;
    /** Sets the selected options based on the specified values. */
    private _setOptionsFromValues;
    /** Returns the values of the selected options. */
    private _getSelectedOptionValues;
    /** Marks all the options to be checked in the next change detection run. */
    private _markOptionsForCheck;
    /**
     * Sets the selected state on all of the options
     * and emits an event if anything changed.
     */
    private _setAllOptionsSelected;
    /** The option components contained within this selection-list. */
    get options(): QueryList<MatListOption>;
    /** Handles keydown events within the list. */
    _handleKeydown(event: KeyboardEvent): void;
    /** Handles focusout events within the list. */
    private _handleFocusout;
    /** Handles focusin events within the list. */
    private _handleFocusin;
    /**
     * Sets up the logic for maintaining the roving tabindex.
     *
     * `skipPredicate` determines if key manager should avoid putting a given list item in the tab
     * index. Allow disabled list items to receive focus to align with WAI ARIA recommendation.
     * Normally WAI ARIA's instructions are to exclude disabled items from the tab order, but it
     * makes a few exceptions for compound widgets.
     *
     * From [Developing a Keyboard Interface](
     * https://www.w3.org/WAI/ARIA/apg/practices/keyboard-interface/):
     *   "For the following composite widget elements, keep them focusable when disabled: Options in a
     *   Listbox..."
     */
    private _setupRovingTabindex;
    /**
     * Sets an option as active.
     * @param index Index of the active option. If set to -1, no option will be active.
     */
    private _setActiveOption;
    /**
     * Resets the active option. When the list is disabled, remove all options from to the tab order.
     * Otherwise, focus the first selected option.
     */
    private _resetActiveOption;
    /** Returns whether the focus is currently within the list. */
    private _containsFocus;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatSelectionList, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatSelectionList, "mat-selection-list", ["matSelectionList"], { "color": { "alias": "color"; "required": false; }; "compareWith": { "alias": "compareWith"; "required": false; }; "multiple": { "alias": "multiple"; "required": false; }; "hideSingleSelectionIndicator": { "alias": "hideSingleSelectionIndicator"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; }, { "selectionChange": "selectionChange"; }, ["_items"], ["*"], true, never>;
}

/** Change event that is being fired whenever the selected state of an option changes. */
export declare class MatSelectionListChange {
    /** Reference to the selection list that emitted the event. */
    source: MatSelectionList;
    /** Reference to the options that have been changed. */
    options: MatListOption[];
    constructor(
    /** Reference to the selection list that emitted the event. */
    source: MatSelectionList, 
    /** Reference to the options that have been changed. */
    options: MatListOption[]);
}

/**
 * Injection token that can be used to reference instances of an `SelectionList`. It serves
 * as alternative token to an actual implementation which would result in circular references.
 * @docs-private
 */
export declare const SELECTION_LIST: InjectionToken<SelectionList>;

/**
 * Interface describing the containing list of an list option. This is used to avoid
 * circular dependencies between the list-option and the selection list.
 * @docs-private
 */
export declare interface SelectionList extends MatListBase {
    multiple: boolean;
    color: ThemePalette;
    selectedOptions: SelectionModel<MatListOption>;
    hideSingleSelectionIndicator: boolean;
    compareWith: (o1: any, o2: any) => boolean;
    _value: string[] | null;
    _reportValueChange(): void;
    _emitChangeEvent(options: MatListOption[]): void;
    _onTouched(): void;
}

export { }
