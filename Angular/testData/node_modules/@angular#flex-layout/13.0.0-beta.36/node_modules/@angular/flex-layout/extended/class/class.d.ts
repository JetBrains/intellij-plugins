/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { DoCheck, ElementRef, IterableDiffers, KeyValueDiffers, Renderer2 } from '@angular/core';
import { NgClass } from '@angular/common';
import { BaseDirective2, StyleUtils, MediaMarshaller } from '@angular/flex-layout/core';
import * as i0 from "@angular/core";
export declare class ClassDirective extends BaseDirective2 implements DoCheck {
    protected readonly ngClassInstance: NgClass;
    protected DIRECTIVE_KEY: string;
    /**
     * Capture class assignments so we cache the default classes
     * which are merged with activated styles and used as fallbacks.
     */
    set klass(val: string);
    constructor(elementRef: ElementRef, styler: StyleUtils, marshal: MediaMarshaller, iterableDiffers: IterableDiffers, keyValueDiffers: KeyValueDiffers, renderer2: Renderer2, ngClassInstance: NgClass);
    protected updateWithValue(value: any): void;
    /**
     * For ChangeDetectionStrategy.onPush and ngOnChanges() updates
     */
    ngDoCheck(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<ClassDirective, [null, null, null, null, null, null, { optional: true; self: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<ClassDirective, never, never, { "klass": "class"; }, {}, never>;
}
/**
 * Directive to add responsive support for ngClass.
 * This maintains the core functionality of 'ngClass' and adds responsive API
 * Note: this class is a no-op when rendered on the server
 */
export declare class DefaultClassDirective extends ClassDirective {
    protected inputs: string[];
    static ɵfac: i0.ɵɵFactoryDeclaration<DefaultClassDirective, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<DefaultClassDirective, "  [ngClass], [ngClass.xs], [ngClass.sm], [ngClass.md], [ngClass.lg], [ngClass.xl],  [ngClass.lt-sm], [ngClass.lt-md], [ngClass.lt-lg], [ngClass.lt-xl],  [ngClass.gt-xs], [ngClass.gt-sm], [ngClass.gt-md], [ngClass.gt-lg]", never, { "ngClass": "ngClass"; "ngClass.xs": "ngClass.xs"; "ngClass.sm": "ngClass.sm"; "ngClass.md": "ngClass.md"; "ngClass.lg": "ngClass.lg"; "ngClass.xl": "ngClass.xl"; "ngClass.lt-sm": "ngClass.lt-sm"; "ngClass.lt-md": "ngClass.lt-md"; "ngClass.lt-lg": "ngClass.lt-lg"; "ngClass.lt-xl": "ngClass.lt-xl"; "ngClass.gt-xs": "ngClass.gt-xs"; "ngClass.gt-sm": "ngClass.gt-sm"; "ngClass.gt-md": "ngClass.gt-md"; "ngClass.gt-lg": "ngClass.gt-lg"; }, {}, never>;
}
