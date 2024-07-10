import { AbstractControl } from '@angular/forms';
import { AfterViewChecked } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';
import { ElementRef } from '@angular/core';
import { EventEmitter } from '@angular/core';
import { FocusableOption } from '@angular/cdk/a11y';
import { FocusOrigin } from '@angular/cdk/a11y';
import { FormGroupDirective } from '@angular/forms';
import { HighContrastModeDetector } from '@angular/cdk/a11y';
import * as i0 from '@angular/core';
import * as i1 from '@angular/cdk/bidi';
import { InjectionToken } from '@angular/core';
import { NgControl } from '@angular/forms';
import { NgForm } from '@angular/forms';
import { NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { OnDestroy } from '@angular/core';
import { OnInit } from '@angular/core';
import { Platform } from '@angular/cdk/platform';
import { Provider } from '@angular/core';
import { QueryList } from '@angular/core';
import { Subject } from 'rxjs';
import { Version } from '@angular/core';

/**
 * This is a permissive type for abstract class constructors.
 * @docs-private
 */
export declare type _AbstractConstructor<T = object> = abstract new (...args: any[]) => T;


/** @docs-private */
export declare class AnimationCurves {
    static STANDARD_CURVE: string;
    static DECELERATION_CURVE: string;
    static ACCELERATION_CURVE: string;
    static SHARP_CURVE: string;
}

/** @docs-private */
export declare class AnimationDurations {
    static COMPLEX: string;
    static ENTERING: string;
    static EXITING: string;
}

/**
 * @docs-private
 * @deprecated Will be removed together with `mixinColor`.
 * @breaking-change 19.0.0
 */
export declare interface CanColor {
    /** Theme color palette for the component. */
    color: ThemePalette;
    /** Default color to fall back to if no value is set. */
    defaultColor: ThemePalette | undefined;
}

declare type CanColorCtor = _Constructor<CanColor> & _AbstractConstructor<CanColor>;

/**
 * @docs-private
 * @deprecated Will be removed together with `mixinDisabled`.
 * @breaking-change 19.0.0
 */
export declare interface CanDisable {
    /** Whether the component is disabled. */
    disabled: boolean;
}

declare type CanDisableCtor = _Constructor<CanDisable> & _AbstractConstructor<CanDisable>;

/**
 * @docs-private
 * @deprecated Will be removed together with `mixinDisableRipple`.
 * @breaking-change 19.0.0
 */
export declare interface CanDisableRipple {
    /** Whether ripples are disabled. */
    disableRipple: boolean;
}

declare type CanDisableRippleCtor = _Constructor<CanDisableRipple> & _AbstractConstructor<CanDisableRipple>;

/**
 * @docs-private
 * @deprecated Will be removed together with `mixinErrorState`.
 * @breaking-change 19.0.0
 */
export declare interface CanUpdateErrorState {
    /** Updates the error state based on the provided error state matcher. */
    updateErrorState(): void;
    /** Whether the component is in an error state. */
    errorState: boolean;
    /** An object used to control the error state of the component. */
    errorStateMatcher: ErrorStateMatcher_2;
}

declare type CanUpdateErrorStateCtor = _Constructor<CanUpdateErrorState> & _AbstractConstructor<CanUpdateErrorState>;


/** @docs-private */
export declare type _Constructor<T> = new (...args: any[]) => T;

/**
 * Counts the amount of option group labels that precede the specified option.
 * @param optionIndex Index of the option at which to start counting.
 * @param options Flat list of all of the options.
 * @param optionGroups Flat list of all of the option groups.
 * @docs-private
 */
export declare function _countGroupLabelsBeforeOption(optionIndex: number, options: QueryList<MatOption>, optionGroups: QueryList<MatOptgroup>): number;

/** Adapts type `D` to be usable as a date by cdk-based components that work with dates. */
export declare abstract class DateAdapter<D, L = any> {
    /** The locale to use for all dates. */
    protected locale: L;
    protected readonly _localeChanges: Subject<void>;
    /** A stream that emits when the locale changes. */
    readonly localeChanges: Observable<void>;
    /**
     * Gets the year component of the given date.
     * @param date The date to extract the year from.
     * @returns The year component.
     */
    abstract getYear(date: D): number;
    /**
     * Gets the month component of the given date.
     * @param date The date to extract the month from.
     * @returns The month component (0-indexed, 0 = January).
     */
    abstract getMonth(date: D): number;
    /**
     * Gets the date of the month component of the given date.
     * @param date The date to extract the date of the month from.
     * @returns The month component (1-indexed, 1 = first of month).
     */
    abstract getDate(date: D): number;
    /**
     * Gets the day of the week component of the given date.
     * @param date The date to extract the day of the week from.
     * @returns The month component (0-indexed, 0 = Sunday).
     */
    abstract getDayOfWeek(date: D): number;
    /**
     * Gets a list of names for the months.
     * @param style The naming style (e.g. long = 'January', short = 'Jan', narrow = 'J').
     * @returns An ordered list of all month names, starting with January.
     */
    abstract getMonthNames(style: 'long' | 'short' | 'narrow'): string[];
    /**
     * Gets a list of names for the dates of the month.
     * @returns An ordered list of all date of the month names, starting with '1'.
     */
    abstract getDateNames(): string[];
    /**
     * Gets a list of names for the days of the week.
     * @param style The naming style (e.g. long = 'Sunday', short = 'Sun', narrow = 'S').
     * @returns An ordered list of all weekday names, starting with Sunday.
     */
    abstract getDayOfWeekNames(style: 'long' | 'short' | 'narrow'): string[];
    /**
     * Gets the name for the year of the given date.
     * @param date The date to get the year name for.
     * @returns The name of the given year (e.g. '2017').
     */
    abstract getYearName(date: D): string;
    /**
     * Gets the first day of the week.
     * @returns The first day of the week (0-indexed, 0 = Sunday).
     */
    abstract getFirstDayOfWeek(): number;
    /**
     * Gets the number of days in the month of the given date.
     * @param date The date whose month should be checked.
     * @returns The number of days in the month of the given date.
     */
    abstract getNumDaysInMonth(date: D): number;
    /**
     * Clones the given date.
     * @param date The date to clone
     * @returns A new date equal to the given date.
     */
    abstract clone(date: D): D;
    /**
     * Creates a date with the given year, month, and date. Does not allow over/under-flow of the
     * month and date.
     * @param year The full year of the date. (e.g. 89 means the year 89, not the year 1989).
     * @param month The month of the date (0-indexed, 0 = January). Must be an integer 0 - 11.
     * @param date The date of month of the date. Must be an integer 1 - length of the given month.
     * @returns The new date, or null if invalid.
     */
    abstract createDate(year: number, month: number, date: number): D;
    /**
     * Gets today's date.
     * @returns Today's date.
     */
    abstract today(): D;
    /**
     * Parses a date from a user-provided value.
     * @param value The value to parse.
     * @param parseFormat The expected format of the value being parsed
     *     (type is implementation-dependent).
     * @returns The parsed date.
     */
    abstract parse(value: any, parseFormat: any): D | null;
    /**
     * Formats a date as a string according to the given format.
     * @param date The value to format.
     * @param displayFormat The format to use to display the date as a string.
     * @returns The formatted date string.
     */
    abstract format(date: D, displayFormat: any): string;
    /**
     * Adds the given number of years to the date. Years are counted as if flipping 12 pages on the
     * calendar for each year and then finding the closest date in the new month. For example when
     * adding 1 year to Feb 29, 2016, the resulting date will be Feb 28, 2017.
     * @param date The date to add years to.
     * @param years The number of years to add (may be negative).
     * @returns A new date equal to the given one with the specified number of years added.
     */
    abstract addCalendarYears(date: D, years: number): D;
    /**
     * Adds the given number of months to the date. Months are counted as if flipping a page on the
     * calendar for each month and then finding the closest date in the new month. For example when
     * adding 1 month to Jan 31, 2017, the resulting date will be Feb 28, 2017.
     * @param date The date to add months to.
     * @param months The number of months to add (may be negative).
     * @returns A new date equal to the given one with the specified number of months added.
     */
    abstract addCalendarMonths(date: D, months: number): D;
    /**
     * Adds the given number of days to the date. Days are counted as if moving one cell on the
     * calendar for each day.
     * @param date The date to add days to.
     * @param days The number of days to add (may be negative).
     * @returns A new date equal to the given one with the specified number of days added.
     */
    abstract addCalendarDays(date: D, days: number): D;
    /**
     * Gets the RFC 3339 compatible string (https://tools.ietf.org/html/rfc3339) for the given date.
     * This method is used to generate date strings that are compatible with native HTML attributes
     * such as the `min` or `max` attribute of an `<input>`.
     * @param date The date to get the ISO date string for.
     * @returns The ISO date string date string.
     */
    abstract toIso8601(date: D): string;
    /**
     * Checks whether the given object is considered a date instance by this DateAdapter.
     * @param obj The object to check
     * @returns Whether the object is a date instance.
     */
    abstract isDateInstance(obj: any): boolean;
    /**
     * Checks whether the given date is valid.
     * @param date The date to check.
     * @returns Whether the date is valid.
     */
    abstract isValid(date: D): boolean;
    /**
     * Gets date instance that is not valid.
     * @returns An invalid date.
     */
    abstract invalid(): D;
    /**
     * Given a potential date object, returns that same date object if it is
     * a valid date, or `null` if it's not a valid date.
     * @param obj The object to check.
     * @returns A date or `null`.
     */
    getValidDateOrNull(obj: unknown): D | null;
    /**
     * Attempts to deserialize a value to a valid date object. This is different from parsing in that
     * deserialize should only accept non-ambiguous, locale-independent formats (e.g. a ISO 8601
     * string). The default implementation does not allow any deserialization, it simply checks that
     * the given value is already a valid date object or null. The `<mat-datepicker>` will call this
     * method on all of its `@Input()` properties that accept dates. It is therefore possible to
     * support passing values from your backend directly to these properties by overriding this method
     * to also deserialize the format used by your backend.
     * @param value The value to be deserialized into a date object.
     * @returns The deserialized date object, either a valid date, null if the value can be
     *     deserialized into a null date (e.g. the empty string), or an invalid date.
     */
    deserialize(value: any): D | null;
    /**
     * Sets the locale used for all dates.
     * @param locale The new locale.
     */
    setLocale(locale: L): void;
    /**
     * Compares two dates.
     * @param first The first date to compare.
     * @param second The second date to compare.
     * @returns 0 if the dates are equal, a number less than 0 if the first date is earlier,
     *     a number greater than 0 if the first date is later.
     */
    compareDate(first: D, second: D): number;
    /**
     * Checks if two dates are equal.
     * @param first The first date to check.
     * @param second The second date to check.
     * @returns Whether the two dates are equal.
     *     Null dates are considered equal to other null dates.
     */
    sameDate(first: D | null, second: D | null): boolean;
    /**
     * Clamp the given date between min and max dates.
     * @param date The date to clamp.
     * @param min The minimum value to allow. If null or omitted no min is enforced.
     * @param max The maximum value to allow. If null or omitted no max is enforced.
     * @returns `min` if `date` is less than `min`, `max` if date is greater than `max`,
     *     otherwise `date`.
     */
    clampDate(date: D, min?: D | null, max?: D | null): D;
}

/**
 * Default ripple animation configuration for ripples without an explicit
 * animation config specified.
 */
export declare const defaultRippleAnimationConfig: {
    enterDuration: number;
    exitDuration: number;
};

/** Provider that defines how form controls behave with regards to displaying error messages. */
export declare class ErrorStateMatcher {
    isErrorState(control: AbstractControl | null, form: FormGroupDirective | NgForm | null): boolean;
    static ɵfac: i0.ɵɵFactoryDeclaration<ErrorStateMatcher, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<ErrorStateMatcher>;
}

declare interface ErrorStateMatcher_2 extends ErrorStateMatcher {
}

/**
 * Class that tracks the error state of a component.
 * @docs-private
 */
export declare class _ErrorStateTracker {
    private _defaultMatcher;
    ngControl: NgControl | null;
    private _parentFormGroup;
    private _parentForm;
    private _stateChanges;
    /** Whether the tracker is currently in an error state. */
    errorState: boolean;
    /** User-defined matcher for the error state. */
    matcher: ErrorStateMatcher_2;
    constructor(_defaultMatcher: ErrorStateMatcher_2 | null, ngControl: NgControl | null, _parentFormGroup: FormGroupDirective | null, _parentForm: NgForm | null, _stateChanges: Subject<void>);
    /** Updates the error state based on the provided error state matcher. */
    updateErrorState(): void;
}

/**
 * Determines the position to which to scroll a panel in order for an option to be into view.
 * @param optionOffset Offset of the option from the top of the panel.
 * @param optionHeight Height of the options.
 * @param currentScrollPosition Current scroll position of the panel.
 * @param panelHeight Height of the panel.
 * @docs-private
 */
export declare function _getOptionScrollPosition(optionOffset: number, optionHeight: number, currentScrollPosition: number, panelHeight: number): number;

/** Object that can be used to configure the sanity checks granularly. */
export declare interface GranularSanityChecks {
    doctype: boolean;
    theme: boolean;
    version: boolean;
}

/** @docs-private */
declare interface HasElementRef {
    _elementRef: ElementRef;
}

/** @docs-private */
declare interface HasErrorState {
    _parentFormGroup: FormGroupDirective | null;
    _parentForm: NgForm | null;
    _defaultErrorStateMatcher: ErrorStateMatcher_2;
    ngControl: NgControl | null;
    stateChanges: Subject<void>;
}

/**
 * Mixin that adds an initialized property to a directive which, when subscribed to, will emit a
 * value once markInitialized has been called, which should be done during the ngOnInit function.
 * If the subscription is made after it has already been marked as initialized, then it will trigger
 * an emit immediately.
 * @docs-private
 * @deprecated Will be removed together with `mixinInitializer`.
 * @breaking-change 19.0.0
 */
export declare interface HasInitialized {
    /** Stream that emits once during the directive/component's ngOnInit. */
    initialized: Observable<void>;
    /**
     * Sets the state as initialized and must be called during ngOnInit to notify subscribers that
     * the directive has been initialized.
     * @docs-private
     */
    _markInitialized: () => void;
}

declare type HasInitializedCtor = _Constructor<HasInitialized>;

/**
 * @docs-private
 * @deprecated Will be removed together with `mixinTabIndex`.
 * @breaking-change 19.0.0
 */
export declare interface HasTabIndex {
    /** Tabindex of the component. */
    tabIndex: number;
    /** Tabindex to which to fall back to if no value is set. */
    defaultTabIndex: number;
}

declare type HasTabIndexCtor = _Constructor<HasTabIndex> & _AbstractConstructor<HasTabIndex>;

declare namespace i1_2 {
    export {
        MATERIAL_SANITY_CHECKS_FACTORY,
        MATERIAL_SANITY_CHECKS,
        SanityChecks,
        GranularSanityChecks,
        MatCommonModule
    }
}

declare namespace i1_3 {
    export {
        MatRippleModule,
        RippleGlobalOptions,
        MAT_RIPPLE_GLOBAL_OPTIONS,
        MatRipple,
        RippleState,
        RippleConfig,
        RippleAnimationConfig,
        RippleRef,
        RippleTarget,
        defaultRippleAnimationConfig,
        RippleRenderer
    }
}

declare namespace i2 {
    export {
        RippleGlobalOptions,
        MAT_RIPPLE_GLOBAL_OPTIONS,
        MatRipple
    }
}

declare namespace i2_2 {
    export {
        MatPseudoCheckboxState,
        MatPseudoCheckbox
    }
}

declare namespace i3 {
    export {
        MatPseudoCheckboxModule
    }
}

declare namespace i4 {
    export {
        _countGroupLabelsBeforeOption,
        _getOptionScrollPosition,
        MatOptionSelectionChange,
        MatOption
    }
}

declare namespace i5 {
    export {
        MAT_OPTGROUP,
        MatOptgroup
    }
}

export declare const MAT_DATE_FORMATS: InjectionToken<MatDateFormats>;

/** InjectionToken for datepicker that can be used to override default locale code. */
export declare const MAT_DATE_LOCALE: InjectionToken<{}>;

/** @docs-private */
export declare function MAT_DATE_LOCALE_FACTORY(): {};

export declare const MAT_NATIVE_DATE_FORMATS: MatDateFormats;

/**
 * Injection token that can be used to reference instances of `MatOptgroup`. It serves as
 * alternative token to the actual `MatOptgroup` class which could cause unnecessary
 * retention of the class and its component metadata.
 */
export declare const MAT_OPTGROUP: InjectionToken<MatOptgroup>;

/**
 * Injection token used to provide the parent component to options.
 */
export declare const MAT_OPTION_PARENT_COMPONENT: InjectionToken<MatOptionParentComponent>;

/** Injection token that can be used to specify the global ripple options. */
export declare const MAT_RIPPLE_GLOBAL_OPTIONS: InjectionToken<RippleGlobalOptions>;

/**
 * Module that captures anything that should be loaded and/or run for *all* Angular Material
 * components. This includes Bidi, etc.
 *
 * This module should be imported to each top-level component module (e.g., MatTabsModule).
 */
export declare class MatCommonModule {
    private _sanityChecks;
    private _document;
    /** Whether we've done the global sanity checks (e.g. a theme is loaded, there is a doctype). */
    private _hasDoneGlobalChecks;
    constructor(highContrastModeDetector: HighContrastModeDetector, _sanityChecks: SanityChecks, _document: Document);
    /** Gets whether a specific sanity check is enabled. */
    private _checkIsEnabled;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatCommonModule, [null, { optional: true; }, null]>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatCommonModule, never, [typeof i1.BidiModule], [typeof i1.BidiModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatCommonModule>;
}

export declare type MatDateFormats = {
    parse: {
        dateInput: any;
    };
    display: {
        dateInput: any;
        monthLabel?: any;
        monthYearLabel: any;
        dateA11yLabel: any;
        monthYearA11yLabel: any;
    };
};

/** Injection token that configures whether the Material sanity checks are enabled. */
export declare const MATERIAL_SANITY_CHECKS: InjectionToken<SanityChecks>;

/** @docs-private */
declare function MATERIAL_SANITY_CHECKS_FACTORY(): SanityChecks;

/**
 * Internal shared component used as a container in form field controls.
 * Not to be confused with `mat-form-field` which MDC calls a "text field".
 * @docs-private
 */
export declare class _MatInternalFormField {
    /** Position of the label relative to the content. */
    labelPosition: 'before' | 'after';
    static ɵfac: i0.ɵɵFactoryDeclaration<_MatInternalFormField, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<_MatInternalFormField, "div[mat-internal-form-field]", never, { "labelPosition": { "alias": "labelPosition"; "required": true; }; }, {}, never, ["*"], true, never>;
}

/**
 * Shared directive to count lines inside a text area, such as a list item.
 * Line elements can be extracted with a @ContentChildren(MatLine) query, then
 * counted by checking the query list's length.
 */
export declare class MatLine {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLine, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLine, "[mat-line], [matLine]", never, {}, {}, never, never, true, never>;
}

export declare class MatLineModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLineModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatLineModule, never, [typeof i1_2.MatCommonModule, typeof MatLine], [typeof MatLine, typeof i1_2.MatCommonModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatLineModule>;
}

export declare class MatNativeDateModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatNativeDateModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatNativeDateModule, never, never, never>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatNativeDateModule>;
}

/**
 * Component that is used to group instances of `mat-option`.
 */
export declare class MatOptgroup {
    /** Label for the option group. */
    label: string;
    /** whether the option group is disabled. */
    disabled: boolean;
    /** Unique id for the underlying label. */
    _labelId: string;
    /** Whether the group is in inert a11y mode. */
    _inert: boolean;
    constructor(parent?: MatOptionParentComponent);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatOptgroup, [{ optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatOptgroup, "mat-optgroup", ["matOptgroup"], { "label": { "alias": "label"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; }, {}, never, ["*", "mat-option, ng-container"], true, never>;
    static ngAcceptInputType_disabled: unknown;
}

/**
 * Single option inside of a `<mat-select>` element.
 */
export declare class MatOption<T = any> implements FocusableOption, AfterViewChecked, OnDestroy {
    private _element;
    _changeDetectorRef: ChangeDetectorRef;
    private _parent;
    group: MatOptgroup;
    private _selected;
    private _active;
    private _disabled;
    private _mostRecentViewValue;
    /** Whether the wrapping component is in multiple selection mode. */
    get multiple(): boolean | undefined;
    /** Whether or not the option is currently selected. */
    get selected(): boolean;
    /** The form value of the option. */
    value: T;
    /** The unique ID of the option. */
    id: string;
    /** Whether the option is disabled. */
    get disabled(): boolean;
    set disabled(value: boolean);
    /** Whether ripples for the option are disabled. */
    get disableRipple(): boolean;
    /** Whether to display checkmark for single-selection. */
    get hideSingleSelectionIndicator(): boolean;
    /** Event emitted when the option is selected or deselected. */
    readonly onSelectionChange: EventEmitter<MatOptionSelectionChange<T>>;
    /** Element containing the option's text. */
    _text: ElementRef<HTMLElement> | undefined;
    /** Emits when the state of the option changes and any parents have to be notified. */
    readonly _stateChanges: Subject<void>;
    constructor(_element: ElementRef<HTMLElement>, _changeDetectorRef: ChangeDetectorRef, _parent: MatOptionParentComponent, group: MatOptgroup);
    /**
     * Whether or not the option is currently active and ready to be selected.
     * An active option displays styles as if it is focused, but the
     * focus is actually retained somewhere else. This comes in handy
     * for components like autocomplete where focus must remain on the input.
     */
    get active(): boolean;
    /**
     * The displayed value of the option. It is necessary to show the selected option in the
     * select's trigger.
     */
    get viewValue(): string;
    /** Selects the option. */
    select(emitEvent?: boolean): void;
    /** Deselects the option. */
    deselect(emitEvent?: boolean): void;
    /** Sets focus onto this option. */
    focus(_origin?: FocusOrigin, options?: FocusOptions): void;
    /**
     * This method sets display styles on the option to make it appear
     * active. This is used by the ActiveDescendantKeyManager so key
     * events will display the proper options as active on arrow key events.
     */
    setActiveStyles(): void;
    /**
     * This method removes display styles on the option that made it appear
     * active. This is used by the ActiveDescendantKeyManager so key
     * events will display the proper options as active on arrow key events.
     */
    setInactiveStyles(): void;
    /** Gets the label to be used when determining whether the option should be focused. */
    getLabel(): string;
    /** Ensures the option is selected when activated from the keyboard. */
    _handleKeydown(event: KeyboardEvent): void;
    /**
     * `Selects the option while indicating the selection came from the user. Used to
     * determine if the select's view -> model callback should be invoked.`
     */
    _selectViaInteraction(): void;
    /** Returns the correct tabindex for the option depending on disabled state. */
    _getTabIndex(): string;
    /** Gets the host DOM element. */
    _getHostElement(): HTMLElement;
    ngAfterViewChecked(): void;
    ngOnDestroy(): void;
    /** Emits the selection change event. */
    private _emitSelectionChangeEvent;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatOption<any>, [null, null, { optional: true; }, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatOption<any>, "mat-option", ["matOption"], { "value": { "alias": "value"; "required": false; }; "id": { "alias": "id"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; }, { "onSelectionChange": "onSelectionChange"; }, never, ["mat-icon", "*"], true, never>;
    static ngAcceptInputType_disabled: unknown;
}

export declare class MatOptionModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatOptionModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatOptionModule, never, [typeof i1_3.MatRippleModule, typeof i1_2.MatCommonModule, typeof i3.MatPseudoCheckboxModule, typeof i4.MatOption, typeof i5.MatOptgroup], [typeof i4.MatOption, typeof i5.MatOptgroup]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatOptionModule>;
}

/**
 * Describes a parent component that manages a list of options.
 * Contains properties that the options can inherit.
 * @docs-private
 */
export declare interface MatOptionParentComponent {
    disableRipple?: boolean;
    multiple?: boolean;
    inertGroups?: boolean;
    hideSingleSelectionIndicator?: boolean;
}

/** Event object emitted by MatOption when selected or deselected. */
export declare class MatOptionSelectionChange<T = any> {
    /** Reference to the option that emitted the event. */
    source: MatOption<T>;
    /** Whether the change in the option's value was a result of a user action. */
    isUserInput: boolean;
    constructor(
    /** Reference to the option that emitted the event. */
    source: MatOption<T>, 
    /** Whether the change in the option's value was a result of a user action. */
    isUserInput?: boolean);
}

/**
 * Component that shows a simplified checkbox without including any kind of "real" checkbox.
 * Meant to be used when the checkbox is purely decorative and a large number of them will be
 * included, such as for the options in a multi-select. Uses no SVGs or complex animations.
 * Note that theming is meant to be handled by the parent element, e.g.
 * `mat-primary .mat-pseudo-checkbox`.
 *
 * Note that this component will be completely invisible to screen-reader users. This is *not*
 * interchangeable with `<mat-checkbox>` and should *not* be used if the user would directly
 * interact with the checkbox. The pseudo-checkbox should only be used as an implementation detail
 * of more complex components that appropriately handle selected / checked state.
 * @docs-private
 */
export declare class MatPseudoCheckbox {
    _animationMode?: string | undefined;
    /** Display state of the checkbox. */
    state: MatPseudoCheckboxState;
    /** Whether the checkbox is disabled. */
    disabled: boolean;
    /**
     * Appearance of the pseudo checkbox. Default appearance of 'full' renders a checkmark/mixedmark
     * indicator inside a square box. 'minimal' appearance only renders the checkmark/mixedmark.
     */
    appearance: 'minimal' | 'full';
    constructor(_animationMode?: string | undefined);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatPseudoCheckbox, [{ optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatPseudoCheckbox, "mat-pseudo-checkbox", never, { "state": { "alias": "state"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; "appearance": { "alias": "appearance"; "required": false; }; }, {}, never, never, true, never>;
}

export declare class MatPseudoCheckboxModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatPseudoCheckboxModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatPseudoCheckboxModule, never, [typeof i1_2.MatCommonModule, typeof i2_2.MatPseudoCheckbox], [typeof i2_2.MatPseudoCheckbox]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatPseudoCheckboxModule>;
}

/**
 * Possible states for a pseudo checkbox.
 * @docs-private
 */
export declare type MatPseudoCheckboxState = 'unchecked' | 'checked' | 'indeterminate';

export declare class MatRipple implements OnInit, OnDestroy, RippleTarget {
    private _elementRef;
    private _animationMode?;
    /** Custom color for all ripples. */
    color: string;
    /** Whether the ripples should be visible outside the component's bounds. */
    unbounded: boolean;
    /**
     * Whether the ripple always originates from the center of the host element's bounds, rather
     * than originating from the location of the click event.
     */
    centered: boolean;
    /**
     * If set, the radius in pixels of foreground ripples when fully expanded. If unset, the radius
     * will be the distance from the center of the ripple to the furthest corner of the host element's
     * bounding rectangle.
     */
    radius: number;
    /**
     * Configuration for the ripple animation. Allows modifying the enter and exit animation
     * duration of the ripples. The animation durations will be overwritten if the
     * `NoopAnimationsModule` is being used.
     */
    animation: RippleAnimationConfig;
    /**
     * Whether click events will not trigger the ripple. Ripples can be still launched manually
     * by using the `launch()` method.
     */
    get disabled(): boolean;
    set disabled(value: boolean);
    private _disabled;
    /**
     * The element that triggers the ripple when click events are received.
     * Defaults to the directive's host element.
     */
    get trigger(): HTMLElement;
    set trigger(trigger: HTMLElement);
    private _trigger;
    /** Renderer for the ripple DOM manipulations. */
    private _rippleRenderer;
    /** Options that are set globally for all ripples. */
    private _globalOptions;
    /** @docs-private Whether ripple directive is initialized and the input bindings are set. */
    _isInitialized: boolean;
    constructor(_elementRef: ElementRef<HTMLElement>, ngZone: NgZone, platform: Platform, globalOptions?: RippleGlobalOptions, _animationMode?: string | undefined);
    ngOnInit(): void;
    ngOnDestroy(): void;
    /** Fades out all currently showing ripple elements. */
    fadeOutAll(): void;
    /** Fades out all currently showing non-persistent ripple elements. */
    fadeOutAllNonPersistent(): void;
    /**
     * Ripple configuration from the directive's input values.
     * @docs-private Implemented as part of RippleTarget
     */
    get rippleConfig(): RippleConfig;
    /**
     * Whether ripples on pointer-down are disabled or not.
     * @docs-private Implemented as part of RippleTarget
     */
    get rippleDisabled(): boolean;
    /** Sets up the trigger event listeners if ripples are enabled. */
    private _setupTriggerEventsIfEnabled;
    /**
     * Launches a manual ripple using the specified ripple configuration.
     * @param config Configuration for the manual ripple.
     */
    launch(config: RippleConfig): RippleRef;
    /**
     * Launches a manual ripple at the specified coordinates relative to the viewport.
     * @param x Coordinate along the X axis at which to fade-in the ripple. Coordinate
     *   should be relative to the viewport.
     * @param y Coordinate along the Y axis at which to fade-in the ripple. Coordinate
     *   should be relative to the viewport.
     * @param config Optional ripple configuration for the manual ripple.
     */
    launch(x: number, y: number, config?: RippleConfig): RippleRef;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatRipple, [null, null, null, { optional: true; }, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatRipple, "[mat-ripple], [matRipple]", ["matRipple"], { "color": { "alias": "matRippleColor"; "required": false; }; "unbounded": { "alias": "matRippleUnbounded"; "required": false; }; "centered": { "alias": "matRippleCentered"; "required": false; }; "radius": { "alias": "matRippleRadius"; "required": false; }; "animation": { "alias": "matRippleAnimation"; "required": false; }; "disabled": { "alias": "matRippleDisabled"; "required": false; }; "trigger": { "alias": "matRippleTrigger"; "required": false; }; }, {}, never, never, true, never>;
}

/**
 * Handles attaching ripples on demand.
 *
 * This service allows us to avoid eagerly creating & attaching MatRipples.
 * It works by creating & attaching a ripple only when a component is first interacted with.
 *
 * @docs-private
 */
export declare class MatRippleLoader implements OnDestroy {
    private _document;
    private _animationMode;
    private _globalRippleOptions;
    private _platform;
    private _ngZone;
    private _hosts;
    constructor();
    ngOnDestroy(): void;
    /**
     * Configures the ripple that will be rendered by the ripple loader.
     *
     * Stores the given information about how the ripple should be configured on the host
     * element so that it can later be retrived & used when the ripple is actually created.
     */
    configureRipple(host: HTMLElement, config: {
        className?: string;
        centered?: boolean;
        disabled?: boolean;
    }): void;
    /** Returns the ripple instance for the given host element. */
    getRipple(host: HTMLElement): MatRipple | undefined;
    /** Sets the disabled state on the ripple instance corresponding to the given host element. */
    setDisabled(host: HTMLElement, disabled: boolean): void;
    /** Handles creating and attaching component internals when a component it is initially interacted with. */
    private _onInteraction;
    /** Creates a MatRipple and appends it to the given element. */
    private _createRipple;
    attachRipple(host: HTMLElement, ripple: MatRipple): void;
    destroyRipple(host: HTMLElement): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatRippleLoader, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<MatRippleLoader>;
}

export declare class MatRippleModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatRippleModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatRippleModule, never, [typeof i1_2.MatCommonModule, typeof i2.MatRipple], [typeof i2.MatRipple, typeof i1_2.MatCommonModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatRippleModule>;
}

/**
 * Mixin to augment a directive with a `color` property.
 * @deprecated Use a plain input and host bindings instead.
 * @breaking-change 19.0.0
 */
export declare function mixinColor<T extends _AbstractConstructor<HasElementRef>>(base: T, defaultColor?: ThemePalette): CanColorCtor & T;

/**
 * Mixin to augment a directive with a `disabled` property.
 * @deprecated Use an input with a transform instead.
 * @breaking-change 19.0.0
 */
export declare function mixinDisabled<T extends _AbstractConstructor<{}>>(base: T): CanDisableCtor & T;

/**
 * Mixin to augment a directive with a `disableRipple` property.
 * @deprecated Use an input with a transform instead.
 * @breaking-change 19.0.0
 */
export declare function mixinDisableRipple<T extends _AbstractConstructor<{}>>(base: T): CanDisableRippleCtor & T;

/**
 * Mixin to augment a directive with updateErrorState method.
 * For component with `errorState` and need to update `errorState`.
 * @deprecated Implement the `updateErrorState` method directly.
 * @breaking-change 19.0.0
 */
export declare function mixinErrorState<T extends _AbstractConstructor<HasErrorState>>(base: T): CanUpdateErrorStateCtor & T;

/**
 * Mixin to augment a directive with an initialized property that will emits when ngOnInit ends.
 * @deprecated Track the initialized state manually.
 * @breaking-change 19.0.0
 */
export declare function mixinInitialized<T extends _Constructor<{}>>(base: T): HasInitializedCtor & T;

/**
 * Mixin to augment a directive with a `tabIndex` property.
 * @deprecated Use an input with a transform instead.
 * @breaking-change 19.0.0
 */
export declare function mixinTabIndex<T extends _AbstractConstructor<CanDisable>>(base: T, defaultTabIndex?: number): HasTabIndexCtor & T;

/** Adapts the native JS Date for use with cdk-based components that work with dates. */
export declare class NativeDateAdapter extends DateAdapter<Date> {
    /**
     * @deprecated No longer being used. To be removed.
     * @breaking-change 14.0.0
     */
    useUtcForDisplay: boolean;
    /** The injected locale. */
    private readonly _matDateLocale;
    constructor(
    /**
     * @deprecated Now injected via inject(), param to be removed.
     * @breaking-change 18.0.0
     */
    matDateLocale?: string);
    getYear(date: Date): number;
    getMonth(date: Date): number;
    getDate(date: Date): number;
    getDayOfWeek(date: Date): number;
    getMonthNames(style: 'long' | 'short' | 'narrow'): string[];
    getDateNames(): string[];
    getDayOfWeekNames(style: 'long' | 'short' | 'narrow'): string[];
    getYearName(date: Date): string;
    getFirstDayOfWeek(): number;
    getNumDaysInMonth(date: Date): number;
    clone(date: Date): Date;
    createDate(year: number, month: number, date: number): Date;
    today(): Date;
    parse(value: any, parseFormat?: any): Date | null;
    format(date: Date, displayFormat: Object): string;
    addCalendarYears(date: Date, years: number): Date;
    addCalendarMonths(date: Date, months: number): Date;
    addCalendarDays(date: Date, days: number): Date;
    toIso8601(date: Date): string;
    /**
     * Returns the given value if given a valid Date or null. Deserializes valid ISO 8601 strings
     * (https://www.ietf.org/rfc/rfc3339.txt) into valid Dates and empty string into null. Returns an
     * invalid date for all other values.
     */
    deserialize(value: any): Date | null;
    isDateInstance(obj: any): boolean;
    isValid(date: Date): boolean;
    invalid(): Date;
    /** Creates a date but allows the month and date to overflow. */
    private _createDateWithOverflow;
    /**
     * Pads a number to make it two digits.
     * @param n The number to pad.
     * @returns The padded number.
     */
    private _2digit;
    /**
     * When converting Date object to string, javascript built-in functions may return wrong
     * results because it applies its internal DST rules. The DST rules around the world change
     * very frequently, and the current valid rule is not always valid in previous years though.
     * We work around this problem building a new Date object which has its internal UTC
     * representation with the local date and time.
     * @param dtf Intl.DateTimeFormat object, containing the desired string format. It must have
     *    timeZone set to 'utc' to work fine.
     * @param date Date from which we want to get the string representation according to dtf
     * @returns A Date object with its UTC representation based on the passed in date info
     */
    private _format;
    static ɵfac: i0.ɵɵFactoryDeclaration<NativeDateAdapter, [{ optional: true; }]>;
    static ɵprov: i0.ɵɵInjectableDeclaration<NativeDateAdapter>;
}

export declare class NativeDateModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<NativeDateModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<NativeDateModule, never, never, never>;
    static ɵinj: i0.ɵɵInjectorDeclaration<NativeDateModule>;
}

export declare function provideNativeDateAdapter(formats?: MatDateFormats): Provider[];

/**
 * Interface that describes the configuration for the animation of a ripple.
 * There are two animation phases with different durations for the ripples.
 */
export declare interface RippleAnimationConfig {
    /** Duration in milliseconds for the enter animation (expansion from point of contact). */
    enterDuration?: number;
    /** Duration in milliseconds for the exit animation (fade-out). */
    exitDuration?: number;
}

export declare type RippleConfig = {
    color?: string;
    centered?: boolean;
    radius?: number;
    persistent?: boolean;
    animation?: RippleAnimationConfig;
    terminateOnPointerUp?: boolean;
};

/** Configurable options for `matRipple`. */
export declare interface RippleGlobalOptions {
    /**
     * Whether ripples should be disabled. Ripples can be still launched manually by using
     * the `launch()` method. Therefore focus indicators will still show up.
     */
    disabled?: boolean;
    /**
     * Default configuration for the animation duration of the ripples. There are two phases with
     * different durations for the ripples: `enter` and `leave`. The durations will be overwritten
     * by the value of `matRippleAnimation` or if the `NoopAnimationsModule` is included.
     */
    animation?: RippleAnimationConfig;
    /**
     * Whether ripples should start fading out immediately after the mouse or touch is released. By
     * default, ripples will wait for the enter animation to complete and for mouse or touch release.
     */
    terminateOnPointerUp?: boolean;
}

/**
 * Reference to a previously launched ripple element.
 */
export declare class RippleRef {
    private _renderer;
    /** Reference to the ripple HTML element. */
    element: HTMLElement;
    /** Ripple configuration used for the ripple. */
    config: RippleConfig;
    _animationForciblyDisabledThroughCss: boolean;
    /** Current state of the ripple. */
    state: RippleState;
    constructor(_renderer: {
        fadeOutRipple(ref: RippleRef): void;
    }, 
    /** Reference to the ripple HTML element. */
    element: HTMLElement, 
    /** Ripple configuration used for the ripple. */
    config: RippleConfig, _animationForciblyDisabledThroughCss?: boolean);
    /** Fades out the ripple element. */
    fadeOut(): void;
}

/**
 * Helper service that performs DOM manipulations. Not intended to be used outside this module.
 * The constructor takes a reference to the ripple directive's host element and a map of DOM
 * event handlers to be installed on the element that triggers ripple animations.
 * This will eventually become a custom renderer once Angular support exists.
 * @docs-private
 */
export declare class RippleRenderer implements EventListenerObject {
    private _target;
    private _ngZone;
    private _platform;
    /** Element where the ripples are being added to. */
    private _containerElement;
    /** Element which triggers the ripple elements on mouse events. */
    private _triggerElement;
    /** Whether the pointer is currently down or not. */
    private _isPointerDown;
    /**
     * Map of currently active ripple references.
     * The ripple reference is mapped to its element event listeners.
     * The reason why `| null` is used is that event listeners are added only
     * when the condition is truthy (see the `_startFadeOutTransition` method).
     */
    private _activeRipples;
    /** Latest non-persistent ripple that was triggered. */
    private _mostRecentTransientRipple;
    /** Time in milliseconds when the last touchstart event happened. */
    private _lastTouchStartEvent;
    /** Whether pointer-up event listeners have been registered. */
    private _pointerUpEventsRegistered;
    /**
     * Cached dimensions of the ripple container. Set when the first
     * ripple is shown and cleared once no more ripples are visible.
     */
    private _containerRect;
    private static _eventManager;
    constructor(_target: RippleTarget, _ngZone: NgZone, elementOrElementRef: HTMLElement | ElementRef<HTMLElement>, _platform: Platform);
    /**
     * Fades in a ripple at the given coordinates.
     * @param x Coordinate within the element, along the X axis at which to start the ripple.
     * @param y Coordinate within the element, along the Y axis at which to start the ripple.
     * @param config Extra ripple options.
     */
    fadeInRipple(x: number, y: number, config?: RippleConfig): RippleRef;
    /** Fades out a ripple reference. */
    fadeOutRipple(rippleRef: RippleRef): void;
    /** Fades out all currently active ripples. */
    fadeOutAll(): void;
    /** Fades out all currently active non-persistent ripples. */
    fadeOutAllNonPersistent(): void;
    /** Sets up the trigger event listeners */
    setupTriggerEvents(elementOrElementRef: HTMLElement | ElementRef<HTMLElement>): void;
    /**
     * Handles all registered events.
     * @docs-private
     */
    handleEvent(event: Event): void;
    /** Method that will be called if the fade-in or fade-in transition completed. */
    private _finishRippleTransition;
    /**
     * Starts the fade-out transition of the given ripple if it's not persistent and the pointer
     * is not held down anymore.
     */
    private _startFadeOutTransition;
    /** Destroys the given ripple by removing it from the DOM and updating its state. */
    private _destroyRipple;
    /** Function being called whenever the trigger is being pressed using mouse. */
    private _onMousedown;
    /** Function being called whenever the trigger is being pressed using touch. */
    private _onTouchStart;
    /** Function being called whenever the trigger is being released. */
    private _onPointerUp;
    private _getActiveRipples;
    /** Removes previously registered event listeners from the trigger element. */
    _removeTriggerEvents(): void;
}


/** Possible states for a ripple element. */
export declare enum RippleState {
    FADING_IN = 0,
    VISIBLE = 1,
    FADING_OUT = 2,
    HIDDEN = 3
}

/**
 * Interface that describes the target for launching ripples.
 * It defines the ripple configuration and disabled state for interaction ripples.
 * @docs-private
 */
export declare interface RippleTarget {
    /** Configuration for ripples that are launched on pointer down. */
    rippleConfig: RippleConfig;
    /** Whether ripples on pointer down should be disabled. */
    rippleDisabled: boolean;
}

/**
 * Possible sanity checks that can be enabled. If set to
 * true/false, all checks will be enabled/disabled.
 */
export declare type SanityChecks = boolean | GranularSanityChecks;

/**
 * Helper that takes a query list of lines and sets the correct class on the host.
 * @docs-private
 */
export declare function setLines(lines: QueryList<unknown>, element: ElementRef<HTMLElement>, prefix?: string): void;

/** Error state matcher that matches when a control is invalid and dirty. */
export declare class ShowOnDirtyErrorStateMatcher implements ErrorStateMatcher {
    isErrorState(control: AbstractControl | null, form: FormGroupDirective | NgForm | null): boolean;
    static ɵfac: i0.ɵɵFactoryDeclaration<ShowOnDirtyErrorStateMatcher, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<ShowOnDirtyErrorStateMatcher>;
}

/** Possible color palette values. */
export declare type ThemePalette = 'primary' | 'accent' | 'warn' | undefined;

/** Current version of Angular Material. */
export declare const VERSION: Version;

export { }
