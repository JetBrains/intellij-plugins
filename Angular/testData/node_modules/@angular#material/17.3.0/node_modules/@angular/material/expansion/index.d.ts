import { AfterContentInit } from '@angular/core';
import { AfterViewInit } from '@angular/core';
import { AnimationEvent as AnimationEvent_2 } from '@angular/animations';
import { AnimationTriggerMetadata } from '@angular/animations';
import { CdkAccordion } from '@angular/cdk/accordion';
import { CdkAccordionItem } from '@angular/cdk/accordion';
import { ChangeDetectorRef } from '@angular/core';
import { ElementRef } from '@angular/core';
import { EventEmitter } from '@angular/core';
import { FocusableOption } from '@angular/cdk/a11y';
import { FocusMonitor } from '@angular/cdk/a11y';
import { FocusOrigin } from '@angular/cdk/a11y';
import * as i0 from '@angular/core';
import * as i1 from '@angular/material/core';
import * as i2 from '@angular/cdk/accordion';
import * as i3 from '@angular/cdk/portal';
import { InjectionToken } from '@angular/core';
import { OnChanges } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { QueryList } from '@angular/core';
import { SimpleChanges } from '@angular/core';
import { Subject } from 'rxjs';
import { TemplatePortal } from '@angular/cdk/portal';
import { TemplateRef } from '@angular/core';
import { UniqueSelectionDispatcher } from '@angular/cdk/collections';
import { ViewContainerRef } from '@angular/core';

/** Time and timing curve for expansion panel animations. */
export declare const EXPANSION_PANEL_ANIMATION_TIMING = "225ms cubic-bezier(0.4,0.0,0.2,1)";

declare namespace i4 {
    export {
        MatAccordion
    }
}

declare namespace i5 {
    export {
        MatExpansionPanelState,
        MatExpansionPanelDefaultOptions,
        MAT_EXPANSION_PANEL_DEFAULT_OPTIONS,
        MatExpansionPanel,
        MatExpansionPanelActionRow
    }
}

declare namespace i6 {
    export {
        MatExpansionPanelHeader,
        MatExpansionPanelDescription,
        MatExpansionPanelTitle
    }
}

declare namespace i7 {
    export {
        MatExpansionPanelContent
    }
}

/**
 * Token used to provide a `MatAccordion` to `MatExpansionPanel`.
 * Used primarily to avoid circular imports between `MatAccordion` and `MatExpansionPanel`.
 */
export declare const MAT_ACCORDION: InjectionToken<MatAccordionBase>;

/**
 * Token used to provide a `MatExpansionPanel` to `MatExpansionPanelContent`.
 * Used to avoid circular imports between `MatExpansionPanel` and `MatExpansionPanelContent`.
 */
export declare const MAT_EXPANSION_PANEL: InjectionToken<MatExpansionPanelBase>;

/**
 * Injection token that can be used to configure the default
 * options for the expansion panel component.
 */
export declare const MAT_EXPANSION_PANEL_DEFAULT_OPTIONS: InjectionToken<MatExpansionPanelDefaultOptions>;

/**
 * Directive for a Material Design Accordion.
 */
export declare class MatAccordion extends CdkAccordion implements MatAccordionBase, AfterContentInit, OnDestroy {
    private _keyManager;
    /** Headers belonging to this accordion. */
    private _ownHeaders;
    /** All headers inside the accordion. Includes headers inside nested accordions. */
    _headers: QueryList<MatExpansionPanelHeader>;
    /** Whether the expansion indicator should be hidden. */
    hideToggle: boolean;
    /**
     * Display mode used for all expansion panels in the accordion. Currently two display
     * modes exist:
     *  default - a gutter-like spacing is placed around any expanded panel, placing the expanded
     *     panel at a different elevation from the rest of the accordion.
     *  flat - no spacing is placed around expanded panels, showing all panels at the same
     *     elevation.
     */
    displayMode: MatAccordionDisplayMode;
    /** The position of the expansion indicator. */
    togglePosition: MatAccordionTogglePosition;
    ngAfterContentInit(): void;
    /** Handles keyboard events coming in from the panel headers. */
    _handleHeaderKeydown(event: KeyboardEvent): void;
    _handleHeaderFocus(header: MatExpansionPanelHeader): void;
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatAccordion, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatAccordion, "mat-accordion", ["matAccordion"], { "hideToggle": { "alias": "hideToggle"; "required": false; }; "displayMode": { "alias": "displayMode"; "required": false; }; "togglePosition": { "alias": "togglePosition"; "required": false; }; }, {}, ["_headers"], never, true, never>;
    static ngAcceptInputType_hideToggle: unknown;
}

/**
 * Base interface for a `MatAccordion`.
 * @docs-private
 */
export declare interface MatAccordionBase extends CdkAccordion {
    /** Whether the expansion indicator should be hidden. */
    hideToggle: boolean;
    /** Display mode used for all expansion panels in the accordion. */
    displayMode: MatAccordionDisplayMode;
    /** The position of the expansion indicator. */
    togglePosition: MatAccordionTogglePosition;
    /** Handles keyboard events coming in from the panel headers. */
    _handleHeaderKeydown: (event: KeyboardEvent) => void;
    /** Handles focus events on the panel headers. */
    _handleHeaderFocus: (header: any) => void;
}

/** MatAccordion's display modes. */
export declare type MatAccordionDisplayMode = 'default' | 'flat';

/** MatAccordion's toggle positions. */
export declare type MatAccordionTogglePosition = 'before' | 'after';

/**
 * Animations used by the Material expansion panel.
 *
 * A bug in angular animation's `state` when ViewContainers are moved using ViewContainerRef.move()
 * causes the animation state of moved components to become `void` upon exit, and not update again
 * upon reentry into the DOM.  This can lead a to situation for the expansion panel where the state
 * of the panel is `expanded` or `collapsed` but the animation state is `void`.
 *
 * To correctly handle animating to the next state, we animate between `void` and `collapsed` which
 * are defined to have the same styles. Since angular animates from the current styles to the
 * destination state's style definition, in situations where we are moving from `void`'s styles to
 * `collapsed` this acts a noop since no style values change.
 *
 * In the case where angular's animation state is out of sync with the expansion panel's state, the
 * expansion panel being `expanded` and angular animations being `void`, the animation from the
 * `expanded`'s effective styles (though in a `void` animation state) to the collapsed state will
 * occur as expected.
 *
 * Angular Bug: https://github.com/angular/angular/issues/18847
 *
 * @docs-private
 */
export declare const matExpansionAnimations: {
    readonly indicatorRotate: AnimationTriggerMetadata;
    readonly bodyExpansion: AnimationTriggerMetadata;
};

export declare class MatExpansionModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatExpansionModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<MatExpansionModule, never, [typeof i1.MatCommonModule, typeof i2.CdkAccordionModule, typeof i3.PortalModule, typeof i4.MatAccordion, typeof i5.MatExpansionPanel, typeof i5.MatExpansionPanelActionRow, typeof i6.MatExpansionPanelHeader, typeof i6.MatExpansionPanelTitle, typeof i6.MatExpansionPanelDescription, typeof i7.MatExpansionPanelContent], [typeof i4.MatAccordion, typeof i5.MatExpansionPanel, typeof i5.MatExpansionPanelActionRow, typeof i6.MatExpansionPanelHeader, typeof i6.MatExpansionPanelTitle, typeof i6.MatExpansionPanelDescription, typeof i7.MatExpansionPanelContent]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<MatExpansionModule>;
}

/**
 * This component can be used as a single element to show expandable content, or as one of
 * multiple children of an element with the MatAccordion directive attached.
 */
export declare class MatExpansionPanel extends CdkAccordionItem implements AfterContentInit, OnChanges, OnDestroy {
    private _viewContainerRef;
    _animationMode: string;
    protected _animationsDisabled: boolean;
    private _document;
    /** Whether the toggle indicator should be hidden. */
    get hideToggle(): boolean;
    set hideToggle(value: boolean);
    private _hideToggle;
    /** The position of the expansion indicator. */
    get togglePosition(): MatAccordionTogglePosition;
    set togglePosition(value: MatAccordionTogglePosition);
    private _togglePosition;
    /** An event emitted after the body's expansion animation happens. */
    readonly afterExpand: EventEmitter<void>;
    /** An event emitted after the body's collapse animation happens. */
    readonly afterCollapse: EventEmitter<void>;
    /** Stream that emits for changes in `@Input` properties. */
    readonly _inputChanges: Subject<SimpleChanges>;
    /** Optionally defined accordion the expansion panel belongs to. */
    accordion: MatAccordionBase;
    /** Content that will be rendered lazily. */
    _lazyContent: MatExpansionPanelContent;
    /** Element containing the panel's user-provided content. */
    _body: ElementRef<HTMLElement>;
    /** Portal holding the user's content. */
    _portal: TemplatePortal;
    /** ID for the associated header element. Used for a11y labelling. */
    _headerId: string;
    constructor(accordion: MatAccordionBase, _changeDetectorRef: ChangeDetectorRef, _uniqueSelectionDispatcher: UniqueSelectionDispatcher, _viewContainerRef: ViewContainerRef, _document: any, _animationMode: string, defaultOptions?: MatExpansionPanelDefaultOptions);
    /** Determines whether the expansion panel should have spacing between it and its siblings. */
    _hasSpacing(): boolean;
    /** Gets the expanded state string. */
    _getExpandedState(): MatExpansionPanelState;
    /** Toggles the expanded state of the expansion panel. */
    toggle(): void;
    /** Sets the expanded state of the expansion panel to false. */
    close(): void;
    /** Sets the expanded state of the expansion panel to true. */
    open(): void;
    ngAfterContentInit(): void;
    ngOnChanges(changes: SimpleChanges): void;
    ngOnDestroy(): void;
    /** Checks whether the expansion panel's content contains the currently-focused element. */
    _containsFocus(): boolean;
    /** Called when the expansion animation has started. */
    protected _animationStarted(event: AnimationEvent_2): void;
    /** Called when the expansion animation has finished. */
    protected _animationDone(event: AnimationEvent_2): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatExpansionPanel, [{ optional: true; skipSelf: true; }, null, null, null, null, { optional: true; }, { optional: true; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatExpansionPanel, "mat-expansion-panel", ["matExpansionPanel"], { "hideToggle": { "alias": "hideToggle"; "required": false; }; "togglePosition": { "alias": "togglePosition"; "required": false; }; }, { "afterExpand": "afterExpand"; "afterCollapse": "afterCollapse"; }, ["_lazyContent"], ["mat-expansion-panel-header", "*", "mat-action-row"], true, never>;
    static ngAcceptInputType_hideToggle: unknown;
}

/**
 * Actions of a `<mat-expansion-panel>`.
 */
export declare class MatExpansionPanelActionRow {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatExpansionPanelActionRow, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatExpansionPanelActionRow, "mat-action-row", never, {}, {}, never, never, true, never>;
}

/**
 * Base interface for a `MatExpansionPanel`.
 * @docs-private
 */
declare interface MatExpansionPanelBase extends CdkAccordionItem {
    /** Whether the toggle indicator should be hidden. */
    hideToggle: boolean;
}

/**
 * Expansion panel content that will be rendered lazily
 * after the panel is opened for the first time.
 */
export declare class MatExpansionPanelContent {
    _template: TemplateRef<any>;
    _expansionPanel?: MatExpansionPanelBase | undefined;
    constructor(_template: TemplateRef<any>, _expansionPanel?: MatExpansionPanelBase | undefined);
    static ɵfac: i0.ɵɵFactoryDeclaration<MatExpansionPanelContent, [null, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatExpansionPanelContent, "ng-template[matExpansionPanelContent]", never, {}, {}, never, never, true, never>;
}

/**
 * Object that can be used to override the default options
 * for all of the expansion panels in a module.
 */
export declare interface MatExpansionPanelDefaultOptions {
    /** Height of the header while the panel is expanded. */
    expandedHeight: string;
    /** Height of the header while the panel is collapsed. */
    collapsedHeight: string;
    /** Whether the toggle indicator should be hidden. */
    hideToggle: boolean;
}

/**
 * Description element of a `<mat-expansion-panel-header>`.
 */
export declare class MatExpansionPanelDescription {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatExpansionPanelDescription, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatExpansionPanelDescription, "mat-panel-description", never, {}, {}, never, never, true, never>;
}

/**
 * Header element of a `<mat-expansion-panel>`.
 */
export declare class MatExpansionPanelHeader implements AfterViewInit, OnDestroy, FocusableOption {
    panel: MatExpansionPanel;
    private _element;
    private _focusMonitor;
    private _changeDetectorRef;
    _animationMode?: string | undefined;
    private _parentChangeSubscription;
    constructor(panel: MatExpansionPanel, _element: ElementRef, _focusMonitor: FocusMonitor, _changeDetectorRef: ChangeDetectorRef, defaultOptions?: MatExpansionPanelDefaultOptions, _animationMode?: string | undefined, tabIndex?: string);
    /** Height of the header while the panel is expanded. */
    expandedHeight: string;
    /** Height of the header while the panel is collapsed. */
    collapsedHeight: string;
    /** Tab index of the header. */
    tabIndex: number;
    /**
     * Whether the associated panel is disabled. Implemented as a part of `FocusableOption`.
     * @docs-private
     */
    get disabled(): boolean;
    /** Toggles the expanded state of the panel. */
    _toggle(): void;
    /** Gets whether the panel is expanded. */
    _isExpanded(): boolean;
    /** Gets the expanded state string of the panel. */
    _getExpandedState(): string;
    /** Gets the panel id. */
    _getPanelId(): string;
    /** Gets the toggle position for the header. */
    _getTogglePosition(): MatAccordionTogglePosition;
    /** Gets whether the expand indicator should be shown. */
    _showToggle(): boolean;
    /**
     * Gets the current height of the header. Null if no custom height has been
     * specified, and if the default height from the stylesheet should be used.
     */
    _getHeaderHeight(): string | null;
    /** Handle keydown event calling to toggle() if appropriate. */
    _keydown(event: KeyboardEvent): void;
    /**
     * Focuses the panel header. Implemented as a part of `FocusableOption`.
     * @param origin Origin of the action that triggered the focus.
     * @docs-private
     */
    focus(origin?: FocusOrigin, options?: FocusOptions): void;
    ngAfterViewInit(): void;
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<MatExpansionPanelHeader, [{ host: true; }, null, null, null, { optional: true; }, { optional: true; }, { attribute: "tabindex"; }]>;
    static ɵcmp: i0.ɵɵComponentDeclaration<MatExpansionPanelHeader, "mat-expansion-panel-header", never, { "expandedHeight": { "alias": "expandedHeight"; "required": false; }; "collapsedHeight": { "alias": "collapsedHeight"; "required": false; }; "tabIndex": { "alias": "tabIndex"; "required": false; }; }, {}, never, ["mat-panel-title", "mat-panel-description", "*"], true, never>;
    static ngAcceptInputType_tabIndex: unknown;
}

/** MatExpansionPanel's states. */
export declare type MatExpansionPanelState = 'expanded' | 'collapsed';

/**
 * Title element of a `<mat-expansion-panel-header>`.
 */
export declare class MatExpansionPanelTitle {
    static ɵfac: i0.ɵɵFactoryDeclaration<MatExpansionPanelTitle, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<MatExpansionPanelTitle, "mat-panel-title", never, {}, {}, never, never, true, never>;
}

export { }
