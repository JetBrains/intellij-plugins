import { BooleanInput } from '@angular/cdk/coercion';
import { ChangeDetectorRef } from '@angular/core';
import { EventEmitter } from '@angular/core';
import * as i0 from '@angular/core';
import { InjectionToken } from '@angular/core';
import { OnChanges } from '@angular/core';
import { OnDestroy } from '@angular/core';
import { SimpleChanges } from '@angular/core';
import { Subject } from 'rxjs';
import { UniqueSelectionDispatcher } from '@angular/cdk/collections';

/**
 * Injection token that can be used to reference instances of `CdkAccordion`. It serves
 * as alternative token to the actual `CdkAccordion` class which could cause unnecessary
 * retention of the class and its directive metadata.
 */
declare const CDK_ACCORDION: InjectionToken<CdkAccordion>;

/**
 * Directive whose purpose is to manage the expanded state of CdkAccordionItem children.
 */
export declare class CdkAccordion implements OnDestroy, OnChanges {
    /** Emits when the state of the accordion changes */
    readonly _stateChanges: Subject<SimpleChanges>;
    /** Stream that emits true/false when openAll/closeAll is triggered. */
    readonly _openCloseAllActions: Subject<boolean>;
    /** A readonly id value to use for unique selection coordination. */
    readonly id: string;
    /** Whether the accordion should allow multiple expanded accordion items simultaneously. */
    get multi(): boolean;
    set multi(multi: BooleanInput);
    private _multi;
    /** Opens all enabled accordion items in an accordion where multi is enabled. */
    openAll(): void;
    /** Closes all enabled accordion items in an accordion where multi is enabled. */
    closeAll(): void;
    ngOnChanges(changes: SimpleChanges): void;
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkAccordion, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkAccordion, "cdk-accordion, [cdkAccordion]", ["cdkAccordion"], { "multi": "multi"; }, {}, never, never, false>;
}

/**
 * An basic directive expected to be extended and decorated as a component.  Sets up all
 * events and attributes needed to be managed by a CdkAccordion parent.
 */
export declare class CdkAccordionItem implements OnDestroy {
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
    set expanded(expanded: BooleanInput);
    private _expanded;
    /** Whether the AccordionItem is disabled. */
    get disabled(): boolean;
    set disabled(disabled: BooleanInput);
    private _disabled;
    /** Unregister function for _expansionDispatcher. */
    private _removeUniqueSelectionListener;
    constructor(accordion: CdkAccordion, _changeDetectorRef: ChangeDetectorRef, _expansionDispatcher: UniqueSelectionDispatcher);
    /** Emits an event for the accordion item being destroyed. */
    ngOnDestroy(): void;
    /** Toggles the expanded state of the accordion item. */
    toggle(): void;
    /** Sets the expanded state of the accordion item to false. */
    close(): void;
    /** Sets the expanded state of the accordion item to true. */
    open(): void;
    private _subscribeToOpenCloseAllActions;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkAccordionItem, [{ optional: true; skipSelf: true; }, null, null]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkAccordionItem, "cdk-accordion-item, [cdkAccordionItem]", ["cdkAccordionItem"], { "expanded": "expanded"; "disabled": "disabled"; }, { "closed": "closed"; "opened": "opened"; "destroyed": "destroyed"; "expandedChange": "expandedChange"; }, never, never, false>;
}

export declare class CdkAccordionModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkAccordionModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<CdkAccordionModule, [typeof i1.CdkAccordion, typeof i2.CdkAccordionItem], never, [typeof i1.CdkAccordion, typeof i2.CdkAccordionItem]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<CdkAccordionModule>;
}

declare namespace i1 {
    export {
        CDK_ACCORDION,
        CdkAccordion
    }
}

declare namespace i2 {
    export {
        CdkAccordionItem
    }
}

export { }
