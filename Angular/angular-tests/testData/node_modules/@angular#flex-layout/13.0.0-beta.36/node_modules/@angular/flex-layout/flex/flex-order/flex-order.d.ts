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
export declare class FlexOrderStyleBuilder extends StyleBuilder {
    buildStyles(value: string): {
        order: string | number;
    };
    static ɵfac: i0.ɵɵFactoryDeclaration<FlexOrderStyleBuilder, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<FlexOrderStyleBuilder>;
}
/**
 * 'flex-order' flexbox styling directive
 * Configures the positional ordering of the element in a sorted layout container
 * @see https://css-tricks.com/almanac/properties/o/order/
 */
export declare class FlexOrderDirective extends BaseDirective2 implements OnChanges {
    protected DIRECTIVE_KEY: string;
    constructor(elRef: ElementRef, styleUtils: StyleUtils, styleBuilder: FlexOrderStyleBuilder, marshal: MediaMarshaller);
    protected styleCache: Map<string, StyleDefinition>;
    static ɵfac: i0.ɵɵFactoryDeclaration<FlexOrderDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<FlexOrderDirective, never, never, {}, {}, never>;
}
export declare class DefaultFlexOrderDirective extends FlexOrderDirective {
    protected inputs: string[];
    static ɵfac: i0.ɵɵFactoryDeclaration<DefaultFlexOrderDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<DefaultFlexOrderDirective, "  [fxFlexOrder], [fxFlexOrder.xs], [fxFlexOrder.sm], [fxFlexOrder.md],  [fxFlexOrder.lg], [fxFlexOrder.xl], [fxFlexOrder.lt-sm], [fxFlexOrder.lt-md],  [fxFlexOrder.lt-lg], [fxFlexOrder.lt-xl], [fxFlexOrder.gt-xs], [fxFlexOrder.gt-sm],  [fxFlexOrder.gt-md], [fxFlexOrder.gt-lg]", never, { "fxFlexOrder": "fxFlexOrder"; "fxFlexOrder.xs": "fxFlexOrder.xs"; "fxFlexOrder.sm": "fxFlexOrder.sm"; "fxFlexOrder.md": "fxFlexOrder.md"; "fxFlexOrder.lg": "fxFlexOrder.lg"; "fxFlexOrder.xl": "fxFlexOrder.xl"; "fxFlexOrder.lt-sm": "fxFlexOrder.lt-sm"; "fxFlexOrder.lt-md": "fxFlexOrder.lt-md"; "fxFlexOrder.lt-lg": "fxFlexOrder.lt-lg"; "fxFlexOrder.lt-xl": "fxFlexOrder.lt-xl"; "fxFlexOrder.gt-xs": "fxFlexOrder.gt-xs"; "fxFlexOrder.gt-sm": "fxFlexOrder.gt-sm"; "fxFlexOrder.gt-md": "fxFlexOrder.gt-md"; "fxFlexOrder.gt-lg": "fxFlexOrder.gt-lg"; }, {}, never>;
}
