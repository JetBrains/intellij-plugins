/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { DoCheck, ElementRef, KeyValueDiffers, Renderer2 } from '@angular/core';
import { NgStyle } from '@angular/common';
import { DomSanitizer } from '@angular/platform-browser';
import { BaseDirective2, StyleUtils, MediaMarshaller } from '@angular/flex-layout/core';
import { NgStyleType, NgStyleMap } from './style-transforms';
import * as i0 from "@angular/core";
export declare class StyleDirective extends BaseDirective2 implements DoCheck {
    protected sanitizer: DomSanitizer;
    private readonly ngStyleInstance;
    protected DIRECTIVE_KEY: string;
    protected fallbackStyles: NgStyleMap;
    protected isServer: boolean;
    constructor(elementRef: ElementRef, styler: StyleUtils, marshal: MediaMarshaller, sanitizer: DomSanitizer, differs: KeyValueDiffers, renderer2: Renderer2, ngStyleInstance: NgStyle, serverLoaded: boolean, platformId: Object);
    /** Add generated styles */
    protected updateWithValue(value: any): void;
    /** Remove generated styles */
    protected clearStyles(): void;
    /**
     * Convert raw strings to ngStyleMap; which is required by ngStyle
     * NOTE: Raw string key-value pairs MUST be delimited by `;`
     *       Comma-delimiters are not supported due to complexities of
     *       possible style values such as `rgba(x,x,x,x)` and others
     */
    protected buildStyleMap(styles: NgStyleType): NgStyleMap;
    /** For ChangeDetectionStrategy.onPush and ngOnChanges() updates */
    ngDoCheck(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<StyleDirective, [null, null, null, null, null, null, { optional: true; self: true; }, null, null]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<StyleDirective, never, never, {}, {}, never>;
}
/**
 * Directive to add responsive support for ngStyle.
 *
 */
export declare class DefaultStyleDirective extends StyleDirective implements DoCheck {
    protected inputs: string[];
    static ɵfac: i0.ɵɵFactoryDeclaration<DefaultStyleDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<DefaultStyleDirective, "  [ngStyle],  [ngStyle.xs], [ngStyle.sm], [ngStyle.md], [ngStyle.lg], [ngStyle.xl],  [ngStyle.lt-sm], [ngStyle.lt-md], [ngStyle.lt-lg], [ngStyle.lt-xl],  [ngStyle.gt-xs], [ngStyle.gt-sm], [ngStyle.gt-md], [ngStyle.gt-lg]", never, { "ngStyle": "ngStyle"; "ngStyle.xs": "ngStyle.xs"; "ngStyle.sm": "ngStyle.sm"; "ngStyle.md": "ngStyle.md"; "ngStyle.lg": "ngStyle.lg"; "ngStyle.xl": "ngStyle.xl"; "ngStyle.lt-sm": "ngStyle.lt-sm"; "ngStyle.lt-md": "ngStyle.lt-md"; "ngStyle.lt-lg": "ngStyle.lt-lg"; "ngStyle.lt-xl": "ngStyle.lt-xl"; "ngStyle.gt-xs": "ngStyle.gt-xs"; "ngStyle.gt-sm": "ngStyle.gt-sm"; "ngStyle.gt-md": "ngStyle.gt-md"; "ngStyle.gt-lg": "ngStyle.gt-lg"; }, {}, never>;
}
