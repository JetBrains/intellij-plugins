import { AfterContentInit } from '@angular/core';
import { AfterViewInit } from '@angular/core';
import { BooleanInput } from '@angular/cdk/coercion';
import { ChangeDetectorRef } from '@angular/core';
import { Directionality } from '@angular/cdk/bidi';
import { ElementRef } from '@angular/core';
import { EventEmitter } from '@angular/core';
import { FocusableOption } from '@angular/cdk/a11y';
import * as i0 from '@angular/core';
import * as i5 from '@angular/cdk/bidi';
import { InjectionToken } from '@angular/core';
import { NumberInput } from '@angular/cdk/coercion';
import { Observable } from 'rxjs';
import { OnChanges } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { QueryList } from '@angular/core';
import { Subject } from 'rxjs';
import { TemplateRef } from '@angular/core';

/**
 * Simplified representation of an "AbstractControl" from @angular/forms.
 * Used to avoid having to bring in @angular/forms for a single optional interface.
 * @docs-private
 */
declare interface AbstractControlLike {
    asyncValidator: ((control: any) => any) | null;
    dirty: boolean;
    disabled: boolean;
    enabled: boolean;
    errors: {
        [key: string]: any;
    } | null;
    invalid: boolean;
    parent: any;
    pending: boolean;
    pristine: boolean;
    root: AbstractControlLike;
    status: string;
    readonly statusChanges: Observable<any>;
    touched: boolean;
    untouched: boolean;
    updateOn: any;
    valid: boolean;
    validator: ((control: any) => any) | null;
    value: any;
    readonly valueChanges: Observable<any>;
    clearAsyncValidators(): void;
    clearValidators(): void;
    disable(opts?: any): void;
    enable(opts?: any): void;
    get(path: (string | number)[] | string): AbstractControlLike | null;
    getError(errorCode: string, path?: (string | number)[] | string): any;
    hasError(errorCode: string, path?: (string | number)[] | string): boolean;
    markAllAsTouched(): void;
    markAsDirty(opts?: any): void;
    markAsPending(opts?: any): void;
    markAsPristine(opts?: any): void;
    markAsTouched(opts?: any): void;
    markAsUntouched(opts?: any): void;
    patchValue(value: any, options?: Object): void;
    reset(value?: any, options?: Object): void;
    setAsyncValidators(newValidator: (control: any) => any | ((control: any) => any)[] | null): void;
    setErrors(errors: {
        [key: string]: any;
    } | null, opts?: any): void;
    setParent(parent: any): void;
    setValidators(newValidator: (control: any) => any | ((control: any) => any)[] | null): void;
    setValue(value: any, options?: Object): void;
    updateValueAndValidity(opts?: any): void;
    patchValue(value: any, options?: any): void;
    reset(formState?: any, options?: any): void;
    setValue(value: any, options?: any): void;
}

export declare class CdkStep implements OnChanges {
    _stepper: CdkStepper;
    private _stepperOptions;
    _displayDefaultIndicatorType: boolean;
    /** Template for step label if it exists. */
    stepLabel: CdkStepLabel;
    /** Template for step content. */
    content: TemplateRef<any>;
    /** The top level abstract control of the step. */
    stepControl: AbstractControlLike;
    /** Whether user has attempted to move away from the step. */
    interacted: boolean;
    /** Emits when the user has attempted to move away from the step. */
    readonly interactedStream: EventEmitter<CdkStep>;
    /** Plain text label of the step. */
    label: string;
    /** Error message to display when there's an error. */
    errorMessage: string;
    /** Aria label for the tab. */
    ariaLabel: string;
    /**
     * Reference to the element that the tab is labelled by.
     * Will be cleared if `aria-label` is set at the same time.
     */
    ariaLabelledby: string;
    /** State of the step. */
    state: StepState;
    /** Whether the user can return to this step once it has been marked as completed. */
    get editable(): boolean;
    set editable(value: BooleanInput);
    private _editable;
    /** Whether the completion of step is optional. */
    get optional(): boolean;
    set optional(value: BooleanInput);
    private _optional;
    /** Whether step is marked as completed. */
    get completed(): boolean;
    set completed(value: BooleanInput);
    _completedOverride: boolean | null;
    private _getDefaultCompleted;
    /** Whether step has an error. */
    get hasError(): boolean;
    set hasError(value: BooleanInput);
    private _customError;
    private _getDefaultError;
    constructor(_stepper: CdkStepper, stepperOptions?: StepperOptions);
    /** Selects this step component. */
    select(): void;
    /** Resets the step to its initial state. Note that this includes resetting form data. */
    reset(): void;
    ngOnChanges(): void;
    _markAsInteracted(): void;
    /** Determines whether the error state can be shown. */
    _showError(): boolean;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkStep, [null, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<CdkStep, "cdk-step", ["cdkStep"], { "stepControl": "stepControl"; "label": "label"; "errorMessage": "errorMessage"; "ariaLabel": "aria-label"; "ariaLabelledby": "aria-labelledby"; "state": "state"; "editable": "editable"; "optional": "optional"; "completed": "completed"; "hasError": "hasError"; }, { "interactedStream": "interacted"; }, ["stepLabel"], ["*"], false>;
}

export declare class CdkStepHeader implements FocusableOption {
    _elementRef: ElementRef<HTMLElement>;
    constructor(_elementRef: ElementRef<HTMLElement>);
    /** Focuses the step header. */
    focus(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkStepHeader, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkStepHeader, "[cdkStepHeader]", never, {}, {}, never, never, false>;
}

export declare class CdkStepLabel {
    template: TemplateRef<any>;
    constructor(/** @docs-private */ template: TemplateRef<any>);
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkStepLabel, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkStepLabel, "[cdkStepLabel]", never, {}, {}, never, never, false>;
}

export declare class CdkStepper implements AfterContentInit, AfterViewInit, OnDestroy {
    private _dir;
    private _changeDetectorRef;
    private _elementRef;
    /** Emits when the component is destroyed. */
    protected readonly _destroyed: Subject<void>;
    /** Used for managing keyboard focus. */
    private _keyManager;
    /** Full list of steps inside the stepper, including inside nested steppers. */
    _steps: QueryList<CdkStep>;
    /** Steps that belong to the current stepper, excluding ones from nested steppers. */
    readonly steps: QueryList<CdkStep>;
    /** The list of step headers of the steps in the stepper. */
    _stepHeader: QueryList<CdkStepHeader>;
    /** List of step headers sorted based on their DOM order. */
    private _sortedHeaders;
    /** Whether the validity of previous steps should be checked or not. */
    get linear(): boolean;
    set linear(value: BooleanInput);
    private _linear;
    /** The index of the selected step. */
    get selectedIndex(): number;
    set selectedIndex(index: NumberInput);
    private _selectedIndex;
    /** The step that is selected. */
    get selected(): CdkStep | undefined;
    set selected(step: CdkStep | undefined);
    /** Event emitted when the selected step has changed. */
    readonly selectionChange: EventEmitter<StepperSelectionEvent>;
    /** Used to track unique ID for each stepper component. */
    _groupId: number;
    /** Orientation of the stepper. */
    get orientation(): StepperOrientation;
    set orientation(value: StepperOrientation);
    private _orientation;
    constructor(_dir: Directionality, _changeDetectorRef: ChangeDetectorRef, _elementRef: ElementRef<HTMLElement>);
    ngAfterContentInit(): void;
    ngAfterViewInit(): void;
    ngOnDestroy(): void;
    /** Selects and focuses the next step in list. */
    next(): void;
    /** Selects and focuses the previous step in list. */
    previous(): void;
    /** Resets the stepper to its initial state. Note that this includes clearing form data. */
    reset(): void;
    /** Returns a unique id for each step label element. */
    _getStepLabelId(i: number): string;
    /** Returns unique id for each step content element. */
    _getStepContentId(i: number): string;
    /** Marks the component to be change detected. */
    _stateChanged(): void;
    /** Returns position state of the step with the given index. */
    _getAnimationDirection(index: number): StepContentPositionState;
    /** Returns the type of icon to be displayed. */
    _getIndicatorType(index: number, state?: StepState): StepState;
    private _getDefaultIndicatorLogic;
    private _getGuidelineLogic;
    private _isCurrentStep;
    /** Returns the index of the currently-focused step header. */
    _getFocusIndex(): number | null;
    private _updateSelectedItemIndex;
    _onKeydown(event: KeyboardEvent): void;
    private _anyControlsInvalidOrPending;
    private _layoutDirection;
    /** Checks whether the stepper contains the focused element. */
    private _containsFocus;
    /** Checks whether the passed-in index is a valid step index. */
    private _isValidIndex;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkStepper, [{ optional: true; }, null, null]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkStepper, "[cdkStepper]", ["cdkStepper"], { "linear": "linear"; "selectedIndex": "selectedIndex"; "selected": "selected"; "orientation": "orientation"; }, { "selectionChange": "selectionChange"; }, ["_steps", "_stepHeader"], never, false>;
}

export declare class CdkStepperModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkStepperModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<CdkStepperModule, [typeof i1.CdkStep, typeof i1.CdkStepper, typeof i2.CdkStepHeader, typeof i3.CdkStepLabel, typeof i4.CdkStepperNext, typeof i4.CdkStepperPrevious], [typeof i5.BidiModule], [typeof i1.CdkStep, typeof i1.CdkStepper, typeof i2.CdkStepHeader, typeof i3.CdkStepLabel, typeof i4.CdkStepperNext, typeof i4.CdkStepperPrevious]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<CdkStepperModule>;
}

/** Button that moves to the next step in a stepper workflow. */
export declare class CdkStepperNext {
    _stepper: CdkStepper;
    /** Type of the next button. Defaults to "submit" if not specified. */
    type: string;
    constructor(_stepper: CdkStepper);
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkStepperNext, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkStepperNext, "button[cdkStepperNext]", never, { "type": "type"; }, {}, never, never, false>;
}

/** Button that moves to the previous step in a stepper workflow. */
export declare class CdkStepperPrevious {
    _stepper: CdkStepper;
    /** Type of the previous button. Defaults to "button" if not specified. */
    type: string;
    constructor(_stepper: CdkStepper);
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkStepperPrevious, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkStepperPrevious, "button[cdkStepperPrevious]", never, { "type": "type"; }, {}, never, never, false>;
}

declare namespace i1 {
    export {
        StepContentPositionState,
        StepperOrientation,
        StepperSelectionEvent,
        StepState,
        STEP_STATE,
        STEPPER_GLOBAL_OPTIONS,
        StepperOptions,
        CdkStep,
        CdkStepper
    }
}

declare namespace i2 {
    export {
        CdkStepHeader
    }
}

declare namespace i3 {
    export {
        CdkStepLabel
    }
}

declare namespace i4 {
    export {
        CdkStepperNext,
        CdkStepperPrevious
    }
}

/** Enum to represent the different states of the steps. */
export declare const STEP_STATE: {
    NUMBER: string;
    EDIT: string;
    DONE: string;
    ERROR: string;
};

/**
 * Position state of the content of each step in stepper that is used for transitioning
 * the content into correct position upon step selection change.
 */
export declare type StepContentPositionState = 'previous' | 'current' | 'next';

/** InjectionToken that can be used to specify the global stepper options. */
export declare const STEPPER_GLOBAL_OPTIONS: InjectionToken<StepperOptions>;

/** Configurable options for stepper. */
export declare interface StepperOptions {
    /**
     * Whether the stepper should display an error state or not.
     * Default behavior is assumed to be false.
     */
    showError?: boolean;
    /**
     * Whether the stepper should display the default indicator type
     * or not.
     * Default behavior is assumed to be true.
     */
    displayDefaultIndicatorType?: boolean;
}

/** Possible orientation of a stepper. */
export declare type StepperOrientation = 'horizontal' | 'vertical';

/** Change event emitted on selection changes. */
export declare class StepperSelectionEvent {
    /** Index of the step now selected. */
    selectedIndex: number;
    /** Index of the step previously selected. */
    previouslySelectedIndex: number;
    /** The step instance now selected. */
    selectedStep: CdkStep;
    /** The step instance previously selected. */
    previouslySelectedStep: CdkStep;
}

/** The state of each step. */
export declare type StepState = 'number' | 'edit' | 'done' | 'error' | string;

export { }
