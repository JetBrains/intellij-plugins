import * as i0 from '@angular/core';
import { OnDestroy, OnChanges, SimpleChanges, InjectionToken, OnInit, EventEmitter } from '@angular/core';
import { UniqueSelectionDispatcher } from '../unique-selection-dispatcher.d-BgWACqWn.js';
import { Subject } from 'rxjs';

/**
 * Injection token that can be used to reference instances of `CdkAccordion`. It serves
 * as alternative token to the actual `CdkAccordion` class which could cause unnecessary
 * retention of the class and its directive metadata.
 */
declare const CDK_ACCORDION: InjectionToken<CdkAccordion>;
/**
 * Directive whose purpose is to manage the expanded state of CdkAccordionItem children.
 */
declare class CdkAccordion implements OnDestroy, OnChanges {
    /** Emits when the state of the accordion changes */
    readonly _stateChanges: Subject<SimpleChanges>;
    /** Stream that emits true/false when openAll/closeAll is triggered. */
    readonly _openCloseAllActions: Subject<boolean>;
    /** A readonly id value to use for unique selection coordination. */
    readonly id: string;
    /** Whether the accordion should allow multiple expanded accordion items simultaneously. */
    multi: boolean;
    /** Opens all enabled accordion items in an accordion where multi is enabled. */
    openAll(): void;
    /** Closes all enabled accordion items. */
    closeAll(): void;
    ngOnChanges(changes: SimpleChanges): void;
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkAccordion, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkAccordion, "cdk-accordion, [cdkAccordion]", ["cdkAccordion"], { "multi": { "alias": "multi"; "required": false; }; }, {}, never, never, true, never>;
    static ngAcceptInputType_multi: unknown;
}

/**
 * A basic directive expected to be extended and decorated as a component.  Sets up all
 * events and attributes needed to be managed by a CdkAccordion parent.
 */
declare class CdkAccordionItem implements OnInit, OnDestroy {
    accordion: CdkAccordion;
    private _changeDetectorRef;
    protected _expansionDispatcher: UniqueSelectionDispatcher;
    /** Subscription to openAll/closeAll events. */
    private _openCloseAllSubscription;
    /** Event emitted every time the AccordionItem is closed. */
    readonly closed: EventEmitter<void>;
    /** Event emitted every time the AccordionItem is opened. */
    readonly opened: EventEmitter<void>;
    /** Event emitted when the AccordionItem is destroyed. */
    readonly destroyed: EventEmitter<void>;
    /**
     * Emits whenever the expanded state of the accordion changes.
     * Primarily used to facilitate two-way binding.
     * @docs-private
     */
    readonly expandedChange: EventEmitter<boolean>;
    /** The unique AccordionItem id. */
    readonly id: string;
    /** Whether the AccordionItem is expanded. */
    get expanded(): boolean;
    set expanded(expanded: boolean);
    private _expanded;
    /** Whether the AccordionItem is disabled. */
    disabled: boolean;
    /** Unregister function for _expansionDispatcher. */
    private _removeUniqueSelectionListener;
    constructor(...args: unknown[]);
    ngOnInit(): void;
    /** Emits an event for the accordion item being destroyed. */
    ngOnDestroy(): void;
    /** Toggles the expanded state of the accordion item. */
    toggle(): void;
    /** Sets the expanded state of the accordion item to false. */
    close(): void;
    /** Sets the expanded state of the accordion item to true. */
    open(): void;
    private _subscribeToOpenCloseAllActions;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkAccordionItem, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkAccordionItem, "cdk-accordion-item, [cdkAccordionItem]", ["cdkAccordionItem"], { "expanded": { "alias": "expanded"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; }, { "closed": "closed"; "opened": "opened"; "destroyed": "destroyed"; "expandedChange": "expandedChange"; }, never, never, true, never>;
    static ngAcceptInputType_expanded: unknown;
    static ngAcceptInputType_disabled: unknown;
}

declare class CdkAccordionModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkAccordionModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<CdkAccordionModule, never, [typeof CdkAccordion, typeof CdkAccordionItem], [typeof CdkAccordion, typeof CdkAccordionItem]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<CdkAccordionModule>;
}

export { CDK_ACCORDION, CdkAccordion, CdkAccordionItem, CdkAccordionModule };
