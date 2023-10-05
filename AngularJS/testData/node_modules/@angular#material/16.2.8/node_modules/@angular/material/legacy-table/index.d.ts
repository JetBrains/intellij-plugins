import { CdkCell } from '@angular/cdk/table';
import { CdkCellDef } from '@angular/cdk/table';
import { CdkColumnDef } from '@angular/cdk/table';
import { CdkFooterCell } from '@angular/cdk/table';
import { CdkFooterCellDef } from '@angular/cdk/table';
import { CdkFooterRow } from '@angular/cdk/table';
import { CdkFooterRowDef } from '@angular/cdk/table';
import { CdkHeaderCell } from '@angular/cdk/table';
import { CdkHeaderCellDef } from '@angular/cdk/table';
import { CdkHeaderRow } from '@angular/cdk/table';
import { CdkHeaderRowDef } from '@angular/cdk/table';
import { CdkNoDataRow } from '@angular/cdk/table';
import { CdkRow } from '@angular/cdk/table';
import { CdkRowDef } from '@angular/cdk/table';
import { CdkTable } from '@angular/cdk/table';
import { CdkTextColumn } from '@angular/cdk/table';
import * as i0 from '@angular/core';
import * as i5 from '@angular/cdk/table';
import * as i6 from '@angular/material/core';
import { MatLegacyPaginator } from '@angular/material/legacy-paginator';
import { _MatTableDataSource as _MatLegacyTableDataSource } from '@angular/material/table';
import { MatTableDataSourcePageEvent as MatLegacyTableDataSourcePageEvent } from '@angular/material/table';
import { MatTableDataSourcePaginator as MatLegacyTableDataSourcePaginator } from '@angular/material/table';

declare namespace i1 {
    export {
        MatLegacyRecycleRows,
        MatLegacyTable
    }
}

declare namespace i2 {
    export {
        MatLegacyCellDef,
        MatLegacyHeaderCellDef,
        MatLegacyFooterCellDef,
        MatLegacyColumnDef,
        MatLegacyHeaderCell,
        MatLegacyFooterCell,
        MatLegacyCell
    }
}

declare namespace i3 {
    export {
        MatLegacyHeaderRowDef,
        MatLegacyFooterRowDef,
        MatLegacyRowDef,
        MatLegacyHeaderRow,
        MatLegacyFooterRow,
        MatLegacyRow,
        MatLegacyNoDataRow
    }
}

declare namespace i4 {
    export {
        MatLegacyTextColumn
    }
}

/**
 * Cell template container that adds the right classes and role.
 * @deprecated Use `MatCell` from `@angular/material/table` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyCell extends CdkCell {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyCell, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyCell, "mat-cell, td[mat-cell]", never, {}, {}, never, never, false, never>;
}

/**
 * Cell definition for the mat-table.
 * Captures the template of a column's data row cell as well as cell-specific properties.
 * @deprecated Use `MatCellDef` from `@angular/material/table` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyCellDef extends CdkCellDef {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyCellDef, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyCellDef, "[matCellDef]", never, {}, {}, never, never, false, never>;
}

/**
 * Column definition for the mat-table.
 * Defines a set of cells available for a table column.
 * @deprecated Use `MatColumnDef` from `@angular/material/table` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyColumnDef extends CdkColumnDef {
    /** Unique name for this column. */
    get name(): string;
    set name(name: string);
    /**
     * Add "mat-column-" prefix in addition to "cdk-column-" prefix.
     * In the future, this will only add "mat-column-" and columnCssClassName
     * will change from type string[] to string.
     * @docs-private
     */
    protected _updateColumnCssClassName(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyColumnDef, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyColumnDef, "[matColumnDef]", never, { "sticky": { "alias": "sticky"; "required": false; }; "name": { "alias": "matColumnDef"; "required": false; }; }, {}, never, never, false, never>;
}

/**
 * Footer cell template container that adds the right classes and role.
 * @deprecated Use `MatFooterCell` from `@angular/material/table` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyFooterCell extends CdkFooterCell {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyFooterCell, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyFooterCell, "mat-footer-cell, td[mat-footer-cell]", never, {}, {}, never, never, false, never>;
}

/**
 * Footer cell definition for the mat-table.
 * Captures the template of a column's footer cell and as well as cell-specific properties.
 * @deprecated Use `MatFooterCellDef` from `@angular/material/table` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyFooterCellDef extends CdkFooterCellDef {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyFooterCellDef, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyFooterCellDef, "[matFooterCellDef]", never, {}, {}, never, never, false, never>;
}

/**
 * Footer template container that contains the cell outlet. Adds the right class and role.
 * @deprecated Use `MatFooterRow` from `@angular/material/table` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyFooterRow extends CdkFooterRow {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyFooterRow, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyFooterRow, "mat-footer-row, tr[mat-footer-row]", ["matFooterRow"], {}, {}, never, never, false, never>;
}

/**
 * Footer row definition for the mat-table.
 * Captures the footer row's template and other footer properties such as the columns to display.
 * @deprecated Use `MatFooterRowDef` from `@angular/material/table` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyFooterRowDef extends CdkFooterRowDef {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyFooterRowDef, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyFooterRowDef, "[matFooterRowDef]", never, { "columns": { "alias": "matFooterRowDef"; "required": false; }; "sticky": { "alias": "matFooterRowDefSticky"; "required": false; }; }, {}, never, never, false, never>;
}

/**
 * Header cell template container that adds the right classes and role.
 * @deprecated Use `MatHeaderCell` from `@angular/material/table` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyHeaderCell extends CdkHeaderCell {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyHeaderCell, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyHeaderCell, "mat-header-cell, th[mat-header-cell]", never, {}, {}, never, never, false, never>;
}

/**
 * Header cell definition for the mat-table.
 * Captures the template of a column's header cell and as well as cell-specific properties.
 * @deprecated Use `MatHeaderCellDef` from `@angular/material/table` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyHeaderCellDef extends CdkHeaderCellDef {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyHeaderCellDef, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyHeaderCellDef, "[matHeaderCellDef]", never, {}, {}, never, never, false, never>;
}

/**
 * Header template container that contains the cell outlet. Adds the right class and role.
 * @deprecated Use `MatHeaderRow` from `@angular/material/table` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyHeaderRow extends CdkHeaderRow {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyHeaderRow, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyHeaderRow, "mat-header-row, tr[mat-header-row]", ["matHeaderRow"], {}, {}, never, never, false, never>;
}

/**
 * Header row definition for the mat-table.
 * Captures the header row's template and other header properties such as the columns to display.
 * @deprecated Use `MatHeaderRowDef` from `@angular/material/table` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyHeaderRowDef extends CdkHeaderRowDef {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyHeaderRowDef, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyHeaderRowDef, "[matHeaderRowDef]", never, { "columns": { "alias": "matHeaderRowDef"; "required": false; }; "sticky": { "alias": "matHeaderRowDefSticky"; "required": false; }; }, {}, never, never, false, never>;
}

/**
 * Row that can be used to display a message when no data is shown in the table.
 * @deprecated Use `MatNoDataRow` from `@angular/material/table` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyNoDataRow extends CdkNoDataRow {
    _contentClassName: string;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyNoDataRow, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyNoDataRow, "ng-template[matNoDataRow]", never, {}, {}, never, never, false, never>;
}

/**
 * Enables the recycle view repeater strategy, which reduces rendering latency. Not compatible with
 * tables that animate rows.
 * @deprecated Use `MatRecycleRows` from `@angular/material/table` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyRecycleRows {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyRecycleRows, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyRecycleRows, "mat-table[recycleRows], table[mat-table][recycleRows]", never, {}, {}, never, never, false, never>;
}

/**
 * Data row template container that contains the cell outlet. Adds the right class and role.
 * @deprecated Use `MatRow` from `@angular/material/table` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyRow extends CdkRow {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyRow, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyRow, "mat-row, tr[mat-row]", ["matRow"], {}, {}, never, never, false, never>;
}

/**
 * Data row definition for the mat-table.
 * Captures the data row's template and other properties such as the columns to display and
 * a when predicate that describes when this row should be used.
 * @deprecated Use `MatRowDef` from `@angular/material/table` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyRowDef<T> extends CdkRowDef<T> {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyRowDef<any>, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyRowDef<any>, "[matRowDef]", never, { "columns": { "alias": "matRowDefColumns"; "required": false; }; "when": { "alias": "matRowDefWhen"; "required": false; }; }, {}, never, never, false, never>;
}

/**
 * Wrapper for the CdkTable with Material design styles.
 * @deprecated Use `MatTable` from `@angular/material/table` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyTable<T> extends CdkTable<T> {
    /** Overrides the sticky CSS class set by the `CdkTable`. */
    protected stickyCssClass: string;
    /** Overrides the need to add position: sticky on every sticky cell element in `CdkTable`. */
    protected needsPositionStickyOnElement: boolean;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyTable<any>, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyTable<any>, "mat-table, table[mat-table]", ["matTable"], {}, {}, never, ["caption", "colgroup, col"], false, never>;
}

/**
 * Data source that accepts a client-side data array and includes native support of filtering,
 * sorting (using MatSort), and pagination (using paginator).
 *
 * Allows for sort customization by overriding sortingDataAccessor, which defines how data
 * properties are accessed. Also allows for filter customization by overriding filterPredicate,
 * which defines how row data is converted to a string for filter matching.
 *
 * **Note:** This class is meant to be a simple data source to help you get started. As such
 * it isn't equipped to handle some more advanced cases like robust i18n support or server-side
 * interactions. If your app needs to support more advanced use cases, consider implementing your
 * own `DataSource`.
 *
 * @deprecated Use `MatTableDataSource` from `@angular/material/table` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyTableDataSource<T> extends _MatLegacyTableDataSource<T, MatLegacyPaginator> {
}

export { _MatLegacyTableDataSource }

export { MatLegacyTableDataSourcePageEvent }

export { MatLegacyTableDataSourcePaginator }

/**
 * @deprecated Use `MatTableModule` from `@angular/material/table` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyTableModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyTableModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatLegacyTableModule, [typeof i1.MatLegacyTable, typeof i1.MatLegacyRecycleRows, typeof i2.MatLegacyHeaderCellDef, typeof i3.MatLegacyHeaderRowDef, typeof i2.MatLegacyColumnDef, typeof i2.MatLegacyCellDef, typeof i3.MatLegacyRowDef, typeof i2.MatLegacyFooterCellDef, typeof i3.MatLegacyFooterRowDef, typeof i2.MatLegacyHeaderCell, typeof i2.MatLegacyCell, typeof i2.MatLegacyFooterCell, typeof i3.MatLegacyHeaderRow, typeof i3.MatLegacyRow, typeof i3.MatLegacyFooterRow, typeof i3.MatLegacyNoDataRow, typeof i4.MatLegacyTextColumn], [typeof i5.CdkTableModule, typeof i6.MatCommonModule], [typeof i6.MatCommonModule, typeof i1.MatLegacyTable, typeof i1.MatLegacyRecycleRows, typeof i2.MatLegacyHeaderCellDef, typeof i3.MatLegacyHeaderRowDef, typeof i2.MatLegacyColumnDef, typeof i2.MatLegacyCellDef, typeof i3.MatLegacyRowDef, typeof i2.MatLegacyFooterCellDef, typeof i3.MatLegacyFooterRowDef, typeof i2.MatLegacyHeaderCell, typeof i2.MatLegacyCell, typeof i2.MatLegacyFooterCell, typeof i3.MatLegacyHeaderRow, typeof i3.MatLegacyRow, typeof i3.MatLegacyFooterRow, typeof i3.MatLegacyNoDataRow, typeof i4.MatLegacyTextColumn]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatLegacyTableModule>;
}

/**
 * Column that simply shows text content for the header and row cells. Assumes that the table
 * is using the native table implementation (`<table>`).
 *
 * By default, the name of this column will be the header text and data property accessor.
 * The header text can be overridden with the `headerText` input. Cell values can be overridden with
 * the `dataAccessor` input. Change the text justification to the start or end using the `justify`
 * input.
 *
 * @deprecated Use `MatTextColumn` from `@angular/material/table` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyTextColumn<T> extends CdkTextColumn<T> {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyTextColumn<any>, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyTextColumn<any>, "mat-text-column", never, {}, {}, never, never, false, never>;
}

export { }
