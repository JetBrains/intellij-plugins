import { AbstractControlDirective } from '@angular/forms';
import { AfterContentChecked } from '@angular/core';
import { AfterContentInit } from '@angular/core';
import { AfterViewInit } from '@angular/core';
import { AnimationTriggerMetadata } from '@angular/animations';
import { BooleanInput } from '@angular/cdk/coercion';
import { ChangeDetectorRef } from '@angular/core';
import { Directionality } from '@angular/cdk/bidi';
import { ElementRef } from '@angular/core';
import * as i0 from '@angular/core';
import * as i10 from '@angular/material/core';
import * as i11 from '@angular/common';
import * as i12 from '@angular/cdk/observers';
import { InjectionToken } from '@angular/core';
import { NgControl } from '@angular/forms';
import { NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { OnDestroy } from '@angular/core';
import { Platform } from '@angular/cdk/platform';
import { QueryList } from '@angular/core';
import { ThemePalette } from '@angular/material/core';

/** An injion token for the parent form-field. */
declare const FLOATING_LABEL_PARENT: InjectionToken<FloatingLabelParent>;

/** An interface that the parent form-field should implement to receive resize events. */
declare interface FloatingLabelParent {
    _handleLabelResized(): void;
}

/** Type for the available floatLabel values. */
export declare type FloatLabelType = 'always' | 'auto';

/** @docs-private */
export declare function getMatFormFieldDuplicatedHintError(align: string): Error;

/** @docs-private */
export declare function getMatFormFieldMissingControlError(): Error;


/** @docs-private */
export declare function getMatFormFieldPlaceholderConflictError(): Error;

declare namespace i1 {
    export {
        FloatLabelType,
        MatFormFieldAppearance,
        SubscriptSizing,
        MatFormFieldDefaultOptions,
        MAT_FORM_FIELD,
        MAT_FORM_FIELD_DEFAULT_OPTIONS,
        MatFormField
    }
}

declare namespace i2 {
    export {
        MatLabel
    }
}

declare namespace i3 {
    export {
        MAT_ERROR,
        MatError
    }
}

declare namespace i4 {
    export {
        MatHint
    }
}

declare namespace i5 {
    export {
        MAT_PREFIX,
        MatPrefix
    }
}

declare namespace i6 {
    export {
        MAT_SUFFIX,
        MatSuffix
    }
}

declare namespace i7 {
    export {
        FloatingLabelParent,
        FLOATING_LABEL_PARENT,
        MatFormFieldFloatingLabel
    }
}

declare namespace i8 {
    export {
        MatFormFieldNotchedOutline
    }
}

declare namespace i9 {
    export {
        MatFormFieldLineRipple
    }
}

/**
 * Injection token that can be used to reference instances of `MatError`. It serves as
 * alternative token to the actual `MatError` class which could cause unnecessary
 * retention of the class and its directive metadata.
 */
export declare const MAT_ERROR: InjectionToken<MatError>;

/**
 * Injection token that can be used to inject an instances of `MatFormField`. It serves
 * as alternative token to the actual `MatFormField` class which would cause unnecessary
 * retention of the `MatFormField` class and its component metadata.
 */
export declare const MAT_FORM_FIELD: InjectionToken<MatFormField>;

/**
 * Injection token that can be used to configure the
 * default options for all form field within an app.
 */
export declare const MAT_FORM_FIELD_DEFAULT_OPTIONS: InjectionToken<MatFormFieldDefaultOptions>;

/**
 * Injection token that can be used to reference instances of `MatPrefix`. It serves as
 * alternative token to the actual `MatPrefix` class which could cause unnecessary
 * retention of the class and its directive metadata.
 */
export declare const MAT_PREFIX: InjectionToken<MatPrefix>;

/**
 * Injection token that can be used to reference instances of `MatSuffix`. It serves as
 * alternative token to the actual `MatSuffix` class which could cause unnecessary
 * retention of the class and its directive metadata.
 */
export declare const MAT_SUFFIX: InjectionToken<MatSuffix>;

/** Single error message to be shown underneath the form-field. */
export declare class MatError {
    id: string;
    constructor(ariaLive: string, elementRef: ElementRef);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatError, [{ attribute: "aria-live"; }, null]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatError, "mat-error, [matError]", never, { "id": { "alias": "id"; "required": false; }; }, {}, never, never, false, never>;
}

/** Container for form controls that applies Material Design styling and behavior. */
export declare class MatFormField implements FloatingLabelParent, AfterContentInit, AfterContentChecked, AfterViewInit, OnDestroy {
    _elementRef: ElementRef;
    private _changeDetectorRef;
    private _ngZone;
    private _dir;
    private _platform;
    private _defaults?;
    _animationMode?: string | undefined;
    _textField: ElementRef<HTMLElement>;
    _iconPrefixContainer: ElementRef<HTMLElement>;
    _textPrefixContainer: ElementRef<HTMLElement>;
    _floatingLabel: MatFormFieldFloatingLabel | undefined;
    _notchedOutline: MatFormFieldNotchedOutline | undefined;
    _lineRipple: MatFormFieldLineRipple | undefined;
    _labelChildNonStatic: MatLabel | undefined;
    _labelChildStatic: MatLabel | undefined;
    _formFieldControl: MatFormFieldControl<any>;
    _prefixChildren: QueryList<MatPrefix>;
    _suffixChildren: QueryList<MatSuffix>;
    _errorChildren: QueryList<MatError>;
    _hintChildren: QueryList<MatHint>;
    /** Whether the required marker should be hidden. */
    get hideRequiredMarker(): boolean;
    set hideRequiredMarker(value: BooleanInput);
    private _hideRequiredMarker;
    /** The color palette for the form field. */
    color: ThemePalette;
    /** Whether the label should always float or float as the user types. */
    get floatLabel(): FloatLabelType;
    set floatLabel(value: FloatLabelType);
    private _floatLabel;
    /** The form field appearance style. */
    get appearance(): MatFormFieldAppearance;
    set appearance(value: MatFormFieldAppearance);
    private _appearance;
    /**
     * Whether the form field should reserve space for one line of hint/error text (default)
     * or to have the spacing grow from 0px as needed based on the size of the hint/error content.
     * Note that when using dynamic sizing, layout shifts will occur when hint/error text changes.
     */
    get subscriptSizing(): SubscriptSizing;
    set subscriptSizing(value: SubscriptSizing);
    private _subscriptSizing;
    /** Text for the form field hint. */
    get hintLabel(): string;
    set hintLabel(value: string);
    private _hintLabel;
    _hasIconPrefix: boolean;
    _hasTextPrefix: boolean;
    _hasIconSuffix: boolean;
    _hasTextSuffix: boolean;
    readonly _labelId: string;
    readonly _hintLabelId: string;
    /** State of the mat-hint and mat-error animations. */
    _subscriptAnimationState: string;
    /** Gets the current form field control */
    get _control(): MatFormFieldControl<any>;
    set _control(value: MatFormFieldControl<any>);
    private _destroyed;
    private _isFocused;
    private _explicitFormFieldControl;
    private _needsOutlineLabelOffsetUpdateOnStable;
    constructor(_elementRef: ElementRef, _changeDetectorRef: ChangeDetectorRef, _ngZone: NgZone, _dir: Directionality, _platform: Platform, _defaults?: MatFormFieldDefaultOptions | undefined, _animationMode?: string | undefined, 
    /**
     * @deprecated not needed, to be removed.
     * @breaking-change 17.0.0 remove this param
     */
    _unusedDocument?: any);
    ngAfterViewInit(): void;
    ngAfterContentInit(): void;
    ngAfterContentChecked(): void;
    ngOnDestroy(): void;
    /**
     * Gets the id of the label element. If no label is present, returns `null`.
     */
    getLabelId(): string | null;
    /**
     * Gets an ElementRef for the element that a overlay attached to the form field
     * should be positioned relative to.
     */
    getConnectedOverlayOrigin(): ElementRef;
    /** Animates the placeholder up and locks it in position. */
    _animateAndLockLabel(): void;
    /** Initializes the registered form field control. */
    private _initializeControl;
    private _checkPrefixAndSuffixTypes;
    /** Initializes the prefix and suffix containers. */
    private _initializePrefixAndSuffix;
    /**
     * Initializes the subscript by validating hints and synchronizing "aria-describedby" ids
     * with the custom form field control. Also subscribes to hint and error changes in order
     * to be able to validate and synchronize ids on change.
     */
    private _initializeSubscript;
    /** Throws an error if the form field's control is missing. */
    private _assertFormFieldControl;
    private _updateFocusState;
    /**
     * The floating label in the docked state needs to account for prefixes. The horizontal offset
     * is calculated whenever the appearance changes to `outline`, the prefixes change, or when the
     * form field is added to the DOM. This method sets up all subscriptions which are needed to
     * trigger the label offset update. In general, we want to avoid performing measurements often,
     * so we rely on the `NgZone` as indicator when the offset should be recalculated, instead of
     * checking every change detection cycle.
     */
    private _initializeOutlineLabelOffsetSubscriptions;
    /** Whether the floating label should always float or not. */
    _shouldAlwaysFloat(): boolean;
    _hasOutline(): boolean;
    /**
     * Whether the label should display in the infix. Labels in the outline appearance are
     * displayed as part of the notched-outline and are horizontally offset to account for
     * form field prefix content. This won't work in server side rendering since we cannot
     * measure the width of the prefix container. To make the docked label appear as if the
     * right offset has been calculated, we forcibly render the label inside the infix. Since
     * the label is part of the infix, the label cannot overflow the prefix content.
     */
    _forceDisplayInfixLabel(): boolean | 0;
    _hasFloatingLabel(): boolean;
    _shouldLabelFloat(): boolean;
    /**
     * Determines whether a class from the AbstractControlDirective
     * should be forwarded to the host element.
     */
    _shouldForward(prop: keyof AbstractControlDirective): boolean;
    /** Determines whether to display hints or errors. */
    _getDisplayedMessages(): 'error' | 'hint';
    /** Handle label resize events. */
    _handleLabelResized(): void;
    /** Refreshes the width of the outline-notch, if present. */
    _refreshOutlineNotchWidth(): void;
    /** Does any extra processing that is required when handling the hints. */
    private _processHints;
    /**
     * Ensure that there is a maximum of one of each "mat-hint" alignment specified. The hint
     * label specified set through the input is being considered as "start" aligned.
     *
     * This method is a noop if Angular runs in production mode.
     */
    private _validateHints;
    /**
     * Sets the list of element IDs that describe the child control. This allows the control to update
     * its `aria-describedby` attribute accordingly.
     */
    private _syncDescribedByIds;
    /**
     * Updates the horizontal offset of the label in the outline appearance. In the outline
     * appearance, the notched-outline and label are not relative to the infix container because
     * the outline intends to surround prefixes, suffixes and the infix. This means that the
     * floating label by default overlaps prefixes in the docked state. To avoid this, we need to
     * horizontally offset the label by the width of the prefix container. The MDC text-field does
     * not need to do this because they use a fixed width for prefixes. Hence, they can simply
     * incorporate the horizontal offset into their default text-field styles.
     */
    private _updateOutlineLabelOffset;
    /** Checks whether the form field is attached to the DOM. */
    private _isAttachedToDom;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatFormField, [null, null, null, null, null, { optional: true; }, { optional: true; }, null]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatFormField, "mat-form-field", ["matFormField"], { "hideRequiredMarker": { "alias": "hideRequiredMarker"; "required": false; }; "color": { "alias": "color"; "required": false; }; "floatLabel": { "alias": "floatLabel"; "required": false; }; "appearance": { "alias": "appearance"; "required": false; }; "subscriptSizing": { "alias": "subscriptSizing"; "required": false; }; "hintLabel": { "alias": "hintLabel"; "required": false; }; }, {}, ["_labelChildNonStatic", "_labelChildStatic", "_formFieldControl", "_prefixChildren", "_suffixChildren", "_errorChildren", "_hintChildren"], ["mat-label", "[matPrefix], [matIconPrefix]", "[matTextPrefix]", "*", "[matTextSuffix]", "[matSuffix], [matIconSuffix]", "mat-error, [matError]", "mat-hint:not([align='end'])", "mat-hint[align='end']"], false, never>;
}

/**
 * Animations used by the MatFormField.
 * @docs-private
 */
export declare const matFormFieldAnimations: {
    readonly transitionMessages: AnimationTriggerMetadata;
};

/** Possible appearance styles for the form field. */
export declare type MatFormFieldAppearance = 'fill' | 'outline';

/** An interface which allows a control to work inside of a `MatFormField`. */
export declare abstract class MatFormFieldControl<T> {
    /** The value of the control. */
    value: T | null;
    /**
     * Stream that emits whenever the state of the control changes such that the parent `MatFormField`
     * needs to run change detection.
     */
    readonly stateChanges: Observable<void>;
    /** The element ID for this control. */
    readonly id: string;
    /** The placeholder for this control. */
    readonly placeholder: string;
    /** Gets the AbstractControlDirective for this control. */
    readonly ngControl: NgControl | AbstractControlDirective | null;
    /** Whether the control is focused. */
    readonly focused: boolean;
    /** Whether the control is empty. */
    readonly empty: boolean;
    /** Whether the `MatFormField` label should try to float. */
    readonly shouldLabelFloat: boolean;
    /** Whether the control is required. */
    readonly required: boolean;
    /** Whether the control is disabled. */
    readonly disabled: boolean;
    /** Whether the control is in an error state. */
    readonly errorState: boolean;
    /**
     * An optional name for the control type that can be used to distinguish `mat-form-field` elements
     * based on their control type. The form field will add a class,
     * `mat-form-field-type-{{controlType}}` to its root element.
     */
    readonly controlType?: string;
    /**
     * Whether the input is currently in an autofilled state. If property is not present on the
     * control it is assumed to be false.
     */
    readonly autofilled?: boolean;
    /**
     * Value of `aria-describedby` that should be merged with the described-by ids
     * which are set by the form-field.
     */
    readonly userAriaDescribedBy?: string;
    /** Sets the list of element IDs that currently describe this control. */
    abstract setDescribedByIds(ids: string[]): void;
    /** Handles a click on the control's container. */
    abstract onContainerClick(event: MouseEvent): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatFormFieldControl<any>, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatFormFieldControl<any>, never, never, {}, {}, never, never, false, never>;
}

/**
 * Represents the default options for the form field that can be configured
 * using the `MAT_FORM_FIELD_DEFAULT_OPTIONS` injection token.
 */
export declare interface MatFormFieldDefaultOptions {
    /** Default form field appearance style. */
    appearance?: MatFormFieldAppearance;
    /** Default color of the form field. */
    color?: ThemePalette;
    /** Whether the required marker should be hidden by default. */
    hideRequiredMarker?: boolean;
    /**
     * Whether the label for form fields should by default float `always`,
     * `never`, or `auto` (only when necessary).
     */
    floatLabel?: FloatLabelType;
    /** Whether the form field should reserve space for one line by default. */
    subscriptSizing?: SubscriptSizing;
}

/**
 * Internal directive that maintains a MDC floating label. This directive does not
 * use the `MDCFloatingLabelFoundation` class, as it is not worth the size cost of
 * including it just to measure the label width and toggle some classes.
 *
 * The use of a directive allows us to conditionally render a floating label in the
 * template without having to manually manage instantiation and destruction of the
 * floating label component based on.
 *
 * The component is responsible for setting up the floating label styles, measuring label
 * width for the outline notch, and providing inputs that can be used to toggle the
 * label's floating or required state.
 */
declare class MatFormFieldFloatingLabel implements OnDestroy {
    private _elementRef;
    /** Whether the label is floating. */
    get floating(): boolean;
    set floating(value: boolean);
    private _floating;
    /** Whether to monitor for resize events on the floating label. */
    get monitorResize(): boolean;
    set monitorResize(value: boolean);
    private _monitorResize;
    /** The shared ResizeObserver. */
    private _resizeObserver;
    /** The Angular zone. */
    private _ngZone;
    /** The parent form-field. */
    private _parent;
    /** The current resize event subscription. */
    private _resizeSubscription;
    constructor(_elementRef: ElementRef<HTMLElement>);
    ngOnDestroy(): void;
    /** Gets the width of the label. Used for the outline notch. */
    getWidth(): number;
    /** Gets the HTML element for the floating label. */
    get element(): HTMLElement;
    /** Handles resize events from the ResizeObserver. */
    private _handleResize;
    /** Subscribes to resize events. */
    private _subscribeToResize;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatFormFieldFloatingLabel, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatFormFieldFloatingLabel, "label[matFormFieldFloatingLabel]", never, { "floating": { "alias": "floating"; "required": false; }; "monitorResize": { "alias": "monitorResize"; "required": false; }; }, {}, never, never, false, never>;
}

/**
 * Internal directive that creates an instance of the MDC line-ripple component. Using a
 * directive allows us to conditionally render a line-ripple in the template without having
 * to manually create and destroy the `MDCLineRipple` component whenever the condition changes.
 *
 * The directive sets up the styles for the line-ripple and provides an API for activating
 * and deactivating the line-ripple.
 */
declare class MatFormFieldLineRipple implements OnDestroy {
    private _elementRef;
    constructor(_elementRef: ElementRef<HTMLElement>, ngZone: NgZone);
    activate(): void;
    deactivate(): void;
    private _handleTransitionEnd;
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatFormFieldLineRipple, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatFormFieldLineRipple, "div[matFormFieldLineRipple]", never, {}, {}, never, never, false, never>;
}

export declare class MatFormFieldModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatFormFieldModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatFormFieldModule, [typeof i1.MatFormField, typeof i2.MatLabel, typeof i3.MatError, typeof i4.MatHint, typeof i5.MatPrefix, typeof i6.MatSuffix, typeof i7.MatFormFieldFloatingLabel, typeof i8.MatFormFieldNotchedOutline, typeof i9.MatFormFieldLineRipple], [typeof i10.MatCommonModule, typeof i11.CommonModule, typeof i12.ObserversModule], [typeof i1.MatFormField, typeof i2.MatLabel, typeof i4.MatHint, typeof i3.MatError, typeof i5.MatPrefix, typeof i6.MatSuffix, typeof i10.MatCommonModule]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatFormFieldModule>;
}

/**
 * Internal component that creates an instance of the MDC notched-outline component.
 *
 * The component sets up the HTML structure and styles for the notched-outline. It provides
 * inputs to toggle the notch state and width.
 */
declare class MatFormFieldNotchedOutline implements AfterViewInit {
    private _elementRef;
    private _ngZone;
    /** Whether the notch should be opened. */
    open: boolean;
    _notch: ElementRef;
    constructor(_elementRef: ElementRef<HTMLElement>, _ngZone: NgZone);
    ngAfterViewInit(): void;
    _setNotchWidth(labelWidth: number): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatFormFieldNotchedOutline, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatFormFieldNotchedOutline, "div[matFormFieldNotchedOutline]", never, { "open": { "alias": "matFormFieldNotchedOutlineOpen"; "required": false; }; }, {}, never, ["*"], false, never>;
}

/** Hint text to be shown underneath the form field control. */
export declare class MatHint {
    /** Whether to align the hint label at the start or end of the line. */
    align: 'start' | 'end';
    /** Unique ID for the hint. Used for the aria-describedby on the form field control. */
    id: string;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatHint, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatHint, "mat-hint", never, { "align": { "alias": "align"; "required": false; }; "id": { "alias": "id"; "required": false; }; }, {}, never, never, false, never>;
}

/** The floating label for a `mat-form-field`. */
export declare class MatLabel {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatLabel, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatLabel, "mat-label", never, {}, {}, never, never, false, never>;
}

/** Prefix to be placed in front of the form field. */
export declare class MatPrefix {
    set _isTextSelector(value: '');
    _isText: boolean;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatPrefix, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatPrefix, "[matPrefix], [matIconPrefix], [matTextPrefix]", never, { "_isTextSelector": { "alias": "matTextPrefix"; "required": false; }; }, {}, never, never, false, never>;
}

/** Suffix to be placed at the end of the form field. */
export declare class MatSuffix {
    set _isTextSelector(value: '');
    _isText: boolean;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatSuffix, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatSuffix, "[matSuffix], [matIconSuffix], [matTextSuffix]", never, { "_isTextSelector": { "alias": "matTextSuffix"; "required": false; }; }, {}, never, never, false, never>;
}

/** Behaviors for how the subscript height is set. */
export declare type SubscriptSizing = 'fixed' | 'dynamic';

export { }
