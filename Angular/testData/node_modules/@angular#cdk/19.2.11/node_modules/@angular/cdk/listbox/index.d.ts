import * as i0 from '@angular/core';
import { OnDestroy, AfterContentInit, QueryList, NgZone, ChangeDetectorRef } from '@angular/core';
import { Highlightable, ActiveDescendantKeyManager } from '../activedescendant-key-manager.d-DSYvyoT0.js';
import { ListKeyManagerOption } from '../list-key-manager.d-CylnKWfo.js';
import { Subject } from 'rxjs';
import { SelectionModel } from '../selection-model.d-mtbiEbzs.js';
import { ControlValueAccessor } from '@angular/forms';

/**
 * An implementation of SelectionModel that internally always represents the selection as a
 * multi-selection. This is necessary so that we can recover the full selection if the user
 * switches the listbox from single-selection to multi-selection after initialization.
 *
 * This selection model may report multiple selected values, even if it is in single-selection
 * mode. It is up to the user (CdkListbox) to check for invalid selections.
 */
declare class ListboxSelectionModel<T> extends SelectionModel<T> {
    multiple: boolean;
    constructor(multiple?: boolean, initiallySelectedValues?: T[], emitChanges?: boolean, compareWith?: (o1: T, o2: T) => boolean);
    isMultipleSelection(): boolean;
    select(...values: T[]): boolean | void;
}
/** A selectable option in a listbox. */
declare class CdkOption<T = unknown> implements ListKeyManagerOption, Highlightable, OnDestroy {
    /** The id of the option's host element. */
    get id(): string;
    set id(value: string);
    private _id;
    private _generatedId;
    /** The value of this option. */
    value: T;
    /**
     * The text used to locate this item during listbox typeahead. If not specified,
     * the `textContent` of the item will be used.
     */
    typeaheadLabel: string | null;
    /** Whether this option is disabled. */
    get disabled(): boolean;
    set disabled(value: boolean);
    private _disabled;
    /** The tabindex of the option when it is enabled. */
    get enabledTabIndex(): number | null | undefined;
    set enabledTabIndex(value: number | null | undefined);
    private _enabledTabIndex;
    /** The option's host element */
    readonly element: HTMLElement;
    /** The parent listbox this option belongs to. */
    protected readonly listbox: CdkListbox<T>;
    /** Emits when the option is destroyed. */
    protected destroyed: Subject<void>;
    /** Emits when the option is clicked. */
    readonly _clicked: Subject<MouseEvent>;
    ngOnDestroy(): void;
    /** Whether this option is selected. */
    isSelected(): boolean;
    /** Whether this option is active. */
    isActive(): boolean;
    /** Toggle the selected state of this option. */
    toggle(): void;
    /** Select this option if it is not selected. */
    select(): void;
    /** Deselect this option if it is selected. */
    deselect(): void;
    /** Focus this option. */
    focus(): void;
    /** Get the label for this element which is required by the FocusableOption interface. */
    getLabel(): string;
    /**
     * No-op implemented as a part of `Highlightable`.
     * @docs-private
     */
    setActiveStyles(): void;
    /**
     * No-op implemented as a part of `Highlightable`.
     * @docs-private
     */
    setInactiveStyles(): void;
    /** Handle focus events on the option. */
    protected _handleFocus(): void;
    /** Get the tabindex for this option. */
    protected _getTabIndex(): number | null | undefined;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkOption<any>, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkOption<any>, "[cdkOption]", ["cdkOption"], { "id": { "alias": "id"; "required": false; }; "value": { "alias": "cdkOption"; "required": false; }; "typeaheadLabel": { "alias": "cdkOptionTypeaheadLabel"; "required": false; }; "disabled": { "alias": "cdkOptionDisabled"; "required": false; }; "enabledTabIndex": { "alias": "tabindex"; "required": false; }; }, {}, never, never, true, never>;
    static ngAcceptInputType_disabled: unknown;
}
declare class CdkListbox<T = unknown> implements AfterContentInit, OnDestroy, ControlValueAccessor {
    private _cleanupWindowBlur;
    /** The id of the option's host element. */
    get id(): string;
    set id(value: string);
    private _id;
    private _generatedId;
    /** The tabindex to use when the listbox is enabled. */
    get enabledTabIndex(): number | null | undefined;
    set enabledTabIndex(value: number | null | undefined);
    private _enabledTabIndex;
    /** The value selected in the listbox, represented as an array of option values. */
    get value(): readonly T[];
    set value(value: readonly T[]);
    /**
     * Whether the listbox allows multiple options to be selected. If the value switches from `true`
     * to `false`, and more than one option is selected, all options are deselected.
     */
    get multiple(): boolean;
    set multiple(value: boolean);
    /** Whether the listbox is disabled. */
    get disabled(): boolean;
    set disabled(value: boolean);
    private _disabled;
    /** Whether the listbox will use active descendant or will move focus onto the options. */
    get useActiveDescendant(): boolean;
    set useActiveDescendant(value: boolean);
    private _useActiveDescendant;
    /** The orientation of the listbox. Only affects keyboard interaction, not visual layout. */
    get orientation(): "horizontal" | "vertical";
    set orientation(value: 'horizontal' | 'vertical');
    private _orientation;
    /** The function used to compare option values. */
    get compareWith(): undefined | ((o1: T, o2: T) => boolean);
    set compareWith(fn: undefined | ((o1: T, o2: T) => boolean));
    /**
     * Whether the keyboard navigation should wrap when the user presses arrow down on the last item
     * or arrow up on the first item.
     */
    get navigationWrapDisabled(): boolean;
    set navigationWrapDisabled(wrap: boolean);
    private _navigationWrapDisabled;
    /** Whether keyboard navigation should skip over disabled items. */
    get navigateDisabledOptions(): boolean;
    set navigateDisabledOptions(skip: boolean);
    private _navigateDisabledOptions;
    /** Emits when the selected value(s) in the listbox change. */
    readonly valueChange: Subject<ListboxValueChangeEvent<T>>;
    /** The child options in this listbox. */
    protected options: QueryList<CdkOption<T>>;
    /** The selection model used by the listbox. */
    protected selectionModel: ListboxSelectionModel<T>;
    /** The key manager that manages keyboard navigation for this listbox. */
    protected listKeyManager: ActiveDescendantKeyManager<CdkOption<T>>;
    /** Emits when the listbox is destroyed. */
    protected readonly destroyed: Subject<void>;
    /** The host element of the listbox. */
    protected readonly element: HTMLElement;
    /** The Angular zone. */
    protected readonly ngZone: NgZone;
    /** The change detector for this listbox. */
    protected readonly changeDetectorRef: ChangeDetectorRef;
    /** Whether the currently selected value in the selection model is invalid. */
    private _invalid;
    /** The last user-triggered option. */
    private _lastTriggered;
    /** Callback called when the listbox has been touched */
    private _onTouched;
    /** Callback called when the listbox value changes */
    private _onChange;
    /** Emits when an option has been clicked. */
    private _optionClicked;
    /** The directionality of the page. */
    private readonly _dir;
    /** Whether the component is being rendered in the browser. */
    private readonly _isBrowser;
    /** A predicate that skips disabled options. */
    private readonly _skipDisabledPredicate;
    /** A predicate that does not skip any options. */
    private readonly _skipNonePredicate;
    /** Whether the listbox currently has focus. */
    private _hasFocus;
    /** A reference to the option that was active before the listbox lost focus. */
    private _previousActiveOption;
    constructor();
    ngAfterContentInit(): void;
    ngOnDestroy(): void;
    /**
     * Toggle the selected state of the given option.
     * @param option The option to toggle
     */
    toggle(option: CdkOption<T>): void;
    /**
     * Toggle the selected state of the given value.
     * @param value The value to toggle
     */
    toggleValue(value: T): void;
    /**
     * Select the given option.
     * @param option The option to select
     */
    select(option: CdkOption<T>): void;
    /**
     * Select the given value.
     * @param value The value to select
     */
    selectValue(value: T): void;
    /**
     * Deselect the given option.
     * @param option The option to deselect
     */
    deselect(option: CdkOption<T>): void;
    /**
     * Deselect the given value.
     * @param value The value to deselect
     */
    deselectValue(value: T): void;
    /**
     * Set the selected state of all options.
     * @param isSelected The new selected state to set
     */
    setAllSelected(isSelected: boolean): void;
    /**
     * Get whether the given option is selected.
     * @param option The option to get the selected state of
     */
    isSelected(option: CdkOption<T>): boolean;
    /**
     * Get whether the given option is active.
     * @param option The option to get the active state of
     */
    isActive(option: CdkOption<T>): boolean;
    /**
     * Get whether the given value is selected.
     * @param value The value to get the selected state of
     */
    isValueSelected(value: T): boolean;
    /**
     * Registers a callback to be invoked when the listbox's value changes from user input.
     * @param fn The callback to register
     * @docs-private
     */
    registerOnChange(fn: (value: readonly T[]) => void): void;
    /**
     * Registers a callback to be invoked when the listbox is blurred by the user.
     * @param fn The callback to register
     * @docs-private
     */
    registerOnTouched(fn: () => {}): void;
    /**
     * Sets the listbox's value.
     * @param value The new value of the listbox
     * @docs-private
     */
    writeValue(value: readonly T[]): void;
    /**
     * Sets the disabled state of the listbox.
     * @param isDisabled The new disabled state
     * @docs-private
     */
    setDisabledState(isDisabled: boolean): void;
    /** Focus the listbox's host element. */
    focus(): void;
    /**
     * Triggers the given option in response to user interaction.
     * - In single selection mode: selects the option and deselects any other selected option.
     * - In multi selection mode: toggles the selected state of the option.
     * @param option The option to trigger
     */
    protected triggerOption(option: CdkOption<T> | null): void;
    /**
     * Trigger the given range of options in response to user interaction.
     * Should only be called in multi-selection mode.
     * @param trigger The option that was triggered
     * @param from The start index of the options to toggle
     * @param to The end index of the options to toggle
     * @param on Whether to toggle the option range on
     */
    protected triggerRange(trigger: CdkOption<T> | null, from: number, to: number, on: boolean): void;
    /**
     * Sets the given option as active.
     * @param option The option to make active
     */
    _setActiveOption(option: CdkOption<T>): void;
    /** Called when the listbox receives focus. */
    protected _handleFocus(): void;
    /** Called when the user presses keydown on the listbox. */
    protected _handleKeydown(event: KeyboardEvent): void;
    /** Called when a focus moves into the listbox. */
    protected _handleFocusIn(): void;
    /**
     * Called when the focus leaves an element in the listbox.
     * @param event The focusout event
     */
    protected _handleFocusOut(event: FocusEvent): void;
    /** Get the id of the active option if active descendant is being used. */
    protected _getAriaActiveDescendant(): string | null | undefined;
    /** Get the tabindex for the listbox. */
    protected _getTabIndex(): number | null | undefined;
    /** Initialize the key manager. */
    private _initKeyManager;
    /** Focus the active option. */
    private _focusActiveOption;
    /**
     * Set the selected values.
     * @param value The list of new selected values.
     */
    private _setSelection;
    /** Sets the first selected option as first in the keyboard focus order. */
    private _setNextFocusToSelectedOption;
    /** Update the internal value of the listbox based on the selection model. */
    private _updateInternalValue;
    /**
     * Gets the index of the given value in the given list of options.
     * @param cache The cache of indices found so far
     * @param value The value to find
     * @return The index of the value in the options list
     */
    private _getIndexForValue;
    /**
     * Handle the user clicking an option.
     * @param option The option that was clicked.
     */
    private _handleOptionClicked;
    /** Verifies that no two options represent the same value under the compareWith function. */
    private _verifyNoOptionValueCollisions;
    /** Verifies that the option values are valid. */
    private _verifyOptionValues;
    /**
     * Coerces a value into an array representing a listbox selection.
     * @param value The value to coerce
     * @return An array
     */
    private _coerceValue;
    /**
     * Get the sublist of values that do not represent valid option values in this listbox.
     * @param values The list of values
     * @return The sublist of values that are not valid option values
     */
    private _getInvalidOptionValues;
    /** Get the index of the last triggered option. */
    private _getLastTriggeredIndex;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkListbox<any>, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkListbox<any>, "[cdkListbox]", ["cdkListbox"], { "id": { "alias": "id"; "required": false; }; "enabledTabIndex": { "alias": "tabindex"; "required": false; }; "value": { "alias": "cdkListboxValue"; "required": false; }; "multiple": { "alias": "cdkListboxMultiple"; "required": false; }; "disabled": { "alias": "cdkListboxDisabled"; "required": false; }; "useActiveDescendant": { "alias": "cdkListboxUseActiveDescendant"; "required": false; }; "orientation": { "alias": "cdkListboxOrientation"; "required": false; }; "compareWith": { "alias": "cdkListboxCompareWith"; "required": false; }; "navigationWrapDisabled": { "alias": "cdkListboxNavigationWrapDisabled"; "required": false; }; "navigateDisabledOptions": { "alias": "cdkListboxNavigatesDisabledOptions"; "required": false; }; }, { "valueChange": "cdkListboxValueChange"; }, ["options"], never, true, never>;
    static ngAcceptInputType_multiple: unknown;
    static ngAcceptInputType_disabled: unknown;
    static ngAcceptInputType_useActiveDescendant: unknown;
    static ngAcceptInputType_navigationWrapDisabled: unknown;
    static ngAcceptInputType_navigateDisabledOptions: unknown;
}
/** Change event that is fired whenever the value of the listbox changes. */
interface ListboxValueChangeEvent<T> {
    /** The new value of the listbox. */
    readonly value: readonly T[];
    /** Reference to the listbox that emitted the event. */
    readonly listbox: CdkListbox<T>;
    /** Reference to the option that was triggered. */
    readonly option: CdkOption<T> | null;
}

declare class CdkListboxModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkListboxModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<CdkListboxModule, never, [typeof CdkListbox, typeof CdkOption], [typeof CdkListbox, typeof CdkOption]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<CdkListboxModule>;
}

export { CdkListbox, CdkListboxModule, CdkOption };
export type { ListboxValueChangeEvent };
