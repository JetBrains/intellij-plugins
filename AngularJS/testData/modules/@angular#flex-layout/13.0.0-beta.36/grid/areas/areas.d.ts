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
export interface GridAreasParent {
    inline: boolean;
}
export declare class GridAreasStyleBuiler extends StyleBuilder {
    buildStyles(input: string, parent: GridAreasParent): {
        display: string;
        'grid-template-areas': string;
    };
    static ɵfac: i0.ɵɵFactoryDeclaration<GridAreasStyleBuiler, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<GridAreasStyleBuiler>;
}
export declare class GridAreasDirective extends BaseDirective2 {
    protected DIRECTIVE_KEY: string;
    get inline(): boolean;
    set inline(val: boolean);
    protected _inline: boolean;
    constructor(elRef: ElementRef, styleUtils: StyleUtils, styleBuilder: GridAreasStyleBuiler, marshal: MediaMarshaller);
    protected updateWithValue(value: string): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<GridAreasDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<GridAreasDirective, never, never, { "inline": "gdInline"; }, {}, never>;
}
/**
 * 'grid-template-areas' CSS Grid styling directive
 * Configures the names of elements within the grid
 * @see https://css-tricks.com/snippets/css/complete-guide-grid/#article-header-id-14
 */
export declare class DefaultGridAreasDirective extends GridAreasDirective {
    protected inputs: string[];
    static ɵfac: i0.ɵɵFactoryDeclaration<DefaultGridAreasDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<DefaultGridAreasDirective, "  [gdAreas],  [gdAreas.xs], [gdAreas.sm], [gdAreas.md], [gdAreas.lg], [gdAreas.xl],  [gdAreas.lt-sm], [gdAreas.lt-md], [gdAreas.lt-lg], [gdAreas.lt-xl],  [gdAreas.gt-xs], [gdAreas.gt-sm], [gdAreas.gt-md], [gdAreas.gt-lg]", never, { "gdAreas": "gdAreas"; "gdAreas.xs": "gdAreas.xs"; "gdAreas.sm": "gdAreas.sm"; "gdAreas.md": "gdAreas.md"; "gdAreas.lg": "gdAreas.lg"; "gdAreas.xl": "gdAreas.xl"; "gdAreas.lt-sm": "gdAreas.lt-sm"; "gdAreas.lt-md": "gdAreas.lt-md"; "gdAreas.lt-lg": "gdAreas.lt-lg"; "gdAreas.lt-xl": "gdAreas.lt-xl"; "gdAreas.gt-xs": "gdAreas.gt-xs"; "gdAreas.gt-sm": "gdAreas.gt-sm"; "gdAreas.gt-md": "gdAreas.gt-md"; "gdAreas.gt-lg": "gdAreas.gt-lg"; }, {}, never>;
}
