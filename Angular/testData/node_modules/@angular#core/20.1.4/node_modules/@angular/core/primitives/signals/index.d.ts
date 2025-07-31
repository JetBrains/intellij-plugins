/**
 * @license Angular v20.1.4
 * (c) 2010-2025 Google LLC. https://angular.io/
 * License: MIT
 */

import { ReactiveNode, ValueEqualityFn, SIGNAL } from '../../graph.d.js';
export { REACTIVE_NODE, Reactive, ReactiveHookFn, consumerAfterComputation, consumerBeforeComputation, consumerDestroy, consumerMarkDirty, consumerPollProducersForChange, defaultEquals, getActiveConsumer, isInNotificationPhase, isReactive, producerAccessed, producerIncrementEpoch, producerMarkClean, producerNotifyConsumers, producerUpdateValueVersion, producerUpdatesAllowed, runPostProducerCreatedFn, setActiveConsumer, setPostProducerCreatedFn } from '../../graph.d.js';
import { SignalNode } from '../../signal.d.js';
export { SIGNAL_NODE, SignalGetter, createSignal, runPostSignalSetFn, setPostSignalSetFn, signalGetFn, signalSetFn, signalUpdateFn } from '../../signal.d.js';
export { setAlternateWeakRefImpl } from '../../weak_ref.d.js';

/**
 * A computation, which derives a value from a declarative reactive expression.
 *
 * `Computed`s are both producers and consumers of reactivity.
 */
interface ComputedNode<T> extends ReactiveNode {
    /**
     * Current value of the computation, or one of the sentinel values above (`UNSET`, `COMPUTING`,
     * `ERROR`).
     */
    value: T;
    /**
     * If `value` is `ERRORED`, the error caught from the last computation attempt which will
     * be re-thrown.
     */
    error: unknown;
    /**
     * The computation function which will produce a new value.
     */
    computation: () => T;
    equal: ValueEqualityFn<T>;
}
type ComputedGetter<T> = (() => T) & {
    [SIGNAL]: ComputedNode<T>;
};
/**
 * Create a computed signal which derives a reactive value from an expression.
 */
declare function createComputed<T>(computation: () => T, equal?: ValueEqualityFn<T>): ComputedGetter<T>;

type ComputationFn<S, D> = (source: S, previous?: {
    source: S;
    value: D;
}) => D;
interface LinkedSignalNode<S, D> extends ReactiveNode {
    /**
     * Value of the source signal that was used to derive the computed value.
     */
    sourceValue: S;
    /**
     * Current state value, or one of the sentinel values (`UNSET`, `COMPUTING`,
     * `ERROR`).
     */
    value: D;
    /**
     * If `value` is `ERRORED`, the error caught from the last computation attempt which will
     * be re-thrown.
     */
    error: unknown;
    /**
     * The source function represents reactive dependency based on which the linked state is reset.
     */
    source: () => S;
    /**
     * The computation function which will produce a new value based on the source and, optionally - previous values.
     */
    computation: ComputationFn<S, D>;
    equal: ValueEqualityFn<D>;
}
type LinkedSignalGetter<S, D> = (() => D) & {
    [SIGNAL]: LinkedSignalNode<S, D>;
};
declare function createLinkedSignal<S, D>(sourceFn: () => S, computationFn: ComputationFn<S, D>, equalityFn?: ValueEqualityFn<D>): LinkedSignalGetter<S, D>;
declare function linkedSignalSetFn<S, D>(node: LinkedSignalNode<S, D>, newValue: D): void;
declare function linkedSignalUpdateFn<S, D>(node: LinkedSignalNode<S, D>, updater: (value: D) => D): void;

declare function setThrowInvalidWriteToSignalError(fn: <T>(node: SignalNode<T>) => never): void;

/**
 * A cleanup function that can be optionally registered from the watch logic. If registered, the
 * cleanup logic runs before the next watch execution.
 */
type WatchCleanupFn = () => void;
/**
 * A callback passed to the watch function that makes it possible to register cleanup logic.
 */
type WatchCleanupRegisterFn = (cleanupFn: WatchCleanupFn) => void;
interface Watch {
    notify(): void;
    /**
     * Execute the reactive expression in the context of this `Watch` consumer.
     *
     * Should be called by the user scheduling algorithm when the provided
     * `schedule` hook is called by `Watch`.
     */
    run(): void;
    cleanup(): void;
    /**
     * Destroy the watcher:
     * - disconnect it from the reactive graph;
     * - mark it as destroyed so subsequent run and notify operations are noop.
     */
    destroy(): void;
    [SIGNAL]: WatchNode;
}
interface WatchNode extends ReactiveNode {
    hasRun: boolean;
    fn: ((onCleanup: WatchCleanupRegisterFn) => void) | null;
    schedule: ((watch: Watch) => void) | null;
    cleanupFn: WatchCleanupFn;
    ref: Watch;
}
declare function createWatch(fn: (onCleanup: WatchCleanupRegisterFn) => void, schedule: (watch: Watch) => void, allowSignalWrites: boolean): Watch;

/**
 * Execute an arbitrary function in a non-reactive (non-tracking) context. The executed function
 * can, optionally, return a value.
 */
declare function untracked<T>(nonReactiveReadsFn: () => T): T;

export { ReactiveNode, SIGNAL, SignalNode, ValueEqualityFn, createComputed, createLinkedSignal, createWatch, linkedSignalSetFn, linkedSignalUpdateFn, setThrowInvalidWriteToSignalError, untracked };
export type { ComputationFn, ComputedNode, LinkedSignalGetter, LinkedSignalNode, Watch, WatchCleanupFn, WatchCleanupRegisterFn };
