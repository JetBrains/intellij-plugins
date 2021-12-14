/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { ElementRef } from '@angular/core';
import { MediaMarshaller, BaseDirective2, StyleBuilder, StyleUtils } from '@angular/flex-layout/core';
import * as i0 from "@angular/core";
export interface GridRowsParent {
    inline: boolean;
}
export declare class GridRowsStyleBuilder extends StyleBuilder {
    buildStyles(input: string, parent: GridRowsParent): {
        display: string;
        'grid-auto-rows': string;
        'grid-template-rows': string;
    };
    static ɵfac: i0.ɵɵFactoryDeclaration<GridRowsStyleBuilder, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<GridRowsStyleBuilder>;
}
export declare class GridRowsDirective extends BaseDirective2 {
    protected DIRECTIVE_KEY: string;
    get inline(): boolean;
    set inline(val: boolean);
    protected _inline: boolean;
    constructor(elementRef: ElementRef, styleBuilder: GridRowsStyleBuilder, styler: StyleUtils, marshal: MediaMarshaller);
    protected updateWithValue(value: string): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<GridRowsDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<GridRowsDirective, never, never, { "inline": "gdInline"; }, {}, never>;
}
/**
 * 'grid-template-rows' CSS Grid styling directive
 * Configures the sizing for the rows in the grid
 * Syntax: <column value> [auto]
 * @see https://css-tricks.com/snippets/css/complete-guide-grid/#article-header-id-13
 */
export declare class DefaultGridRowsDirective extends GridRowsDirective {
    protected inputs: string[];
    static ɵfac: i0.ɵɵFactoryDeclaration<DefaultGridRowsDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<DefaultGridRowsDirective, "  [gdRows],  [gdRows.xs], [gdRows.sm], [gdRows.md], [gdRows.lg], [gdRows.xl],  [gdRows.lt-sm], [gdRows.lt-md], [gdRows.lt-lg], [gdRows.lt-xl],  [gdRows.gt-xs], [gdRows.gt-sm], [gdRows.gt-md], [gdRows.gt-lg]", never, { "gdRows": "gdRows"; "gdRows.xs": "gdRows.xs"; "gdRows.sm": "gdRows.sm"; "gdRows.md": "gdRows.md"; "gdRows.lg": "gdRows.lg"; "gdRows.xl": "gdRows.xl"; "gdRows.lt-sm": "gdRows.lt-sm"; "gdRows.lt-md": "gdRows.lt-md"; "gdRows.lt-lg": "gdRows.lt-lg"; "gdRows.lt-xl": "gdRows.lt-xl"; "gdRows.gt-xs": "gdRows.gt-xs"; "gdRows.gt-sm": "gdRows.gt-sm"; "gdRows.gt-md": "gdRows.gt-md"; "gdRows.gt-lg": "gdRows.gt-lg"; }, {}, never>;
}
