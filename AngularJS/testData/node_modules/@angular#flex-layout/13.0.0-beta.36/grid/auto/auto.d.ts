/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { ElementRef } from '@angular/core';
import { BaseDirective2, StyleUtils, StyleBuilder, MediaMarshaller } from '@angular/flex-layout/core';
import * as i0 from "@angular/core";
export interface GridAutoParent {
    inline: boolean;
}
export declare class GridAutoStyleBuilder extends StyleBuilder {
    buildStyles(input: string, parent: GridAutoParent): {
        display: string;
        'grid-auto-flow': string;
    };
    static ɵfac: i0.ɵɵFactoryDeclaration<GridAutoStyleBuilder, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<GridAutoStyleBuilder>;
}
export declare class GridAutoDirective extends BaseDirective2 {
    get inline(): boolean;
    set inline(val: boolean);
    protected _inline: boolean;
    protected DIRECTIVE_KEY: string;
    constructor(elementRef: ElementRef, styleBuilder: GridAutoStyleBuilder, styler: StyleUtils, marshal: MediaMarshaller);
    protected updateWithValue(value: string): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<GridAutoDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<GridAutoDirective, never, never, { "inline": "gdInline"; }, {}, never>;
}
/**
 * 'grid-auto-flow' CSS Grid styling directive
 * Configures the auto placement algorithm for the grid
 * @see https://css-tricks.com/snippets/css/complete-guide-grid/#article-header-id-23
 */
export declare class DefaultGridAutoDirective extends GridAutoDirective {
    protected inputs: string[];
    static ɵfac: i0.ɵɵFactoryDeclaration<DefaultGridAutoDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<DefaultGridAutoDirective, "  [gdAuto],  [gdAuto.xs], [gdAuto.sm], [gdAuto.md], [gdAuto.lg], [gdAuto.xl],  [gdAuto.lt-sm], [gdAuto.lt-md], [gdAuto.lt-lg], [gdAuto.lt-xl],  [gdAuto.gt-xs], [gdAuto.gt-sm], [gdAuto.gt-md], [gdAuto.gt-lg]", never, { "gdAuto": "gdAuto"; "gdAuto.xs": "gdAuto.xs"; "gdAuto.sm": "gdAuto.sm"; "gdAuto.md": "gdAuto.md"; "gdAuto.lg": "gdAuto.lg"; "gdAuto.xl": "gdAuto.xl"; "gdAuto.lt-sm": "gdAuto.lt-sm"; "gdAuto.lt-md": "gdAuto.lt-md"; "gdAuto.lt-lg": "gdAuto.lt-lg"; "gdAuto.lt-xl": "gdAuto.lt-xl"; "gdAuto.gt-xs": "gdAuto.gt-xs"; "gdAuto.gt-sm": "gdAuto.gt-sm"; "gdAuto.gt-md": "gdAuto.gt-md"; "gdAuto.gt-lg": "gdAuto.gt-lg"; }, {}, never>;
}
