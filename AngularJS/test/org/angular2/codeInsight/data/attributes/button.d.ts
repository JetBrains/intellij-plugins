/**
 * @license
 * Copyright Google Inc. All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import {ElementRef, OnDestroy, Renderer2} from '@angular/core';
import {Platform} from '@angular/cdk/platform';
import {CanColor, CanDisable, CanDisableRipple} from './color';
import {FocusMonitor} from '@angular/cdk/a11y';

/**
 * Directive whose purpose is to add the mat- CSS styling to this selector.
 * @docs-private
 */
export declare class MatButtonCssMatStyler {
}
/**
 * Directive whose purpose is to add the mat- CSS styling to this selector.
 * @docs-private
 */
export declare class MatRaisedButtonCssMatStyler {
}
/**
 * Directive whose purpose is to add the mat- CSS styling to this selector.
 * @docs-private
 */
export declare class MatIconButtonCssMatStyler {
}
/**
 * Directive whose purpose is to add the mat- CSS styling to this selector.
 * @docs-private
 */
export declare class MatFab {
    constructor(button: MatButton, anchor: MatAnchor);
}
/**
 * Directive that targets mini-fab buttons and anchors. It's used to apply the `mat-` class
 * to all mini-fab buttons and also is responsible for setting the default color palette.
 * @docs-private
 */
export declare class MatMiniFab {
    constructor(button: MatButton, anchor: MatAnchor);
}
/** @docs-private */
export declare class MatButtonBase {
    _renderer: Renderer2;
    _elementRef: ElementRef;
    constructor(_renderer: Renderer2, _elementRef: ElementRef);
}
export declare const _MatButtonMixinBase: (new (...args: any[]) => CanColor) & (new (...args: any[]) => CanDisable) & (new (...args: any[]) => CanDisableRipple) & typeof MatButtonBase;
/**
 * Material design button.
 */
export declare class MatButton extends _MatButtonMixinBase implements OnDestroy, CanDisable, CanColor, CanDisableRipple {
    private _platform;
    private _focusMonitor;
    /** Whether the button is round. */
    _isRoundButton: boolean;
    /** Whether the button is icon button. */
    _isIconButton: boolean;
    constructor(renderer: Renderer2, elementRef: ElementRef, _platform: Platform, _focusMonitor: FocusMonitor);
    ngOnDestroy(): void;
    /** Focuses the button. */
    focus(): void;
    _getHostElement(): any;
    _isRippleDisabled(): boolean;
    /** Gets whether the button has one of the given attributes with a 'mat-' prefix. */
    _hasAttributeWithPrefix(...unprefixedAttributeNames: string[]): boolean;
}
/**
 * Raised Material design button.
 */
export declare class MatAnchor extends MatButton {
    constructor(platform: Platform, focusMonitor: FocusMonitor, elementRef: ElementRef, renderer: Renderer2);
    _haltDisabledEvents(event: Event): void;
}
