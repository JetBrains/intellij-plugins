import { _AbstractConstructor } from '@angular/material/core';
import { AfterViewInit } from '@angular/core';
import { AnimationTriggerMetadata } from '@angular/animations';
import { AriaDescriber } from '@angular/cdk/a11y';
import { BooleanInput } from '@angular/cdk/coercion';
import { CanDisable } from '@angular/material/core';
import { ChangeDetectorRef } from '@angular/core';
import { _Constructor } from '@angular/material/core';
import { ElementRef } from '@angular/core';
import { EventEmitter } from '@angular/core';
import { FocusMonitor } from '@angular/cdk/a11y';
import { HasInitialized } from '@angular/material/core';
import * as i0 from '@angular/core';
import * as i3 from '@angular/common';
import * as i4 from '@angular/material/core';
import { InjectionToken } from '@angular/core';
import { OnChanges } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { OnInit } from '@angular/core';
import { Optional } from '@angular/core';
import { Subject } from 'rxjs';

/**
 * Valid positions for the arrow to be in for its opacity and translation. If the state is a
 * sort direction, the position of the arrow will be above/below and opacity 0. If the state is
 * hint, the arrow will be in the center with a slight opacity. Active state means the arrow will
 * be fully opaque in the center.
 *
 * @docs-private
 */
export declare type ArrowViewState = SortDirection | 'hint' | 'active';

/**
 * States describing the arrow's animated position (animating fromState to toState).
 * If the fromState is not defined, there will be no animated transition to the toState.
 * @docs-private
 */
export declare interface ArrowViewStateTransition {
    fromState?: ArrowViewState;
    toState?: ArrowViewState;
}

declare namespace i1 {
    export {
        SortHeaderArrowPosition,
        MatSortable,
        Sort,
        MatSortDefaultOptions,
        MAT_SORT_DEFAULT_OPTIONS,
        MatSort
    }
}

declare namespace i2 {
    export {
        ArrowViewState,
        ArrowViewStateTransition,
        MatSortHeader
    }
}

/** Injection token to be used to override the default options for `mat-sort`. */
export declare const MAT_SORT_DEFAULT_OPTIONS: InjectionToken<MatSortDefaultOptions>;

/** @docs-private */
export declare const MAT_SORT_HEADER_INTL_PROVIDER: {
    provide: typeof MatSortHeaderIntl;
    deps: Optional[][];
    useFactory: typeof MAT_SORT_HEADER_INTL_PROVIDER_FACTORY;
};

/** @docs-private */
export declare function MAT_SORT_HEADER_INTL_PROVIDER_FACTORY(parentIntl: MatSortHeaderIntl): MatSortHeaderIntl;

/** Container for MatSortables to manage the sort state and provide default sort parameters. */
export declare class MatSort extends _MatSortBase implements CanDisable, HasInitialized, OnChanges, OnDestroy, OnInit {
    private _defaultOptions?;
    /** Collection of all registered sortables that this directive manages. */
    sortables: Map<string, MatSortable>;
    /** Used to notify any child components listening to state changes. */
    readonly _stateChanges: Subject<void>;
    /** The id of the most recently sorted MatSortable. */
    active: string;
    /**
     * The direction to set when an MatSortable is initially sorted.
     * May be overridden by the MatSortable's sort start.
     */
    start: SortDirection;
    /** The sort direction of the currently active MatSortable. */
    get direction(): SortDirection;
    set direction(direction: SortDirection);
    private _direction;
    /**
     * Whether to disable the user from clearing the sort by finishing the sort direction cycle.
     * May be overridden by the MatSortable's disable clear input.
     */
    get disableClear(): boolean;
    set disableClear(v: BooleanInput);
    private _disableClear;
    /** Event emitted when the user changes either the active sort or sort direction. */
    readonly sortChange: EventEmitter<Sort>;
    constructor(_defaultOptions?: MatSortDefaultOptions | undefined);
    /**
     * Register function to be used by the contained MatSortables. Adds the MatSortable to the
     * collection of MatSortables.
     */
    register(sortable: MatSortable): void;
    /**
     * Unregister function to be used by the contained MatSortables. Removes the MatSortable from the
     * collection of contained MatSortables.
     */
    deregister(sortable: MatSortable): void;
    /** Sets the active sort id and determines the new sort direction. */
    sort(sortable: MatSortable): void;
    /** Returns the next sort direction of the active sortable, checking for potential overrides. */
    getNextSortDirection(sortable: MatSortable): SortDirection;
    ngOnInit(): void;
    ngOnChanges(): void;
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatSort, [{ optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatSort, "[matSort]", ["matSort"], { "disabled": { "alias": "matSortDisabled"; "required": false; }; "active": { "alias": "matSortActive"; "required": false; }; "start": { "alias": "matSortStart"; "required": false; }; "direction": { "alias": "matSortDirection"; "required": false; }; "disableClear": { "alias": "matSortDisableClear"; "required": false; }; }, { "sortChange": "matSortChange"; }, never, never, false, never>;
}

/** Interface for a directive that holds sorting state consumed by `MatSortHeader`. */
export declare interface MatSortable {
    /** The id of the column being sorted. */
    id: string;
    /** Starting sort direction. */
    start: SortDirection;
    /** Whether to disable clearing the sorting state. */
    disableClear: boolean;
}

/**
 * Animations used by MatSort.
 * @docs-private
 */
export declare const matSortAnimations: {
    readonly indicator: AnimationTriggerMetadata;
    readonly leftPointer: AnimationTriggerMetadata;
    readonly rightPointer: AnimationTriggerMetadata;
    readonly arrowOpacity: AnimationTriggerMetadata;
    readonly arrowPosition: AnimationTriggerMetadata;
    readonly allowChildren: AnimationTriggerMetadata;
};

/** @docs-private */
declare const _MatSortBase: (new (...args: any[]) => HasInitialized) & _Constructor<CanDisable> & _AbstractConstructor<CanDisable> & {
    new (): {};
};

/** Default options for `mat-sort`.  */
export declare interface MatSortDefaultOptions {
    /** Whether to disable clearing the sorting state. */
    disableClear?: boolean;
    /** Position of the arrow that displays when sorted. */
    arrowPosition?: SortHeaderArrowPosition;
}

/**
 * Applies sorting behavior (click to change sort) and styles to an element, including an
 * arrow to display the current sort direction.
 *
 * Must be provided with an id and contained within a parent MatSort directive.
 *
 * If used on header cells in a CdkTable, it will automatically default its id from its containing
 * column definition.
 */
export declare class MatSortHeader extends _MatSortHeaderBase implements CanDisable, MatSortable, OnDestroy, OnInit, AfterViewInit {
    /**
     * @deprecated `_intl` parameter isn't being used anymore and it'll be removed.
     * @breaking-change 13.0.0
     */
    _intl: MatSortHeaderIntl;
    private _changeDetectorRef;
    _sort: MatSort;
    _columnDef: MatSortHeaderColumnDef;
    private _focusMonitor;
    private _elementRef;
    /** @breaking-change 14.0.0 _ariaDescriber will be required. */
    private _ariaDescriber?;
    private _rerenderSubscription;
    /**
     * The element with role="button" inside this component's view. We need this
     * in order to apply a description with AriaDescriber.
     */
    private _sortButton;
    /**
     * Flag set to true when the indicator should be displayed while the sort is not active. Used to
     * provide an affordance that the header is sortable by showing on focus and hover.
     */
    _showIndicatorHint: boolean;
    /**
     * The view transition state of the arrow (translation/ opacity) - indicates its `from` and `to`
     * position through the animation. If animations are currently disabled, the fromState is removed
     * so that there is no animation displayed.
     */
    _viewState: ArrowViewStateTransition;
    /** The direction the arrow should be facing according to the current state. */
    _arrowDirection: SortDirection;
    /**
     * Whether the view state animation should show the transition between the `from` and `to` states.
     */
    _disableViewStateAnimation: boolean;
    /**
     * ID of this sort header. If used within the context of a CdkColumnDef, this will default to
     * the column's name.
     */
    id: string;
    /** Sets the position of the arrow that displays when sorted. */
    arrowPosition: SortHeaderArrowPosition;
    /** Overrides the sort start value of the containing MatSort for this MatSortable. */
    start: SortDirection;
    /**
     * Description applied to MatSortHeader's button element with aria-describedby. This text should
     * describe the action that will occur when the user clicks the sort header.
     */
    get sortActionDescription(): string;
    set sortActionDescription(value: string);
    private _sortActionDescription;
    /** Overrides the disable clear value of the containing MatSort for this MatSortable. */
    get disableClear(): boolean;
    set disableClear(v: BooleanInput);
    private _disableClear;
    constructor(
    /**
     * @deprecated `_intl` parameter isn't being used anymore and it'll be removed.
     * @breaking-change 13.0.0
     */
    _intl: MatSortHeaderIntl, _changeDetectorRef: ChangeDetectorRef, _sort: MatSort, _columnDef: MatSortHeaderColumnDef, _focusMonitor: FocusMonitor, _elementRef: ElementRef<HTMLElement>, 
    /** @breaking-change 14.0.0 _ariaDescriber will be required. */
    _ariaDescriber?: AriaDescriber | null | undefined, defaultOptions?: MatSortDefaultOptions);
    ngOnInit(): void;
    ngAfterViewInit(): void;
    ngOnDestroy(): void;
    /**
     * Sets the "hint" state such that the arrow will be semi-transparently displayed as a hint to the
     * user showing what the active sort will become. If set to false, the arrow will fade away.
     */
    _setIndicatorHintVisible(visible: boolean): void;
    /**
     * Sets the animation transition view state for the arrow's position and opacity. If the
     * `disableViewStateAnimation` flag is set to true, the `fromState` will be ignored so that
     * no animation appears.
     */
    _setAnimationTransitionState(viewState: ArrowViewStateTransition): void;
    /** Triggers the sort on this sort header and removes the indicator hint. */
    _toggleOnInteraction(): void;
    _handleClick(): void;
    _handleKeydown(event: KeyboardEvent): void;
    /** Whether this MatSortHeader is currently sorted in either ascending or descending order. */
    _isSorted(): boolean;
    /** Returns the animation state for the arrow direction (indicator and pointers). */
    _getArrowDirectionState(): string;
    /** Returns the arrow position state (opacity, translation). */
    _getArrowViewState(): string;
    /**
     * Updates the direction the arrow should be pointing. If it is not sorted, the arrow should be
     * facing the start direction. Otherwise if it is sorted, the arrow should point in the currently
     * active sorted direction. The reason this is updated through a function is because the direction
     * should only be changed at specific times - when deactivated but the hint is displayed and when
     * the sort is active and the direction changes. Otherwise the arrow's direction should linger
     * in cases such as the sort becoming deactivated but we want to animate the arrow away while
     * preserving its direction, even though the next sort direction is actually different and should
     * only be changed once the arrow displays again (hint or activation).
     */
    _updateArrowDirection(): void;
    _isDisabled(): boolean;
    /**
     * Gets the aria-sort attribute that should be applied to this sort header. If this header
     * is not sorted, returns null so that the attribute is removed from the host element. Aria spec
     * says that the aria-sort property should only be present on one header at a time, so removing
     * ensures this is true.
     */
    _getAriaSortAttribute(): "none" | "ascending" | "descending";
    /** Whether the arrow inside the sort header should be rendered. */
    _renderArrow(): boolean;
    private _updateSortActionDescription;
    /** Handles changes in the sorting state. */
    private _handleStateChanges;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatSortHeader, [null, null, { optional: true; }, { optional: true; }, null, null, { optional: true; }, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatSortHeader, "[mat-sort-header]", ["matSortHeader"], { "disabled": { "alias": "disabled"; "required": false; }; "id": { "alias": "mat-sort-header"; "required": false; }; "arrowPosition": { "alias": "arrowPosition"; "required": false; }; "start": { "alias": "start"; "required": false; }; "sortActionDescription": { "alias": "sortActionDescription"; "required": false; }; "disableClear": { "alias": "disableClear"; "required": false; }; }, {}, never, ["*"], false, never>;
}

/** @docs-private */
declare const _MatSortHeaderBase: _Constructor<CanDisable> & _AbstractConstructor<CanDisable> & {
    new (): {};
};

/** Column definition associated with a `MatSortHeader`. */
declare interface MatSortHeaderColumnDef {
    name: string;
}

/**
 * To modify the labels and text displayed, create a new instance of MatSortHeaderIntl and
 * include it in a custom provider.
 */
export declare class MatSortHeaderIntl {
    /**
     * Stream that emits whenever the labels here are changed. Use this to notify
     * components if the labels have changed after initialization.
     */
    readonly changes: Subject<void>;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatSortHeaderIntl, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<MatSortHeaderIntl>;
}

export declare class MatSortModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatSortModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatSortModule, [typeof i1.MatSort, typeof i2.MatSortHeader], [typeof i3.CommonModule, typeof i4.MatCommonModule], [typeof i1.MatSort, typeof i2.MatSortHeader]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatSortModule>;
}

/** The current sort state. */
export declare interface Sort {
    /** The id of the column being sorted. */
    active: string;
    /** The sort direction. */
    direction: SortDirection;
}


export declare type SortDirection = 'asc' | 'desc' | '';

/** Position of the arrow that displays when sorted. */
export declare type SortHeaderArrowPosition = 'before' | 'after';

export { }
