/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { ElementRef, OnChanges } from '@angular/core';
import { BaseDirective2, StyleBuilder, StyleDefinition, StyleUtils, MediaMarshaller } from '@angular/flex-layout/core';
import * as i0 from "@angular/core";
export declare class LayoutStyleBuilder extends StyleBuilder {
    buildStyles(input: string): {
        display: string;
        'box-sizing': string;
        'flex-direction': string;
        'flex-wrap': string | null;
    };
    static ɵfac: i0.ɵɵFactoryDeclaration<LayoutStyleBuilder, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<LayoutStyleBuilder>;
}
/**
 * 'layout' flexbox styling directive
 * Defines the positioning flow direction for the child elements: row or column
 * Optional values: column or row (default)
 * @see https://css-tricks.com/almanac/properties/f/flex-direction/
 *
 */
export declare class LayoutDirective extends BaseDirective2 implements OnChanges {
    protected DIRECTIVE_KEY: string;
    constructor(elRef: ElementRef, styleUtils: StyleUtils, styleBuilder: LayoutStyleBuilder, marshal: MediaMarshaller);
    protected styleCache: Map<string, StyleDefinition>;
    static ɵfac: i0.ɵɵFactoryDeclaration<LayoutDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<LayoutDirective, never, never, {}, {}, never>;
}
export declare class DefaultLayoutDirective extends LayoutDirective {
    protected inputs: string[];
    static ɵfac: i0.ɵɵFactoryDeclaration<DefaultLayoutDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<DefaultLayoutDirective, "  [fxLayout], [fxLayout.xs], [fxLayout.sm], [fxLayout.md],  [fxLayout.lg], [fxLayout.xl], [fxLayout.lt-sm], [fxLayout.lt-md],  [fxLayout.lt-lg], [fxLayout.lt-xl], [fxLayout.gt-xs], [fxLayout.gt-sm],  [fxLayout.gt-md], [fxLayout.gt-lg]", never, { "fxLayout": "fxLayout"; "fxLayout.xs": "fxLayout.xs"; "fxLayout.sm": "fxLayout.sm"; "fxLayout.md": "fxLayout.md"; "fxLayout.lg": "fxLayout.lg"; "fxLayout.xl": "fxLayout.xl"; "fxLayout.lt-sm": "fxLayout.lt-sm"; "fxLayout.lt-md": "fxLayout.lt-md"; "fxLayout.lt-lg": "fxLayout.lt-lg"; "fxLayout.lt-xl": "fxLayout.lt-xl"; "fxLayout.gt-xs": "fxLayout.gt-xs"; "fxLayout.gt-sm": "fxLayout.gt-sm"; "fxLayout.gt-md": "fxLayout.gt-md"; "fxLayout.gt-lg": "fxLayout.gt-lg"; }, {}, never>;
}
