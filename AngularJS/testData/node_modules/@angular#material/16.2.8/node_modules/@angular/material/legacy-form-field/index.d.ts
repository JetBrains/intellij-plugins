import { _AbstractConstructor } from '@angular/material/core';
import { AbstractControlDirective } from '@angular/forms';
import { AfterContentChecked } from '@angular/core';
import { AfterContentInit } from '@angular/core';
import { AfterViewInit } from '@angular/core';
import { BooleanInput } from '@angular/cdk/coercion';
import { CanColor } from '@angular/material/core';
import { ChangeDetectorRef } from '@angular/core';
import { _Constructor } from '@angular/material/core';
import { Directionality } from '@angular/cdk/bidi';
import { ElementRef } from '@angular/core';
import { getMatFormFieldDuplicatedHintError as getMatLegacyFormFieldDuplicatedHintError } from '@angular/material/form-field';
import { getMatFormFieldMissingControlError as getMatLegacyFormFieldMissingControlError } from '@angular/material/form-field';
import { getMatFormFieldPlaceholderConflictError as getMatLegacyFormFieldPlaceholderConflictError } from '@angular/material/form-field';
import * as i0 from '@angular/core';
import * as i10 from '@angular/cdk/observers';
import * as i8 from '@angular/common';
import * as i9 from '@angular/material/core';
import { InjectionToken } from '@angular/core';
import { MAT_ERROR as MAT_LEGACY_ERROR } from '@angular/material/form-field';
import { MAT_FORM_FIELD as MAT_LEGACY_FORM_FIELD } from '@angular/material/form-field';
import { MAT_PREFIX as MAT_LEGACY_PREFIX } from '@angular/material/form-field';
import { MAT_SUFFIX as MAT_LEGACY_SUFFIX } from '@angular/material/form-field';
import { matFormFieldAnimations as matLegacyFormFieldAnimations } from '@angular/material/form-field';
import { MatFormFieldControl as MatLegacyFormFieldControl } from '@angular/material/form-field';
import { NgZone } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { Platform } from '@angular/cdk/platform';
import { QueryList } from '@angular/core';
import { ThemePalette } from '@angular/material/core';

export { getMatLegacyFormFieldDuplicatedHintError }

export { getMatLegacyFormFieldMissingControlError }

export { getMatLegacyFormFieldPlaceholderConflictError }

declare namespace i1 {
    export {
        MatLegacyError
    }
}

declare namespace i2 {
    export {
        MatLegacyFormFieldAppearance,
        LegacyFloatLabelType,
        MatLegacyFormFieldDefaultOptions,
        MAT_LEGACY_FORM_FIELD_DEFAULT_OPTIONS,
        MatLegacyFormField
    }
}

declare namespace i3 {
    export {
        _MAT_LEGACY_HINT,
        MatLegacyHint
    }
}

declare namespace i4 {
    export {
        MatLegacyLabel
    }
}

declare namespace i5 {
    export {
        MatLegacyPlaceholder
    }
}

declare namespace i6 {
    export {
        MatLegacyPrefix
    }
}

declare namespace i7 {
    export {
        MatLegacySuffix
    }
}

/**
 * Possible values for the "floatLabel" form field input.
 * @deprecated Use `FloatLabelType` from `@angular/material/form-field` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare type LegacyFloatLabelType = 'always' | 'never' | 'auto';

export { MAT_LEGACY_ERROR }

export { MAT_LEGACY_FORM_FIELD }

/**
 * Injection token that can be used to configure the
 * default options for all form field within an app.
 * @deprecated Use `MAT_FORM_FIELD_DEFAULT_OPTIONS` from `@angular/material/form-field` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare const MAT_LEGACY_FORM_FIELD_DEFAULT_OPTIONS: InjectionToken<MatLegacyFormFieldDefaultOptions>;

/**
 * Injection token that can be used to reference instances of `MatHint`. It serves as
 * alternative token to the actual `MatHint` class which could cause unnecessary
 * retention of the class and its directive metadata.
 *
 * *Note*: This is not part of the public API as the MDC-based form-field will not
 * need a lightweight token for `MatHint` and we want to reduce breaking changes.
 *
 * @deprecated Use `_MAT_HINT` from `@angular/material/form-field` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare const _MAT_LEGACY_HINT: InjectionToken<MatLegacyHint>;

export { MAT_LEGACY_PREFIX }

export { MAT_LEGACY_SUFFIX }

/**
 * Boilerplate for applying mixins to MatFormField.
 * @docs-private
 */
declare const _MatFormFieldBase: _Constructor<CanColor> & _AbstractConstructor<CanColor> & {
    new (_elementRef: ElementRef): {
        _elementRef: ElementRef;
    };
};

/**
 * Single error message to be shown underneath the form field.
 * @deprecated Use `MatError` from `@angular/material/form-field` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyError {
    id: string;
    constructor(ariaLive: string, elementRef: ElementRef);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyError, [{ attribute: "aria-live"; }, null]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyError, "mat-error", never, { "id": { "alias": "id"; "required": false; }; }, {}, never, never, false, never>;
}

/**
 * Container for form controls that applies Material Design styling and behavior.
 * @deprecated Use `MatFormField` from `@angular/material/form-field` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyFormField extends _MatFormFieldBase implements AfterContentInit, AfterContentChecked, AfterViewInit, OnDestroy, CanColor {
    private _changeDetectorRef;
    private _dir;
    private _defaults;
    private _platform;
    private _ngZone;
    /**
     * Whether the outline gap needs to be calculated
     * immediately on the next change detection run.
     */
    private _outlineGapCalculationNeededImmediately;
    /** Whether the outline gap needs to be calculated next time the zone has stabilized. */
    private _outlineGapCalculationNeededOnStable;
    private readonly _destroyed;
    /** The form field appearance style. */
    get appearance(): MatLegacyFormFieldAppearance;
    set appearance(value: MatLegacyFormFieldAppearance);
    _appearance: MatLegacyFormFieldAppearance;
    /** Whether the required marker should be hidden. */
    get hideRequiredMarker(): boolean;
    set hideRequiredMarker(value: BooleanInput);
    private _hideRequiredMarker;
    /** Override for the logic that disables the label animation in certain cases. */
    private _showAlwaysAnimate;
    /** Whether the floating label should always float or not. */
    _shouldAlwaysFloat(): boolean;
    /** Whether the label can float or not. */
    _canLabelFloat(): boolean;
    /** State of the mat-hint and mat-error animations. */
    _subscriptAnimationState: string;
    /** Text for the form field hint. */
    get hintLabel(): string;
    set hintLabel(value: string);
    private _hintLabel;
    readonly _hintLabelId: string;
    readonly _labelId: string;
    /**
     * Whether the label should always float, never float or float as the user types.
     *
     * Note: only the legacy appearance supports the `never` option. `never` was originally added as a
     * way to make the floating label emulate the behavior of a standard input placeholder. However
     * the form field now supports both floating labels and placeholders. Therefore in the non-legacy
     * appearances the `never` option has been disabled in favor of just using the placeholder.
     */
    get floatLabel(): LegacyFloatLabelType;
    set floatLabel(value: LegacyFloatLabelType);
    private _floatLabel;
    /** Whether the Angular animations are enabled. */
    _animationsEnabled: boolean;
    _connectionContainerRef: ElementRef;
    _inputContainerRef: ElementRef;
    private _label;
    _controlNonStatic: MatLegacyFormFieldControl<any>;
    _controlStatic: MatLegacyFormFieldControl<any>;
    get _control(): MatLegacyFormFieldControl<any>;
    set _control(value: MatLegacyFormFieldControl<any>);
    private _explicitFormFieldControl;
    _labelChildNonStatic: MatLegacyLabel;
    _labelChildStatic: MatLegacyLabel;
    _placeholderChild: MatLegacyPlaceholder;
    _errorChildren: QueryList<MatLegacyError>;
    _hintChildren: QueryList<MatLegacyHint>;
    _prefixChildren: QueryList<MatLegacyPrefix>;
    _suffixChildren: QueryList<MatLegacySuffix>;
    constructor(elementRef: ElementRef, _changeDetectorRef: ChangeDetectorRef, _dir: Directionality, _defaults: MatLegacyFormFieldDefaultOptions, _platform: Platform, _ngZone: NgZone, _animationMode: string);
    /**
     * Gets the id of the label element. If no label is present, returns `null`.
     */
    getLabelId(): string | null;
    /**
     * Gets an ElementRef for the element that a overlay attached to the form field should be
     * positioned relative to.
     */
    getConnectedOverlayOrigin(): ElementRef;
    ngAfterContentInit(): void;
    ngAfterContentChecked(): void;
    ngAfterViewInit(): void;
    ngOnDestroy(): void;
    /**
     * Determines whether a class from the AbstractControlDirective
     * should be forwarded to the host element.
     */
    _shouldForward(prop: keyof AbstractControlDirective): boolean;
    _hasPlaceholder(): boolean;
    _hasLabel(): boolean;
    _shouldLabelFloat(): boolean;
    _hideControlPlaceholder(): boolean;
    _hasFloatingLabel(): boolean;
    /** Determines whether to display hints or errors. */
    _getDisplayedMessages(): 'error' | 'hint';
    /** Animates the placeholder up and locks it in position. */
    _animateAndLockLabel(): void;
    /**
     * Ensure that there is only one placeholder (either `placeholder` attribute on the child control
     * or child element with the `mat-placeholder` directive).
     */
    private _validatePlaceholders;
    /** Does any extra processing that is required when handling the hints. */
    private _processHints;
    /**
     * Ensure that there is a maximum of one of each `<mat-hint>` alignment specified, with the
     * attribute being considered as `align="start"`.
     */
    private _validateHints;
    /** Gets the default float label state. */
    private _getDefaultFloatLabelState;
    /**
     * Sets the list of element IDs that describe the child control. This allows the control to update
     * its `aria-describedby` attribute accordingly.
     */
    private _syncDescribedByIds;
    /** Throws an error if the form field's control is missing. */
    protected _validateControlChild(): void;
    /**
     * Updates the width and position of the gap in the outline. Only relevant for the outline
     * appearance.
     */
    updateOutlineGap(): void;
    /** Gets the start end of the rect considering the current directionality. */
    private _getStartEnd;
    /** Checks whether the form field is attached to the DOM. */
    private _isAttachedToDOM;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyFormField, [null, null, { optional: true; }, { optional: true; }, null, null, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatLegacyFormField, "mat-form-field", ["matFormField"], { "color": { "alias": "color"; "required": false; }; "appearance": { "alias": "appearance"; "required": false; }; "hideRequiredMarker": { "alias": "hideRequiredMarker"; "required": false; }; "hintLabel": { "alias": "hintLabel"; "required": false; }; "floatLabel": { "alias": "floatLabel"; "required": false; }; }, {}, ["_controlNonStatic", "_controlStatic", "_labelChildNonStatic", "_labelChildStatic", "_placeholderChild", "_errorChildren", "_hintChildren", "_prefixChildren", "_suffixChildren"], ["[matPrefix]", "*", "mat-placeholder", "mat-label", "[matSuffix]", "mat-error", "mat-hint:not([align='end'])", "mat-hint[align='end']"], false, never>;
}

export { matLegacyFormFieldAnimations }

/**
 * Possible appearance styles for the form field.
 * @deprecated Use `MatFormFieldAppearance` from `@angular/material/form-field` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare type MatLegacyFormFieldAppearance = 'legacy' | 'standard' | 'fill' | 'outline';

export { MatLegacyFormFieldControl }

/**
 * Represents the default options for the form field that can be configured
 * using the `MAT_FORM_FIELD_DEFAULT_OPTIONS` injection token.
 * @deprecated Use `MatFormFieldDefaultOptions` from `@angular/material/form-field` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare interface MatLegacyFormFieldDefaultOptions {
    /** Default form field appearance style. */
    appearance?: MatLegacyFormFieldAppearance;
    /** Default color of the form field. */
    color?: ThemePalette;
    /** Whether the required marker should be hidden by default. */
    hideRequiredMarker?: boolean;
    /**
     * Whether the label for form fields should by default float `always`,
     * `never`, or `auto` (only when necessary).
     */
    floatLabel?: LegacyFloatLabelType;
}

/**
 * @deprecated Use `MatFormFieldModule` from `@angular/material/form-field` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyFormFieldModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyFormFieldModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatLegacyFormFieldModule, [typeof i1.MatLegacyError, typeof i2.MatLegacyFormField, typeof i3.MatLegacyHint, typeof i4.MatLegacyLabel, typeof i5.MatLegacyPlaceholder, typeof i6.MatLegacyPrefix, typeof i7.MatLegacySuffix], [typeof i8.CommonModule, typeof i9.MatCommonModule, typeof i10.ObserversModule], [typeof i9.MatCommonModule, typeof i1.MatLegacyError, typeof i2.MatLegacyFormField, typeof i3.MatLegacyHint, typeof i4.MatLegacyLabel, typeof i5.MatLegacyPlaceholder, typeof i6.MatLegacyPrefix, typeof i7.MatLegacySuffix]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatLegacyFormFieldModule>;
}

/**
 * Hint text to be shown underneath the form field control.
 * @deprecated Use `MatHint` from `@angular/material/form-field` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyHint {
    /** Whether to align the hint label at the start or end of the line. */
    align: 'start' | 'end';
    /** Unique ID for the hint. Used for the aria-describedby on the form field control. */
    id: string;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyHint, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyHint, "mat-hint", never, { "align": { "alias": "align"; "required": false; }; "id": { "alias": "id"; "required": false; }; }, {}, never, never, false, never>;
}

/**
 * The floating label for a `mat-form-field`.
 * @deprecated Use `MatLabel` from `@angular/material/form-field` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyLabel {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyLabel, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyLabel, "mat-label", never, {}, {}, never, never, false, never>;
}

/**
 * The placeholder text for an `MatFormField`.
 * @deprecated Use `<mat-label>` to specify the label and the `placeholder` attribute to specify the
 *     placeholder.
 * @breaking-change 8.0.0
 */
export declare class MatLegacyPlaceholder {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyPlaceholder, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyPlaceholder, "mat-placeholder", never, {}, {}, never, never, false, never>;
}

/**
 * Prefix to be placed in front of the form field.
 * @deprecated Use `MatPrefix` from `@angular/material/form-field` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacyPrefix {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacyPrefix, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacyPrefix, "[matPrefix]", never, {}, {}, never, never, false, never>;
}

/**
 * Suffix to be placed at the end of the form field.
 * @deprecated Use `MatSuffix` from `@angular/material/form-field` instead. See https://material.angular.io/guide/mdc-migration for information about migrating.
 * @breaking-change 17.0.0
 */
export declare class MatLegacySuffix {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLegacySuffix, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLegacySuffix, "[matSuffix]", never, {}, {}, never, never, false, never>;
}

export { }
