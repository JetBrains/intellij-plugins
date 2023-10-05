import { _AbstractConstructor } from '@angular/material/core';
import { ActiveDescendantKeyManager } from '@angular/cdk/a11y';
import { AfterContentInit } from '@angular/core';
import { AnimationTriggerMetadata } from '@angular/animations';
import { BooleanInput } from '@angular/cdk/coercion';
import { CanDisable } from '@angular/material/core';
import { CanDisableRipple } from '@angular/material/core';
import { CanUpdateErrorState } from '@angular/material/core';
import { CdkConnectedOverlay } from '@angular/cdk/overlay';
import { CdkOverlayOrigin } from '@angular/cdk/overlay';
import { ChangeDetectorRef } from '@angular/core';
import { ConnectedPosition } from '@angular/cdk/overlay';
import { _Constructor } from '@angular/material/core';
import { ControlValueAccessor } from '@angular/forms';
import { Directionality } from '@angular/cdk/bidi';
import { DoCheck } from '@angular/core';
import { ElementRef } from '@angular/core';
import { ErrorStateMatcher } from '@angular/material/core';
import { EventEmitter } from '@angular/core';
import { FormGroupDirective } from '@angular/forms';
import { HasTabIndex } from '@angular/material/core';
import * as i0 from '@angular/core';
import * as i2 from '@angular/common';
import * as i3 from '@angular/cdk/overlay';
import * as i4 from '@angular/material/core';
import * as i5 from '@angular/cdk/scrolling';
import * as i6 from '@angular/material/form-field';
import { InjectionToken } from '@angular/core';
import { LiveAnnouncer } from '@angular/cdk/a11y';
import { MatFormField } from '@angular/material/form-field';
import { MatFormFieldControl } from '@angular/material/form-field';
import { MatOptgroup } from '@angular/material/core';
import { MatOption } from '@angular/material/core';
import { _MatOptionBase } from '@angular/material/core';
import { MatOptionSelectionChange } from '@angular/material/core';
import { NgControl } from '@angular/forms';
import { NgForm } from '@angular/forms';
import { NgZone } from '@angular/core';
import { NumberInput } from '@angular/cdk/coercion';
import { Observable } from 'rxjs';
import { OnChanges } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { OnInit } from '@angular/core';
import { Overlay } from '@angular/cdk/overlay';
import { QueryList } from '@angular/core';
import { ScrollStrategy } from '@angular/cdk/overlay';
import { SelectionModel } from '@angular/cdk/collections';
import { SimpleChanges } from '@angular/core';
import { Subject } from 'rxjs';
import { ViewportRuler } from '@angular/cdk/scrolling';

declare namespace i1 {
    export {
        MAT_SELECT_SCROLL_STRATEGY_PROVIDER_FACTORY,
        MAT_SELECT_SCROLL_STRATEGY,
        MatSelectConfig,
        MAT_SELECT_CONFIG,
        MAT_SELECT_SCROLL_STRATEGY_PROVIDER,
        MAT_SELECT_TRIGGER,
        MatSelectChange,
        _MatSelectBase,
        MatSelectTrigger,
        MatSelect
    }
}

/** Injection token that can be used to provide the default options the select module. */
export declare const MAT_SELECT_CONFIG: InjectionToken<MatSelectConfig>;

/** Injection token that determines the scroll handling while a select is open. */
export declare const MAT_SELECT_SCROLL_STRATEGY: InjectionToken<() => ScrollStrategy>;

/** @docs-private */
export declare const MAT_SELECT_SCROLL_STRATEGY_PROVIDER: {
    provide: InjectionToken<() => ScrollStrategy>;
    deps: (typeof Overlay)[];
    useFactory: typeof MAT_SELECT_SCROLL_STRATEGY_PROVIDER_FACTORY;
};

/** @docs-private */
export declare function MAT_SELECT_SCROLL_STRATEGY_PROVIDER_FACTORY(overlay: Overlay): () => ScrollStrategy;

/**
 * Injection token that can be used to reference instances of `MatSelectTrigger`. It serves as
 * alternative token to the actual `MatSelectTrigger` class which could cause unnecessary
 * retention of the class and its directive metadata.
 */
export declare const MAT_SELECT_TRIGGER: InjectionToken<MatSelectTrigger>;

export declare class MatSelect extends _MatSelectBase<MatSelectChange> implements OnInit {
    options: QueryList<MatOption>;
    optionGroups: QueryList<MatOptgroup>;
    customTrigger: MatSelectTrigger;
    /**
     * Width of the panel. If set to `auto`, the panel will match the trigger width.
     * If set to null or an empty string, the panel will grow to match the longest option's text.
     */
    panelWidth: string | number | null;
    _positions: ConnectedPosition[];
    /** Ideal origin for the overlay panel. */
    _preferredOverlayOrigin: CdkOverlayOrigin | ElementRef | undefined;
    /** Width of the overlay panel. */
    _overlayWidth: string | number;
    get shouldLabelFloat(): boolean;
    ngOnInit(): void;
    open(): void;
    close(): void;
    /** Scrolls the active option into view. */
    protected _scrollOptionIntoView(index: number): void;
    protected _positioningSettled(): void;
    protected _getChangeEvent(value: any): MatSelectChange;
    /** Gets how wide the overlay panel should be. */
    private _getOverlayWidth;
    /** Whether checkmark indicator for single-selection options is hidden. */
    get hideSingleSelectionIndicator(): boolean;
    set hideSingleSelectionIndicator(value: BooleanInput);
    private _hideSingleSelectionIndicator;
    /** Syncs the parent state with the individual options. */
    _syncParentProperties(): void;
    protected _skipPredicate: (option: MatOption) => boolean;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatSelect, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatSelect, "mat-select", ["matSelect"], { "disabled": { "alias": "disabled"; "required": false; }; "disableRipple": { "alias": "disableRipple"; "required": false; }; "tabIndex": { "alias": "tabIndex"; "required": false; }; "panelWidth": { "alias": "panelWidth"; "required": false; }; "hideSingleSelectionIndicator": { "alias": "hideSingleSelectionIndicator"; "required": false; }; }, {}, ["customTrigger", "options", "optionGroups"], ["mat-select-trigger", "*"], false, never>;
}

/**
 * The following are all the animations for the mat-select component, with each
 * const containing the metadata for one animation.
 *
 * The values below match the implementation of the AngularJS Material mat-select animation.
 * @docs-private
 */
export declare const matSelectAnimations: {
    /**
     * @deprecated No longer being used. To be removed.
     * @breaking-change 12.0.0
     */
    readonly transformPanelWrap: AnimationTriggerMetadata;
    readonly transformPanel: AnimationTriggerMetadata;
};

/** Base class with all of the `MatSelect` functionality. */
export declare abstract class _MatSelectBase<C> extends _MatSelectMixinBase implements AfterContentInit, OnChanges, OnDestroy, OnInit, DoCheck, ControlValueAccessor, CanDisable, HasTabIndex, MatFormFieldControl<any>, CanUpdateErrorState, CanDisableRipple {
    protected _viewportRuler: ViewportRuler;
    protected _changeDetectorRef: ChangeDetectorRef;
    protected _ngZone: NgZone;
    private _dir;
    protected _parentFormField: MatFormField;
    private _liveAnnouncer;
    protected _defaultOptions?: MatSelectConfig | undefined;
    /** All of the defined select options. */
    abstract options: QueryList<_MatOptionBase>;
    /** All of the defined groups of options. */
    abstract optionGroups: QueryList<MatOptgroup>;
    /** User-supplied override of the trigger element. */
    abstract customTrigger: {};
    /**
     * This position config ensures that the top "start" corner of the overlay
     * is aligned with with the top "start" of the origin by default (overlapping
     * the trigger completely). If the panel cannot fit below the trigger, it
     * will fall back to a position above the trigger.
     */
    abstract _positions: ConnectedPosition[];
    /** Scrolls a particular option into the view. */
    protected abstract _scrollOptionIntoView(index: number): void;
    /** Called when the panel has been opened and the overlay has settled on its final position. */
    protected abstract _positioningSettled(): void;
    /** Creates a change event object that should be emitted by the select. */
    protected abstract _getChangeEvent(value: any): C;
    /** Factory function used to create a scroll strategy for this select. */
    private _scrollStrategyFactory;
    /** Whether or not the overlay panel is open. */
    private _panelOpen;
    /** Comparison function to specify which option is displayed. Defaults to object equality. */
    private _compareWith;
    /** Unique id for this input. */
    private _uid;
    /** Current `aria-labelledby` value for the select trigger. */
    private _triggerAriaLabelledBy;
    /**
     * Keeps track of the previous form control assigned to the select.
     * Used to detect if it has changed.
     */
    private _previousControl;
    /** Emits whenever the component is destroyed. */
    protected readonly _destroy: Subject<void>;
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    userAriaDescribedBy: string;
    /** Deals with the selection logic. */
    _selectionModel: SelectionModel<MatOption>;
    /** Manages keyboard events for options in the panel. */
    _keyManager: ActiveDescendantKeyManager<MatOption>;
    /** `View -> model callback called when value changes` */
    _onChange: (value: any) => void;
    /** `View -> model callback called when select has been touched` */
    _onTouched: () => void;
    /** ID for the DOM node containing the select's value. */
    _valueId: string;
    /** Emits when the panel element is finished transforming in. */
    readonly _panelDoneAnimatingStream: Subject<string>;
    /** Strategy that will be used to handle scrolling while the select panel is open. */
    _scrollStrategy: ScrollStrategy;
    _overlayPanelClass: string | string[];
    /** Whether the select is focused. */
    get focused(): boolean;
    private _focused;
    /** A name for this control that can be used by `mat-form-field`. */
    controlType: string;
    /** Trigger that opens the select. */
    trigger: ElementRef;
    /** Panel containing the select options. */
    panel: ElementRef;
    /** Overlay pane containing the options. */
    protected _overlayDir: CdkConnectedOverlay;
    /** Classes to be passed to the select panel. Supports the same syntax as `ngClass`. */
    panelClass: string | string[] | Set<string> | {
        [key: string]: any;
    };
    /** Placeholder to be shown if no value has been selected. */
    get placeholder(): string;
    set placeholder(value: string);
    private _placeholder;
    /** Whether the component is required. */
    get required(): boolean;
    set required(value: BooleanInput);
    private _required;
    /** Whether the user should be allowed to select multiple options. */
    get multiple(): boolean;
    set multiple(value: BooleanInput);
    private _multiple;
    /** Whether to center the active option over the trigger. */
    get disableOptionCentering(): boolean;
    set disableOptionCentering(value: BooleanInput);
    private _disableOptionCentering;
    /**
     * Function to compare the option values with the selected values. The first argument
     * is a value from an option. The second is a value from the selection. A boolean
     * should be returned.
     */
    get compareWith(): (o1: any, o2: any) => boolean;
    set compareWith(fn: (o1: any, o2: any) => boolean);
    /** Value of the select control. */
    get value(): any;
    set value(newValue: any);
    private _value;
    /** Aria label of the select. */
    ariaLabel: string;
    /** Input that can be used to specify the `aria-labelledby` attribute. */
    ariaLabelledby: string;
    /** Object used to control when error messages are shown. */
    errorStateMatcher: ErrorStateMatcher;
    /** Time to wait in milliseconds after the last keystroke before moving focus to an item. */
    get typeaheadDebounceInterval(): number;
    set typeaheadDebounceInterval(value: NumberInput);
    private _typeaheadDebounceInterval;
    /**
     * Function used to sort the values in a select in multiple mode.
     * Follows the same logic as `Array.prototype.sort`.
     */
    sortComparator: (a: MatOption, b: MatOption, options: MatOption[]) => number;
    /** Unique id of the element. */
    get id(): string;
    set id(value: string);
    private _id;
    /** Combined stream of all of the child options' change events. */
    readonly optionSelectionChanges: Observable<MatOptionSelectionChange>;
    /** Event emitted when the select panel has been toggled. */
    readonly openedChange: EventEmitter<boolean>;
    /** Event emitted when the select has been opened. */
    readonly _openedStream: Observable<void>;
    /** Event emitted when the select has been closed. */
    readonly _closedStream: Observable<void>;
    /** Event emitted when the selected value has been changed by the user. */
    readonly selectionChange: EventEmitter<C>;
    /**
     * Event that emits whenever the raw value of the select changes. This is here primarily
     * to facilitate the two-way binding for the `value` input.
     * @docs-private
     */
    readonly valueChange: EventEmitter<any>;
    constructor(_viewportRuler: ViewportRuler, _changeDetectorRef: ChangeDetectorRef, _ngZone: NgZone, _defaultErrorStateMatcher: ErrorStateMatcher, elementRef: ElementRef, _dir: Directionality, _parentForm: NgForm, _parentFormGroup: FormGroupDirective, _parentFormField: MatFormField, ngControl: NgControl, tabIndex: string, scrollStrategyFactory: any, _liveAnnouncer: LiveAnnouncer, _defaultOptions?: MatSelectConfig | undefined);
    ngOnInit(): void;
    ngAfterContentInit(): void;
    ngDoCheck(): void;
    ngOnChanges(changes: SimpleChanges): void;
    ngOnDestroy(): void;
    /** Toggles the overlay panel open or closed. */
    toggle(): void;
    /** Opens the overlay panel. */
    open(): void;
    /**
     * Track which modal we have modified the `aria-owns` attribute of. When the combobox trigger is
     * inside an aria-modal, we apply aria-owns to the parent modal with the `id` of the options
     * panel. Track the modal we have changed so we can undo the changes on destroy.
     */
    private _trackedModal;
    /**
     * If the autocomplete trigger is inside of an `aria-modal` element, connect
     * that modal to the options panel with `aria-owns`.
     *
     * For some browser + screen reader combinations, when navigation is inside
     * of an `aria-modal` element, the screen reader treats everything outside
     * of that modal as hidden or invisible.
     *
     * This causes a problem when the combobox trigger is _inside_ of a modal, because the
     * options panel is rendered _outside_ of that modal, preventing screen reader navigation
     * from reaching the panel.
     *
     * We can work around this issue by applying `aria-owns` to the modal with the `id` of
     * the options panel. This effectively communicates to assistive technology that the
     * options panel is part of the same interaction as the modal.
     *
     * At time of this writing, this issue is present in VoiceOver.
     * See https://github.com/angular/components/issues/20694
     */
    private _applyModalPanelOwnership;
    /** Clears the reference to the listbox overlay element from the modal it was added to. */
    private _clearFromModal;
    /** Closes the overlay panel and focuses the host element. */
    close(): void;
    /**
     * Sets the select's value. Part of the ControlValueAccessor interface
     * required to integrate with Angular's core forms API.
     *
     * @param value New value to be written to the model.
     */
    writeValue(value: any): void;
    /**
     * Saves a callback function to be invoked when the select's value
     * changes from user input. Part of the ControlValueAccessor interface
     * required to integrate with Angular's core forms API.
     *
     * @param fn Callback to be triggered when the value changes.
     */
    registerOnChange(fn: (value: any) => void): void;
    /**
     * Saves a callback function to be invoked when the select is blurred
     * by the user. Part of the ControlValueAccessor interface required
     * to integrate with Angular's core forms API.
     *
     * @param fn Callback to be triggered when the component has been touched.
     */
    registerOnTouched(fn: () => {}): void;
    /**
     * Disables the select. Part of the ControlValueAccessor interface required
     * to integrate with Angular's core forms API.
     *
     * @param isDisabled Sets whether the component is disabled.
     */
    setDisabledState(isDisabled: boolean): void;
    /** Whether or not the overlay panel is open. */
    get panelOpen(): boolean;
    /** The currently selected option. */
    get selected(): MatOption | MatOption[];
    /** The value displayed in the trigger. */
    get triggerValue(): string;
    /** Whether the element is in RTL mode. */
    _isRtl(): boolean;
    /** Handles all keydown events on the select. */
    _handleKeydown(event: KeyboardEvent): void;
    /** Handles keyboard events while the select is closed. */
    private _handleClosedKeydown;
    /** Handles keyboard events when the selected is open. */
    private _handleOpenKeydown;
    _onFocus(): void;
    /**
     * Calls the touched callback only if the panel is closed. Otherwise, the trigger will
     * "blur" to the panel when it opens, causing a false positive.
     */
    _onBlur(): void;
    /**
     * Callback that is invoked when the overlay panel has been attached.
     */
    _onAttached(): void;
    /** Returns the theme to be used on the panel. */
    _getPanelTheme(): string;
    /** Whether the select has a value. */
    get empty(): boolean;
    private _initializeSelection;
    /**
     * Sets the selected option based on a value. If no option can be
     * found with the designated value, the select trigger is cleared.
     */
    private _setSelectionByValue;
    /**
     * Finds and selects and option based on its value.
     * @returns Option that has the corresponding value.
     */
    private _selectOptionByValue;
    /** Assigns a specific value to the select. Returns whether the value has changed. */
    private _assignValue;
    protected _skipPredicate(item: MatOption): boolean;
    /** Sets up a key manager to listen to keyboard events on the overlay panel. */
    private _initKeyManager;
    /** Drops current option subscriptions and IDs and resets from scratch. */
    private _resetOptions;
    /** Invoked when an option is clicked. */
    private _onSelect;
    /** Sorts the selected values in the selected based on their order in the panel. */
    private _sortValues;
    /** Emits change event to set the model value. */
    private _propagateChanges;
    /**
     * Highlights the selected item. If no option is selected, it will highlight
     * the first *enabled* option.
     */
    private _highlightCorrectOption;
    /** Whether the panel is allowed to open. */
    protected _canOpen(): boolean;
    /** Focuses the select element. */
    focus(options?: FocusOptions): void;
    /** Gets the aria-labelledby for the select panel. */
    _getPanelAriaLabelledby(): string | null;
    /** Determines the `aria-activedescendant` to be set on the host. */
    _getAriaActiveDescendant(): string | null;
    /** Gets the aria-labelledby of the select component trigger. */
    private _getTriggerAriaLabelledby;
    /** Called when the overlay panel is done animating. */
    protected _panelDoneAnimating(isOpen: boolean): void;
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    setDescribedByIds(ids: string[]): void;
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    onContainerClick(): void;
    /**
     * Implemented as part of MatFormFieldControl.
     * @docs-private
     */
    get shouldLabelFloat(): boolean;
    static ɵfac: i0.ɵɵFactoryDeclaration<_MatSelectBase<any>, [null, null, null, null, null, { optional: true; }, { optional: true; }, { optional: true; }, { optional: true; }, { optional: true; self: true; }, { attribute: "tabindex"; }, null, null, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<_MatSelectBase<any>, never, never, { "userAriaDescribedBy": { "alias": "aria-describedby"; "required": false; }; "panelClass": { "alias": "panelClass"; "required": false; }; "placeholder": { "alias": "placeholder"; "required": false; }; "required": { "alias": "required"; "required": false; }; "multiple": { "alias": "multiple"; "required": false; }; "disableOptionCentering": { "alias": "disableOptionCentering"; "required": false; }; "compareWith": { "alias": "compareWith"; "required": false; }; "value": { "alias": "value"; "required": false; }; "ariaLabel": { "alias": "aria-label"; "required": false; }; "ariaLabelledby": { "alias": "aria-labelledby"; "required": false; }; "errorStateMatcher": { "alias": "errorStateMatcher"; "required": false; }; "typeaheadDebounceInterval": { "alias": "typeaheadDebounceInterval"; "required": false; }; "sortComparator": { "alias": "sortComparator"; "required": false; }; "id": { "alias": "id"; "required": false; }; }, { "openedChange": "openedChange"; "_openedStream": "opened"; "_closedStream": "closed"; "selectionChange": "selectionChange"; "valueChange": "valueChange"; }, never, never, false, never>;
}

/** Change event object that is emitted when the select value has changed. */
export declare class MatSelectChange {
    /** Reference to the select that emitted the change event. */
    source: MatSelect;
    /** Current value of the select that emitted the event. */
    value: any;
    constructor(
    /** Reference to the select that emitted the change event. */
    source: MatSelect, 
    /** Current value of the select that emitted the event. */
    value: any);
}

/** Object that can be used to configure the default options for the select module. */
export declare interface MatSelectConfig {
    /** Whether option centering should be disabled. */
    disableOptionCentering?: boolean;
    /** Time to wait in milliseconds after the last keystroke before moving focus to an item. */
    typeaheadDebounceInterval?: number;
    /** Class or list of classes to be applied to the menu's overlay panel. */
    overlayPanelClass?: string | string[];
    /** Wheter icon indicators should be hidden for single-selection. */
    hideSingleSelectionIndicator?: boolean;
    /**
     * Width of the panel. If set to `auto`, the panel will match the trigger width.
     * If set to null or an empty string, the panel will grow to match the longest option's text.
     */
    panelWidth?: string | number | null;
}

/** @docs-private */
declare const _MatSelectMixinBase: _Constructor<CanDisableRipple> & _AbstractConstructor<CanDisableRipple> & _Constructor<HasTabIndex> & _AbstractConstructor<HasTabIndex> & _Constructor<CanDisable> & _AbstractConstructor<CanDisable> & _Constructor<CanUpdateErrorState> & _AbstractConstructor<CanUpdateErrorState> & {
    new (_elementRef: ElementRef, _defaultErrorStateMatcher: ErrorStateMatcher, _parentForm: NgForm, _parentFormGroup: FormGroupDirective, ngControl: NgControl): {
        /**
         * Emits whenever the component state changes and should cause the parent
         * form-field to update. Implemented as part of `MatFormFieldControl`.
         * @docs-private
         */
        readonly stateChanges: Subject<void>;
        _elementRef: ElementRef;
        _defaultErrorStateMatcher: ErrorStateMatcher;
        _parentForm: NgForm;
        _parentFormGroup: FormGroupDirective;
        /**
         * Form control bound to the component.
         * Implemented as part of `MatFormFieldControl`.
         * @docs-private
         */
        ngControl: NgControl;
    };
};

export declare class MatSelectModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatSelectModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatSelectModule, [typeof i1.MatSelect, typeof i1.MatSelectTrigger], [typeof i2.CommonModule, typeof i3.OverlayModule, typeof i4.MatOptionModule, typeof i4.MatCommonModule], [typeof i5.CdkScrollableModule, typeof i6.MatFormFieldModule, typeof i1.MatSelect, typeof i1.MatSelectTrigger, typeof i4.MatOptionModule, typeof i4.MatCommonModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatSelectModule>;
}

/**
 * Allows the user to customize the trigger that is displayed when the select has a value.
 */
export declare class MatSelectTrigger {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatSelectTrigger, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatSelectTrigger, "mat-select-trigger", never, {}, {}, never, never, false, never>;
}

export { }
