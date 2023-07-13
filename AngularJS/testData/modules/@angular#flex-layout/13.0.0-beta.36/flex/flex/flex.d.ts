/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { ElementRef, OnInit } from '@angular/core';
import { BaseDirective2, LayoutConfigOptions, StyleUtils, StyleBuilder, StyleDefinition, MediaMarshaller, ElementMatcher } from '@angular/flex-layout/core';
import * as i0 from "@angular/core";
interface FlexBuilderParent {
    direction: string;
    hasWrap: boolean;
}
export declare class FlexStyleBuilder extends StyleBuilder {
    protected layoutConfig: LayoutConfigOptions;
    constructor(layoutConfig: LayoutConfigOptions);
    buildStyles(input: string, parent: FlexBuilderParent): StyleDefinition;
    static ɵfac: i0.ɵɵFactoryDeclaration<FlexStyleBuilder, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<FlexStyleBuilder>;
}
/**
 * Directive to control the size of a flex item using flex-basis, flex-grow, and flex-shrink.
 * Corresponds to the css `flex` shorthand property.
 *
 * @see https://css-tricks.com/snippets/css/a-guide-to-flexbox/
 */
export declare class FlexDirective extends BaseDirective2 implements OnInit {
    protected layoutConfig: LayoutConfigOptions;
    protected marshal: MediaMarshaller;
    protected DIRECTIVE_KEY: string;
    protected direction?: string;
    protected wrap?: boolean;
    get shrink(): string;
    set shrink(value: string);
    get grow(): string;
    set grow(value: string);
    protected flexGrow: string;
    protected flexShrink: string;
    constructor(elRef: ElementRef, styleUtils: StyleUtils, layoutConfig: LayoutConfigOptions, styleBuilder: FlexStyleBuilder, marshal: MediaMarshaller);
    ngOnInit(): void;
    /**
     * Caches the parent container's 'flex-direction' and updates the element's style.
     * Used as a handler for layout change events from the parent flex container.
     */
    protected onLayoutChange(matcher: ElementMatcher): void;
    /** Input to this is exclusively the basis input value */
    protected updateWithValue(value: string): void;
    /** Trigger a style reflow, usually based on a shrink/grow input event */
    protected triggerReflow(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<FlexDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<FlexDirective, never, never, { "shrink": "fxShrink"; "grow": "fxGrow"; }, {}, never>;
}
export declare class DefaultFlexDirective extends FlexDirective {
    protected inputs: string[];
    static ɵfac: i0.ɵɵFactoryDeclaration<DefaultFlexDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<DefaultFlexDirective, "  [fxFlex], [fxFlex.xs], [fxFlex.sm], [fxFlex.md],  [fxFlex.lg], [fxFlex.xl], [fxFlex.lt-sm], [fxFlex.lt-md],  [fxFlex.lt-lg], [fxFlex.lt-xl], [fxFlex.gt-xs], [fxFlex.gt-sm],  [fxFlex.gt-md], [fxFlex.gt-lg]", never, { "fxFlex": "fxFlex"; "fxFlex.xs": "fxFlex.xs"; "fxFlex.sm": "fxFlex.sm"; "fxFlex.md": "fxFlex.md"; "fxFlex.lg": "fxFlex.lg"; "fxFlex.xl": "fxFlex.xl"; "fxFlex.lt-sm": "fxFlex.lt-sm"; "fxFlex.lt-md": "fxFlex.lt-md"; "fxFlex.lt-lg": "fxFlex.lt-lg"; "fxFlex.lt-xl": "fxFlex.lt-xl"; "fxFlex.gt-xs": "fxFlex.gt-xs"; "fxFlex.gt-sm": "fxFlex.gt-sm"; "fxFlex.gt-md": "fxFlex.gt-md"; "fxFlex.gt-lg": "fxFlex.gt-lg"; }, {}, never>;
}
export {};
