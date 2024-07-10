import { AbstractControl } from '@angular/forms';
import { AfterContentInit } from '@angular/core';
import { AfterViewInit } from '@angular/core';
import { AnimationEvent as AnimationEvent_2 } from '@angular/animations';
import { AnimationTriggerMetadata } from '@angular/animations';
import { CdkStep } from '@angular/cdk/stepper';
import { CdkStepHeader } from '@angular/cdk/stepper';
import { CdkStepLabel } from '@angular/cdk/stepper';
import { CdkStepper } from '@angular/cdk/stepper';
import { CdkStepperNext } from '@angular/cdk/stepper';
import { CdkStepperPrevious } from '@angular/cdk/stepper';
import { ChangeDetectorRef } from '@angular/core';
import { Directionality } from '@angular/cdk/bidi';
import { ElementRef } from '@angular/core';
import { ErrorStateMatcher } from '@angular/material/core';
import { EventEmitter } from '@angular/core';
import { FocusMonitor } from '@angular/cdk/a11y';
import { FocusOrigin } from '@angular/cdk/a11y';
import { FormGroupDirective } from '@angular/forms';
import * as i0 from '@angular/core';
import * as i1 from '@angular/material/core';
import * as i2 from '@angular/common';
import * as i3 from '@angular/cdk/portal';
import * as i4 from '@angular/cdk/stepper';
import * as i5 from '@angular/material/icon';
import { NgForm } from '@angular/forms';
import { OnDestroy } from '@angular/core';
import { Optional } from '@angular/core';
import { QueryList } from '@angular/core';
import { StepperOptions } from '@angular/cdk/stepper';
import { StepperOrientation } from '@angular/cdk/stepper';
import { StepState } from '@angular/cdk/stepper';
import { Subject } from 'rxjs';
import { TemplatePortal } from '@angular/cdk/portal';
import { TemplateRef } from '@angular/core';
import { ThemePalette } from '@angular/material/core';
import { ViewContainerRef } from '@angular/core';

declare namespace i10 {
    export {
        MatStepperIconContext,
        MatStepperIcon
    }
}

declare namespace i11 {
    export {
        MatStepContent
    }
}

declare namespace i6 {
    export {
        MatStep,
        MatStepper
    }
}

declare namespace i7 {
    export {
        MatStepLabel
    }
}

declare namespace i8 {
    export {
        MatStepperNext,
        MatStepperPrevious
    }
}

declare namespace i9 {
    export {
        MatStepHeader
    }
}

/** @docs-private */
export declare const MAT_STEPPER_INTL_PROVIDER: {
    provide: typeof MatStepperIntl;
    deps: Optional[][];
    useFactory: typeof MAT_STEPPER_INTL_PROVIDER_FACTORY;
};

/** @docs-private */
export declare function MAT_STEPPER_INTL_PROVIDER_FACTORY(parentIntl: MatStepperIntl): MatStepperIntl;

export declare class MatStep extends CdkStep implements ErrorStateMatcher, AfterContentInit, OnDestroy {
    private _errorStateMatcher;
    private _viewContainerRef;
    private _isSelected;
    /** Content for step label given by `<ng-template matStepLabel>`. */
    stepLabel: MatStepLabel;
    /** Theme color for the particular step. */
    color: ThemePalette;
    /** Content that will be rendered lazily. */
    _lazyContent: MatStepContent;
    /** Currently-attached portal containing the lazy content. */
    _portal: TemplatePortal;
    constructor(stepper: MatStepper, _errorStateMatcher: ErrorStateMatcher, _viewContainerRef: ViewContainerRef, stepperOptions?: StepperOptions);
    ngAfterContentInit(): void;
    ngOnDestroy(): void;
    /** Custom error state matcher that additionally checks for validity of interacted form. */
    isErrorState(control: AbstractControl | null, form: FormGroupDirective | NgForm | null): boolean;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatStep, [null, { skipSelf: true; }, null, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatStep, "mat-step", ["matStep"], { "color": { "alias": "color"; "required": false; }; }, {}, ["stepLabel", "_lazyContent"], ["*"], true, never>;
}

/**
 * Content for a `mat-step` that will be rendered lazily.
 */
export declare class MatStepContent {
    _template: TemplateRef<any>;
    constructor(_template: TemplateRef<any>);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatStepContent, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatStepContent, "ng-template[matStepContent]", never, {}, {}, never, never, true, never>;
}

export declare class MatStepHeader extends CdkStepHeader implements AfterViewInit, OnDestroy {
    _intl: MatStepperIntl;
    private _focusMonitor;
    private _intlSubscription;
    /** State of the given step. */
    state: StepState;
    /** Label of the given step. */
    label: MatStepLabel | string;
    /** Error message to display when there's an error. */
    errorMessage: string;
    /** Overrides for the header icons, passed in via the stepper. */
    iconOverrides: {
        [key: string]: TemplateRef<MatStepperIconContext>;
    };
    /** Index of the given step. */
    index: number;
    /** Whether the given step is selected. */
    selected: boolean;
    /** Whether the given step label is active. */
    active: boolean;
    /** Whether the given step is optional. */
    optional: boolean;
    /** Whether the ripple should be disabled. */
    disableRipple: boolean;
    /** Theme palette color of the step header. */
    color: ThemePalette;
    constructor(_intl: MatStepperIntl, _focusMonitor: FocusMonitor, _elementRef: ElementRef<HTMLElement>, changeDetectorRef: ChangeDetectorRef);
    ngAfterViewInit(): void;
    ngOnDestroy(): void;
    /** Focuses the step header. */
    focus(origin?: FocusOrigin, options?: FocusOptions): void;
    /** Returns string label of given step if it is a text label. */
    _stringLabel(): string | null;
    /** Returns MatStepLabel if the label of given step is a template label. */
    _templateLabel(): MatStepLabel | null;
    /** Returns the host HTML element. */
    _getHostElement(): HTMLElement;
    /** Template context variables that are exposed to the `matStepperIcon` instances. */
    _getIconContext(): MatStepperIconContext;
    _getDefaultTextForState(state: StepState): string;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatStepHeader, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatStepHeader, "mat-step-header", never, { "state": { "alias": "state"; "required": false; }; "label": { "alias": "label"; "required": false; }; "errorMessage": { "alias": "errorMessage"; "required": false; }; "iconOverrides": { "alias": "iconOverrides"; "required": false; }; "index": { "alias": "index"; "required": false; }; "selected": { "alias": "selected"; "required": false; }; "active": { "alias": "active"; "required": false; }; "optional": { "alias": "optional"; "required": false; }; "disableRipple": { "alias": "disableRipple"; "required": false; }; "color": { "alias": "color"; "required": false; }; }, {}, never, never, true, never>;
}

export declare class MatStepLabel extends CdkStepLabel {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatStepLabel, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatStepLabel, "[matStepLabel]", never, {}, {}, never, never, true, never>;
}

export declare class MatStepper extends CdkStepper implements AfterContentInit {
    /** The list of step headers of the steps in the stepper. */
    _stepHeader: QueryList<MatStepHeader>;
    /** Full list of steps inside the stepper, including inside nested steppers. */
    _steps: QueryList<MatStep>;
    /** Steps that belong to the current stepper, excluding ones from nested steppers. */
    readonly steps: QueryList<MatStep>;
    /** Custom icon overrides passed in by the consumer. */
    _icons: QueryList<MatStepperIcon>;
    /** Event emitted when the current step is done transitioning in. */
    readonly animationDone: EventEmitter<void>;
    /** Whether ripples should be disabled for the step headers. */
    disableRipple: boolean;
    /** Theme color for all of the steps in stepper. */
    color: ThemePalette;
    /**
     * Whether the label should display in bottom or end position.
     * Only applies in the `horizontal` orientation.
     */
    labelPosition: 'bottom' | 'end';
    /**
     * Position of the stepper's header.
     * Only applies in the `horizontal` orientation.
     */
    headerPosition: 'top' | 'bottom';
    /** Consumer-specified template-refs to be used to override the header icons. */
    _iconOverrides: Record<string, TemplateRef<MatStepperIconContext>>;
    /** Stream of animation `done` events when the body expands/collapses. */
    readonly _animationDone: Subject<AnimationEvent_2>;
    /** Duration for the animation. Will be normalized to milliseconds if no units are set. */
    get animationDuration(): string;
    set animationDuration(value: string);
    private _animationDuration;
    /** Whether the stepper is rendering on the server. */
    protected _isServer: boolean;
    constructor(dir: Directionality, changeDetectorRef: ChangeDetectorRef, elementRef: ElementRef<HTMLElement>);
    ngAfterContentInit(): void;
    _stepIsNavigable(index: number, step: MatStep): boolean;
    _getAnimationDuration(): string;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatStepper, [{ optional: true; }, null, null]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatStepper, "mat-stepper, mat-vertical-stepper, mat-horizontal-stepper, [matStepper]", ["matStepper", "matVerticalStepper", "matHorizontalStepper"], { "disableRipple": { "alias": "disableRipple"; "required": false; }; "color": { "alias": "color"; "required": false; }; "labelPosition": { "alias": "labelPosition"; "required": false; }; "headerPosition": { "alias": "headerPosition"; "required": false; }; "animationDuration": { "alias": "animationDuration"; "required": false; }; }, { "animationDone": "animationDone"; }, ["_steps", "_icons"], ["*"], true, never>;
}

/**
 * Animations used by the Material steppers.
 * @docs-private
 */
export declare const matStepperAnimations: {
    readonly horizontalStepTransition: AnimationTriggerMetadata;
    readonly verticalStepTransition: AnimationTriggerMetadata;
};

/**
 * Template to be used to override the icons inside the step header.
 */
export declare class MatStepperIcon {
    templateRef: TemplateRef<MatStepperIconContext>;
    /** Name of the icon to be overridden. */
    name: StepState;
    constructor(templateRef: TemplateRef<MatStepperIconContext>);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatStepperIcon, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatStepperIcon, "ng-template[matStepperIcon]", never, { "name": { "alias": "matStepperIcon"; "required": false; }; }, {}, never, never, true, never>;
}

/** Template context available to an attached `matStepperIcon`. */
export declare interface MatStepperIconContext {
    /** Index of the step. */
    index: number;
    /** Whether the step is currently active. */
    active: boolean;
    /** Whether the step is optional. */
    optional: boolean;
}

/** Stepper data that is required for internationalization. */
export declare class MatStepperIntl {
    /**
     * Stream that emits whenever the labels here are changed. Use this to notify
     * components if the labels have changed after initialization.
     */
    readonly changes: Subject<void>;
    /** Label that is rendered below optional steps. */
    optionalLabel: string;
    /** Label that is used to indicate step as completed to screen readers. */
    completedLabel: string;
    /** Label that is used to indicate step as editable to screen readers. */
    editableLabel: string;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatStepperIntl, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<MatStepperIntl>;
}

export declare class MatStepperModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatStepperModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatStepperModule, never, [typeof i1.MatCommonModule, typeof i2.CommonModule, typeof i3.PortalModule, typeof i4.CdkStepperModule, typeof i5.MatIconModule, typeof i1.MatRippleModule, typeof i6.MatStep, typeof i7.MatStepLabel, typeof i6.MatStepper, typeof i8.MatStepperNext, typeof i8.MatStepperPrevious, typeof i9.MatStepHeader, typeof i10.MatStepperIcon, typeof i11.MatStepContent], [typeof i1.MatCommonModule, typeof i6.MatStep, typeof i7.MatStepLabel, typeof i6.MatStepper, typeof i8.MatStepperNext, typeof i8.MatStepperPrevious, typeof i9.MatStepHeader, typeof i10.MatStepperIcon, typeof i11.MatStepContent]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatStepperModule>;
}

/** Button that moves to the next step in a stepper workflow. */
export declare class MatStepperNext extends CdkStepperNext {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatStepperNext, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatStepperNext, "button[matStepperNext]", never, {}, {}, never, never, true, never>;
}

/** Button that moves to the previous step in a stepper workflow. */
export declare class MatStepperPrevious extends CdkStepperPrevious {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatStepperPrevious, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatStepperPrevious, "button[matStepperPrevious]", never, {}, {}, never, never, true, never>;
}

export { StepperOrientation }

export { StepState }

export { }
