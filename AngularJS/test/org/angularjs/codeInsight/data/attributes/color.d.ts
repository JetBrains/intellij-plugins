/**
 * @license
 * Copyright Google Inc. All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { Constructor } from './constructor';
import { ElementRef, Renderer2 } from '@angular/core';
/** @docs-private */
export interface CanColor {
    color: ThemePalette;
}
/** @docs-private */
export interface HasRenderer {
    _renderer: Renderer2;
    _elementRef: ElementRef;
}
/** Possible color palette values.  */
export declare type ThemePalette = 'primary' | 'accent' | 'warn' | undefined;
/** Mixin to augment a directive with a `color` property. */
export declare function mixinColor<T extends Constructor<HasRenderer>>(base: T, defaultColor?: ThemePalette): Constructor<CanColor> & T;
