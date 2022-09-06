import { AfterContentInit } from '@angular/core';
import { BooleanInput } from '@angular/cdk/coercion';
import { ElementRef } from '@angular/core';
import { EventEmitter } from '@angular/core';
import * as i0 from '@angular/core';
import { NgZone } from '@angular/core';
import { NumberInput } from '@angular/cdk/coercion';
import { Observable } from 'rxjs';
import { OnDestroy } from '@angular/core';

/**
 * Directive that triggers a callback whenever the content of
 * its associated element has changed.
 */
export declare class CdkObserveContent implements AfterContentInit, OnDestroy {
    private _contentObserver;
    private _elementRef;
    private _ngZone;
    /** Event emitted for each change in the element's content. */
    readonly event: EventEmitter<MutationRecord[]>;
    /**
     * Whether observing content is disabled. This option can be used
     * to disconnect the underlying MutationObserver until it is needed.
     */
    get disabled(): boolean;
    set disabled(value: BooleanInput);
    private _disabled;
    /** Debounce interval for emitting the changes. */
    get debounce(): number;
    set debounce(value: NumberInput);
    private _debounce;
    private _currentSubscription;
    constructor(_contentObserver: ContentObserver, _elementRef: ElementRef<HTMLElement>, _ngZone: NgZone);
    ngAfterContentInit(): void;
    ngOnDestroy(): void;
    private _subscribe;
    private _unsubscribe;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkObserveContent, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkObserveContent, "[cdkObserveContent]", ["cdkObserveContent"], { "disabled": "cdkObserveContentDisabled"; "debounce": "debounce"; }, { "event": "cdkObserveContent"; }, never, never, false>;
}

/** An injectable service that allows watching elements for changes to their content. */
export declare class ContentObserver implements OnDestroy {
    private _mutationObserverFactory;
    /** Keeps track of the existing MutationObservers so they can be reused. */
    private _observedElements;
    constructor(_mutationObserverFactory: MutationObserverFactory);
    ngOnDestroy(): void;
    /**
     * Observe content changes on an element.
     * @param element The element to observe for content changes.
     */
    observe(element: Element): Observable<MutationRecord[]>;
    /**
     * Observe content changes on an element.
     * @param element The element to observe for content changes.
     */
    observe(element: ElementRef<Element>): Observable<MutationRecord[]>;
    /**
     * Observes the given element by using the existing MutationObserver if available, or creating a
     * new one if not.
     */
    private _observeElement;
    /**
     * Un-observes the given element and cleans up the underlying MutationObserver if nobody else is
     * observing this element.
     */
    private _unobserveElement;
    /** Clean up the underlying MutationObserver for the specified element. */
    private _cleanupObserver;
    static ɵfac: i0.ɵɵFactoryDeclaration<ContentObserver, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<ContentObserver>;
}

/**
 * Factory that creates a new MutationObserver and allows us to stub it out in unit tests.
 * @docs-private
 */
export declare class MutationObserverFactory {
    create(callback: MutationCallback): MutationObserver | null;
    static ɵfac: i0.ɵɵFactoryDeclaration<MutationObserverFactory, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<MutationObserverFactory>;
}

export declare class ObserversModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<ObserversModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<ObserversModule, [typeof CdkObserveContent], never, [typeof CdkObserveContent]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<ObserversModule>;
}

export { }
