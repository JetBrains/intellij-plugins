/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */

import {
    ChangeDetectionStrategy,
    Component,
    Directive,
    IterableChanges,
    IterableDiffer,
    IterableDiffers,
    OnChanges,
    OnDestroy,
    SimpleChanges,
    TemplateRef,
    ViewContainerRef,
    ViewEncapsulation,
} from '@angular/core';
import {CanStick, CanStickCtor, mixinHasStickyInput} from './can-stick';
import {CdkCellDef, CdkColumnDef} from './cell';

/**
 * The row template that can be used by the mat-table. Should not be used outside of the
 * material library.
 */
export const CDK_ROW_TEMPLATE = `<ng-container cdkCellOutlet></ng-container>`;

/**
 * Base class for the CdkHeaderRowDef and CdkRowDef that handles checking their columns inputs
 * for changes and notifying the table.
 */
export abstract class BaseRowDef implements OnChanges {
  /** The columns to be displayed on this row. */
  columns: Iterable<string>;

  /** Differ used to check if any changes were made to the columns. */
  protected _columnsDiffer: IterableDiffer<any>;

  constructor(/** @docs-private */ public template: TemplateRef<any>,
              protected _differs: IterableDiffers) { }

  ngOnChanges(changes: SimpleChanges): void {
    // Create a new columns differ if one does not yet exist. Initialize it based on initial value
    // of the columns property or an empty array if none is provided.
    if (!this._columnsDiffer) {
      const columns = (changes['columns'] && changes['columns'].currentValue) || [];
      this._columnsDiffer = this._differs.find(columns).create();
      this._columnsDiffer.diff(columns);
    }
  }

  /**
   * Returns the difference between the current columns and the columns from the last diff, or null
   * if there is no difference.
   */
  getColumnsDiff(): IterableChanges<any> | null {
    return this._columnsDiffer.diff(this.columns);
  }

  /** Gets this row def's relevant cell template from the provided column def. */
  extractCellTemplate(column: CdkColumnDef): TemplateRef<any> {
    if (this instanceof CdkHeaderRowDef) {
      return column.headerCell.template;
    } if (this instanceof CdkFooterRowDef) {
      return column.footerCell.template;
    } else {
      return column.cell.template;
    }
  }
}

// Boilerplate for applying mixins to CdkHeaderRowDef.
/** @docs-private */
export class CdkHeaderRowDefBase extends BaseRowDef {}
export const _CdkHeaderRowDefBase: CanStickCtor & typeof CdkHeaderRowDefBase =
    mixinHasStickyInput(CdkHeaderRowDefBase);

/**
 * Header row definition for the CDK table.
 * Captures the header row's template and other header properties such as the columns to display.
 */
@Directive({
  selector: '[cdkHeaderRowDef]',
  inputs: ['columns: cdkHeaderRowDef', 'sticky: cdkHeaderRowDefSticky'],
})
export class CdkHeaderRowDef extends _CdkHeaderRowDefBase implements CanStick, OnChanges {
  constructor(template: TemplateRef<any>, _differs: IterableDiffers) {
    super(template, _differs);
  }

  // Prerender fails to recognize that ngOnChanges in a part of this class through inheritance.
  // Explicitly define it so that the method is called as part of the Angular lifecycle.
  ngOnChanges(changes: SimpleChanges): void {
    super.ngOnChanges(changes);
  }
}

/** Context provided to the row cells when `multiTemplateDataRows` is false */
export interface CdkCellOutletRowContext<T> {
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
export interface CdkCellOutletMultiRowContext<T> {
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
