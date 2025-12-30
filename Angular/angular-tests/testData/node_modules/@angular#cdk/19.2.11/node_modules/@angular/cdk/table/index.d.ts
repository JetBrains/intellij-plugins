import { Directionality, Direction } from '../bidi-module.d-BSI86Zrk.js';
import * as i0 from '@angular/core';
import { TemplateRef, ElementRef, InjectionToken, OnChanges, IterableDiffers, IterableDiffer, SimpleChanges, IterableChanges, OnDestroy, ViewContainerRef, AfterContentInit, AfterContentChecked, OnInit, ChangeDetectorRef, TrackByFunction, EventEmitter, QueryList, Injector } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { DataSource, CollectionViewer } from '../data-source.d-DAIyaEMO.js';
import { _ViewRepeater } from '../view-repeater.d-CJ9e48MX.js';
import { ScrollingModule } from '../scrolling-module.d-CUKr8D_p.js';
import '../number-property.d-BzBQchZ2.js';

/** @docs-private */
type Constructor<T> = new (...args: any[]) => T;
/**
 * Interface for a mixin to provide a directive with a function that checks if the sticky input has
 * been changed since the last time the function was called. Essentially adds a dirty-check to the
 * sticky value.
 * @docs-private
 */
interface CanStick {
    /** Whether sticky positioning should be applied. */
    sticky: boolean;
    /** Whether the sticky value has changed since this was last called. */
    hasStickyChanged(): boolean;
    /** Resets the dirty check for cases where the sticky state has been used without checking. */
    resetStickyChanged(): void;
}
/** @docs-private */
type CanStickCtor = Constructor<CanStick>;
/**
 * Mixin to provide a directive with a function that checks if the sticky input has been
 * changed since the last time the function was called. Essentially adds a dirty-check to the
 * sticky value.
 * @docs-private
 * @deprecated Implement the `CanStick` interface instead.
 * @breaking-change 19.0.0
 */
declare function mixinHasStickyInput<T extends Constructor<{}>>(base: T): CanStickCtor & T;

/** Base interface for a cell definition. Captures a column's cell template definition. */
interface CellDef {
    template: TemplateRef<any>;
}
/**
 * Cell definition for a CDK table.
 * Captures the template of a column's data row cell as well as cell-specific properties.
 */
declare class CdkCellDef implements CellDef {
    /** @docs-private */
    template: TemplateRef<any>;
    constructor(...args: unknown[]);
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkCellDef, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkCellDef, "[cdkCellDef]", never, {}, {}, never, never, true, never>;
}
/**
 * Header cell definition for a CDK table.
 * Captures the template of a column's header cell and as well as cell-specific properties.
 */
declare class CdkHeaderCellDef implements CellDef {
    /** @docs-private */
    template: TemplateRef<any>;
    constructor(...args: unknown[]);
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkHeaderCellDef, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkHeaderCellDef, "[cdkHeaderCellDef]", never, {}, {}, never, never, true, never>;
}
/**
 * Footer cell definition for a CDK table.
 * Captures the template of a column's footer cell and as well as cell-specific properties.
 */
declare class CdkFooterCellDef implements CellDef {
    /** @docs-private */
    template: TemplateRef<any>;
    constructor(...args: unknown[]);
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkFooterCellDef, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkFooterCellDef, "[cdkFooterCellDef]", never, {}, {}, never, never, true, never>;
}
/**
 * Column definition for the CDK table.
 * Defines a set of cells available for a table column.
 */
declare class CdkColumnDef implements CanStick {
    _table?: any;
    private _hasStickyChanged;
    /** Unique name for this column. */
    get name(): string;
    set name(name: string);
    protected _name: string;
    /** Whether the cell is sticky. */
    get sticky(): boolean;
    set sticky(value: boolean);
    private _sticky;
    /**
     * Whether this column should be sticky positioned on the end of the row. Should make sure
     * that it mimics the `CanStick` mixin such that `_hasStickyChanged` is set to true if the value
     * has been changed.
     */
    get stickyEnd(): boolean;
    set stickyEnd(value: boolean);
    _stickyEnd: boolean;
    /** @docs-private */
    cell: CdkCellDef;
    /** @docs-private */
    headerCell: CdkHeaderCellDef;
    /** @docs-private */
    footerCell: CdkFooterCellDef;
    /**
     * Transformed version of the column name that can be used as part of a CSS classname. Excludes
     * all non-alphanumeric characters and the special characters '-' and '_'. Any characters that
     * do not match are replaced by the '-' character.
     */
    cssClassFriendlyName: string;
    /**
     * Class name for cells in this column.
     * @docs-private
     */
    _columnCssClassName: string[];
    constructor(...args: unknown[]);
    /** Whether the sticky state has changed. */
    hasStickyChanged(): boolean;
    /** Resets the sticky changed state. */
    resetStickyChanged(): void;
    /**
     * Overridable method that sets the css classes that will be added to every cell in this
     * column.
     * In the future, columnCssClassName will change from type string[] to string and this
     * will set a single string value.
     * @docs-private
     */
    protected _updateColumnCssClassName(): void;
    /**
     * This has been extracted to a util because of TS 4 and VE.
     * View Engine doesn't support property rename inheritance.
     * TS 4.0 doesn't allow properties to override accessors or vice-versa.
     * @docs-private
     */
    protected _setNameInput(value: string): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkColumnDef, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkColumnDef, "[cdkColumnDef]", never, { "name": { "alias": "cdkColumnDef"; "required": false; }; "sticky": { "alias": "sticky"; "required": false; }; "stickyEnd": { "alias": "stickyEnd"; "required": false; }; }, {}, ["cell", "headerCell", "footerCell"], never, true, never>;
    static ngAcceptInputType_sticky: unknown;
    static ngAcceptInputType_stickyEnd: unknown;
}
/** Base class for the cells. Adds a CSS classname that identifies the column it renders in. */
declare class BaseCdkCell {
    constructor(columnDef: CdkColumnDef, elementRef: ElementRef);
}
/** Header cell template container that adds the right classes and role. */
declare class CdkHeaderCell extends BaseCdkCell {
    constructor(...args: unknown[]);
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkHeaderCell, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkHeaderCell, "cdk-header-cell, th[cdk-header-cell]", never, {}, {}, never, never, true, never>;
}
/** Footer cell template container that adds the right classes and role. */
declare class CdkFooterCell extends BaseCdkCell {
    constructor(...args: unknown[]);
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkFooterCell, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkFooterCell, "cdk-footer-cell, td[cdk-footer-cell]", never, {}, {}, never, never, true, never>;
}
/** Cell template container that adds the right classes and role. */
declare class CdkCell extends BaseCdkCell {
    constructor(...args: unknown[]);
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkCell, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkCell, "cdk-cell, td[cdk-cell]", never, {}, {}, never, never, true, never>;
}

/**
 * @docs-private
 */
declare class _Schedule {
    tasks: (() => unknown)[];
    endTasks: (() => unknown)[];
}
/** Injection token used to provide a coalesced style scheduler. */
declare const _COALESCED_STYLE_SCHEDULER: InjectionToken<_CoalescedStyleScheduler>;
/**
 * Allows grouping up CSSDom mutations after the current execution context.
 * This can significantly improve performance when separate consecutive functions are
 * reading from the CSSDom and then mutating it.
 *
 * @docs-private
 */
declare class _CoalescedStyleScheduler {
    private _currentSchedule;
    private _ngZone;
    constructor(...args: unknown[]);
    /**
     * Schedules the specified task to run at the end of the current VM turn.
     */
    schedule(task: () => unknown): void;
    /**
     * Schedules the specified task to run after other scheduled tasks at the end of the current
     * VM turn.
     */
    scheduleEnd(task: () => unknown): void;
    private _createScheduleIfNeeded;
    static ɵfac: i0.ɵɵFactoryDeclaration<_CoalescedStyleScheduler, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<_CoalescedStyleScheduler>;
}

/**
 * The row template that can be used by the mat-table. Should not be used outside of the
 * material library.
 */
declare const CDK_ROW_TEMPLATE = "<ng-container cdkCellOutlet></ng-container>";
/**
 * Base class for the CdkHeaderRowDef and CdkRowDef that handles checking their columns inputs
 * for changes and notifying the table.
 */
declare abstract class BaseRowDef implements OnChanges {
    template: TemplateRef<any>;
    protected _differs: IterableDiffers;
    /** The columns to be displayed on this row. */
    columns: Iterable<string>;
    /** Differ used to check if any changes were made to the columns. */
    protected _columnsDiffer: IterableDiffer<any>;
    constructor(...args: unknown[]);
    ngOnChanges(changes: SimpleChanges): void;
    /**
     * Returns the difference between the current columns and the columns from the last diff, or null
     * if there is no difference.
     */
    getColumnsDiff(): IterableChanges<any> | null;
    /** Gets this row def's relevant cell template from the provided column def. */
    extractCellTemplate(column: CdkColumnDef): TemplateRef<any>;
    static ɵfac: i0.ɵɵFactoryDeclaration<BaseRowDef, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<BaseRowDef, never, never, {}, {}, never, never, true, never>;
}
/**
 * Header row definition for the CDK table.
 * Captures the header row's template and other header properties such as the columns to display.
 */
declare class CdkHeaderRowDef extends BaseRowDef implements CanStick, OnChanges {
    _table?: any;
    private _hasStickyChanged;
    /** Whether the row is sticky. */
    get sticky(): boolean;
    set sticky(value: boolean);
    private _sticky;
    constructor(...args: unknown[]);
    ngOnChanges(changes: SimpleChanges): void;
    /** Whether the sticky state has changed. */
    hasStickyChanged(): boolean;
    /** Resets the sticky changed state. */
    resetStickyChanged(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkHeaderRowDef, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkHeaderRowDef, "[cdkHeaderRowDef]", never, { "columns": { "alias": "cdkHeaderRowDef"; "required": false; }; "sticky": { "alias": "cdkHeaderRowDefSticky"; "required": false; }; }, {}, never, never, true, never>;
    static ngAcceptInputType_sticky: unknown;
}
/**
 * Footer row definition for the CDK table.
 * Captures the footer row's template and other footer properties such as the columns to display.
 */
declare class CdkFooterRowDef extends BaseRowDef implements CanStick, OnChanges {
    _table?: any;
    private _hasStickyChanged;
    /** Whether the row is sticky. */
    get sticky(): boolean;
    set sticky(value: boolean);
    private _sticky;
    constructor(...args: unknown[]);
    ngOnChanges(changes: SimpleChanges): void;
    /** Whether the sticky state has changed. */
    hasStickyChanged(): boolean;
    /** Resets the sticky changed state. */
    resetStickyChanged(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkFooterRowDef, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkFooterRowDef, "[cdkFooterRowDef]", never, { "columns": { "alias": "cdkFooterRowDef"; "required": false; }; "sticky": { "alias": "cdkFooterRowDefSticky"; "required": false; }; }, {}, never, never, true, never>;
    static ngAcceptInputType_sticky: unknown;
}
/**
 * Data row definition for the CDK table.
 * Captures the header row's template and other row properties such as the columns to display and
 * a when predicate that describes when this row should be used.
 */
declare class CdkRowDef<T> extends BaseRowDef {
    _table?: any;
    /**
     * Function that should return true if this row template should be used for the provided index
     * and row data. If left undefined, this row will be considered the default row template to use
     * when no other when functions return true for the data.
     * For every row, there must be at least one when function that passes or an undefined to default.
     */
    when: (index: number, rowData: T) => boolean;
    constructor(...args: unknown[]);
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkRowDef<any>, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkRowDef<any>, "[cdkRowDef]", never, { "columns": { "alias": "cdkRowDefColumns"; "required": false; }; "when": { "alias": "cdkRowDefWhen"; "required": false; }; }, {}, never, never, true, never>;
}
/** Context provided to the row cells when `multiTemplateDataRows` is false */
interface CdkCellOutletRowContext<T> {
    /** Data for the row that this cell is located within. */
    $implicit?: T;
    /** Index of the data object in the provided data array. */
    index?: number;
    /** Length of the number of total rows. */
    count?: number;
    /** True if this cell is contained in the first row. */
    first?: boolean;
    /** True if this cell is contained in the last row. */
    last?: boolean;
    /** True if this cell is contained in a row with an even-numbered index. */
    even?: boolean;
    /** True if this cell is contained in a row with an odd-numbered index. */
    odd?: boolean;
}
/**
 * Context provided to the row cells when `multiTemplateDataRows` is true. This context is the same
 * as CdkCellOutletRowContext except that the single `index` value is replaced by `dataIndex` and
 * `renderIndex`.
 */
interface CdkCellOutletMultiRowContext<T> {
    /** Data for the row that this cell is located within. */
    $implicit?: T;
    /** Index of the data object in the provided data array. */
    dataIndex?: number;
    /** Index location of the rendered row that this cell is located within. */
    renderIndex?: number;
    /** Length of the number of total rows. */
    count?: number;
    /** True if this cell is contained in the first row. */
    first?: boolean;
    /** True if this cell is contained in the last row. */
    last?: boolean;
    /** True if this cell is contained in a row with an even-numbered index. */
    even?: boolean;
    /** True if this cell is contained in a row with an odd-numbered index. */
    odd?: boolean;
}
/**
 * Outlet for rendering cells inside of a row or header row.
 * @docs-private
 */
declare class CdkCellOutlet implements OnDestroy {
    _viewContainer: ViewContainerRef;
    /** The ordered list of cells to render within this outlet's view container */
    cells: CdkCellDef[];
    /** The data context to be provided to each cell */
    context: any;
    /**
     * Static property containing the latest constructed instance of this class.
     * Used by the CDK table when each CdkHeaderRow and CdkRow component is created using
     * createEmbeddedView. After one of these components are created, this property will provide
     * a handle to provide that component's cells and context. After init, the CdkCellOutlet will
     * construct the cells with the provided context.
     */
    static mostRecentCellOutlet: CdkCellOutlet | null;
    constructor(...args: unknown[]);
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkCellOutlet, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkCellOutlet, "[cdkCellOutlet]", never, {}, {}, never, never, true, never>;
}
/** Header template container that contains the cell outlet. Adds the right class and role. */
declare class CdkHeaderRow {
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkHeaderRow, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<CdkHeaderRow, "cdk-header-row, tr[cdk-header-row]", never, {}, {}, never, never, true, never>;
}
/** Footer template container that contains the cell outlet. Adds the right class and role. */
declare class CdkFooterRow {
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkFooterRow, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<CdkFooterRow, "cdk-footer-row, tr[cdk-footer-row]", never, {}, {}, never, never, true, never>;
}
/** Data row template container that contains the cell outlet. Adds the right class and role. */
declare class CdkRow {
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkRow, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<CdkRow, "cdk-row, tr[cdk-row]", never, {}, {}, never, never, true, never>;
}
/** Row that can be used to display a message when no data is shown in the table. */
declare class CdkNoDataRow {
    templateRef: TemplateRef<any>;
    _contentClassName: string;
    constructor(...args: unknown[]);
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkNoDataRow, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkNoDataRow, "ng-template[cdkNoDataRow]", never, {}, {}, never, never, true, never>;
}

/** The injection token used to specify the StickyPositioningListener. */
declare const STICKY_POSITIONING_LISTENER: InjectionToken<StickyPositioningListener>;
type StickySize = number | null | undefined;
type StickyOffset = number | null | undefined;
interface StickyUpdate {
    elements?: readonly (HTMLElement[] | undefined)[];
    offsets?: StickyOffset[];
    sizes: StickySize[];
}
/**
 * If provided, CdkTable will call the methods below when it updates the size/
 * position/etc of its sticky rows and columns.
 */
interface StickyPositioningListener {
    /** Called when CdkTable updates its sticky start columns. */
    stickyColumnsUpdated(update: StickyUpdate): void;
    /** Called when CdkTable updates its sticky end columns. */
    stickyEndColumnsUpdated(update: StickyUpdate): void;
    /** Called when CdkTable updates its sticky header rows. */
    stickyHeaderRowsUpdated(update: StickyUpdate): void;
    /** Called when CdkTable updates its sticky footer rows. */
    stickyFooterRowsUpdated(update: StickyUpdate): void;
}

/**
 * Enables the recycle view repeater strategy, which reduces rendering latency. Not compatible with
 * tables that animate rows.
 */
declare class CdkRecycleRows {
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkRecycleRows, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkRecycleRows, "cdk-table[recycleRows], table[cdk-table][recycleRows]", never, {}, {}, never, never, true, never>;
}
/** Interface used to provide an outlet for rows to be inserted into. */
interface RowOutlet {
    viewContainer: ViewContainerRef;
}
/** Possible types that can be set as the data source for a `CdkTable`. */
type CdkTableDataSourceInput<T> = readonly T[] | DataSource<T> | Observable<readonly T[]>;
/**
 * Provides a handle for the table to grab the view container's ng-container to insert data rows.
 * @docs-private
 */
declare class DataRowOutlet implements RowOutlet {
    viewContainer: ViewContainerRef;
    elementRef: ElementRef<any>;
    constructor(...args: unknown[]);
    static ɵfac: i0.ɵɵFactoryDeclaration<DataRowOutlet, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<DataRowOutlet, "[rowOutlet]", never, {}, {}, never, never, true, never>;
}
/**
 * Provides a handle for the table to grab the view container's ng-container to insert the header.
 * @docs-private
 */
declare class HeaderRowOutlet implements RowOutlet {
    viewContainer: ViewContainerRef;
    elementRef: ElementRef<any>;
    constructor(...args: unknown[]);
    static ɵfac: i0.ɵɵFactoryDeclaration<HeaderRowOutlet, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<HeaderRowOutlet, "[headerRowOutlet]", never, {}, {}, never, never, true, never>;
}
/**
 * Provides a handle for the table to grab the view container's ng-container to insert the footer.
 * @docs-private
 */
declare class FooterRowOutlet implements RowOutlet {
    viewContainer: ViewContainerRef;
    elementRef: ElementRef<any>;
    constructor(...args: unknown[]);
    static ɵfac: i0.ɵɵFactoryDeclaration<FooterRowOutlet, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<FooterRowOutlet, "[footerRowOutlet]", never, {}, {}, never, never, true, never>;
}
/**
 * Provides a handle for the table to grab the view
 * container's ng-container to insert the no data row.
 * @docs-private
 */
declare class NoDataRowOutlet implements RowOutlet {
    viewContainer: ViewContainerRef;
    elementRef: ElementRef<any>;
    constructor(...args: unknown[]);
    static ɵfac: i0.ɵɵFactoryDeclaration<NoDataRowOutlet, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<NoDataRowOutlet, "[noDataRowOutlet]", never, {}, {}, never, never, true, never>;
}
/**
 * The table template that can be used by the mat-table. Should not be used outside of the
 * material library.
 * @docs-private
 */
declare const CDK_TABLE_TEMPLATE = "\n  <ng-content select=\"caption\"/>\n  <ng-content select=\"colgroup, col\"/>\n\n  <!--\n    Unprojected content throws a hydration error so we need this to capture it.\n    It gets removed on the client so it doesn't affect the layout.\n  -->\n  @if (_isServer) {\n    <ng-content/>\n  }\n\n  @if (_isNativeHtmlTable) {\n    <thead role=\"rowgroup\">\n      <ng-container headerRowOutlet/>\n    </thead>\n    <tbody role=\"rowgroup\">\n      <ng-container rowOutlet/>\n      <ng-container noDataRowOutlet/>\n    </tbody>\n    <tfoot role=\"rowgroup\">\n      <ng-container footerRowOutlet/>\n    </tfoot>\n  } @else {\n    <ng-container headerRowOutlet/>\n    <ng-container rowOutlet/>\n    <ng-container noDataRowOutlet/>\n    <ng-container footerRowOutlet/>\n  }\n";
/**
 * Interface used to conveniently type the possible context interfaces for the render row.
 * @docs-private
 */
interface RowContext<T> extends CdkCellOutletMultiRowContext<T>, CdkCellOutletRowContext<T> {
}
/**
 * Set of properties that represents the identity of a single rendered row.
 *
 * When the table needs to determine the list of rows to render, it will do so by iterating through
 * each data object and evaluating its list of row templates to display (when multiTemplateDataRows
 * is false, there is only one template per data object). For each pair of data object and row
 * template, a `RenderRow` is added to the list of rows to render. If the data object and row
 * template pair has already been rendered, the previously used `RenderRow` is added; else a new
 * `RenderRow` is * created. Once the list is complete and all data objects have been iterated
 * through, a diff is performed to determine the changes that need to be made to the rendered rows.
 *
 * @docs-private
 */
interface RenderRow<T> {
    data: T;
    dataIndex: number;
    rowDef: CdkRowDef<T>;
}
/**
 * A data table that can render a header row, data rows, and a footer row.
 * Uses the dataSource input to determine the data to be rendered. The data can be provided either
 * as a data array, an Observable stream that emits the data array to render, or a DataSource with a
 * connect function that will return an Observable stream that emits the data array to render.
 */
declare class CdkTable<T> implements AfterContentInit, AfterContentChecked, CollectionViewer, OnDestroy, OnInit {
    protected readonly _differs: IterableDiffers;
    protected readonly _changeDetectorRef: ChangeDetectorRef;
    protected readonly _elementRef: ElementRef<any>;
    protected readonly _dir: Directionality | null;
    private _platform;
    protected readonly _viewRepeater: _ViewRepeater<T, RenderRow<T>, RowContext<T>>;
    protected readonly _coalescedStyleScheduler: _CoalescedStyleScheduler;
    private readonly _viewportRuler;
    protected readonly _stickyPositioningListener: StickyPositioningListener;
    private _document;
    /** Latest data provided by the data source. */
    protected _data: readonly T[] | undefined;
    /** Subject that emits when the component has been destroyed. */
    private readonly _onDestroy;
    /** List of the rendered rows as identified by their `RenderRow` object. */
    private _renderRows;
    /** Subscription that listens for the data provided by the data source. */
    private _renderChangeSubscription;
    /**
     * Map of all the user's defined columns (header, data, and footer cell template) identified by
     * name. Collection populated by the column definitions gathered by `ContentChildren` as well as
     * any custom column definitions added to `_customColumnDefs`.
     */
    private _columnDefsByName;
    /**
     * Set of all row definitions that can be used by this table. Populated by the rows gathered by
     * using `ContentChildren` as well as any custom row definitions added to `_customRowDefs`.
     */
    private _rowDefs;
    /**
     * Set of all header row definitions that can be used by this table. Populated by the rows
     * gathered by using `ContentChildren` as well as any custom row definitions added to
     * `_customHeaderRowDefs`.
     */
    private _headerRowDefs;
    /**
     * Set of all row definitions that can be used by this table. Populated by the rows gathered by
     * using `ContentChildren` as well as any custom row definitions added to
     * `_customFooterRowDefs`.
     */
    private _footerRowDefs;
    /** Differ used to find the changes in the data provided by the data source. */
    private _dataDiffer;
    /** Stores the row definition that does not have a when predicate. */
    private _defaultRowDef;
    /**
     * Column definitions that were defined outside of the direct content children of the table.
     * These will be defined when, e.g., creating a wrapper around the cdkTable that has
     * column definitions as *its* content child.
     */
    private _customColumnDefs;
    /**
     * Data row definitions that were defined outside of the direct content children of the table.
     * These will be defined when, e.g., creating a wrapper around the cdkTable that has
     * built-in data rows as *its* content child.
     */
    private _customRowDefs;
    /**
     * Header row definitions that were defined outside of the direct content children of the table.
     * These will be defined when, e.g., creating a wrapper around the cdkTable that has
     * built-in header rows as *its* content child.
     */
    private _customHeaderRowDefs;
    /**
     * Footer row definitions that were defined outside of the direct content children of the table.
     * These will be defined when, e.g., creating a wrapper around the cdkTable that has a
     * built-in footer row as *its* content child.
     */
    private _customFooterRowDefs;
    /** No data row that was defined outside of the direct content children of the table. */
    private _customNoDataRow;
    /**
     * Whether the header row definition has been changed. Triggers an update to the header row after
     * content is checked. Initialized as true so that the table renders the initial set of rows.
     */
    private _headerRowDefChanged;
    /**
     * Whether the footer row definition has been changed. Triggers an update to the footer row after
     * content is checked. Initialized as true so that the table renders the initial set of rows.
     */
    private _footerRowDefChanged;
    /**
     * Whether the sticky column styles need to be updated. Set to `true` when the visible columns
     * change.
     */
    private _stickyColumnStylesNeedReset;
    /**
     * Whether the sticky styler should recalculate cell widths when applying sticky styles. If
     * `false`, cached values will be used instead. This is only applicable to tables with
     * {@link fixedLayout} enabled. For other tables, cell widths will always be recalculated.
     */
    private _forceRecalculateCellWidths;
    /**
     * Cache of the latest rendered `RenderRow` objects as a map for easy retrieval when constructing
     * a new list of `RenderRow` objects for rendering rows. Since the new list is constructed with
     * the cached `RenderRow` objects when possible, the row identity is preserved when the data
     * and row template matches, which allows the `IterableDiffer` to check rows by reference
     * and understand which rows are added/moved/removed.
     *
     * Implemented as a map of maps where the first key is the `data: T` object and the second is the
     * `CdkRowDef<T>` object. With the two keys, the cache points to a `RenderRow<T>` object that
     * contains an array of created pairs. The array is necessary to handle cases where the data
     * array contains multiple duplicate data objects and each instantiated `RenderRow` must be
     * stored.
     */
    private _cachedRenderRowsMap;
    /** Whether the table is applied to a native `<table>`. */
    protected _isNativeHtmlTable: boolean;
    /**
     * Utility class that is responsible for applying the appropriate sticky positioning styles to
     * the table's rows and cells.
     */
    private _stickyStyler;
    /**
     * CSS class added to any row or cell that has sticky positioning applied. May be overridden by
     * table subclasses.
     */
    protected stickyCssClass: string;
    /**
     * Whether to manually add position: sticky to all sticky cell elements. Not needed if
     * the position is set in a selector associated with the value of stickyCssClass. May be
     * overridden by table subclasses
     */
    protected needsPositionStickyOnElement: boolean;
    /** Whether the component is being rendered on the server. */
    protected _isServer: boolean;
    /** Whether the no data row is currently showing anything. */
    private _isShowingNoDataRow;
    /** Whether the table has rendered out all the outlets for the first time. */
    private _hasAllOutlets;
    /** Whether the table is done initializing. */
    private _hasInitialized;
    /** Aria role to apply to the table's cells based on the table's own role. */
    _getCellRole(): string | null;
    private _cellRoleInternal;
    /**
     * Tracking function that will be used to check the differences in data changes. Used similarly
     * to `ngFor` `trackBy` function. Optimize row operations by identifying a row based on its data
     * relative to the function to know if a row should be added/removed/moved.
     * Accepts a function that takes two parameters, `index` and `item`.
     */
    get trackBy(): TrackByFunction<T>;
    set trackBy(fn: TrackByFunction<T>);
    private _trackByFn;
    /**
     * The table's source of data, which can be provided in three ways (in order of complexity):
     *   - Simple data array (each object represents one table row)
     *   - Stream that emits a data array each time the array changes
     *   - `DataSource` object that implements the connect/disconnect interface.
     *
     * If a data array is provided, the table must be notified when the array's objects are
     * added, removed, or moved. This can be done by calling the `renderRows()` function which will
     * render the diff since the last table render. If the data array reference is changed, the table
     * will automatically trigger an update to the rows.
     *
     * When providing an Observable stream, the table will trigger an update automatically when the
     * stream emits a new array of data.
     *
     * Finally, when providing a `DataSource` object, the table will use the Observable stream
     * provided by the connect function and trigger updates when that stream emits new data array
     * values. During the table's ngOnDestroy or when the data source is removed from the table, the
     * table will call the DataSource's `disconnect` function (may be useful for cleaning up any
     * subscriptions registered during the connect process).
     */
    get dataSource(): CdkTableDataSourceInput<T>;
    set dataSource(dataSource: CdkTableDataSourceInput<T>);
    private _dataSource;
    /**
     * Whether to allow multiple rows per data object by evaluating which rows evaluate their 'when'
     * predicate to true. If `multiTemplateDataRows` is false, which is the default value, then each
     * dataobject will render the first row that evaluates its when predicate to true, in the order
     * defined in the table, or otherwise the default row which does not have a when predicate.
     */
    get multiTemplateDataRows(): boolean;
    set multiTemplateDataRows(value: boolean);
    _multiTemplateDataRows: boolean;
    /**
     * Whether to use a fixed table layout. Enabling this option will enforce consistent column widths
     * and optimize rendering sticky styles for native tables. No-op for flex tables.
     */
    get fixedLayout(): boolean;
    set fixedLayout(value: boolean);
    private _fixedLayout;
    /**
     * Emits when the table completes rendering a set of data rows based on the latest data from the
     * data source, even if the set of rows is empty.
     */
    readonly contentChanged: EventEmitter<void>;
    /**
     * Stream containing the latest information on what rows are being displayed on screen.
     * Can be used by the data source to as a heuristic of what data should be provided.
     *
     * @docs-private
     */
    readonly viewChange: BehaviorSubject<{
        start: number;
        end: number;
    }>;
    _rowOutlet: DataRowOutlet;
    _headerRowOutlet: HeaderRowOutlet;
    _footerRowOutlet: FooterRowOutlet;
    _noDataRowOutlet: NoDataRowOutlet;
    /**
     * The column definitions provided by the user that contain what the header, data, and footer
     * cells should render for each column.
     */
    _contentColumnDefs: QueryList<CdkColumnDef>;
    /** Set of data row definitions that were provided to the table as content children. */
    _contentRowDefs: QueryList<CdkRowDef<T>>;
    /** Set of header row definitions that were provided to the table as content children. */
    _contentHeaderRowDefs: QueryList<CdkHeaderRowDef>;
    /** Set of footer row definitions that were provided to the table as content children. */
    _contentFooterRowDefs: QueryList<CdkFooterRowDef>;
    /** Row definition that will only be rendered if there's no data in the table. */
    _noDataRow: CdkNoDataRow;
    private _injector;
    constructor(...args: unknown[]);
    ngOnInit(): void;
    ngAfterContentInit(): void;
    ngAfterContentChecked(): void;
    ngOnDestroy(): void;
    /**
     * Renders rows based on the table's latest set of data, which was either provided directly as an
     * input or retrieved through an Observable stream (directly or from a DataSource).
     * Checks for differences in the data since the last diff to perform only the necessary
     * changes (add/remove/move rows).
     *
     * If the table's data source is a DataSource or Observable, this will be invoked automatically
     * each time the provided Observable stream emits a new data array. Otherwise if your data is
     * an array, this function will need to be called to render any changes.
     */
    renderRows(): void;
    /** Adds a column definition that was not included as part of the content children. */
    addColumnDef(columnDef: CdkColumnDef): void;
    /** Removes a column definition that was not included as part of the content children. */
    removeColumnDef(columnDef: CdkColumnDef): void;
    /** Adds a row definition that was not included as part of the content children. */
    addRowDef(rowDef: CdkRowDef<T>): void;
    /** Removes a row definition that was not included as part of the content children. */
    removeRowDef(rowDef: CdkRowDef<T>): void;
    /** Adds a header row definition that was not included as part of the content children. */
    addHeaderRowDef(headerRowDef: CdkHeaderRowDef): void;
    /** Removes a header row definition that was not included as part of the content children. */
    removeHeaderRowDef(headerRowDef: CdkHeaderRowDef): void;
    /** Adds a footer row definition that was not included as part of the content children. */
    addFooterRowDef(footerRowDef: CdkFooterRowDef): void;
    /** Removes a footer row definition that was not included as part of the content children. */
    removeFooterRowDef(footerRowDef: CdkFooterRowDef): void;
    /** Sets a no data row definition that was not included as a part of the content children. */
    setNoDataRow(noDataRow: CdkNoDataRow | null): void;
    /**
     * Updates the header sticky styles. First resets all applied styles with respect to the cells
     * sticking to the top. Then, evaluating which cells need to be stuck to the top. This is
     * automatically called when the header row changes its displayed set of columns, or if its
     * sticky input changes. May be called manually for cases where the cell content changes outside
     * of these events.
     */
    updateStickyHeaderRowStyles(): void;
    /**
     * Updates the footer sticky styles. First resets all applied styles with respect to the cells
     * sticking to the bottom. Then, evaluating which cells need to be stuck to the bottom. This is
     * automatically called when the footer row changes its displayed set of columns, or if its
     * sticky input changes. May be called manually for cases where the cell content changes outside
     * of these events.
     */
    updateStickyFooterRowStyles(): void;
    /**
     * Updates the column sticky styles. First resets all applied styles with respect to the cells
     * sticking to the left and right. Then sticky styles are added for the left and right according
     * to the column definitions for each cell in each row. This is automatically called when
     * the data source provides a new set of data or when a column definition changes its sticky
     * input. May be called manually for cases where the cell content changes outside of these events.
     */
    updateStickyColumnStyles(): void;
    /** Invoked whenever an outlet is created and has been assigned to the table. */
    _outletAssigned(): void;
    /** Whether the table has all the information to start rendering. */
    private _canRender;
    /** Renders the table if its state has changed. */
    private _render;
    /**
     * Get the list of RenderRow objects to render according to the current list of data and defined
     * row definitions. If the previous list already contained a particular pair, it should be reused
     * so that the differ equates their references.
     */
    private _getAllRenderRows;
    /**
     * Gets a list of `RenderRow<T>` for the provided data object and any `CdkRowDef` objects that
     * should be rendered for this data. Reuses the cached RenderRow objects if they match the same
     * `(T, CdkRowDef)` pair.
     */
    private _getRenderRowsForData;
    /** Update the map containing the content's column definitions. */
    private _cacheColumnDefs;
    /** Update the list of all available row definitions that can be used. */
    private _cacheRowDefs;
    /**
     * Check if the header, data, or footer rows have changed what columns they want to display or
     * whether the sticky states have changed for the header or footer. If there is a diff, then
     * re-render that section.
     */
    private _renderUpdatedColumns;
    /**
     * Switch to the provided data source by resetting the data and unsubscribing from the current
     * render change subscription if one exists. If the data source is null, interpret this by
     * clearing the row outlet. Otherwise start listening for new data.
     */
    private _switchDataSource;
    /** Set up a subscription for the data provided by the data source. */
    private _observeRenderChanges;
    /**
     * Clears any existing content in the header row outlet and creates a new embedded view
     * in the outlet using the header row definition.
     */
    private _forceRenderHeaderRows;
    /**
     * Clears any existing content in the footer row outlet and creates a new embedded view
     * in the outlet using the footer row definition.
     */
    private _forceRenderFooterRows;
    /** Adds the sticky column styles for the rows according to the columns' stick states. */
    private _addStickyColumnStyles;
    /** Gets the list of rows that have been rendered in the row outlet. */
    _getRenderedRows(rowOutlet: RowOutlet): HTMLElement[];
    /**
     * Get the matching row definitions that should be used for this row data. If there is only
     * one row definition, it is returned. Otherwise, find the row definitions that has a when
     * predicate that returns true with the data. If none return true, return the default row
     * definition.
     */
    _getRowDefs(data: T, dataIndex: number): CdkRowDef<T>[];
    private _getEmbeddedViewArgs;
    /**
     * Creates a new row template in the outlet and fills it with the set of cell templates.
     * Optionally takes a context to provide to the row and cells, as well as an optional index
     * of where to place the new row template in the outlet.
     */
    private _renderRow;
    private _renderCellTemplateForItem;
    /**
     * Updates the index-related context for each row to reflect any changes in the index of the rows,
     * e.g. first/last/even/odd.
     */
    private _updateRowIndexContext;
    /** Gets the column definitions for the provided row def. */
    private _getCellTemplates;
    /**
     * Forces a re-render of the data rows. Should be called in cases where there has been an input
     * change that affects the evaluation of which rows should be rendered, e.g. toggling
     * `multiTemplateDataRows` or adding/removing row definitions.
     */
    private _forceRenderDataRows;
    /**
     * Checks if there has been a change in sticky states since last check and applies the correct
     * sticky styles. Since checking resets the "dirty" state, this should only be performed once
     * during a change detection and after the inputs are settled (after content check).
     */
    private _checkStickyStates;
    /**
     * Creates the sticky styler that will be used for sticky rows and columns. Listens
     * for directionality changes and provides the latest direction to the styler. Re-applies column
     * stickiness when directionality changes.
     */
    private _setupStickyStyler;
    /** Filters definitions that belong to this table from a QueryList. */
    private _getOwnDefs;
    /** Creates or removes the no data row, depending on whether any data is being shown. */
    private _updateNoDataRow;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkTable<any>, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<CdkTable<any>, "cdk-table, table[cdk-table]", ["cdkTable"], { "trackBy": { "alias": "trackBy"; "required": false; }; "dataSource": { "alias": "dataSource"; "required": false; }; "multiTemplateDataRows": { "alias": "multiTemplateDataRows"; "required": false; }; "fixedLayout": { "alias": "fixedLayout"; "required": false; }; }, { "contentChanged": "contentChanged"; }, ["_noDataRow", "_contentColumnDefs", "_contentRowDefs", "_contentHeaderRowDefs", "_contentFooterRowDefs"], ["caption", "colgroup, col", "*"], true, never>;
    static ngAcceptInputType_multiTemplateDataRows: unknown;
    static ngAcceptInputType_fixedLayout: unknown;
}

/**
 * Column that simply shows text content for the header and row cells. Assumes that the table
 * is using the native table implementation (`<table>`).
 *
 * By default, the name of this column will be the header text and data property accessor.
 * The header text can be overridden with the `headerText` input. Cell values can be overridden with
 * the `dataAccessor` input. Change the text justification to the start or end using the `justify`
 * input.
 */
declare class CdkTextColumn<T> implements OnDestroy, OnInit {
    private _table;
    private _options;
    /** Column name that should be used to reference this column. */
    get name(): string;
    set name(name: string);
    _name: string;
    /**
     * Text label that should be used for the column header. If this property is not
     * set, the header text will default to the column name with its first letter capitalized.
     */
    headerText: string;
    /**
     * Accessor function to retrieve the data rendered for each cell. If this
     * property is not set, the data cells will render the value found in the data's property matching
     * the column's name. For example, if the column is named `id`, then the rendered value will be
     * value defined by the data's `id` property.
     */
    dataAccessor: (data: T, name: string) => string;
    /** Alignment of the cell values. */
    justify: 'start' | 'end' | 'center';
    /** @docs-private */
    columnDef: CdkColumnDef;
    /**
     * The column cell is provided to the column during `ngOnInit` with a static query.
     * Normally, this will be retrieved by the column using `ContentChild`, but that assumes the
     * column definition was provided in the same view as the table, which is not the case with this
     * component.
     * @docs-private
     */
    cell: CdkCellDef;
    /**
     * The column headerCell is provided to the column during `ngOnInit` with a static query.
     * Normally, this will be retrieved by the column using `ContentChild`, but that assumes the
     * column definition was provided in the same view as the table, which is not the case with this
     * component.
     * @docs-private
     */
    headerCell: CdkHeaderCellDef;
    constructor(...args: unknown[]);
    ngOnInit(): void;
    ngOnDestroy(): void;
    /**
     * Creates a default header text. Use the options' header text transformation function if one
     * has been provided. Otherwise simply capitalize the column name.
     */
    _createDefaultHeaderText(): string;
    /** Synchronizes the column definition name with the text column name. */
    private _syncColumnDefName;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkTextColumn<any>, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<CdkTextColumn<any>, "cdk-text-column", never, { "name": { "alias": "name"; "required": false; }; "headerText": { "alias": "headerText"; "required": false; }; "dataAccessor": { "alias": "dataAccessor"; "required": false; }; "justify": { "alias": "justify"; "required": false; }; }, {}, never, never, true, never>;
}

declare class CdkTableModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkTableModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<CdkTableModule, never, [typeof ScrollingModule, typeof CdkTable, typeof CdkRowDef, typeof CdkCellDef, typeof CdkCellOutlet, typeof CdkHeaderCellDef, typeof CdkFooterCellDef, typeof CdkColumnDef, typeof CdkCell, typeof CdkRow, typeof CdkHeaderCell, typeof CdkFooterCell, typeof CdkHeaderRow, typeof CdkHeaderRowDef, typeof CdkFooterRow, typeof CdkFooterRowDef, typeof DataRowOutlet, typeof HeaderRowOutlet, typeof FooterRowOutlet, typeof CdkTextColumn, typeof CdkNoDataRow, typeof CdkRecycleRows, typeof NoDataRowOutlet], [typeof CdkTable, typeof CdkRowDef, typeof CdkCellDef, typeof CdkCellOutlet, typeof CdkHeaderCellDef, typeof CdkFooterCellDef, typeof CdkColumnDef, typeof CdkCell, typeof CdkRow, typeof CdkHeaderCell, typeof CdkFooterCell, typeof CdkHeaderRow, typeof CdkHeaderRowDef, typeof CdkFooterRow, typeof CdkFooterRowDef, typeof DataRowOutlet, typeof HeaderRowOutlet, typeof FooterRowOutlet, typeof CdkTextColumn, typeof CdkNoDataRow, typeof CdkRecycleRows, typeof NoDataRowOutlet]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<CdkTableModule>;
}

/**
 * Directions that can be used when setting sticky positioning.
 * @docs-private
 */

type StickyDirection = 'top' | 'bottom' | 'left' | 'right';
/**
 * List of all possible directions that can be used for sticky positioning.
 * @docs-private
 */
declare const STICKY_DIRECTIONS: StickyDirection[];
/**
 * Applies and removes sticky positioning styles to the `CdkTable` rows and columns cells.
 * @docs-private
 */
declare class StickyStyler {
    private _isNativeHtmlTable;
    private _stickCellCss;
    direction: Direction;
    private _coalescedStyleScheduler;
    private _isBrowser;
    private readonly _needsPositionStickyOnElement;
    private readonly _positionListener?;
    private readonly _tableInjector?;
    private _elemSizeCache;
    private _resizeObserver;
    private _updatedStickyColumnsParamsToReplay;
    private _stickyColumnsReplayTimeout;
    private _cachedCellWidths;
    private readonly _borderCellCss;
    private _destroyed;
    /**
     * @param _isNativeHtmlTable Whether the sticky logic should be based on a table
     *     that uses the native `<table>` element.
     * @param _stickCellCss The CSS class that will be applied to every row/cell that has
     *     sticky positioning applied.
     * @param direction The directionality context of the table (ltr/rtl); affects column positioning
     *     by reversing left/right positions.
     * @param _isBrowser Whether the table is currently being rendered on the server or the client.
     * @param _needsPositionStickyOnElement Whether we need to specify position: sticky on cells
     *     using inline styles. If false, it is assumed that position: sticky is included in
     *     the component stylesheet for _stickCellCss.
     * @param _positionListener A listener that is notified of changes to sticky rows/columns
     *     and their dimensions.
     * @param _tableInjector The table's Injector.
     */
    constructor(_isNativeHtmlTable: boolean, _stickCellCss: string, direction: Direction, _coalescedStyleScheduler: _CoalescedStyleScheduler, _isBrowser?: boolean, _needsPositionStickyOnElement?: boolean, _positionListener?: StickyPositioningListener | undefined, _tableInjector?: Injector | undefined);
    /**
     * Clears the sticky positioning styles from the row and its cells by resetting the `position`
     * style, setting the zIndex to 0, and unsetting each provided sticky direction.
     * @param rows The list of rows that should be cleared from sticking in the provided directions
     * @param stickyDirections The directions that should no longer be set as sticky on the rows.
     */
    clearStickyPositioning(rows: HTMLElement[], stickyDirections: StickyDirection[]): void;
    /**
     * Applies sticky left and right positions to the cells of each row according to the sticky
     * states of the rendered column definitions.
     * @param rows The rows that should have its set of cells stuck according to the sticky states.
     * @param stickyStartStates A list of boolean states where each state represents whether the cell
     *     in this index position should be stuck to the start of the row.
     * @param stickyEndStates A list of boolean states where each state represents whether the cell
     *     in this index position should be stuck to the end of the row.
     * @param recalculateCellWidths Whether the sticky styler should recalculate the width of each
     *     column cell. If `false` cached widths will be used instead.
     * @param replay Whether to enqueue this call for replay after a ResizeObserver update.
     */
    updateStickyColumns(rows: HTMLElement[], stickyStartStates: boolean[], stickyEndStates: boolean[], recalculateCellWidths?: boolean, replay?: boolean): void;
    /**
     * Applies sticky positioning to the row's cells if using the native table layout, and to the
     * row itself otherwise.
     * @param rowsToStick The list of rows that should be stuck according to their corresponding
     *     sticky state and to the provided top or bottom position.
     * @param stickyStates A list of boolean states where each state represents whether the row
     *     should be stuck in the particular top or bottom position.
     * @param position The position direction in which the row should be stuck if that row should be
     *     sticky.
     *
     */
    stickRows(rowsToStick: HTMLElement[], stickyStates: boolean[], position: 'top' | 'bottom'): void;
    /**
     * When using the native table in Safari, sticky footer cells do not stick. The only way to stick
     * footer rows is to apply sticky styling to the tfoot container. This should only be done if
     * all footer rows are sticky. If not all footer rows are sticky, remove sticky positioning from
     * the tfoot element.
     */
    updateStickyFooterContainer(tableElement: Element, stickyStates: boolean[]): void;
    /** Triggered by the table's OnDestroy hook. */
    destroy(): void;
    /**
     * Removes the sticky style on the element by removing the sticky cell CSS class, re-evaluating
     * the zIndex, removing each of the provided sticky directions, and removing the
     * sticky position if there are no more directions.
     */
    _removeStickyStyle(element: HTMLElement, stickyDirections: StickyDirection[]): void;
    /**
     * Adds the sticky styling to the element by adding the sticky style class, changing position
     * to be sticky (and -webkit-sticky), setting the appropriate zIndex, and adding a sticky
     * direction and value.
     */
    _addStickyStyle(element: HTMLElement, dir: StickyDirection, dirValue: number, isBorderElement: boolean): void;
    /**
     * Calculate what the z-index should be for the element, depending on what directions (top,
     * bottom, left, right) have been set. It should be true that elements with a top direction
     * should have the highest index since these are elements like a table header. If any of those
     * elements are also sticky in another direction, then they should appear above other elements
     * that are only sticky top (e.g. a sticky column on a sticky header). Bottom-sticky elements
     * (e.g. footer rows) should then be next in the ordering such that they are below the header
     * but above any non-sticky elements. Finally, left/right sticky elements (e.g. sticky columns)
     * should minimally increment so that they are above non-sticky elements but below top and bottom
     * elements.
     */
    _getCalculatedZIndex(element: HTMLElement): string;
    /** Gets the widths for each cell in the provided row. */
    _getCellWidths(row: HTMLElement, recalculateCellWidths?: boolean): number[];
    /**
     * Determines the left and right positions of each sticky column cell, which will be the
     * accumulation of all sticky column cell widths to the left and right, respectively.
     * Non-sticky cells do not need to have a value set since their positions will not be applied.
     */
    _getStickyStartColumnPositions(widths: number[], stickyStates: boolean[]): number[];
    /**
     * Determines the left and right positions of each sticky column cell, which will be the
     * accumulation of all sticky column cell widths to the left and right, respectively.
     * Non-sticky cells do not need to have a value set since their positions will not be applied.
     */
    _getStickyEndColumnPositions(widths: number[], stickyStates: boolean[]): number[];
    /**
     * Retreives the most recently observed size of the specified element from the cache, or
     * meaures it directly if not yet cached.
     */
    private _retrieveElementSize;
    /**
     * Conditionally enqueue the requested sticky update and clear previously queued updates
     * for the same rows.
     */
    private _updateStickyColumnReplayQueue;
    /** Remove updates for the specified rows from the queue. */
    private _removeFromStickyColumnReplayQueue;
    /** Update _elemSizeCache with the observed sizes. */
    private _updateCachedSizes;
    /**
     * Invoke afterNextRender with the table's injector, falling back to CoalescedStyleScheduler
     * if the injector was not provided.
     */
    private _afterNextRender;
}

/**
 * Used to provide a table to some of the sub-components without causing a circular dependency.
 * @docs-private
 */
declare const CDK_TABLE: InjectionToken<any>;
/** Configurable options for `CdkTextColumn`. */
interface TextColumnOptions<T> {
    /**
     * Default function that provides the header text based on the column name if a header
     * text is not provided.
     */
    defaultHeaderTextTransform?: (name: string) => string;
    /** Default data accessor to use if one is not provided. */
    defaultDataAccessor?: (data: T, name: string) => string;
}
/** Injection token that can be used to specify the text column options. */
declare const TEXT_COLUMN_OPTIONS: InjectionToken<TextColumnOptions<any>>;

export { BaseCdkCell, BaseRowDef, CDK_ROW_TEMPLATE, CDK_TABLE, CDK_TABLE_TEMPLATE, CdkCell, CdkCellDef, CdkCellOutlet, CdkColumnDef, CdkFooterCell, CdkFooterCellDef, CdkFooterRow, CdkFooterRowDef, CdkHeaderCell, CdkHeaderCellDef, CdkHeaderRow, CdkHeaderRowDef, CdkNoDataRow, CdkRecycleRows, CdkRow, CdkRowDef, CdkTable, CdkTableModule, CdkTextColumn, DataRowOutlet, DataSource, FooterRowOutlet, HeaderRowOutlet, NoDataRowOutlet, STICKY_DIRECTIONS, STICKY_POSITIONING_LISTENER, StickyStyler, TEXT_COLUMN_OPTIONS, _COALESCED_STYLE_SCHEDULER, _CoalescedStyleScheduler, _Schedule, mixinHasStickyInput };
export type { CanStick, CanStickCtor, CdkCellOutletMultiRowContext, CdkCellOutletRowContext, CdkTableDataSourceInput, CellDef, Constructor, RenderRow, RowContext, RowOutlet, StickyDirection, StickyOffset, StickyPositioningListener, StickySize, StickyUpdate, TextColumnOptions };
