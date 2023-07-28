/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { ElementRef } from '@angular/core';
import { BaseDirective2, StyleUtils, MediaMarshaller, StyleBuilder, StyleDefinition } from '@angular/flex-layout/core';
import * as i0 from "@angular/core";
export declare class GridRowStyleBuilder extends StyleBuilder {
    buildStyles(input: string): {
        'grid-row': string;
    };
    static ɵfac: i0.ɵɵFactoryDeclaration<GridRowStyleBuilder, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<GridRowStyleBuilder>;
}
export declare class GridRowDirective extends BaseDirective2 {
    protected DIRECTIVE_KEY: string;
    constructor(elementRef: ElementRef, styleBuilder: GridRowStyleBuilder, styler: StyleUtils, marshal: MediaMarshaller);
    protected styleCache: Map<string, StyleDefinition>;
    static ɵfac: i0.ɵɵFactoryDeclaration<GridRowDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<GridRowDirective, never, never, {}, {}, never>;
}
/**
 * 'grid-row' CSS Grid styling directive
 * Configures the name or position of an element within the grid
 * @see https://css-tricks.com/snippets/css/complete-guide-grid/#article-header-id-26
 */
export declare class DefaultGridRowDirective extends GridRowDirective {
    protected inputs: string[];
    static ɵfac: i0.ɵɵFactoryDeclaration<DefaultGridRowDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<DefaultGridRowDirective, "  [gdRow],  [gdRow.xs], [gdRow.sm], [gdRow.md], [gdRow.lg], [gdRow.xl],  [gdRow.lt-sm], [gdRow.lt-md], [gdRow.lt-lg], [gdRow.lt-xl],  [gdRow.gt-xs], [gdRow.gt-sm], [gdRow.gt-md], [gdRow.gt-lg]", never, { "gdRow": "gdRow"; "gdRow.xs": "gdRow.xs"; "gdRow.sm": "gdRow.sm"; "gdRow.md": "gdRow.md"; "gdRow.lg": "gdRow.lg"; "gdRow.xl": "gdRow.xl"; "gdRow.lt-sm": "gdRow.lt-sm"; "gdRow.lt-md": "gdRow.lt-md"; "gdRow.lt-lg": "gdRow.lt-lg"; "gdRow.lt-xl": "gdRow.lt-xl"; "gdRow.gt-xs": "gdRow.gt-xs"; "gdRow.gt-sm": "gdRow.gt-sm"; "gdRow.gt-md": "gdRow.gt-md"; "gdRow.gt-lg": "gdRow.gt-lg"; }, {}, never>;
}
