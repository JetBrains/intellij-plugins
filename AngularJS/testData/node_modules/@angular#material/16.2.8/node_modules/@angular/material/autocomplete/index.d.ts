import { _AbstractConstructor } from '@angular/material/core';
import { ActiveDescendantKeyManager } from '@angular/cdk/a11y';
import { AfterContentInit } from '@angular/core';
import { AfterViewInit } from '@angular/core';
import { AnimationEvent as AnimationEvent_2 } from '@angular/animations';
import { BooleanInput } from '@angular/cdk/coercion';
import { CanDisableRipple } from '@angular/material/core';
import { ChangeDetectorRef } from '@angular/core';
import { _Constructor } from '@angular/material/core';
import { ControlValueAccessor } from '@angular/forms';
import { Directionality } from '@angular/cdk/bidi';
import { ElementRef } from '@angular/core';
import { EventEmitter } from '@angular/core';
import * as i0 from '@angular/core';
import * as i4 from '@angular/cdk/overlay';
import * as i5 from '@angular/material/core';
import * as i6 from '@angular/common';
import * as i7 from '@angular/cdk/scrolling';
import { InjectionToken } from '@angular/core';
import { MatFormField } from '@angular/material/form-field';
import { MatOptgroup } from '@angular/material/core';
import { _MatOptgroupBase } from '@angular/material/core';
import { MatOption } from '@angular/material/core';
import { _MatOptionBase } from '@angular/material/core';
import { MatOptionSelectionChange } from '@angular/material/core';
import { NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { OnChanges } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { Overlay } from '@angular/cdk/overlay';
import { Platform } from '@angular/cdk/platform';
import { QueryList } from '@angular/core';
import { ScrollStrategy } from '@angular/cdk/overlay';
import { SimpleChanges } from '@angular/core';
import { TemplateRef } from '@angular/core';
import { ThemePalette } from '@angular/material/core';
import { ViewContainerRef } from '@angular/core';
import { ViewportRuler } from '@angular/cdk/scrolling';

/**
 * Creates an error to be thrown when attempting to use an autocomplete trigger without a panel.
 * @docs-private
 */
export declare function getMatAutocompleteMissingPanelError(): Error;

declare namespace i1 {
    export {
        MAT_AUTOCOMPLETE_DEFAULT_OPTIONS_FACTORY,
        MatAutocompleteSelectedEvent,
        MatAutocompleteActivatedEvent,
        MatAutocompleteDefaultOptions,
        MAT_AUTOCOMPLETE_DEFAULT_OPTIONS,
        _MatAutocompleteBase,
        MatAutocomplete
    }
}

declare namespace i2 {
    export {
        getMatAutocompleteMissingPanelError,
        MAT_AUTOCOMPLETE_SCROLL_STRATEGY_FACTORY,
        MAT_AUTOCOMPLETE_VALUE_ACCESSOR,
        MAT_AUTOCOMPLETE_SCROLL_STRATEGY,
        MAT_AUTOCOMPLETE_SCROLL_STRATEGY_FACTORY_PROVIDER,
        _MatAutocompleteTriggerBase,
        MatAutocompleteTrigger
    }
}

declare namespace i3 {
    export {
        _MatAutocompleteOriginBase,
        MatAutocompleteOrigin
    }
}

/** Injection token to be used to override the default options for `mat-autocomplete`. */
export declare const MAT_AUTOCOMPLETE_DEFAULT_OPTIONS: InjectionToken<MatAutocompleteDefaultOptions>;

/** @docs-private */
export declare function MAT_AUTOCOMPLETE_DEFAULT_OPTIONS_FACTORY(): MatAutocompleteDefaultOptions;

/** Injection token that determines the scroll handling while the autocomplete panel is open. */
export declare const MAT_AUTOCOMPLETE_SCROLL_STRATEGY: InjectionToken<() => ScrollStrategy>;

/** @docs-private */
export declare function MAT_AUTOCOMPLETE_SCROLL_STRATEGY_FACTORY(overlay: Overlay): () => ScrollStrategy;

/** @docs-private */
export declare const MAT_AUTOCOMPLETE_SCROLL_STRATEGY_FACTORY_PROVIDER: {
    provide: InjectionToken<() => ScrollStrategy>;
    deps: (typeof Overlay)[];
    useFactory: typeof MAT_AUTOCOMPLETE_SCROLL_STRATEGY_FACTORY;
};

/**
 * Provider that allows the autocomplete to register as a ControlValueAccessor.
 * @docs-private
 */
export declare const MAT_AUTOCOMPLETE_VALUE_ACCESSOR: any;

export declare class MatAutocomplete extends _MatAutocompleteBase implements OnDestroy {
    /** Reference to all option groups within the autocomplete. */
    optionGroups: QueryList<MatOptgroup>;
    /** Reference to all options within the autocomplete. */
    options: QueryList<MatOption>;
    protected _visibleClass: string;
    protected _hiddenClass: string;
    _animationDone: EventEmitter<AnimationEvent_2>;
    /** Whether checkmark indicator for single-selection options is hidden. */
    get hideSingleSelectionIndicator(): boolean;
    set hideSingleSelectionIndicator(value: BooleanInput);
    private _hideSingleSelectionIndicator;
    /** Syncs the parent state with the individual options. */
    _syncParentProperties(): void;
    ngOnDestroy(): void;
    protected _skipPredicate(_option: _MatOptionBase): boolean;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatAutocomplete, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatAutocomplete, "mat-autocomplete", ["matAutocomplete"], { "disableRipple": { "alias": "disableRipple"; "required": false; }; "hideSingleSelectionIndicator": { "alias": "hideSingleSelectionIndicator"; "required": false; }; }, {}, ["optionGroups", "options"], ["*"], false, never>;
}

/** Event object that is emitted when an autocomplete option is activated. */
export declare interface MatAutocompleteActivatedEvent {
    /** Reference to the autocomplete panel that emitted the event. */
    source: _MatAutocompleteBase;
    /** Option that was selected. */
    option: _MatOptionBase | null;
}

/** Base class with all of the `MatAutocomplete` functionality. */
export declare abstract class _MatAutocompleteBase extends _MatAutocompleteMixinBase implements AfterContentInit, CanDisableRipple, OnDestroy {
    private _changeDetectorRef;
    private _elementRef;
    protected _defaults: MatAutocompleteDefaultOptions;
    private _activeOptionChanges;
    /** Class to apply to the panel when it's visible. */
    protected abstract _visibleClass: string;
    /** Class to apply to the panel when it's hidden. */
    protected abstract _hiddenClass: string;
    /** Emits when the panel animation is done. Null if the panel doesn't animate. */
    abstract _animationDone: EventEmitter<AnimationEvent_2> | null;
    /** Manages active item in option list based on key events. */
    _keyManager: ActiveDescendantKeyManager<_MatOptionBase>;
    /** Whether the autocomplete panel should be visible, depending on option length. */
    showPanel: boolean;
    /** Whether the autocomplete panel is open. */
    get isOpen(): boolean;
    _isOpen: boolean;
    /** @docs-private Sets the theme color of the panel. */
    _setColor(value: ThemePalette): void;
    /** @docs-private theme color of the panel */
    private _color;
    /** @docs-private */
    template: TemplateRef<any>;
    /** Element for the panel containing the autocomplete options. */
    panel: ElementRef;
    /** Reference to all options within the autocomplete. */
    abstract options: QueryList<_MatOptionBase>;
    /** Reference to all option groups within the autocomplete. */
    abstract optionGroups: QueryList<_MatOptgroupBase>;
    /** Aria label of the autocomplete. */
    ariaLabel: string;
    /** Input that can be used to specify the `aria-labelledby` attribute. */
    ariaLabelledby: string;
    /** Function that maps an option's control value to its display value in the trigger. */
    displayWith: ((value: any) => string) | null;
    /**
     * Whether the first option should be highlighted when the autocomplete panel is opened.
     * Can be configured globally through the `MAT_AUTOCOMPLETE_DEFAULT_OPTIONS` token.
     */
    get autoActiveFirstOption(): boolean;
    set autoActiveFirstOption(value: BooleanInput);
    private _autoActiveFirstOption;
    /** Whether the active option should be selected as the user is navigating. */
    get autoSelectActiveOption(): boolean;
    set autoSelectActiveOption(value: BooleanInput);
    private _autoSelectActiveOption;
    /**
     * Whether the user is required to make a selection when they're interacting with the
     * autocomplete. If the user moves away from the autocomplete without selecting an option from
     * the list, the value will be reset. If the user opens the panel and closes it without
     * interacting or selecting a value, the initial value will be kept.
     */
    get requireSelection(): boolean;
    set requireSelection(value: BooleanInput);
    private _requireSelection;
    /**
     * Specify the width of the autocomplete panel.  Can be any CSS sizing value, otherwise it will
     * match the width of its host.
     */
    panelWidth: string | number;
    /** Event that is emitted whenever an option from the list is selected. */
    readonly optionSelected: EventEmitter<MatAutocompleteSelectedEvent>;
    /** Event that is emitted when the autocomplete panel is opened. */
    readonly opened: EventEmitter<void>;
    /** Event that is emitted when the autocomplete panel is closed. */
    readonly closed: EventEmitter<void>;
    /** Emits whenever an option is activated. */
    readonly optionActivated: EventEmitter<MatAutocompleteActivatedEvent>;
    /**
     * Takes classes set on the host mat-autocomplete element and applies them to the panel
     * inside the overlay container to allow for easy styling.
     */
    set classList(value: string | string[]);
    _classList: {
        [key: string]: boolean;
    };
    /** Unique ID to be used by autocomplete trigger's "aria-owns" property. */
    id: string;
    /**
     * Tells any descendant `mat-optgroup` to use the inert a11y pattern.
     * @docs-private
     */
    readonly inertGroups: boolean;
    constructor(_changeDetectorRef: ChangeDetectorRef, _elementRef: ElementRef<HTMLElement>, _defaults: MatAutocompleteDefaultOptions, platform?: Platform);
    ngAfterContentInit(): void;
    ngOnDestroy(): void;
    /**
     * Sets the panel scrollTop. This allows us to manually scroll to display options
     * above or below the fold, as they are not actually being focused when active.
     */
    _setScrollTop(scrollTop: number): void;
    /** Returns the panel's scrollTop. */
    _getScrollTop(): number;
    /** Panel should hide itself when the option list is empty. */
    _setVisibility(): void;
    /** Emits the `select` event. */
    _emitSelectEvent(option: _MatOptionBase): void;
    /** Gets the aria-labelledby for the autocomplete panel. */
    _getPanelAriaLabelledby(labelId: string | null): string | null;
    /** Sets the autocomplete visibility classes on a classlist based on the panel is visible. */
    private _setVisibilityClasses;
    /** Sets the theming classes on a classlist based on the theme of the panel. */
    private _setThemeClasses;
    protected _skipPredicate(option: _MatOptionBase): boolean;
    static ɵfac: i0.ɵɵFactoryDeclaration<_MatAutocompleteBase, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<_MatAutocompleteBase, never, never, { "ariaLabel": { "alias": "aria-label"; "required": false; }; "ariaLabelledby": { "alias": "aria-labelledby"; "required": false; }; "displayWith": { "alias": "displayWith"; "required": false; }; "autoActiveFirstOption": { "alias": "autoActiveFirstOption"; "required": false; }; "autoSelectActiveOption": { "alias": "autoSelectActiveOption"; "required": false; }; "requireSelection": { "alias": "requireSelection"; "required": false; }; "panelWidth": { "alias": "panelWidth"; "required": false; }; "classList": { "alias": "class"; "required": false; }; }, { "optionSelected": "optionSelected"; "opened": "opened"; "closed": "closed"; "optionActivated": "optionActivated"; }, never, never, false, never>;
}

/** Default `mat-autocomplete` options that can be overridden. */
export declare interface MatAutocompleteDefaultOptions {
    /** Whether the first option should be highlighted when an autocomplete panel is opened. */
    autoActiveFirstOption?: boolean;
    /** Whether the active option should be selected as the user is navigating. */
    autoSelectActiveOption?: boolean;
    /**
     * Whether the user is required to make a selection when
     * they're interacting with the autocomplete.
     */
    requireSelection?: boolean;
    /** Class or list of classes to be applied to the autocomplete's overlay panel. */
    overlayPanelClass?: string | string[];
    /** Wheter icon indicators should be hidden for single-selection. */
    hideSingleSelectionIndicator?: boolean;
}

/** @docs-private */
declare const _MatAutocompleteMixinBase: _Constructor<CanDisableRipple> & _AbstractConstructor<CanDisableRipple> & {
    new (): {};
};

export declare class MatAutocompleteModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatAutocompleteModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatAutocompleteModule, [typeof i1.MatAutocomplete, typeof i2.MatAutocompleteTrigger, typeof i3.MatAutocompleteOrigin], [typeof i4.OverlayModule, typeof i5.MatOptionModule, typeof i5.MatCommonModule, typeof i6.CommonModule], [typeof i7.CdkScrollableModule, typeof i1.MatAutocomplete, typeof i5.MatOptionModule, typeof i5.MatCommonModule, typeof i2.MatAutocompleteTrigger, typeof i3.MatAutocompleteOrigin]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatAutocompleteModule>;
}

/**
 * Directive applied to an element to make it usable
 * as a connection point for an autocomplete panel.
 */
export declare class MatAutocompleteOrigin extends _MatAutocompleteOriginBase {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatAutocompleteOrigin, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatAutocompleteOrigin, "[matAutocompleteOrigin]", ["matAutocompleteOrigin"], {}, {}, never, never, false, never>;
}

/** Base class containing all of the functionality for `MatAutocompleteOrigin`. */
export declare abstract class _MatAutocompleteOriginBase {
    /** Reference to the element on which the directive is applied. */
    elementRef: ElementRef<HTMLElement>;
    constructor(
    /** Reference to the element on which the directive is applied. */
    elementRef: ElementRef<HTMLElement>);
    static ɵfac: i0.ɵɵFactoryDeclaration<_MatAutocompleteOriginBase, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<_MatAutocompleteOriginBase, never, never, {}, {}, never, never, false, never>;
}

/** Event object that is emitted when an autocomplete option is selected. */
export declare class MatAutocompleteSelectedEvent {
    /** Reference to the autocomplete panel that emitted the event. */
    source: _MatAutocompleteBase;
    /** Option that was selected. */
    option: _MatOptionBase;
    constructor(
    /** Reference to the autocomplete panel that emitted the event. */
    source: _MatAutocompleteBase, 
    /** Option that was selected. */
    option: _MatOptionBase);
}

export declare class MatAutocompleteTrigger extends _MatAutocompleteTriggerBase {
    protected _aboveClass: string;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatAutocompleteTrigger, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatAutocompleteTrigger, "input[matAutocomplete], textarea[matAutocomplete]", ["matAutocompleteTrigger"], {}, {}, never, never, false, never>;
}

/** Base class with all of the `MatAutocompleteTrigger` functionality. */
export declare abstract class _MatAutocompleteTriggerBase implements ControlValueAccessor, AfterViewInit, OnChanges, OnDestroy {
    private _element;
    private _overlay;
    private _viewContainerRef;
    private _zone;
    private _changeDetectorRef;
    private _dir;
    private _formField;
    private _document;
    private _viewportRuler;
    private _defaults?;
    private _overlayRef;
    private _portal;
    private _componentDestroyed;
    private _autocompleteDisabled;
    private _scrollStrategy;
    private _keydownSubscription;
    private _outsideClickSubscription;
    /** Old value of the native input. Used to work around issues with the `input` event on IE. */
    private _previousValue;
    /** Value of the input element when the panel was attached (even if there are no options). */
    private _valueOnAttach;
    /** Strategy that is used to position the panel. */
    private _positionStrategy;
    /** Whether or not the label state is being overridden. */
    private _manuallyFloatingLabel;
    /** The subscription for closing actions (some are bound to document). */
    private _closingActionsSubscription;
    /** Subscription to viewport size changes. */
    private _viewportSubscription;
    /**
     * Whether the autocomplete can open the next time it is focused. Used to prevent a focused,
     * closed autocomplete from being reopened if the user switches to another browser tab and then
     * comes back.
     */
    private _canOpenOnNextFocus;
    /** Value inside the input before we auto-selected an option. */
    private _valueBeforeAutoSelection;
    /**
     * Current option that we have auto-selected as the user is navigating,
     * but which hasn't been propagated to the model value yet.
     */
    private _pendingAutoselectedOption;
    /** Stream of keyboard events that can close the panel. */
    private readonly _closeKeyEventStream;
    /**
     * Event handler for when the window is blurred. Needs to be an
     * arrow function in order to preserve the context.
     */
    private _windowBlurHandler;
    /** `View -> model callback called when value changes` */
    _onChange: (value: any) => void;
    /** `View -> model callback called when autocomplete has been touched` */
    _onTouched: () => void;
    /** The autocomplete panel to be attached to this trigger. */
    autocomplete: _MatAutocompleteBase;
    /**
     * Position of the autocomplete panel relative to the trigger element. A position of `auto`
     * will render the panel underneath the trigger if there is enough space for it to fit in
     * the viewport, otherwise the panel will be shown above it. If the position is set to
     * `above` or `below`, the panel will always be shown above or below the trigger. no matter
     * whether it fits completely in the viewport.
     */
    position: 'auto' | 'above' | 'below';
    /**
     * Reference relative to which to position the autocomplete panel.
     * Defaults to the autocomplete trigger element.
     */
    connectedTo: _MatAutocompleteOriginBase;
    /**
     * `autocomplete` attribute to be set on the input element.
     * @docs-private
     */
    autocompleteAttribute: string;
    /**
     * Whether the autocomplete is disabled. When disabled, the element will
     * act as a regular input and the user won't be able to open the panel.
     */
    get autocompleteDisabled(): boolean;
    set autocompleteDisabled(value: BooleanInput);
    constructor(_element: ElementRef<HTMLInputElement>, _overlay: Overlay, _viewContainerRef: ViewContainerRef, _zone: NgZone, _changeDetectorRef: ChangeDetectorRef, scrollStrategy: any, _dir: Directionality | null, _formField: MatFormField | null, _document: any, _viewportRuler: ViewportRuler, _defaults?: MatAutocompleteDefaultOptions | null | undefined);
    /** Class to apply to the panel when it's above the input. */
    protected abstract _aboveClass: string;
    ngAfterViewInit(): void;
    ngOnChanges(changes: SimpleChanges): void;
    ngOnDestroy(): void;
    /** Whether or not the autocomplete panel is open. */
    get panelOpen(): boolean;
    private _overlayAttached;
    /** Opens the autocomplete suggestion panel. */
    openPanel(): void;
    /** Closes the autocomplete suggestion panel. */
    closePanel(): void;
    /**
     * Updates the position of the autocomplete suggestion panel to ensure that it fits all options
     * within the viewport.
     */
    updatePosition(): void;
    /**
     * A stream of actions that should close the autocomplete panel, including
     * when an option is selected, on blur, and when TAB is pressed.
     */
    get panelClosingActions(): Observable<MatOptionSelectionChange | null>;
    /** Stream of changes to the selection state of the autocomplete options. */
    readonly optionSelections: Observable<MatOptionSelectionChange>;
    /** The currently active option, coerced to MatOption type. */
    get activeOption(): _MatOptionBase | null;
    /** Stream of clicks outside of the autocomplete panel. */
    private _getOutsideClickStream;
    writeValue(value: any): void;
    registerOnChange(fn: (value: any) => {}): void;
    registerOnTouched(fn: () => {}): void;
    setDisabledState(isDisabled: boolean): void;
    _handleKeydown(event: KeyboardEvent): void;
    _handleInput(event: KeyboardEvent): void;
    _handleFocus(): void;
    _handleClick(): void;
    /**
     * In "auto" mode, the label will animate down as soon as focus is lost.
     * This causes the value to jump when selecting an option with the mouse.
     * This method manually floats the label until the panel can be closed.
     * @param shouldAnimate Whether the label should be animated when it is floated.
     */
    private _floatLabel;
    /** If the label has been manually elevated, return it to its normal state. */
    private _resetLabel;
    /**
     * This method listens to a stream of panel closing actions and resets the
     * stream every time the option list changes.
     */
    private _subscribeToClosingActions;
    /**
     * Emits the opened event once it's known that the panel will be shown and stores
     * the state of the trigger right before the opening sequence was finished.
     */
    private _emitOpened;
    /** Intended to be called when the panel is attached. Captures the current value of the input. */
    private _captureValueOnAttach;
    /** Destroys the autocomplete suggestion panel. */
    private _destroyPanel;
    private _assignOptionValue;
    private _updateNativeInputValue;
    /**
     * This method closes the panel, and if a value is specified, also sets the associated
     * control to that value. It will also mark the control as dirty if this interaction
     * stemmed from the user.
     */
    private _setValueAndClose;
    /**
     * Clear any previous selected option and emit a selection change event for this option
     */
    private _clearPreviousSelectedOption;
    private _attachOverlay;
    /** Handles keyboard events coming from the overlay panel. */
    private _handlePanelKeydown;
    /** Updates the panel's visibility state and any trigger state tied to id. */
    private _updatePanelState;
    private _getOverlayConfig;
    private _getOverlayPosition;
    /** Sets the positions on a position strategy based on the directive's input state. */
    private _setStrategyPositions;
    private _getConnectedElement;
    private _getPanelWidth;
    /** Returns the width of the input element, so the panel width can match it. */
    private _getHostWidth;
    /**
     * Reset the active item to -1. This is so that pressing arrow keys will activate the correct
     * option.
     *
     * If the consumer opted-in to automatically activatating the first option, activate the first
     * *enabled* option.
     */
    private _resetActiveItem;
    /** Determines whether the panel can be opened. */
    private _canOpen;
    /** Use defaultView of injected document if available or fallback to global window reference */
    private _getWindow;
    /** Scrolls to a particular option in the list. */
    private _scrollToOption;
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
    /** Clears the references to the listbox overlay element from the modal it was added to. */
    private _clearFromModal;
    static ɵfac: i0.ɵɵFactoryDeclaration<_MatAutocompleteTriggerBase, [null, null, null, null, null, null, { optional: true; }, { optional: true; host: true; }, { optional: true; }, null, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<_MatAutocompleteTriggerBase, never, never, { "autocomplete": { "alias": "matAutocomplete"; "required": false; }; "position": { "alias": "matAutocompletePosition"; "required": false; }; "connectedTo": { "alias": "matAutocompleteConnectedTo"; "required": false; }; "autocompleteAttribute": { "alias": "autocomplete"; "required": false; }; "autocompleteDisabled": { "alias": "matAutocompleteDisabled"; "required": false; }; }, {}, never, never, false, never>;
}

export { }
