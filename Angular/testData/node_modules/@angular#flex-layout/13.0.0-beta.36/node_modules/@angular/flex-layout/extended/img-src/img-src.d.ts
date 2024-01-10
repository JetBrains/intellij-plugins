/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { ElementRef } from '@angular/core';
import { MediaMarshaller, BaseDirective2, StyleBuilder, StyleDefinition, StyleUtils } from '@angular/flex-layout/core';
import * as i0 from "@angular/core";
export declare class ImgSrcStyleBuilder extends StyleBuilder {
    buildStyles(url: string): {
        content: string;
    };
    static ɵfac: i0.ɵɵFactoryDeclaration<ImgSrcStyleBuilder, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<ImgSrcStyleBuilder>;
}
export declare class ImgSrcDirective extends BaseDirective2 {
    protected platformId: Object;
    protected serverModuleLoaded: boolean;
    protected DIRECTIVE_KEY: string;
    protected defaultSrc: string;
    set src(val: string);
    constructor(elementRef: ElementRef, styleBuilder: ImgSrcStyleBuilder, styler: StyleUtils, marshal: MediaMarshaller, platformId: Object, serverModuleLoaded: boolean);
    /**
     * Use the [responsively] activated input value to update
     * the host img src attribute or assign a default `img.src=''`
     * if the src has not been defined.
     *
     * Do nothing to standard `<img src="">` usages, only when responsive
     * keys are present do we actually call `setAttribute()`
     */
    protected updateWithValue(value?: string): void;
    protected styleCache: Map<string, StyleDefinition>;
    static ɵfac: i0.ɵɵFactoryDeclaration<ImgSrcDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<ImgSrcDirective, never, never, { "src": "src"; }, {}, never>;
}
/**
 * This directive provides a responsive API for the HTML <img> 'src' attribute
 * and will update the img.src property upon each responsive activation.
 *
 * e.g.
 *      <img src="defaultScene.jpg" src.xs="mobileScene.jpg"></img>
 *
 * @see https://css-tricks.com/responsive-images-youre-just-changing-resolutions-use-src/
 */
export declare class DefaultImgSrcDirective extends ImgSrcDirective {
    protected inputs: string[];
    static ɵfac: i0.ɵɵFactoryDeclaration<DefaultImgSrcDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<DefaultImgSrcDirective, "  img[src.xs],    img[src.sm],    img[src.md],    img[src.lg],   img[src.xl],  img[src.lt-sm], img[src.lt-md], img[src.lt-lg], img[src.lt-xl],  img[src.gt-xs], img[src.gt-sm], img[src.gt-md], img[src.gt-lg]", never, { "src.xs": "src.xs"; "src.sm": "src.sm"; "src.md": "src.md"; "src.lg": "src.lg"; "src.xl": "src.xl"; "src.lt-sm": "src.lt-sm"; "src.lt-md": "src.lt-md"; "src.lt-lg": "src.lt-lg"; "src.lt-xl": "src.lt-xl"; "src.gt-xs": "src.gt-xs"; "src.gt-sm": "src.gt-sm"; "src.gt-md": "src.gt-md"; "src.gt-lg": "src.gt-lg"; }, {}, never>;
}
