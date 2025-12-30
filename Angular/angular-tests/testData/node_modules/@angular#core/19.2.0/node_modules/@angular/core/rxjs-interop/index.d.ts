/**
 * @license Angular v19.2.0
 * (c) 2010-2024 Google LLC. https://angular.io/
 * License: MIT
 */


import { BaseResourceOptions } from '@angular/core';
import { DestroyRef } from '@angular/core';
import { Injector } from '@angular/core';
import { MonoTypeOperatorFunction } from 'rxjs';
import { Observable } from 'rxjs';
import { OutputOptions } from '@angular/core';
import { OutputRef } from '@angular/core';
import { ResourceLoaderParams } from '@angular/core';
import { ResourceRef } from '@angular/core';
import { Signal } from '@angular/core';
import { Subscribable } from 'rxjs';
import { ValueEqualityFn } from '@angular/core/primitives/signals';

/**
 * Declares an Angular output that is using an RxJS observable as a source
 * for events dispatched to parent subscribers.
 *
 * The behavior for an observable as source is defined as followed:
 *    1. New values are forwarded to the Angular output (next notifications).
 *    2. Errors notifications are not handled by Angular. You need to handle these manually.
 *       For example by using `catchError`.
 *    3. Completion notifications stop the output from emitting new values.
 *
 * @usageNotes
 * Initialize an output in your directive by declaring a
 * class field and initializing it with the `outputFromObservable()` function.
 *
 * ```ts
 * @Directive({..})
 * export class MyDir {
 *   nameChange$ = <some-observable>;
 *   nameChange = outputFromObservable(this.nameChange$);
 * }
 * ```
 *
 * @publicApi
 */
export declare function outputFromObservable<T>(observable: Observable<T>, opts?: OutputOptions): OutputRef<T>;

/**
 * Converts an Angular output declared via `output()` or `outputFromObservable()`
 * to an observable.
 *
 * You can subscribe to the output via `Observable.subscribe` then.
 *
 * @publicApi
 */
export declare function outputToObservable<T>(ref: OutputRef<T>): Observable<T>;

/**
 * Operator which makes the application unstable until the observable emits, complets, errors, or is unsubscribed.
 *
 * Use this operator in observables whose subscriptions are important for rendering and should be included in SSR serialization.
 *
 * @param injector The `Injector` to use during creation. If this is not provided, the current injection context will be used instead (via `inject`).
 *
 * @experimental
 */
export declare function pendingUntilEvent<T>(injector?: Injector): MonoTypeOperatorFunction<T>;

/**
 * Like `resource` but uses an RxJS based `loader` which maps the request to an `Observable` of the
 * resource's value.
 *
 * @experimental
 */
export declare function rxResource<T, R>(opts: RxResourceOptions<T, R> & {
    defaultValue: NoInfer<T>;
}): ResourceRef<T>;

/**
 * Like `resource` but uses an RxJS based `loader` which maps the request to an `Observable` of the
 * resource's value.
 *
 * @experimental
 */
export declare function rxResource<T, R>(opts: RxResourceOptions<T, R>): ResourceRef<T | undefined>;

/**
 * Like `ResourceOptions` but uses an RxJS-based `loader`.
 *
 * @experimental
 */
export declare interface RxResourceOptions<T, R> extends BaseResourceOptions<T, R> {
    loader: (params: ResourceLoaderParams<R>) => Observable<T>;
}

/**
 * Operator which completes the Observable when the calling context (component, directive, service,
 * etc) is destroyed.
 *
 * @param destroyRef optionally, the `DestroyRef` representing the current context. This can be
 *     passed explicitly to use `takeUntilDestroyed` outside of an [injection
 * context](guide/di/dependency-injection-context). Otherwise, the current `DestroyRef` is injected.
 *
 * @publicApi
 */
export declare function takeUntilDestroyed<T>(destroyRef?: DestroyRef): MonoTypeOperatorFunction<T>;

/**
 * Exposes the value of an Angular `Signal` as an RxJS `Observable`.
 *
 * The signal's value will be propagated into the `Observable`'s subscribers using an `effect`.
 *
 * `toObservable` must be called in an injection context unless an injector is provided via options.
 *
 * @developerPreview
 */
export declare function toObservable<T>(source: Signal<T>, options?: ToObservableOptions): Observable<T>;

/**
 * Options for `toObservable`.
 *
 * @developerPreview
 */
export declare interface ToObservableOptions {
    /**
     * The `Injector` to use when creating the underlying `effect` which watches the signal.
     *
     * If this isn't specified, the current [injection context](guide/di/dependency-injection-context)
     * will be used.
     */
    injector?: Injector;
}

export declare function toSignal<T>(source: Observable<T> | Subscribable<T>): Signal<T | undefined>;

export declare function toSignal<T>(source: Observable<T> | Subscribable<T>, options: NoInfer<ToSignalOptions<T | undefined>> & {
    initialValue?: undefined;
    requireSync?: false;
}): Signal<T | undefined>;

export declare function toSignal<T>(source: Observable<T> | Subscribable<T>, options: NoInfer<ToSignalOptions<T | null>> & {
    initialValue?: null;
    requireSync?: false;
}): Signal<T | null>;

export declare function toSignal<T>(source: Observable<T> | Subscribable<T>, options: NoInfer<ToSignalOptions<T>> & {
    initialValue?: undefined;
    requireSync: true;
}): Signal<T>;

export declare function toSignal<T, const U extends T>(source: Observable<T> | Subscribable<T>, options: NoInfer<ToSignalOptions<T | U>> & {
    initialValue: U;
    requireSync?: false;
}): Signal<T | U>;

/**
 * Options for `toSignal`.
 *
 * @publicApi
 */
export declare interface ToSignalOptions<T> {
    /**
     * Initial value for the signal produced by `toSignal`.
     *
     * This will be the value of the signal until the observable emits its first value.
     */
    initialValue?: unknown;
    /**
     * Whether to require that the observable emits synchronously when `toSignal` subscribes.
     *
     * If this is `true`, `toSignal` will assert that the observable produces a value immediately upon
     * subscription. Setting this option removes the need to either deal with `undefined` in the
     * signal type or provide an `initialValue`, at the cost of a runtime error if this requirement is
     * not met.
     */
    requireSync?: boolean;
    /**
     * `Injector` which will provide the `DestroyRef` used to clean up the Observable subscription.
     *
     * If this is not provided, a `DestroyRef` will be retrieved from the current [injection
     * context](guide/di/dependency-injection-context), unless manual cleanup is requested.
     */
    injector?: Injector;
    /**
     * Whether the subscription should be automatically cleaned up (via `DestroyRef`) when
     * `toSignal`'s creation context is destroyed.
     *
     * If manual cleanup is enabled, then `DestroyRef` is not used, and the subscription will persist
     * until the `Observable` itself completes.
     */
    manualCleanup?: boolean;
    /**
     * Whether `toSignal` should throw errors from the Observable error channel back to RxJS, where
     * they'll be processed as uncaught exceptions.
     *
     * In practice, this means that the signal returned by `toSignal` will keep returning the last
     * good value forever, as Observables which error produce no further values. This option emulates
     * the behavior of the `async` pipe.
     */
    rejectErrors?: boolean;
    /**
     * A comparison function which defines equality for values emitted by the observable.
     *
     * Equality comparisons are executed against the initial value if one is provided.
     */
    equal?: ValueEqualityFn<T>;
}

export declare function ɵtoObservableMicrotask<T>(source: Signal<T>, options?: ToObservableOptions): Observable<T>;

export { }
