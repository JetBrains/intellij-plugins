import { _AbstractConstructor } from '@angular/material/core';
import { AbstractControl } from '@angular/forms';
import { AfterContentInit } from '@angular/core';
import { AfterViewChecked } from '@angular/core';
import { AfterViewInit } from '@angular/core';
import { AnimationEvent as AnimationEvent_2 } from '@angular/animations';
import { AnimationTriggerMetadata } from '@angular/animations';
import { BooleanInput } from '@angular/cdk/coercion';
import { CanColor } from '@angular/material/core';
import { CanUpdateErrorState } from '@angular/material/core';
import { ChangeDetectorRef } from '@angular/core';
import { ComponentType } from '@angular/cdk/portal';
import { _Constructor } from '@angular/material/core';
import { ControlContainer } from '@angular/forms';
import { ControlValueAccessor } from '@angular/forms';
import { DateAdapter } from '@angular/material/core';
import { Directionality } from '@angular/cdk/bidi';
import { DoCheck } from '@angular/core';
import { ElementRef } from '@angular/core';
import { ErrorStateMatcher } from '@angular/material/core';
import { EventEmitter } from '@angular/core';
import { FactoryProvider } from '@angular/core';
import { FocusOrigin } from '@angular/cdk/a11y';
import { FormGroupDirective } from '@angular/forms';
import * as i0 from '@angular/core';
import * as i14 from '@angular/common';
import * as i15 from '@angular/material/button';
import * as i16 from '@angular/cdk/overlay';
import * as i17 from '@angular/cdk/a11y';
import * as i18 from '@angular/cdk/portal';
import * as i19 from '@angular/material/core';
import * as i20 from '@angular/cdk/scrolling';
import { InjectionToken } from '@angular/core';
import { Injector } from '@angular/core';
import { MatButton } from '@angular/material/button';
import { MatDateFormats } from '@angular/material/core';
import { MatFormFieldControl } from '@angular/material/form-field';
import { NgControl } from '@angular/forms';
import { NgForm } from '@angular/forms';
import { NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { OnChanges } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { OnInit } from '@angular/core';
import { Overlay } from '@angular/cdk/overlay';
import { Portal } from '@angular/cdk/portal';
import { ScrollStrategy } from '@angular/cdk/overlay';
import { SimpleChanges } from '@angular/core';
import { Subject } from 'rxjs';
import { TemplatePortal } from '@angular/cdk/portal';
import { TemplateRef } from '@angular/core';
import { ThemePalette } from '@angular/material/core';
import { ValidationErrors } from '@angular/forms';
import { Validator } from '@angular/forms';
import { ValidatorFn } from '@angular/forms';
import { ViewContainerRef } from '@angular/core';

/** Function that can be used to filter out dates from a calendar. */
export declare type DateFilterFn<D> = (date: D | null) => boolean;

/** Possible positions for the datepicker dropdown along the X axis. */
export declare type DatepickerDropdownPositionX = 'start' | 'end';

/** Possible positions for the datepicker dropdown along the Y axis. */
export declare type DatepickerDropdownPositionY = 'above' | 'below';

/** A class representing a range of dates. */
export declare class DateRange<D> {
    /** The start date of the range. */
    readonly start: D | null;
    /** The end date of the range. */
    readonly end: D | null;
    /**
     * Ensures that objects with a `start` and `end` property can't be assigned to a variable that
     * expects a `DateRange`
     */
    private _disableStructuralEquivalency;
    constructor(
    /** The start date of the range. */
    start: D | null, 
    /** The end date of the range. */
    end: D | null);
}

/**
 * Event emitted by the date selection model when its selection changes.
 * @docs-private
 */
export declare interface DateSelectionModelChange<S> {
    /** New value for the selection. */
    selection: S;
    /** Object that triggered the change. */
    source: unknown;
    /** Previous value */
    oldValue?: S;
}

/** Provides the default date range selection behavior. */
export declare class DefaultMatCalendarRangeStrategy<D> implements MatDateRangeSelectionStrategy<D> {
    private _dateAdapter;
    constructor(_dateAdapter: DateAdapter<D>);
    selectionFinished(date: D, currentRange: DateRange<D>): DateRange<D>;
    createPreview(activeDate: D | null, currentRange: DateRange<D>): DateRange<D>;
    createDrag(dragOrigin: D, originalRange: DateRange<D>, newDate: D): DateRange<D> | null;
    static ɵfac: i0.ɵɵFactoryDeclaration<DefaultMatCalendarRangeStrategy<any>, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<DefaultMatCalendarRangeStrategy<any>>;
}

/**
 * Conditionally picks the date type, if a DateRange is passed in.
 * @docs-private
 */
export declare type ExtractDateTypeFromSelection<T> = T extends DateRange<infer D> ? D : NonNullable<T>;

/**
 * When the multi-year view is first opened, the active year will be in view.
 * So we compute how many years are between the active year and the *slot* where our
 * "startingYear" will render when paged into view.
 */
declare function getActiveOffset<D>(dateAdapter: DateAdapter<D>, activeDate: D, minDate: D | null, maxDate: D | null): number;

declare namespace i1 {
    export {
        MatCalendarView,
        MatCalendarHeader,
        MatCalendar
    }
}

declare namespace i10 {
    export {
        MatDateRangeInput
    }
}

declare namespace i11 {
    export {
        MatDateRangeInputParent,
        MAT_DATE_RANGE_INPUT_PARENT,
        MatStartDate,
        MatEndDate
    }
}

declare namespace i12 {
    export {
        MatDateRangePickerInput,
        MatDateRangePicker
    }
}

declare namespace i13 {
    export {
        MatDatepickerApply,
        MatDatepickerCancel,
        MatDatepickerActions
    }
}

declare namespace i2 {
    export {
        MatCalendarCellCssClasses,
        MatCalendarCellClassFunction,
        MatCalendarCell,
        MatCalendarUserEvent,
        MatCalendarBody
    }
}

declare namespace i3 {
    export {
        MatDatepicker
    }
}

declare namespace i4 {
    export {
        MAT_DATEPICKER_SCROLL_STRATEGY_FACTORY,
        MAT_DATEPICKER_SCROLL_STRATEGY,
        DatepickerDropdownPositionX,
        DatepickerDropdownPositionY,
        MAT_DATEPICKER_SCROLL_STRATEGY_FACTORY_PROVIDER,
        MatDatepickerContent,
        MatDatepickerControl,
        MatDatepickerPanel,
        MatDatepickerBase
    }
}

declare namespace i5 {
    export {
        MAT_DATEPICKER_VALUE_ACCESSOR,
        MAT_DATEPICKER_VALIDATORS,
        MatDatepickerInput
    }
}

declare namespace i6 {
    export {
        MatDatepickerToggleIcon,
        MatDatepickerToggle
    }
}

declare namespace i7 {
    export {
        MatMonthView
    }
}

declare namespace i8 {
    export {
        MatYearView
    }
}

declare namespace i9 {
    export {
        isSameMultiYearView,
        getActiveOffset,
        yearsPerPage,
        yearsPerRow,
        MatMultiYearView
    }
}

declare function isSameMultiYearView<D>(dateAdapter: DateAdapter<D>, date1: D, date2: D, minDate: D | null, maxDate: D | null): boolean;

/**
 * Used to provide the date range input wrapper component
 * to the parts without circular dependencies.
 */
declare const MAT_DATE_RANGE_INPUT_PARENT: InjectionToken<MatDateRangeInputParent<unknown>>;

/** Injection token used to customize the date range selection behavior. */
export declare const MAT_DATE_RANGE_SELECTION_STRATEGY: InjectionToken<MatDateRangeSelectionStrategy<any>>;

/** Injection token that determines the scroll handling while the calendar is open. */
export declare const MAT_DATEPICKER_SCROLL_STRATEGY: InjectionToken<() => ScrollStrategy>;

/** @docs-private */
export declare function MAT_DATEPICKER_SCROLL_STRATEGY_FACTORY(overlay: Overlay): () => ScrollStrategy;

/** @docs-private */
export declare const MAT_DATEPICKER_SCROLL_STRATEGY_FACTORY_PROVIDER: {
    provide: InjectionToken<() => ScrollStrategy>;
    deps: (typeof Overlay)[];
    useFactory: typeof MAT_DATEPICKER_SCROLL_STRATEGY_FACTORY;
};

/** @docs-private */
export declare const MAT_DATEPICKER_VALIDATORS: any;

/** @docs-private */
export declare const MAT_DATEPICKER_VALUE_ACCESSOR: any;

/** @docs-private */
export declare function MAT_RANGE_DATE_SELECTION_MODEL_FACTORY(parent: MatSingleDateSelectionModel<unknown>, adapter: DateAdapter<unknown>): MatSingleDateSelectionModel<unknown>;

/**
 * Used to provide a range selection model to a component.
 * @docs-private
 */
export declare const MAT_RANGE_DATE_SELECTION_MODEL_PROVIDER: FactoryProvider;

/** @docs-private */
export declare function MAT_SINGLE_DATE_SELECTION_MODEL_FACTORY(parent: MatSingleDateSelectionModel<unknown>, adapter: DateAdapter<unknown>): MatSingleDateSelectionModel<unknown>;

/**
 * Used to provide a single selection model to a component.
 * @docs-private
 */
export declare const MAT_SINGLE_DATE_SELECTION_MODEL_PROVIDER: FactoryProvider;

/** A calendar that is used as part of the datepicker. */
export declare class MatCalendar<D> implements AfterContentInit, AfterViewChecked, OnDestroy, OnChanges {
    private _dateAdapter;
    private _dateFormats;
    private _changeDetectorRef;
    /** An input indicating the type of the header component, if set. */
    headerComponent: ComponentType<any>;
    /** A portal containing the header component type for this calendar. */
    _calendarHeaderPortal: Portal<any>;
    private _intlChanges;
    /**
     * Used for scheduling that focus should be moved to the active cell on the next tick.
     * We need to schedule it, rather than do it immediately, because we have to wait
     * for Angular to re-evaluate the view children.
     */
    private _moveFocusOnNextTick;
    /** A date representing the period (month or year) to start the calendar in. */
    get startAt(): D | null;
    set startAt(value: D | null);
    private _startAt;
    /** Whether the calendar should be started in month or year view. */
    startView: MatCalendarView;
    /** The currently selected date. */
    get selected(): DateRange<D> | D | null;
    set selected(value: DateRange<D> | D | null);
    private _selected;
    /** The minimum selectable date. */
    get minDate(): D | null;
    set minDate(value: D | null);
    private _minDate;
    /** The maximum selectable date. */
    get maxDate(): D | null;
    set maxDate(value: D | null);
    private _maxDate;
    /** Function used to filter which dates are selectable. */
    dateFilter: (date: D) => boolean;
    /** Function that can be used to add custom CSS classes to dates. */
    dateClass: MatCalendarCellClassFunction<D>;
    /** Start of the comparison range. */
    comparisonStart: D | null;
    /** End of the comparison range. */
    comparisonEnd: D | null;
    /** ARIA Accessible name of the `<input matStartDate/>` */
    startDateAccessibleName: string | null;
    /** ARIA Accessible name of the `<input matEndDate/>` */
    endDateAccessibleName: string | null;
    /** Emits when the currently selected date changes. */
    readonly selectedChange: EventEmitter<D | null>;
    /**
     * Emits the year chosen in multiyear view.
     * This doesn't imply a change on the selected date.
     */
    readonly yearSelected: EventEmitter<D>;
    /**
     * Emits the month chosen in year view.
     * This doesn't imply a change on the selected date.
     */
    readonly monthSelected: EventEmitter<D>;
    /**
     * Emits when the current view changes.
     */
    readonly viewChanged: EventEmitter<MatCalendarView>;
    /** Emits when any date is selected. */
    readonly _userSelection: EventEmitter<MatCalendarUserEvent<D | null>>;
    /** Emits a new date range value when the user completes a drag drop operation. */
    readonly _userDragDrop: EventEmitter<MatCalendarUserEvent<DateRange<D>>>;
    /** Reference to the current month view component. */
    monthView: MatMonthView<D>;
    /** Reference to the current year view component. */
    yearView: MatYearView<D>;
    /** Reference to the current multi-year view component. */
    multiYearView: MatMultiYearView<D>;
    /**
     * The current active date. This determines which time period is shown and which date is
     * highlighted when using keyboard navigation.
     */
    get activeDate(): D;
    set activeDate(value: D);
    private _clampedActiveDate;
    /** Whether the calendar is in month view. */
    get currentView(): MatCalendarView;
    set currentView(value: MatCalendarView);
    private _currentView;
    /** Origin of active drag, or null when dragging is not active. */
    protected _activeDrag: MatCalendarUserEvent<D> | null;
    /**
     * Emits whenever there is a state change that the header may need to respond to.
     */
    readonly stateChanges: Subject<void>;
    constructor(_intl: MatDatepickerIntl, _dateAdapter: DateAdapter<D>, _dateFormats: MatDateFormats, _changeDetectorRef: ChangeDetectorRef);
    ngAfterContentInit(): void;
    ngAfterViewChecked(): void;
    ngOnDestroy(): void;
    ngOnChanges(changes: SimpleChanges): void;
    /** Focuses the active date. */
    focusActiveCell(): void;
    /** Updates today's date after an update of the active date */
    updateTodaysDate(): void;
    /** Handles date selection in the month view. */
    _dateSelected(event: MatCalendarUserEvent<D | null>): void;
    /** Handles year selection in the multiyear view. */
    _yearSelectedInMultiYearView(normalizedYear: D): void;
    /** Handles month selection in the year view. */
    _monthSelectedInYearView(normalizedMonth: D): void;
    /** Handles year/month selection in the multi-year/year views. */
    _goToDateInView(date: D, view: 'month' | 'year' | 'multi-year'): void;
    /** Called when the user starts dragging to change a date range. */
    _dragStarted(event: MatCalendarUserEvent<D>): void;
    /**
     * Called when a drag completes. It may end in cancelation or in the selection
     * of a new range.
     */
    _dragEnded(event: MatCalendarUserEvent<DateRange<D> | null>): void;
    /** Returns the component instance that corresponds to the current calendar view. */
    private _getCurrentViewComponent;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatCalendar<any>, [null, { optional: true; }, { optional: true; }, null]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatCalendar<any>, "mat-calendar", ["matCalendar"], { "headerComponent": "headerComponent"; "startAt": "startAt"; "startView": "startView"; "selected": "selected"; "minDate": "minDate"; "maxDate": "maxDate"; "dateFilter": "dateFilter"; "dateClass": "dateClass"; "comparisonStart": "comparisonStart"; "comparisonEnd": "comparisonEnd"; "startDateAccessibleName": "startDateAccessibleName"; "endDateAccessibleName": "endDateAccessibleName"; }, { "selectedChange": "selectedChange"; "yearSelected": "yearSelected"; "monthSelected": "monthSelected"; "viewChanged": "viewChanged"; "_userSelection": "_userSelection"; "_userDragDrop": "_userDragDrop"; }, never, never, false, never>;
}

/**
 * An internal component used to display calendar data in a table.
 * @docs-private
 */
export declare class MatCalendarBody<D = any> implements OnChanges, OnDestroy, AfterViewChecked {
    private _elementRef;
    private _ngZone;
    private _platform;
    /**
     * Used to skip the next focus event when rendering the preview range.
     * We need a flag like this, because some browsers fire focus events asynchronously.
     */
    private _skipNextFocus;
    /**
     * Used to focus the active cell after change detection has run.
     */
    private _focusActiveCellAfterViewChecked;
    /** The label for the table. (e.g. "Jan 2017"). */
    label: string;
    /** The cells to display in the table. */
    rows: MatCalendarCell[][];
    /** The value in the table that corresponds to today. */
    todayValue: number;
    /** Start value of the selected date range. */
    startValue: number;
    /** End value of the selected date range. */
    endValue: number;
    /** The minimum number of free cells needed to fit the label in the first row. */
    labelMinRequiredCells: number;
    /** The number of columns in the table. */
    numCols: number;
    /** The cell number of the active cell in the table. */
    activeCell: number;
    ngAfterViewChecked(): void;
    /** Whether a range is being selected. */
    isRange: boolean;
    /**
     * The aspect ratio (width / height) to use for the cells in the table. This aspect ratio will be
     * maintained even as the table resizes.
     */
    cellAspectRatio: number;
    /** Start of the comparison range. */
    comparisonStart: number | null;
    /** End of the comparison range. */
    comparisonEnd: number | null;
    /** Start of the preview range. */
    previewStart: number | null;
    /** End of the preview range. */
    previewEnd: number | null;
    /** ARIA Accessible name of the `<input matStartDate/>` */
    startDateAccessibleName: string | null;
    /** ARIA Accessible name of the `<input matEndDate/>` */
    endDateAccessibleName: string | null;
    /** Emits when a new value is selected. */
    readonly selectedValueChange: EventEmitter<MatCalendarUserEvent<number>>;
    /** Emits when the preview has changed as a result of a user action. */
    readonly previewChange: EventEmitter<MatCalendarUserEvent<MatCalendarCell<any> | null>>;
    readonly activeDateChange: EventEmitter<MatCalendarUserEvent<number>>;
    /** Emits the date at the possible start of a drag event. */
    readonly dragStarted: EventEmitter<MatCalendarUserEvent<D>>;
    /** Emits the date at the conclusion of a drag, or null if mouse was not released on a date. */
    readonly dragEnded: EventEmitter<MatCalendarUserEvent<D | null>>;
    /** The number of blank cells to put at the beginning for the first row. */
    _firstRowOffset: number;
    /** Padding for the individual date cells. */
    _cellPadding: string;
    /** Width of an individual cell. */
    _cellWidth: string;
    private _didDragSinceMouseDown;
    constructor(_elementRef: ElementRef<HTMLElement>, _ngZone: NgZone);
    /** Called when a cell is clicked. */
    _cellClicked(cell: MatCalendarCell, event: MouseEvent): void;
    _emitActiveDateChange(cell: MatCalendarCell, event: FocusEvent): void;
    /** Returns whether a cell should be marked as selected. */
    _isSelected(value: number): boolean;
    ngOnChanges(changes: SimpleChanges): void;
    ngOnDestroy(): void;
    /** Returns whether a cell is active. */
    _isActiveCell(rowIndex: number, colIndex: number): boolean;
    /**
     * Focuses the active cell after the microtask queue is empty.
     *
     * Adding a 0ms setTimeout seems to fix Voiceover losing focus when pressing PageUp/PageDown
     * (issue #24330).
     *
     * Determined a 0ms by gradually increasing duration from 0 and testing two use cases with screen
     * reader enabled:
     *
     * 1. Pressing PageUp/PageDown repeatedly with pausing between each key press.
     * 2. Pressing and holding the PageDown key with repeated keys enabled.
     *
     * Test 1 worked roughly 95-99% of the time with 0ms and got a little bit better as the duration
     * increased. Test 2 got slightly better until the duration was long enough to interfere with
     * repeated keys. If the repeated key speed was faster than the timeout duration, then pressing
     * and holding pagedown caused the entire page to scroll.
     *
     * Since repeated key speed can verify across machines, determined that any duration could
     * potentially interfere with repeated keys. 0ms would be best because it almost entirely
     * eliminates the focus being lost in Voiceover (#24330) without causing unintended side effects.
     * Adding delay also complicates writing tests.
     */
    _focusActiveCell(movePreview?: boolean): void;
    /** Focuses the active cell after change detection has run and the microtask queue is empty. */
    _scheduleFocusActiveCellAfterViewChecked(): void;
    /** Gets whether a value is the start of the main range. */
    _isRangeStart(value: number): boolean;
    /** Gets whether a value is the end of the main range. */
    _isRangeEnd(value: number): boolean;
    /** Gets whether a value is within the currently-selected range. */
    _isInRange(value: number): boolean;
    /** Gets whether a value is the start of the comparison range. */
    _isComparisonStart(value: number): boolean;
    /** Whether the cell is a start bridge cell between the main and comparison ranges. */
    _isComparisonBridgeStart(value: number, rowIndex: number, colIndex: number): boolean;
    /** Whether the cell is an end bridge cell between the main and comparison ranges. */
    _isComparisonBridgeEnd(value: number, rowIndex: number, colIndex: number): boolean;
    /** Gets whether a value is the end of the comparison range. */
    _isComparisonEnd(value: number): boolean;
    /** Gets whether a value is within the current comparison range. */
    _isInComparisonRange(value: number): boolean;
    /**
     * Gets whether a value is the same as the start and end of the comparison range.
     * For context, the functions that we use to determine whether something is the start/end of
     * a range don't allow for the start and end to be on the same day, because we'd have to use
     * much more specific CSS selectors to style them correctly in all scenarios. This is fine for
     * the regular range, because when it happens, the selected styles take over and still show where
     * the range would've been, however we don't have these selected styles for a comparison range.
     * This function is used to apply a class that serves the same purpose as the one for selected
     * dates, but it only applies in the context of a comparison range.
     */
    _isComparisonIdentical(value: number): boolean;
    /** Gets whether a value is the start of the preview range. */
    _isPreviewStart(value: number): boolean;
    /** Gets whether a value is the end of the preview range. */
    _isPreviewEnd(value: number): boolean;
    /** Gets whether a value is inside the preview range. */
    _isInPreview(value: number): boolean;
    /** Gets ids of aria descriptions for the start and end of a date range. */
    _getDescribedby(value: number): string | null;
    /**
     * Event handler for when the user enters an element
     * inside the calendar body (e.g. by hovering in or focus).
     */
    private _enterHandler;
    private _touchmoveHandler;
    /**
     * Event handler for when the user's pointer leaves an element
     * inside the calendar body (e.g. by hovering out or blurring).
     */
    private _leaveHandler;
    /**
     * Triggered on mousedown or touchstart on a date cell.
     * Respsonsible for starting a drag sequence.
     */
    private _mousedownHandler;
    /** Triggered on mouseup anywhere. Respsonsible for ending a drag sequence. */
    private _mouseupHandler;
    /** Triggered on touchend anywhere. Respsonsible for ending a drag sequence. */
    private _touchendHandler;
    /** Finds the MatCalendarCell that corresponds to a DOM node. */
    private _getCellFromElement;
    private _id;
    _startDateLabelId: string;
    _endDateLabelId: string;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatCalendarBody<any>, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatCalendarBody<any>, "[mat-calendar-body]", ["matCalendarBody"], { "label": "label"; "rows": "rows"; "todayValue": "todayValue"; "startValue": "startValue"; "endValue": "endValue"; "labelMinRequiredCells": "labelMinRequiredCells"; "numCols": "numCols"; "activeCell": "activeCell"; "isRange": "isRange"; "cellAspectRatio": "cellAspectRatio"; "comparisonStart": "comparisonStart"; "comparisonEnd": "comparisonEnd"; "previewStart": "previewStart"; "previewEnd": "previewEnd"; "startDateAccessibleName": "startDateAccessibleName"; "endDateAccessibleName": "endDateAccessibleName"; }, { "selectedValueChange": "selectedValueChange"; "previewChange": "previewChange"; "activeDateChange": "activeDateChange"; "dragStarted": "dragStarted"; "dragEnded": "dragEnded"; }, never, never, false, never>;
}

/**
 * An internal class that represents the data corresponding to a single calendar cell.
 * @docs-private
 */
export declare class MatCalendarCell<D = any> {
    value: number;
    displayValue: string;
    ariaLabel: string;
    enabled: boolean;
    cssClasses: MatCalendarCellCssClasses;
    compareValue: number;
    rawValue?: D | undefined;
    constructor(value: number, displayValue: string, ariaLabel: string, enabled: boolean, cssClasses?: MatCalendarCellCssClasses, compareValue?: number, rawValue?: D | undefined);
}

/** Function that can generate the extra classes that should be added to a calendar cell. */
export declare type MatCalendarCellClassFunction<D> = (date: D, view: 'month' | 'year' | 'multi-year') => MatCalendarCellCssClasses;

/** Extra CSS classes that can be associated with a calendar cell. */
export declare type MatCalendarCellCssClasses = string | string[] | Set<string> | {
    [key: string]: any;
};

/** Default header for MatCalendar */
export declare class MatCalendarHeader<D> {
    private _intl;
    calendar: MatCalendar<D>;
    private _dateAdapter;
    private _dateFormats;
    constructor(_intl: MatDatepickerIntl, calendar: MatCalendar<D>, _dateAdapter: DateAdapter<D>, _dateFormats: MatDateFormats, changeDetectorRef: ChangeDetectorRef);
    /** The display text for the current calendar view. */
    get periodButtonText(): string;
    /** The aria description for the current calendar view. */
    get periodButtonDescription(): string;
    /** The `aria-label` for changing the calendar view. */
    get periodButtonLabel(): string;
    /** The label for the previous button. */
    get prevButtonLabel(): string;
    /** The label for the next button. */
    get nextButtonLabel(): string;
    /** Handles user clicks on the period label. */
    currentPeriodClicked(): void;
    /** Handles user clicks on the previous button. */
    previousClicked(): void;
    /** Handles user clicks on the next button. */
    nextClicked(): void;
    /** Whether the previous period button is enabled. */
    previousEnabled(): boolean;
    /** Whether the next period button is enabled. */
    nextEnabled(): boolean;
    /** Whether the two dates represent the same view in the current view mode (month or year). */
    private _isSameView;
    /**
     * Format two individual labels for the minimum year and maximum year available in the multi-year
     * calendar view. Returns an array of two strings where the first string is the formatted label
     * for the minimum year, and the second string is the formatted label for the maximum year.
     */
    private _formatMinAndMaxYearLabels;
    private _id;
    _periodButtonLabelId: string;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatCalendarHeader<any>, [null, null, { optional: true; }, { optional: true; }, null]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatCalendarHeader<any>, "mat-calendar-header", ["matCalendarHeader"], {}, {}, never, ["*"], false, never>;
}

/** Event emitted when a date inside the calendar is triggered as a result of a user action. */
export declare interface MatCalendarUserEvent<D> {
    value: D;
    event: Event;
}

/**
 * Possible views for the calendar.
 * @docs-private
 */
export declare type MatCalendarView = 'month' | 'year' | 'multi-year';

/** Component responsible for managing the datepicker popup/dialog. */
export declare class MatDatepicker<D> extends MatDatepickerBase<MatDatepickerControl<D>, D | null, D> {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDatepicker<any>, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatDatepicker<any>, "mat-datepicker", ["matDatepicker"], {}, {}, never, never, false, never>;
}

/**
 * Container that can be used to project a row of action buttons
 * to the bottom of a datepicker or date range picker.
 */
export declare class MatDatepickerActions implements AfterViewInit, OnDestroy {
    private _datepicker;
    private _viewContainerRef;
    _template: TemplateRef<unknown>;
    private _portal;
    constructor(_datepicker: MatDatepickerBase<MatDatepickerControl<any>, unknown>, _viewContainerRef: ViewContainerRef);
    ngAfterViewInit(): void;
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDatepickerActions, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatDatepickerActions, "mat-datepicker-actions, mat-date-range-picker-actions", never, {}, {}, never, ["*"], false, never>;
}

/**
 * Animations used by the Material datepicker.
 * @docs-private
 */
export declare const matDatepickerAnimations: {
    readonly transformPanel: AnimationTriggerMetadata;
    readonly fadeInCalendar: AnimationTriggerMetadata;
};

/** Button that will close the datepicker and assign the current selection to the data model. */
export declare class MatDatepickerApply {
    private _datepicker;
    constructor(_datepicker: MatDatepickerBase<MatDatepickerControl<any>, unknown>);
    _applySelection(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDatepickerApply, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatDatepickerApply, "[matDatepickerApply], [matDateRangePickerApply]", never, {}, {}, never, never, false, never>;
}

/** Base class for a datepicker. */
declare abstract class MatDatepickerBase<C extends MatDatepickerControl<D>, S, D = ExtractDateTypeFromSelection<S>> implements MatDatepickerPanel<C, S, D>, OnDestroy, OnChanges {
    private _overlay;
    private _ngZone;
    private _viewContainerRef;
    private _dateAdapter;
    private _dir;
    private _model;
    private _scrollStrategy;
    private _inputStateChanges;
    private _document;
    /** An input indicating the type of the custom header component for the calendar, if set. */
    calendarHeaderComponent: ComponentType<any>;
    /** The date to open the calendar to initially. */
    get startAt(): D | null;
    set startAt(value: D | null);
    private _startAt;
    /** The view that the calendar should start in. */
    startView: 'month' | 'year' | 'multi-year';
    /** Color palette to use on the datepicker's calendar. */
    get color(): ThemePalette;
    set color(value: ThemePalette);
    _color: ThemePalette;
    /**
     * Whether the calendar UI is in touch mode. In touch mode the calendar opens in a dialog rather
     * than a dropdown and elements have more padding to allow for bigger touch targets.
     */
    get touchUi(): boolean;
    set touchUi(value: BooleanInput);
    private _touchUi;
    /** Whether the datepicker pop-up should be disabled. */
    get disabled(): boolean;
    set disabled(value: BooleanInput);
    private _disabled;
    /** Preferred position of the datepicker in the X axis. */
    xPosition: DatepickerDropdownPositionX;
    /** Preferred position of the datepicker in the Y axis. */
    yPosition: DatepickerDropdownPositionY;
    /**
     * Whether to restore focus to the previously-focused element when the calendar is closed.
     * Note that automatic focus restoration is an accessibility feature and it is recommended that
     * you provide your own equivalent, if you decide to turn it off.
     */
    get restoreFocus(): boolean;
    set restoreFocus(value: BooleanInput);
    private _restoreFocus;
    /**
     * Emits selected year in multiyear view.
     * This doesn't imply a change on the selected date.
     */
    readonly yearSelected: EventEmitter<D>;
    /**
     * Emits selected month in year view.
     * This doesn't imply a change on the selected date.
     */
    readonly monthSelected: EventEmitter<D>;
    /**
     * Emits when the current view changes.
     */
    readonly viewChanged: EventEmitter<MatCalendarView>;
    /** Function that can be used to add custom CSS classes to dates. */
    dateClass: MatCalendarCellClassFunction<D>;
    /** Emits when the datepicker has been opened. */
    readonly openedStream: EventEmitter<void>;
    /** Emits when the datepicker has been closed. */
    readonly closedStream: EventEmitter<void>;
    /**
     * Classes to be passed to the date picker panel.
     * Supports string and string array values, similar to `ngClass`.
     */
    get panelClass(): string | string[];
    set panelClass(value: string | string[]);
    private _panelClass;
    /** Whether the calendar is open. */
    get opened(): boolean;
    set opened(value: BooleanInput);
    private _opened;
    /** The id for the datepicker calendar. */
    id: string;
    /** The minimum selectable date. */
    _getMinDate(): D | null;
    /** The maximum selectable date. */
    _getMaxDate(): D | null;
    _getDateFilter(): DateFilterFn<D>;
    /** A reference to the overlay into which we've rendered the calendar. */
    private _overlayRef;
    /** Reference to the component instance rendered in the overlay. */
    private _componentRef;
    /** The element that was focused before the datepicker was opened. */
    private _focusedElementBeforeOpen;
    /** Unique class that will be added to the backdrop so that the test harnesses can look it up. */
    private _backdropHarnessClass;
    /** Currently-registered actions portal. */
    private _actionsPortal;
    /** The input element this datepicker is associated with. */
    datepickerInput: C;
    /** Emits when the datepicker's state changes. */
    readonly stateChanges: Subject<void>;
    constructor(_overlay: Overlay, _ngZone: NgZone, _viewContainerRef: ViewContainerRef, scrollStrategy: any, _dateAdapter: DateAdapter<D>, _dir: Directionality, _model: MatDateSelectionModel<S, D>);
    ngOnChanges(changes: SimpleChanges): void;
    ngOnDestroy(): void;
    /** Selects the given date */
    select(date: D): void;
    /** Emits the selected year in multiyear view */
    _selectYear(normalizedYear: D): void;
    /** Emits selected month in year view */
    _selectMonth(normalizedMonth: D): void;
    /** Emits changed view */
    _viewChanged(view: MatCalendarView): void;
    /**
     * Register an input with this datepicker.
     * @param input The datepicker input to register with this datepicker.
     * @returns Selection model that the input should hook itself up to.
     */
    registerInput(input: C): MatDateSelectionModel<S, D>;
    /**
     * Registers a portal containing action buttons with the datepicker.
     * @param portal Portal to be registered.
     */
    registerActions(portal: TemplatePortal): void;
    /**
     * Removes a portal containing action buttons from the datepicker.
     * @param portal Portal to be removed.
     */
    removeActions(portal: TemplatePortal): void;
    /** Open the calendar. */
    open(): void;
    /** Close the calendar. */
    close(): void;
    /** Applies the current pending selection on the overlay to the model. */
    _applyPendingSelection(): void;
    /** Forwards relevant values from the datepicker to the datepicker content inside the overlay. */
    protected _forwardContentValues(instance: MatDatepickerContent<S, D>): void;
    /** Opens the overlay with the calendar. */
    private _openOverlay;
    /** Destroys the current overlay. */
    private _destroyOverlay;
    /** Gets a position strategy that will open the calendar as a dropdown. */
    private _getDialogStrategy;
    /** Gets a position strategy that will open the calendar as a dropdown. */
    private _getDropdownStrategy;
    /** Sets the positions of the datepicker in dropdown mode based on the current configuration. */
    private _setConnectedPositions;
    /** Gets an observable that will emit when the overlay is supposed to be closed. */
    private _getCloseStream;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDatepickerBase<any, any, any>, [null, null, null, null, { optional: true; }, { optional: true; }, null]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatDatepickerBase<any, any, any>, never, never, { "calendarHeaderComponent": "calendarHeaderComponent"; "startAt": "startAt"; "startView": "startView"; "color": "color"; "touchUi": "touchUi"; "disabled": "disabled"; "xPosition": "xPosition"; "yPosition": "yPosition"; "restoreFocus": "restoreFocus"; "dateClass": "dateClass"; "panelClass": "panelClass"; "opened": "opened"; }, { "yearSelected": "yearSelected"; "monthSelected": "monthSelected"; "viewChanged": "viewChanged"; "openedStream": "opened"; "closedStream": "closed"; }, never, never, false, never>;
}

/** Button that will close the datepicker and discard the current selection. */
export declare class MatDatepickerCancel {
    _datepicker: MatDatepickerBase<MatDatepickerControl<any>, unknown>;
    constructor(_datepicker: MatDatepickerBase<MatDatepickerControl<any>, unknown>);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDatepickerCancel, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatDatepickerCancel, "[matDatepickerCancel], [matDateRangePickerCancel]", never, {}, {}, never, never, false, never>;
}

/**
 * Component used as the content for the datepicker overlay. We use this instead of using
 * MatCalendar directly as the content so we can control the initial focus. This also gives us a
 * place to put additional features of the overlay that are not part of the calendar itself in the
 * future. (e.g. confirmation buttons).
 * @docs-private
 */
export declare class MatDatepickerContent<S, D = ExtractDateTypeFromSelection<S>> extends _MatDatepickerContentBase implements OnInit, AfterViewInit, OnDestroy, CanColor {
    private _changeDetectorRef;
    private _globalModel;
    private _dateAdapter;
    private _rangeSelectionStrategy;
    private _subscriptions;
    private _model;
    /** Reference to the internal calendar component. */
    _calendar: MatCalendar<D>;
    /** Reference to the datepicker that created the overlay. */
    datepicker: MatDatepickerBase<any, S, D>;
    /** Start of the comparison range. */
    comparisonStart: D | null;
    /** End of the comparison range. */
    comparisonEnd: D | null;
    /** ARIA Accessible name of the `<input matStartDate/>` */
    startDateAccessibleName: string | null;
    /** ARIA Accessible name of the `<input matEndDate/>` */
    endDateAccessibleName: string | null;
    /** Whether the datepicker is above or below the input. */
    _isAbove: boolean;
    /** Current state of the animation. */
    _animationState: 'enter-dropdown' | 'enter-dialog' | 'void';
    /** Emits when an animation has finished. */
    readonly _animationDone: Subject<void>;
    /** Whether there is an in-progress animation. */
    _isAnimating: boolean;
    /** Text for the close button. */
    _closeButtonText: string;
    /** Whether the close button currently has focus. */
    _closeButtonFocused: boolean;
    /** Portal with projected action buttons. */
    _actionsPortal: TemplatePortal | null;
    /** Id of the label for the `role="dialog"` element. */
    _dialogLabelId: string | null;
    constructor(elementRef: ElementRef, _changeDetectorRef: ChangeDetectorRef, _globalModel: MatDateSelectionModel<S, D>, _dateAdapter: DateAdapter<D>, _rangeSelectionStrategy: MatDateRangeSelectionStrategy<D>, intl: MatDatepickerIntl);
    ngOnInit(): void;
    ngAfterViewInit(): void;
    ngOnDestroy(): void;
    _handleUserSelection(event: MatCalendarUserEvent<D | null>): void;
    _handleUserDragDrop(event: MatCalendarUserEvent<DateRange<D>>): void;
    _startExitAnimation(): void;
    _handleAnimationEvent(event: AnimationEvent_2): void;
    _getSelected(): D | DateRange<D> | null;
    /** Applies the current pending selection to the global model. */
    _applyPendingSelection(): void;
    /**
     * Assigns a new portal containing the datepicker actions.
     * @param portal Portal with the actions to be assigned.
     * @param forceRerender Whether a re-render of the portal should be triggered. This isn't
     * necessary if the portal is assigned during initialization, but it may be required if it's
     * added at a later point.
     */
    _assignActions(portal: TemplatePortal<any> | null, forceRerender: boolean): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDatepickerContent<any, any>, [null, null, null, null, { optional: true; }, null]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatDatepickerContent<any, any>, "mat-datepicker-content", ["matDatepickerContent"], { "color": "color"; }, {}, never, never, false, never>;
}

/** @docs-private */
declare const _MatDatepickerContentBase: _Constructor<CanColor> & _AbstractConstructor<CanColor> & {
    new (_elementRef: ElementRef): {
        _elementRef: ElementRef;
    };
};

/** Form control that can be associated with a datepicker. */
export declare interface MatDatepickerControl<D> {
    getStartValue(): D | null;
    getThemePalette(): ThemePalette;
    min: D | null;
    max: D | null;
    disabled: boolean;
    dateFilter: DateFilterFn<D>;
    getConnectedOverlayOrigin(): ElementRef;
    getOverlayLabelId(): string | null;
    stateChanges: Observable<void>;
}

/** Directive used to connect an input to a MatDatepicker. */
export declare class MatDatepickerInput<D> extends MatDatepickerInputBase<D | null, D> implements MatDatepickerControl<D | null>, OnDestroy {
    private _formField?;
    private _closedSubscription;
    /** The datepicker that this input is associated with. */
    set matDatepicker(datepicker: MatDatepickerPanel<MatDatepickerControl<D>, D | null, D>);
    _datepicker: MatDatepickerPanel<MatDatepickerControl<D>, D | null, D>;
    /** The minimum valid date. */
    get min(): D | null;
    set min(value: D | null);
    private _min;
    /** The maximum valid date. */
    get max(): D | null;
    set max(value: D | null);
    private _max;
    /** Function that can be used to filter out dates within the datepicker. */
    get dateFilter(): DateFilterFn<D | null>;
    set dateFilter(value: DateFilterFn<D | null>);
    private _dateFilter;
    /** The combined form control validator for this input. */
    protected _validator: ValidatorFn | null;
    constructor(elementRef: ElementRef<HTMLInputElement>, dateAdapter: DateAdapter<D>, dateFormats: MatDateFormats, _formField?: _MatFormFieldPartial | undefined);
    /**
     * Gets the element that the datepicker popup should be connected to.
     * @return The element to connect the popup to.
     */
    getConnectedOverlayOrigin(): ElementRef;
    /** Gets the ID of an element that should be used a description for the calendar overlay. */
    getOverlayLabelId(): string | null;
    /** Returns the palette used by the input's form field, if any. */
    getThemePalette(): ThemePalette;
    /** Gets the value at which the calendar should start. */
    getStartValue(): D | null;
    ngOnDestroy(): void;
    /** Opens the associated datepicker. */
    protected _openPopup(): void;
    protected _getValueFromModel(modelValue: D | null): D | null;
    protected _assignValueToModel(value: D | null): void;
    /** Gets the input's minimum date. */
    _getMinDate(): D | null;
    /** Gets the input's maximum date. */
    _getMaxDate(): D | null;
    /** Gets the input's date filtering function. */
    protected _getDateFilter(): DateFilterFn<D | null>;
    protected _shouldHandleChangeEvent(event: DateSelectionModelChange<D>): boolean;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDatepickerInput<any>, [null, { optional: true; }, { optional: true; }, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatDatepickerInput<any>, "input[matDatepicker]", ["matDatepickerInput"], { "matDatepicker": "matDatepicker"; "min": "min"; "max": "max"; "dateFilter": "matDatepickerFilter"; }, {}, never, never, false, never>;
}

/** Base class for datepicker inputs. */
declare abstract class MatDatepickerInputBase<S, D = ExtractDateTypeFromSelection<S>> implements ControlValueAccessor, AfterViewInit, OnChanges, OnDestroy, Validator {
    protected _elementRef: ElementRef<HTMLInputElement>;
    _dateAdapter: DateAdapter<D>;
    private _dateFormats;
    /** Whether the component has been initialized. */
    private _isInitialized;
    /** The value of the input. */
    get value(): D | null;
    set value(value: any);
    protected _model: MatDateSelectionModel<S, D> | undefined;
    /** Whether the datepicker-input is disabled. */
    get disabled(): boolean;
    set disabled(value: BooleanInput);
    private _disabled;
    /** Emits when a `change` event is fired on this `<input>`. */
    readonly dateChange: EventEmitter<MatDatepickerInputEvent<D, S>>;
    /** Emits when an `input` event is fired on this `<input>`. */
    readonly dateInput: EventEmitter<MatDatepickerInputEvent<D, S>>;
    /** Emits when the internal state has changed */
    readonly stateChanges: Subject<void>;
    _onTouched: () => void;
    _validatorOnChange: () => void;
    private _cvaOnChange;
    private _valueChangesSubscription;
    private _localeSubscription;
    /**
     * Since the value is kept on the model which is assigned in an Input,
     * we might get a value before we have a model. This property keeps track
     * of the value until we have somewhere to assign it.
     */
    private _pendingValue;
    /** The form control validator for whether the input parses. */
    private _parseValidator;
    /** The form control validator for the date filter. */
    private _filterValidator;
    /** The form control validator for the min date. */
    private _minValidator;
    /** The form control validator for the max date. */
    private _maxValidator;
    /** Gets the base validator functions. */
    protected _getValidators(): ValidatorFn[];
    /** Gets the minimum date for the input. Used for validation. */
    abstract _getMinDate(): D | null;
    /** Gets the maximum date for the input. Used for validation. */
    abstract _getMaxDate(): D | null;
    /** Gets the date filter function. Used for validation. */
    protected abstract _getDateFilter(): DateFilterFn<D> | undefined;
    /** Registers a date selection model with the input. */
    _registerModel(model: MatDateSelectionModel<S, D>): void;
    /** Opens the popup associated with the input. */
    protected abstract _openPopup(): void;
    /** Assigns a value to the input's model. */
    protected abstract _assignValueToModel(model: D | null): void;
    /** Converts a value from the model into a native value for the input. */
    protected abstract _getValueFromModel(modelValue: S): D | null;
    /** Combined form control validator for this input. */
    protected abstract _validator: ValidatorFn | null;
    /** Predicate that determines whether the input should handle a particular change event. */
    protected abstract _shouldHandleChangeEvent(event: DateSelectionModelChange<S>): boolean;
    /** Whether the last value set on the input was valid. */
    protected _lastValueValid: boolean;
    constructor(_elementRef: ElementRef<HTMLInputElement>, _dateAdapter: DateAdapter<D>, _dateFormats: MatDateFormats);
    ngAfterViewInit(): void;
    ngOnChanges(changes: SimpleChanges): void;
    ngOnDestroy(): void;
    /** @docs-private */
    registerOnValidatorChange(fn: () => void): void;
    /** @docs-private */
    validate(c: AbstractControl): ValidationErrors | null;
    writeValue(value: D): void;
    registerOnChange(fn: (value: any) => void): void;
    registerOnTouched(fn: () => void): void;
    setDisabledState(isDisabled: boolean): void;
    _onKeydown(event: KeyboardEvent): void;
    _onInput(value: string): void;
    _onChange(): void;
    /** Handles blur events on the input. */
    _onBlur(): void;
    /** Formats a value and sets it on the input element. */
    protected _formatValue(value: D | null): void;
    /** Assigns a value to the model. */
    private _assignValue;
    /** Whether a value is considered valid. */
    private _isValidValue;
    /**
     * Checks whether a parent control is disabled. This is in place so that it can be overridden
     * by inputs extending this one which can be placed inside of a group that can be disabled.
     */
    protected _parentDisabled(): boolean;
    /** Programmatically assigns a value to the input. */
    protected _assignValueProgrammatically(value: D | null): void;
    /** Gets whether a value matches the current date filter. */
    _matchesFilter(value: D | null): boolean;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDatepickerInputBase<any, any>, [null, { optional: true; }, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatDatepickerInputBase<any, any>, never, never, { "value": "value"; "disabled": "disabled"; }, { "dateChange": "dateChange"; "dateInput": "dateInput"; }, never, never, false, never>;
}

/**
 * An event used for datepicker input and change events. We don't always have access to a native
 * input or change event because the event may have been triggered by the user clicking on the
 * calendar popup. For consistency, we always use MatDatepickerInputEvent instead.
 */
export declare class MatDatepickerInputEvent<D, S = unknown> {
    /** Reference to the datepicker input component that emitted the event. */
    target: MatDatepickerInputBase<S, D>;
    /** Reference to the native input element associated with the datepicker input. */
    targetElement: HTMLElement;
    /** The new value for the target datepicker input. */
    value: D | null;
    constructor(
    /** Reference to the datepicker input component that emitted the event. */
    target: MatDatepickerInputBase<S, D>, 
    /** Reference to the native input element associated with the datepicker input. */
    targetElement: HTMLElement);
}

/** Datepicker data that requires internationalization. */
export declare class MatDatepickerIntl {
    /**
     * Stream that emits whenever the labels here are changed. Use this to notify
     * components if the labels have changed after initialization.
     */
    readonly changes: Subject<void>;
    /** A label for the calendar popup (used by screen readers). */
    calendarLabel: string;
    /** A label for the button used to open the calendar popup (used by screen readers). */
    openCalendarLabel: string;
    /** Label for the button used to close the calendar popup. */
    closeCalendarLabel: string;
    /** A label for the previous month button (used by screen readers). */
    prevMonthLabel: string;
    /** A label for the next month button (used by screen readers). */
    nextMonthLabel: string;
    /** A label for the previous year button (used by screen readers). */
    prevYearLabel: string;
    /** A label for the next year button (used by screen readers). */
    nextYearLabel: string;
    /** A label for the previous multi-year button (used by screen readers). */
    prevMultiYearLabel: string;
    /** A label for the next multi-year button (used by screen readers). */
    nextMultiYearLabel: string;
    /** A label for the 'switch to month view' button (used by screen readers). */
    switchToMonthViewLabel: string;
    /** A label for the 'switch to year view' button (used by screen readers). */
    switchToMultiYearViewLabel: string;
    /**
     * A label for the first date of a range of dates (used by screen readers).
     * @deprecated Provide your own internationalization string.
     * @breaking-change 17.0.0
     */
    startDateLabel: string;
    /**
     * A label for the last date of a range of dates (used by screen readers).
     * @deprecated Provide your own internationalization string.
     * @breaking-change 17.0.0
     */
    endDateLabel: string;
    /** Formats a range of years (used for visuals). */
    formatYearRange(start: string, end: string): string;
    /** Formats a label for a range of years (used by screen readers). */
    formatYearRangeLabel(start: string, end: string): string;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDatepickerIntl, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<MatDatepickerIntl>;
}

export declare class MatDatepickerModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDatepickerModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatDatepickerModule, [typeof i1.MatCalendar, typeof i2.MatCalendarBody, typeof i3.MatDatepicker, typeof i4.MatDatepickerContent, typeof i5.MatDatepickerInput, typeof i6.MatDatepickerToggle, typeof i6.MatDatepickerToggleIcon, typeof i7.MatMonthView, typeof i8.MatYearView, typeof i9.MatMultiYearView, typeof i1.MatCalendarHeader, typeof i10.MatDateRangeInput, typeof i11.MatStartDate, typeof i11.MatEndDate, typeof i12.MatDateRangePicker, typeof i13.MatDatepickerActions, typeof i13.MatDatepickerCancel, typeof i13.MatDatepickerApply], [typeof i14.CommonModule, typeof i15.MatButtonModule, typeof i16.OverlayModule, typeof i17.A11yModule, typeof i18.PortalModule, typeof i19.MatCommonModule], [typeof i20.CdkScrollableModule, typeof i1.MatCalendar, typeof i2.MatCalendarBody, typeof i3.MatDatepicker, typeof i4.MatDatepickerContent, typeof i5.MatDatepickerInput, typeof i6.MatDatepickerToggle, typeof i6.MatDatepickerToggleIcon, typeof i7.MatMonthView, typeof i8.MatYearView, typeof i9.MatMultiYearView, typeof i1.MatCalendarHeader, typeof i10.MatDateRangeInput, typeof i11.MatStartDate, typeof i11.MatEndDate, typeof i12.MatDateRangePicker, typeof i13.MatDatepickerActions, typeof i13.MatDatepickerCancel, typeof i13.MatDatepickerApply]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatDatepickerModule>;
}

/** A datepicker that can be attached to a {@link MatDatepickerControl}. */
export declare interface MatDatepickerPanel<C extends MatDatepickerControl<D>, S, D = ExtractDateTypeFromSelection<S>> {
    /** Stream that emits whenever the date picker is closed. */
    closedStream: EventEmitter<void>;
    /** Color palette to use on the datepicker's calendar. */
    color: ThemePalette;
    /** The input element the datepicker is associated with. */
    datepickerInput: C;
    /** Whether the datepicker pop-up should be disabled. */
    disabled: boolean;
    /** The id for the datepicker's calendar. */
    id: string;
    /** Whether the datepicker is open. */
    opened: boolean;
    /** Stream that emits whenever the date picker is opened. */
    openedStream: EventEmitter<void>;
    /** Emits when the datepicker's state changes. */
    stateChanges: Subject<void>;
    /** Opens the datepicker. */
    open(): void;
    /** Register an input with the datepicker. */
    registerInput(input: C): MatDateSelectionModel<S, D>;
}

export declare class MatDatepickerToggle<D> implements AfterContentInit, OnChanges, OnDestroy {
    _intl: MatDatepickerIntl;
    private _changeDetectorRef;
    private _stateChanges;
    /** Datepicker instance that the button will toggle. */
    datepicker: MatDatepickerPanel<MatDatepickerControl<any>, D>;
    /** Tabindex for the toggle. */
    tabIndex: number | null;
    /** Screen-reader label for the button. */
    ariaLabel: string;
    /** Whether the toggle button is disabled. */
    get disabled(): boolean;
    set disabled(value: BooleanInput);
    private _disabled;
    /** Whether ripples on the toggle should be disabled. */
    disableRipple: boolean;
    /** Custom icon set by the consumer. */
    _customIcon: MatDatepickerToggleIcon;
    /** Underlying button element. */
    _button: MatButton;
    constructor(_intl: MatDatepickerIntl, _changeDetectorRef: ChangeDetectorRef, defaultTabIndex: string);
    ngOnChanges(changes: SimpleChanges): void;
    ngOnDestroy(): void;
    ngAfterContentInit(): void;
    _open(event: Event): void;
    private _watchStateChanges;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDatepickerToggle<any>, [null, null, { attribute: "tabindex"; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatDatepickerToggle<any>, "mat-datepicker-toggle", ["matDatepickerToggle"], { "datepicker": "for"; "tabIndex": "tabIndex"; "ariaLabel": "aria-label"; "disabled": "disabled"; "disableRipple": "disableRipple"; }, {}, ["_customIcon"], ["[matDatepickerToggleIcon]"], false, never>;
}

/** Can be used to override the icon of a `matDatepickerToggle`. */
export declare class MatDatepickerToggleIcon {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDatepickerToggleIcon, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatDatepickerToggleIcon, "[matDatepickerToggleIcon]", never, {}, {}, never, never, false, never>;
}

export declare class MatDateRangeInput<D> implements MatFormFieldControl<DateRange<D>>, MatDatepickerControl<D>, MatDateRangeInputParent<D>, MatDateRangePickerInput<D>, AfterContentInit, OnChanges, OnDestroy {
    private _changeDetectorRef;
    private _elementRef;
    private _dateAdapter;
    private _formField?;
    private _closedSubscription;
    /** Current value of the range input. */
    get value(): DateRange<D> | null;
    /** Unique ID for the group. */
    id: string;
    /** Whether the control is focused. */
    focused: boolean;
    /** Whether the control's label should float. */
    get shouldLabelFloat(): boolean;
    /** Name of the form control. */
    controlType: string;
    /**
     * Implemented as a part of `MatFormFieldControl`.
     * Set the placeholder attribute on `matStartDate` and `matEndDate`.
     * @docs-private
     */
    get placeholder(): string;
    /** The range picker that this input is associated with. */
    get rangePicker(): MatDatepickerPanel<MatDatepickerControl<D>, DateRange<D>, D>;
    set rangePicker(rangePicker: MatDatepickerPanel<MatDatepickerControl<D>, DateRange<D>, D>);
    private _rangePicker;
    /** Whether the input is required. */
    get required(): boolean;
    set required(value: BooleanInput);
    private _required;
    /** Function that can be used to filter out dates within the date range picker. */
    get dateFilter(): DateFilterFn<D>;
    set dateFilter(value: DateFilterFn<D>);
    private _dateFilter;
    /** The minimum valid date. */
    get min(): D | null;
    set min(value: D | null);
    private _min;
    /** The maximum valid date. */
    get max(): D | null;
    set max(value: D | null);
    private _max;
    /** Whether the input is disabled. */
    get disabled(): boolean;
    set disabled(value: BooleanInput);
    _groupDisabled: boolean;
    /** Whether the input is in an error state. */
    get errorState(): boolean;
    /** Whether the datepicker input is empty. */
    get empty(): boolean;
    /** Value for the `aria-describedby` attribute of the inputs. */
    _ariaDescribedBy: string | null;
    /** Date selection model currently registered with the input. */
    private _model;
    /** Separator text to be shown between the inputs. */
    separator: string;
    /** Start of the comparison range that should be shown in the calendar. */
    comparisonStart: D | null;
    /** End of the comparison range that should be shown in the calendar. */
    comparisonEnd: D | null;
    _startInput: MatStartDate<D>;
    _endInput: MatEndDate<D>;
    /**
     * Implemented as a part of `MatFormFieldControl`.
     * TODO(crisbeto): change type to `AbstractControlDirective` after #18206 lands.
     * @docs-private
     */
    ngControl: NgControl | null;
    /** Emits when the input's state has changed. */
    readonly stateChanges: Subject<void>;
    constructor(_changeDetectorRef: ChangeDetectorRef, _elementRef: ElementRef<HTMLElement>, control: ControlContainer, _dateAdapter: DateAdapter<D>, _formField?: _MatFormFieldPartial | undefined);
    /**
     * Implemented as a part of `MatFormFieldControl`.
     * @docs-private
     */
    setDescribedByIds(ids: string[]): void;
    /**
     * Implemented as a part of `MatFormFieldControl`.
     * @docs-private
     */
    onContainerClick(): void;
    ngAfterContentInit(): void;
    ngOnChanges(changes: SimpleChanges): void;
    ngOnDestroy(): void;
    /** Gets the date at which the calendar should start. */
    getStartValue(): D | null;
    /** Gets the input's theme palette. */
    getThemePalette(): ThemePalette;
    /** Gets the element to which the calendar overlay should be attached. */
    getConnectedOverlayOrigin(): ElementRef;
    /** Gets the ID of an element that should be used a description for the calendar overlay. */
    getOverlayLabelId(): string | null;
    /** Gets the value that is used to mirror the state input. */
    _getInputMirrorValue(part: 'start' | 'end'): string;
    /** Whether the input placeholders should be hidden. */
    _shouldHidePlaceholders(): boolean;
    /** Handles the value in one of the child inputs changing. */
    _handleChildValueChange(): void;
    /** Opens the date range picker associated with the input. */
    _openDatepicker(): void;
    /** Whether the separate text should be hidden. */
    _shouldHideSeparator(): boolean | "" | null;
    /** Gets the value for the `aria-labelledby` attribute of the inputs. */
    _getAriaLabelledby(): string | null;
    _getStartDateAccessibleName(): string;
    _getEndDateAccessibleName(): string;
    /** Updates the focused state of the range input. */
    _updateFocus(origin: FocusOrigin): void;
    /** Re-runs the validators on the start/end inputs. */
    private _revalidate;
    /** Registers the current date selection model with the start/end inputs. */
    private _registerModel;
    /** Checks whether a specific range input directive is required. */
    private _isTargetRequired;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDateRangeInput<any>, [null, null, { optional: true; self: true; }, { optional: true; }, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatDateRangeInput<any>, "mat-date-range-input", ["matDateRangeInput"], { "rangePicker": "rangePicker"; "required": "required"; "dateFilter": "dateFilter"; "min": "min"; "max": "max"; "disabled": "disabled"; "separator": "separator"; "comparisonStart": "comparisonStart"; "comparisonEnd": "comparisonEnd"; }, {}, ["_startInput", "_endInput"], ["input[matStartDate]", "input[matEndDate]"], false, never>;
}

declare const _MatDateRangeInputBase: _Constructor<CanUpdateErrorState> & _AbstractConstructor<CanUpdateErrorState> & typeof MatDateRangeInputPartBase;

/** Parent component that should be wrapped around `MatStartDate` and `MatEndDate`. */
declare interface MatDateRangeInputParent<D> {
    id: string;
    min: D | null;
    max: D | null;
    dateFilter: DateFilterFn<D>;
    rangePicker: {
        opened: boolean;
        id: string;
    };
    _startInput: MatDateRangeInputPartBase<D>;
    _endInput: MatDateRangeInputPartBase<D>;
    _groupDisabled: boolean;
    _handleChildValueChange(): void;
    _openDatepicker(): void;
}

/**
 * Base class for the individual inputs that can be projected inside a `mat-date-range-input`.
 */
declare abstract class MatDateRangeInputPartBase<D> extends MatDatepickerInputBase<DateRange<D>> implements OnInit, DoCheck {
    _rangeInput: MatDateRangeInputParent<D>;
    _elementRef: ElementRef<HTMLInputElement>;
    _defaultErrorStateMatcher: ErrorStateMatcher;
    private _injector;
    _parentForm: NgForm;
    _parentFormGroup: FormGroupDirective;
    /**
     * Form control bound to this input part.
     * @docs-private
     */
    ngControl: NgControl;
    /** @docs-private */
    abstract updateErrorState(): void;
    protected abstract _validator: ValidatorFn | null;
    protected abstract _assignValueToModel(value: D | null): void;
    protected abstract _getValueFromModel(modelValue: DateRange<D>): D | null;
    protected readonly _dir: Directionality | null;
    constructor(_rangeInput: MatDateRangeInputParent<D>, _elementRef: ElementRef<HTMLInputElement>, _defaultErrorStateMatcher: ErrorStateMatcher, _injector: Injector, _parentForm: NgForm, _parentFormGroup: FormGroupDirective, dateAdapter: DateAdapter<D>, dateFormats: MatDateFormats);
    ngOnInit(): void;
    ngDoCheck(): void;
    /** Gets whether the input is empty. */
    isEmpty(): boolean;
    /** Gets the placeholder of the input. */
    _getPlaceholder(): string;
    /** Focuses the input. */
    focus(): void;
    /** Gets the value that should be used when mirroring the input's size. */
    getMirrorValue(): string;
    /** Handles `input` events on the input element. */
    _onInput(value: string): void;
    /** Opens the datepicker associated with the input. */
    protected _openPopup(): void;
    /** Gets the minimum date from the range input. */
    _getMinDate(): D | null;
    /** Gets the maximum date from the range input. */
    _getMaxDate(): D | null;
    /** Gets the date filter function from the range input. */
    protected _getDateFilter(): DateFilterFn<D>;
    protected _parentDisabled(): boolean;
    protected _shouldHandleChangeEvent({ source }: DateSelectionModelChange<DateRange<D>>): boolean;
    protected _assignValueProgrammatically(value: D | null): void;
    /** return the ARIA accessible name of the input element */
    _getAccessibleName(): string;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDateRangeInputPartBase<any>, [null, null, null, null, { optional: true; }, { optional: true; }, { optional: true; }, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatDateRangeInputPartBase<any>, never, never, {}, {}, never, never, false, never>;
}

/** Component responsible for managing the date range picker popup/dialog. */
export declare class MatDateRangePicker<D> extends MatDatepickerBase<MatDateRangePickerInput<D>, DateRange<D>, D> {
    protected _forwardContentValues(instance: MatDatepickerContent<DateRange<D>, D>): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDateRangePicker<any>, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatDateRangePicker<any>, "mat-date-range-picker", ["matDateRangePicker"], {}, {}, never, never, false, never>;
}

/**
 * Input that can be associated with a date range picker.
 * @docs-private
 */
declare interface MatDateRangePickerInput<D> extends MatDatepickerControl<D> {
    _getEndDateAccessibleName(): string | null;
    _getStartDateAccessibleName(): string | null;
    comparisonStart: D | null;
    comparisonEnd: D | null;
}

/** Object that can be provided in order to customize the date range selection behavior. */
export declare interface MatDateRangeSelectionStrategy<D> {
    /**
     * Called when the user has finished selecting a value.
     * @param date Date that was selected. Will be null if the user cleared the selection.
     * @param currentRange Range that is currently show in the calendar.
     * @param event DOM event that triggered the selection. Currently only corresponds to a `click`
     *    event, but it may get expanded in the future.
     */
    selectionFinished(date: D | null, currentRange: DateRange<D>, event: Event): DateRange<D>;
    /**
     * Called when the user has activated a new date (e.g. by hovering over
     * it or moving focus) and the calendar tries to display a date range.
     *
     * @param activeDate Date that the user has activated. Will be null if the user moved
     *    focus to an element that's no a calendar cell.
     * @param currentRange Range that is currently shown in the calendar.
     * @param event DOM event that caused the preview to be changed. Will be either a
     *    `mouseenter`/`mouseleave` or `focus`/`blur` depending on how the user is navigating.
     */
    createPreview(activeDate: D | null, currentRange: DateRange<D>, event: Event): DateRange<D>;
    /**
     * Called when the user has dragged a date in the currently selected range to another
     * date. Returns the date updated range that should result from this interaction.
     *
     * @param dateOrigin The date the user started dragging from.
     * @param originalRange The originally selected date range.
     * @param newDate The currently targeted date in the drag operation.
     * @param event DOM event that triggered the updated drag state. Will be
     *     `mouseenter`/`mouseup` or `touchmove`/`touchend` depending on the device type.
     */
    createDrag?(dragOrigin: D, originalRange: DateRange<D>, newDate: D, event: Event): DateRange<D> | null;
}

/**
 * A selection model containing a date selection.
 * @docs-private
 */
export declare abstract class MatDateSelectionModel<S, D = ExtractDateTypeFromSelection<S>> implements OnDestroy {
    /** The current selection. */
    readonly selection: S;
    protected _adapter: DateAdapter<D>;
    private readonly _selectionChanged;
    /** Emits when the selection has changed. */
    selectionChanged: Observable<DateSelectionModelChange<S>>;
    protected constructor(
    /** The current selection. */
    selection: S, _adapter: DateAdapter<D>);
    /**
     * Updates the current selection in the model.
     * @param value New selection that should be assigned.
     * @param source Object that triggered the selection change.
     */
    updateSelection(value: S, source: unknown): void;
    ngOnDestroy(): void;
    protected _isValidDateInstance(date: D): boolean;
    /** Adds a date to the current selection. */
    abstract add(date: D | null): void;
    /** Checks whether the current selection is valid. */
    abstract isValid(): boolean;
    /** Checks whether the current selection is complete. */
    abstract isComplete(): boolean;
    /** Clones the selection model. */
    abstract clone(): MatDateSelectionModel<S, D>;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatDateSelectionModel<any, any>, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<MatDateSelectionModel<any, any>>;
}

/** Input for entering the end date in a `mat-date-range-input`. */
export declare class MatEndDate<D> extends _MatDateRangeInputBase<D> implements CanUpdateErrorState {
    /** Validator that checks that the end date isn't before the start date. */
    private _endValidator;
    constructor(rangeInput: MatDateRangeInputParent<D>, elementRef: ElementRef<HTMLInputElement>, defaultErrorStateMatcher: ErrorStateMatcher, injector: Injector, parentForm: NgForm, parentFormGroup: FormGroupDirective, dateAdapter: DateAdapter<D>, dateFormats: MatDateFormats);
    protected _validator: ValidatorFn | null;
    protected _getValueFromModel(modelValue: DateRange<D>): D | null;
    protected _shouldHandleChangeEvent(change: DateSelectionModelChange<DateRange<D>>): boolean;
    protected _assignValueToModel(value: D | null): void;
    _onKeydown(event: KeyboardEvent): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatEndDate<any>, [null, null, null, null, { optional: true; }, { optional: true; }, { optional: true; }, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatEndDate<any>, "input[matEndDate]", never, { "errorStateMatcher": "errorStateMatcher"; }, { "dateChange": "dateChange"; "dateInput": "dateInput"; }, never, never, false, never>;
}

/**
 * Partial representation of `MatFormField` that is used for backwards-compatibility
 * between the legacy and non-legacy variants.
 */
declare interface _MatFormFieldPartial {
    getConnectedOverlayOrigin(): ElementRef;
    getLabelId(): string | null;
    color: ThemePalette;
    _elementRef: ElementRef;
    _shouldLabelFloat(): boolean;
    _hasFloatingLabel(): boolean;
    _labelId: string;
}

/**
 * An internal component used to display a single month in the datepicker.
 * @docs-private
 */
export declare class MatMonthView<D> implements AfterContentInit, OnChanges, OnDestroy {
    readonly _changeDetectorRef: ChangeDetectorRef;
    private _dateFormats;
    _dateAdapter: DateAdapter<D>;
    private _dir?;
    private _rangeStrategy?;
    private _rerenderSubscription;
    /** Flag used to filter out space/enter keyup events that originated outside of the view. */
    private _selectionKeyPressed;
    /**
     * The date to display in this month view (everything other than the month and year is ignored).
     */
    get activeDate(): D;
    set activeDate(value: D);
    private _activeDate;
    /** The currently selected date. */
    get selected(): DateRange<D> | D | null;
    set selected(value: DateRange<D> | D | null);
    private _selected;
    /** The minimum selectable date. */
    get minDate(): D | null;
    set minDate(value: D | null);
    private _minDate;
    /** The maximum selectable date. */
    get maxDate(): D | null;
    set maxDate(value: D | null);
    private _maxDate;
    /** Function used to filter which dates are selectable. */
    dateFilter: (date: D) => boolean;
    /** Function that can be used to add custom CSS classes to dates. */
    dateClass: MatCalendarCellClassFunction<D>;
    /** Start of the comparison range. */
    comparisonStart: D | null;
    /** End of the comparison range. */
    comparisonEnd: D | null;
    /** ARIA Accessible name of the `<input matStartDate/>` */
    startDateAccessibleName: string | null;
    /** ARIA Accessible name of the `<input matEndDate/>` */
    endDateAccessibleName: string | null;
    /** Origin of active drag, or null when dragging is not active. */
    activeDrag: MatCalendarUserEvent<D> | null;
    /** Emits when a new date is selected. */
    readonly selectedChange: EventEmitter<D | null>;
    /** Emits when any date is selected. */
    readonly _userSelection: EventEmitter<MatCalendarUserEvent<D | null>>;
    /** Emits when the user initiates a date range drag via mouse or touch. */
    readonly dragStarted: EventEmitter<MatCalendarUserEvent<D>>;
    /**
     * Emits when the user completes or cancels a date range drag.
     * Emits null when the drag was canceled or the newly selected date range if completed.
     */
    readonly dragEnded: EventEmitter<MatCalendarUserEvent<DateRange<D> | null>>;
    /** Emits when any date is activated. */
    readonly activeDateChange: EventEmitter<D>;
    /** The body of calendar table */
    _matCalendarBody: MatCalendarBody;
    /** The label for this month (e.g. "January 2017"). */
    _monthLabel: string;
    /** Grid of calendar cells representing the dates of the month. */
    _weeks: MatCalendarCell[][];
    /** The number of blank cells in the first row before the 1st of the month. */
    _firstWeekOffset: number;
    /** Start value of the currently-shown date range. */
    _rangeStart: number | null;
    /** End value of the currently-shown date range. */
    _rangeEnd: number | null;
    /** Start value of the currently-shown comparison date range. */
    _comparisonRangeStart: number | null;
    /** End value of the currently-shown comparison date range. */
    _comparisonRangeEnd: number | null;
    /** Start of the preview range. */
    _previewStart: number | null;
    /** End of the preview range. */
    _previewEnd: number | null;
    /** Whether the user is currently selecting a range of dates. */
    _isRange: boolean;
    /** The date of the month that today falls on. Null if today is in another month. */
    _todayDate: number | null;
    /** The names of the weekdays. */
    _weekdays: {
        long: string;
        narrow: string;
    }[];
    constructor(_changeDetectorRef: ChangeDetectorRef, _dateFormats: MatDateFormats, _dateAdapter: DateAdapter<D>, _dir?: Directionality | undefined, _rangeStrategy?: MatDateRangeSelectionStrategy<D> | undefined);
    ngAfterContentInit(): void;
    ngOnChanges(changes: SimpleChanges): void;
    ngOnDestroy(): void;
    /** Handles when a new date is selected. */
    _dateSelected(event: MatCalendarUserEvent<number>): void;
    /**
     * Takes the index of a calendar body cell wrapped in in an event as argument. For the date that
     * corresponds to the given cell, set `activeDate` to that date and fire `activeDateChange` with
     * that date.
     *
     * This function is used to match each component's model of the active date with the calendar
     * body cell that was focused. It updates its value of `activeDate` synchronously and updates the
     * parent's value asynchronously via the `activeDateChange` event. The child component receives an
     * updated value asynchronously via the `activeCell` Input.
     */
    _updateActiveDate(event: MatCalendarUserEvent<number>): void;
    /** Handles keydown events on the calendar body when calendar is in month view. */
    _handleCalendarBodyKeydown(event: KeyboardEvent): void;
    /** Handles keyup events on the calendar body when calendar is in month view. */
    _handleCalendarBodyKeyup(event: KeyboardEvent): void;
    /** Initializes this month view. */
    _init(): void;
    /** Focuses the active cell after the microtask queue is empty. */
    _focusActiveCell(movePreview?: boolean): void;
    /** Focuses the active cell after change detection has run and the microtask queue is empty. */
    _focusActiveCellAfterViewChecked(): void;
    /** Called when the user has activated a new cell and the preview needs to be updated. */
    _previewChanged({ event, value: cell }: MatCalendarUserEvent<MatCalendarCell<D> | null>): void;
    /**
     * Called when the user has ended a drag. If the drag/drop was successful,
     * computes and emits the new range selection.
     */
    protected _dragEnded(event: MatCalendarUserEvent<D | null>): void;
    /**
     * Takes a day of the month and returns a new date in the same month and year as the currently
     *  active date. The returned date will have the same day of the month as the argument date.
     */
    private _getDateFromDayOfMonth;
    /** Initializes the weekdays. */
    private _initWeekdays;
    /** Creates MatCalendarCells for the dates in this month. */
    private _createWeekCells;
    /** Date filter for the month */
    private _shouldEnableDate;
    /**
     * Gets the date in this month that the given Date falls on.
     * Returns null if the given Date is in another month.
     */
    private _getDateInCurrentMonth;
    /** Checks whether the 2 dates are non-null and fall within the same month of the same year. */
    private _hasSameMonthAndYear;
    /** Gets the value that will be used to one cell to another. */
    private _getCellCompareValue;
    /** Determines whether the user has the RTL layout direction. */
    private _isRtl;
    /** Sets the current range based on a model value. */
    private _setRanges;
    /** Gets whether a date can be selected in the month view. */
    private _canSelect;
    /** Clears out preview state. */
    private _clearPreview;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatMonthView<any>, [null, { optional: true; }, { optional: true; }, { optional: true; }, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatMonthView<any>, "mat-month-view", ["matMonthView"], { "activeDate": "activeDate"; "selected": "selected"; "minDate": "minDate"; "maxDate": "maxDate"; "dateFilter": "dateFilter"; "dateClass": "dateClass"; "comparisonStart": "comparisonStart"; "comparisonEnd": "comparisonEnd"; "startDateAccessibleName": "startDateAccessibleName"; "endDateAccessibleName": "endDateAccessibleName"; "activeDrag": "activeDrag"; }, { "selectedChange": "selectedChange"; "_userSelection": "_userSelection"; "dragStarted": "dragStarted"; "dragEnded": "dragEnded"; "activeDateChange": "activeDateChange"; }, never, never, false, never>;
}

/**
 * An internal component used to display a year selector in the datepicker.
 * @docs-private
 */
export declare class MatMultiYearView<D> implements AfterContentInit, OnDestroy {
    private _changeDetectorRef;
    _dateAdapter: DateAdapter<D>;
    private _dir?;
    private _rerenderSubscription;
    /** Flag used to filter out space/enter keyup events that originated outside of the view. */
    private _selectionKeyPressed;
    /** The date to display in this multi-year view (everything other than the year is ignored). */
    get activeDate(): D;
    set activeDate(value: D);
    private _activeDate;
    /** The currently selected date. */
    get selected(): DateRange<D> | D | null;
    set selected(value: DateRange<D> | D | null);
    private _selected;
    /** The minimum selectable date. */
    get minDate(): D | null;
    set minDate(value: D | null);
    private _minDate;
    /** The maximum selectable date. */
    get maxDate(): D | null;
    set maxDate(value: D | null);
    private _maxDate;
    /** A function used to filter which dates are selectable. */
    dateFilter: (date: D) => boolean;
    /** Function that can be used to add custom CSS classes to date cells. */
    dateClass: MatCalendarCellClassFunction<D>;
    /** Emits when a new year is selected. */
    readonly selectedChange: EventEmitter<D>;
    /** Emits the selected year. This doesn't imply a change on the selected date */
    readonly yearSelected: EventEmitter<D>;
    /** Emits when any date is activated. */
    readonly activeDateChange: EventEmitter<D>;
    /** The body of calendar table */
    _matCalendarBody: MatCalendarBody;
    /** Grid of calendar cells representing the currently displayed years. */
    _years: MatCalendarCell[][];
    /** The year that today falls on. */
    _todayYear: number;
    /** The year of the selected date. Null if the selected date is null. */
    _selectedYear: number | null;
    constructor(_changeDetectorRef: ChangeDetectorRef, _dateAdapter: DateAdapter<D>, _dir?: Directionality | undefined);
    ngAfterContentInit(): void;
    ngOnDestroy(): void;
    /** Initializes this multi-year view. */
    _init(): void;
    /** Handles when a new year is selected. */
    _yearSelected(event: MatCalendarUserEvent<number>): void;
    /**
     * Takes the index of a calendar body cell wrapped in in an event as argument. For the date that
     * corresponds to the given cell, set `activeDate` to that date and fire `activeDateChange` with
     * that date.
     *
     * This function is used to match each component's model of the active date with the calendar
     * body cell that was focused. It updates its value of `activeDate` synchronously and updates the
     * parent's value asynchronously via the `activeDateChange` event. The child component receives an
     * updated value asynchronously via the `activeCell` Input.
     */
    _updateActiveDate(event: MatCalendarUserEvent<number>): void;
    /** Handles keydown events on the calendar body when calendar is in multi-year view. */
    _handleCalendarBodyKeydown(event: KeyboardEvent): void;
    /** Handles keyup events on the calendar body when calendar is in multi-year view. */
    _handleCalendarBodyKeyup(event: KeyboardEvent): void;
    _getActiveCell(): number;
    /** Focuses the active cell after the microtask queue is empty. */
    _focusActiveCell(): void;
    /** Focuses the active cell after change detection has run and the microtask queue is empty. */
    _focusActiveCellAfterViewChecked(): void;
    /**
     * Takes a year and returns a new date on the same day and month as the currently active date
     *  The returned date will have the same year as the argument date.
     */
    private _getDateFromYear;
    /** Creates an MatCalendarCell for the given year. */
    private _createCellForYear;
    /** Whether the given year is enabled. */
    private _shouldEnableYear;
    /** Determines whether the user has the RTL layout direction. */
    private _isRtl;
    /** Sets the currently-highlighted year based on a model value. */
    private _setSelectedYear;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatMultiYearView<any>, [null, { optional: true; }, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatMultiYearView<any>, "mat-multi-year-view", ["matMultiYearView"], { "activeDate": "activeDate"; "selected": "selected"; "minDate": "minDate"; "maxDate": "maxDate"; "dateFilter": "dateFilter"; "dateClass": "dateClass"; }, { "selectedChange": "selectedChange"; "yearSelected": "yearSelected"; "activeDateChange": "activeDateChange"; }, never, never, false, never>;
}

/**
 * A selection model that contains a date range.
 * @docs-private
 */
export declare class MatRangeDateSelectionModel<D> extends MatDateSelectionModel<DateRange<D>, D> {
    constructor(adapter: DateAdapter<D>);
    /**
     * Adds a date to the current selection. In the case of a date range selection, the added date
     * fills in the next `null` value in the range. If both the start and the end already have a date,
     * the selection is reset so that the given date is the new `start` and the `end` is null.
     */
    add(date: D | null): void;
    /** Checks whether the current selection is valid. */
    isValid(): boolean;
    /**
     * Checks whether the current selection is complete. In the case of a date range selection, this
     * is true if the current selection has a non-null `start` and `end`.
     */
    isComplete(): boolean;
    /** Clones the selection model. */
    clone(): MatRangeDateSelectionModel<D>;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatRangeDateSelectionModel<any>, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<MatRangeDateSelectionModel<any>>;
}

/**
 * A selection model that contains a single date.
 * @docs-private
 */
export declare class MatSingleDateSelectionModel<D> extends MatDateSelectionModel<D | null, D> {
    constructor(adapter: DateAdapter<D>);
    /**
     * Adds a date to the current selection. In the case of a single date selection, the added date
     * simply overwrites the previous selection
     */
    add(date: D | null): void;
    /** Checks whether the current selection is valid. */
    isValid(): boolean;
    /**
     * Checks whether the current selection is complete. In the case of a single date selection, this
     * is true if the current selection is not null.
     */
    isComplete(): boolean;
    /** Clones the selection model. */
    clone(): MatSingleDateSelectionModel<D>;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatSingleDateSelectionModel<any>, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<MatSingleDateSelectionModel<any>>;
}

/** Input for entering the start date in a `mat-date-range-input`. */
export declare class MatStartDate<D> extends _MatDateRangeInputBase<D> implements CanUpdateErrorState {
    /** Validator that checks that the start date isn't after the end date. */
    private _startValidator;
    constructor(rangeInput: MatDateRangeInputParent<D>, elementRef: ElementRef<HTMLInputElement>, defaultErrorStateMatcher: ErrorStateMatcher, injector: Injector, parentForm: NgForm, parentFormGroup: FormGroupDirective, dateAdapter: DateAdapter<D>, dateFormats: MatDateFormats);
    protected _validator: ValidatorFn | null;
    protected _getValueFromModel(modelValue: DateRange<D>): D | null;
    protected _shouldHandleChangeEvent(change: DateSelectionModelChange<DateRange<D>>): boolean;
    protected _assignValueToModel(value: D | null): void;
    protected _formatValue(value: D | null): void;
    _onKeydown(event: KeyboardEvent): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatStartDate<any>, [null, null, null, null, { optional: true; }, { optional: true; }, { optional: true; }, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatStartDate<any>, "input[matStartDate]", never, { "errorStateMatcher": "errorStateMatcher"; }, { "dateChange": "dateChange"; "dateInput": "dateInput"; }, never, never, false, never>;
}

/**
 * An internal component used to display a single year in the datepicker.
 * @docs-private
 */
export declare class MatYearView<D> implements AfterContentInit, OnDestroy {
    readonly _changeDetectorRef: ChangeDetectorRef;
    private _dateFormats;
    _dateAdapter: DateAdapter<D>;
    private _dir?;
    private _rerenderSubscription;
    /** Flag used to filter out space/enter keyup events that originated outside of the view. */
    private _selectionKeyPressed;
    /** The date to display in this year view (everything other than the year is ignored). */
    get activeDate(): D;
    set activeDate(value: D);
    private _activeDate;
    /** The currently selected date. */
    get selected(): DateRange<D> | D | null;
    set selected(value: DateRange<D> | D | null);
    private _selected;
    /** The minimum selectable date. */
    get minDate(): D | null;
    set minDate(value: D | null);
    private _minDate;
    /** The maximum selectable date. */
    get maxDate(): D | null;
    set maxDate(value: D | null);
    private _maxDate;
    /** A function used to filter which dates are selectable. */
    dateFilter: (date: D) => boolean;
    /** Function that can be used to add custom CSS classes to date cells. */
    dateClass: MatCalendarCellClassFunction<D>;
    /** Emits when a new month is selected. */
    readonly selectedChange: EventEmitter<D>;
    /** Emits the selected month. This doesn't imply a change on the selected date */
    readonly monthSelected: EventEmitter<D>;
    /** Emits when any date is activated. */
    readonly activeDateChange: EventEmitter<D>;
    /** The body of calendar table */
    _matCalendarBody: MatCalendarBody;
    /** Grid of calendar cells representing the months of the year. */
    _months: MatCalendarCell[][];
    /** The label for this year (e.g. "2017"). */
    _yearLabel: string;
    /** The month in this year that today falls on. Null if today is in a different year. */
    _todayMonth: number | null;
    /**
     * The month in this year that the selected Date falls on.
     * Null if the selected Date is in a different year.
     */
    _selectedMonth: number | null;
    constructor(_changeDetectorRef: ChangeDetectorRef, _dateFormats: MatDateFormats, _dateAdapter: DateAdapter<D>, _dir?: Directionality | undefined);
    ngAfterContentInit(): void;
    ngOnDestroy(): void;
    /** Handles when a new month is selected. */
    _monthSelected(event: MatCalendarUserEvent<number>): void;
    /**
     * Takes the index of a calendar body cell wrapped in in an event as argument. For the date that
     * corresponds to the given cell, set `activeDate` to that date and fire `activeDateChange` with
     * that date.
     *
     * This function is used to match each component's model of the active date with the calendar
     * body cell that was focused. It updates its value of `activeDate` synchronously and updates the
     * parent's value asynchronously via the `activeDateChange` event. The child component receives an
     * updated value asynchronously via the `activeCell` Input.
     */
    _updateActiveDate(event: MatCalendarUserEvent<number>): void;
    /** Handles keydown events on the calendar body when calendar is in year view. */
    _handleCalendarBodyKeydown(event: KeyboardEvent): void;
    /** Handles keyup events on the calendar body when calendar is in year view. */
    _handleCalendarBodyKeyup(event: KeyboardEvent): void;
    /** Initializes this year view. */
    _init(): void;
    /** Focuses the active cell after the microtask queue is empty. */
    _focusActiveCell(): void;
    /** Schedules the matCalendarBody to focus the active cell after change detection has run */
    _focusActiveCellAfterViewChecked(): void;
    /**
     * Gets the month in this year that the given Date falls on.
     * Returns null if the given Date is in another year.
     */
    private _getMonthInCurrentYear;
    /**
     * Takes a month and returns a new date in the same day and year as the currently active date.
     *  The returned date will have the same month as the argument date.
     */
    private _getDateFromMonth;
    /** Creates an MatCalendarCell for the given month. */
    private _createCellForMonth;
    /** Whether the given month is enabled. */
    private _shouldEnableMonth;
    /**
     * Tests whether the combination month/year is after this.maxDate, considering
     * just the month and year of this.maxDate
     */
    private _isYearAndMonthAfterMaxDate;
    /**
     * Tests whether the combination month/year is before this.minDate, considering
     * just the month and year of this.minDate
     */
    private _isYearAndMonthBeforeMinDate;
    /** Determines whether the user has the RTL layout direction. */
    private _isRtl;
    /** Sets the currently-selected month based on a model value. */
    private _setSelectedMonth;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatYearView<any>, [null, { optional: true; }, { optional: true; }, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatYearView<any>, "mat-year-view", ["matYearView"], { "activeDate": "activeDate"; "selected": "selected"; "minDate": "minDate"; "maxDate": "maxDate"; "dateFilter": "dateFilter"; "dateClass": "dateClass"; }, { "selectedChange": "selectedChange"; "monthSelected": "monthSelected"; "activeDateChange": "activeDateChange"; }, never, never, false, never>;
}

export declare const yearsPerPage = 24;

export declare const yearsPerRow = 4;

export { }
