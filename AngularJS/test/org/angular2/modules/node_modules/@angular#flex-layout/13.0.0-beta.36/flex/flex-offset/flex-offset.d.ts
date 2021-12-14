/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { ElementRef, OnChanges } from '@angular/core';
import { Directionality } from '@angular/cdk/bidi';
import { MediaMarshaller, BaseDirective2, StyleBuilder, StyleDefinition, StyleUtils } from '@angular/flex-layout/core';
import * as i0 from "@angular/core";
export interface FlexOffsetParent {
    layout: string;
    isRtl: boolean;
}
export declare class FlexOffsetStyleBuilder extends StyleBuilder {
    buildStyles(offset: string, parent: FlexOffsetParent): StyleDefinition;
    static ɵfac: i0.ɵɵFactoryDeclaration<FlexOffsetStyleBuilder, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<FlexOffsetStyleBuilder>;
}
/**
 * 'flex-offset' flexbox styling directive
 * Configures the 'margin-left' of the element in a layout container
 */
export declare class FlexOffsetDirective extends BaseDirective2 implements OnChanges {
    protected directionality: Directionality;
    protected DIRECTIVE_KEY: string;
    constructor(elRef: ElementRef, directionality: Directionality, styleBuilder: FlexOffsetStyleBuilder, marshal: MediaMarshaller, styler: StyleUtils);
    /**
     * Using the current fxFlexOffset value, update the inline CSS
     * NOTE: this will assign `margin-left` if the parent flex-direction == 'row',
     *       otherwise `margin-top` is used for the offset.
     */
    protected updateWithValue(value?: string | number): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<FlexOffsetDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<FlexOffsetDirective, never, never, {}, {}, never>;
}
export declare class DefaultFlexOffsetDirective extends FlexOffsetDirective {
    protected inputs: string[];
    static ɵfac: i0.ɵɵFactoryDeclaration<DefaultFlexOffsetDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<DefaultFlexOffsetDirective, "  [fxFlexOffset], [fxFlexOffset.xs], [fxFlexOffset.sm], [fxFlexOffset.md],  [fxFlexOffset.lg], [fxFlexOffset.xl], [fxFlexOffset.lt-sm], [fxFlexOffset.lt-md],  [fxFlexOffset.lt-lg], [fxFlexOffset.lt-xl], [fxFlexOffset.gt-xs], [fxFlexOffset.gt-sm],  [fxFlexOffset.gt-md], [fxFlexOffset.gt-lg]", never, { "fxFlexOffset": "fxFlexOffset"; "fxFlexOffset.xs": "fxFlexOffset.xs"; "fxFlexOffset.sm": "fxFlexOffset.sm"; "fxFlexOffset.md": "fxFlexOffset.md"; "fxFlexOffset.lg": "fxFlexOffset.lg"; "fxFlexOffset.xl": "fxFlexOffset.xl"; "fxFlexOffset.lt-sm": "fxFlexOffset.lt-sm"; "fxFlexOffset.lt-md": "fxFlexOffset.lt-md"; "fxFlexOffset.lt-lg": "fxFlexOffset.lt-lg"; "fxFlexOffset.lt-xl": "fxFlexOffset.lt-xl"; "fxFlexOffset.gt-xs": "fxFlexOffset.gt-xs"; "fxFlexOffset.gt-sm": "fxFlexOffset.gt-sm"; "fxFlexOffset.gt-md": "fxFlexOffset.gt-md"; "fxFlexOffset.gt-lg": "fxFlexOffset.gt-lg"; }, {}, never>;
}
