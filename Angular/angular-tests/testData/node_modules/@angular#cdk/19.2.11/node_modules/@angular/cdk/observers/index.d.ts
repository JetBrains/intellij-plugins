import { NumberInput } from '../number-property.d-BzBQchZ2.js';
import * as i0 from '@angular/core';
import { AfterContentInit, OnDestroy, EventEmitter, ElementRef } from '@angular/core';
import { Observable } from 'rxjs';

/**
 * Factory that creates a new MutationObserver and allows us to stub it out in unit tests.
 * @docs-private
 */
declare class MutationObserverFactory {
    create(callback: MutationCallback): MutationObserver | null;
    static ɵfac: i0.ɵɵFactoryDeclaration<MutationObserverFactory, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<MutationObserverFactory>;
}
/** An injectable service that allows watching elements for changes to their content. */
declare class ContentObserver implements OnDestroy {
    private _mutationObserverFactory;
    /** Keeps track of the existing MutationObservers so they can be reused. */
    private _observedElements;
    private _ngZone;
    constructor(...args: unknown[]);
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
 * Directive that triggers a callback whenever the content of
 * its associated element has changed.
 */
declare class CdkObserveContent implements AfterContentInit, OnDestroy {
    private _contentObserver;
    private _elementRef;
    /** Event emitted for each change in the element's content. */
    readonly event: EventEmitter<MutationRecord[]>;
    /**
     * Whether observing content is disabled. This option can be used
     * to disconnect the underlying MutationObserver until it is needed.
     */
    get disabled(): boolean;
    set disabled(value: boolean);
    private _disabled;
    /** Debounce interval for emitting the changes. */
    get debounce(): number;
    set debounce(value: NumberInput);
    private _debounce;
    private _currentSubscription;
    constructor(...args: unknown[]);
    ngAfterContentInit(): void;
    ngOnDestroy(): void;
    private _subscribe;
    private _unsubscribe;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkObserveContent, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkObserveContent, "[cdkObserveContent]", ["cdkObserveContent"], { "disabled": { "alias": "cdkObserveContentDisabled"; "required": false; }; "debounce": { "alias": "debounce"; "required": false; }; }, { "event": "cdkObserveContent"; }, never, never, true, never>;
    static ngAcceptInputType_disabled: unknown;
}
declare class ObserversModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<ObserversModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<ObserversModule, never, [typeof CdkObserveContent], [typeof CdkObserveContent]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<ObserversModule>;
}

export { CdkObserveContent, ContentObserver, MutationObserverFactory, ObserversModule };
