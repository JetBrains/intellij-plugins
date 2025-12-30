/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { ElementRef, OnChanges, SimpleChanges, AfterViewInit } from '@angular/core';
import { BaseDirective2, LayoutConfigOptions, MediaMarshaller, StyleUtils, StyleBuilder } from '@angular/flex-layout/core';
import * as i0 from "@angular/core";
export interface ShowHideParent {
    display: string;
    isServer: boolean;
}
export declare class ShowHideStyleBuilder extends StyleBuilder {
    buildStyles(show: string, parent: ShowHideParent): {
        display: string;
    };
    static ɵfac: i0.ɵɵFactoryDeclaration<ShowHideStyleBuilder, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<ShowHideStyleBuilder>;
}
export declare class ShowHideDirective extends BaseDirective2 implements AfterViewInit, OnChanges {
    protected layoutConfig: LayoutConfigOptions;
    protected platformId: Object;
    protected serverModuleLoaded: boolean;
    protected DIRECTIVE_KEY: string;
    /** Original DOM Element CSS display style */
    protected display: string;
    protected hasLayout: boolean;
    protected hasFlexChild: boolean;
    constructor(elementRef: ElementRef, styleBuilder: ShowHideStyleBuilder, styler: StyleUtils, marshal: MediaMarshaller, layoutConfig: LayoutConfigOptions, platformId: Object, serverModuleLoaded: boolean);
    ngAfterViewInit(): void;
    /**
     * On changes to any @Input properties...
     * Default to use the non-responsive Input value ('fxShow')
     * Then conditionally override with the mq-activated Input's current value
     */
    ngOnChanges(changes: SimpleChanges): void;
    /**
     *  Watch for these extra triggers to update fxShow, fxHide stylings
     */
    protected trackExtraTriggers(): void;
    /**
     * Override accessor to the current HTMLElement's `display` style
     * Note: Show/Hide will not change the display to 'flex' but will set it to 'block'
     * unless it was already explicitly specified inline or in a CSS stylesheet.
     */
    protected getDisplayStyle(): string;
    /** Validate the visibility value and then update the host's inline display style */
    protected updateWithValue(value?: boolean | string): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<ShowHideDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<ShowHideDirective, never, never, {}, {}, never>;
}
/**
 * 'show' Layout API directive
 */
export declare class DefaultShowHideDirective extends ShowHideDirective {
    protected inputs: string[];
    static ɵfac: i0.ɵɵFactoryDeclaration<DefaultShowHideDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<DefaultShowHideDirective, "  [fxShow], [fxShow.print],  [fxShow.xs], [fxShow.sm], [fxShow.md], [fxShow.lg], [fxShow.xl],  [fxShow.lt-sm], [fxShow.lt-md], [fxShow.lt-lg], [fxShow.lt-xl],  [fxShow.gt-xs], [fxShow.gt-sm], [fxShow.gt-md], [fxShow.gt-lg],  [fxHide], [fxHide.print],  [fxHide.xs], [fxHide.sm], [fxHide.md], [fxHide.lg], [fxHide.xl],  [fxHide.lt-sm], [fxHide.lt-md], [fxHide.lt-lg], [fxHide.lt-xl],  [fxHide.gt-xs], [fxHide.gt-sm], [fxHide.gt-md], [fxHide.gt-lg]", never, { "fxShow": "fxShow"; "fxShow.print": "fxShow.print"; "fxShow.xs": "fxShow.xs"; "fxShow.sm": "fxShow.sm"; "fxShow.md": "fxShow.md"; "fxShow.lg": "fxShow.lg"; "fxShow.xl": "fxShow.xl"; "fxShow.lt-sm": "fxShow.lt-sm"; "fxShow.lt-md": "fxShow.lt-md"; "fxShow.lt-lg": "fxShow.lt-lg"; "fxShow.lt-xl": "fxShow.lt-xl"; "fxShow.gt-xs": "fxShow.gt-xs"; "fxShow.gt-sm": "fxShow.gt-sm"; "fxShow.gt-md": "fxShow.gt-md"; "fxShow.gt-lg": "fxShow.gt-lg"; "fxHide": "fxHide"; "fxHide.print": "fxHide.print"; "fxHide.xs": "fxHide.xs"; "fxHide.sm": "fxHide.sm"; "fxHide.md": "fxHide.md"; "fxHide.lg": "fxHide.lg"; "fxHide.xl": "fxHide.xl"; "fxHide.lt-sm": "fxHide.lt-sm"; "fxHide.lt-md": "fxHide.lt-md"; "fxHide.lt-lg": "fxHide.lt-lg"; "fxHide.lt-xl": "fxHide.lt-xl"; "fxHide.gt-xs": "fxHide.gt-xs"; "fxHide.gt-sm": "fxHide.gt-sm"; "fxHide.gt-md": "fxHide.gt-md"; "fxHide.gt-lg": "fxHide.gt-lg"; }, {}, never>;
}
