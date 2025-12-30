/**
 * @license Angular v19.2.0
 * (c) 2010-2024 Google LLC. https://angular.io/
 * License: MIT
 */


export declare type ComputationFn<S, D> = (source: S, previous?: {
    source: S;
    value: D;
}) => D;

declare type ComputedGetter<T> = (() => T) & {
    [SIGNAL]: ComputedNode<T>;
};

/**
 * A computation, which derives a value from a declarative reactive expression.
 *
 * `Computed`s are both producers and consumers of reactivity.
 */
export declare interface ComputedNode<T> extends ReactiveNode {
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

/**
 * Finalize this consumer's state after a reactive computation has run.
 *
 * Must be called by subclasses which represent reactive computations, after those computations
 * have finished.
 */
export declare function consumerAfterComputation(node: ReactiveNode | null, prevConsumer: ReactiveNode | null): void;

/**
 * Prepare this consumer to run a computation in its reactive context.
 *
 * Must be called by subclasses which represent reactive computations, before those computations
 * begin.
 */
export declare function consumerBeforeComputation(node: ReactiveNode | null): ReactiveNode | null;

/**
 * Disconnect this consumer from the graph.
 */
export declare function consumerDestroy(node: ReactiveNode): void;

export declare function consumerMarkDirty(node: ReactiveNode): void;

/**
 * Determine whether this consumer has any dependencies which have changed since the last time
 * they were read.
 */
export declare function consumerPollProducersForChange(node: ReactiveNode): boolean;

/**
 * Create a computed signal which derives a reactive value from an expression.
 */
export declare function createComputed<T>(computation: () => T): ComputedGetter<T>;

export declare function createLinkedSignal<S, D>(sourceFn: () => S, computationFn: ComputationFn<S, D>, equalityFn?: ValueEqualityFn<D>): LinkedSignalGetter<S, D>;

/**
 * Create a `Signal` that can be set or updated directly.
 */
export declare function createSignal<T>(initialValue: T): SignalGetter<T>;

export declare function createWatch(fn: (onCleanup: WatchCleanupRegisterFn) => void, schedule: (watch: Watch) => void, allowSignalWrites: boolean): Watch;

/**
 * The default equality function used for `signal` and `computed`, which uses referential equality.
 */
export declare function defaultEquals<T>(a: T, b: T): boolean;

export declare function getActiveConsumer(): ReactiveNode | null;

export declare function isInNotificationPhase(): boolean;

export declare function isReactive(value: unknown): value is Reactive;

export declare type LinkedSignalGetter<S, D> = (() => D) & {
    [SIGNAL]: LinkedSignalNode<S, D>;
};

export declare interface LinkedSignalNode<S, D> extends ReactiveNode {
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

export declare function linkedSignalSetFn<S, D>(node: LinkedSignalNode<S, D>, newValue: D): void;

export declare function linkedSignalUpdateFn<S, D>(node: LinkedSignalNode<S, D>, updater: (value: D) => D): void;

/**
 * Called by implementations when a producer's signal is read.
 */
export declare function producerAccessed(node: ReactiveNode): void;

/**
 * Increment the global epoch counter.
 *
 * Called by source producers (that is, not computeds) whenever their values change.
 */
export declare function producerIncrementEpoch(): void;

export declare function producerMarkClean(node: ReactiveNode): void;

/**
 * Propagate a dirty notification to live consumers of this producer.
 */
export declare function producerNotifyConsumers(node: ReactiveNode): void;

/**
 * Whether this `ReactiveNode` in its producer capacity is currently allowed to initiate updates,
 * based on the current consumer context.
 */
export declare function producerUpdatesAllowed(): boolean;

/**
 * Ensure this producer's `version` is up-to-date.
 */
export declare function producerUpdateValueVersion(node: ReactiveNode): void;

export declare interface Reactive {
    [SIGNAL]: ReactiveNode;
}

export declare const REACTIVE_NODE: ReactiveNode;

/**
 * A producer and/or consumer which participates in the reactive graph.
 *
 * Producer `ReactiveNode`s which are accessed when a consumer `ReactiveNode` is the
 * `activeConsumer` are tracked as dependencies of that consumer.
 *
 * Certain consumers are also tracked as "live" consumers and create edges in the other direction,
 * from producer to consumer. These edges are used to propagate change notifications when a
 * producer's value is updated.
 *
 * A `ReactiveNode` may be both a producer and consumer.
 */
export declare interface ReactiveNode {
    /**
     * Version of the value that this node produces.
     *
     * This is incremented whenever a new value is produced by this node which is not equal to the
     * previous value (by whatever definition of equality is in use).
     */
    version: Version;
    /**
     * Epoch at which this node is verified to be clean.
     *
     * This allows skipping of some polling operations in the case where no signals have been set
     * since this node was last read.
     */
    lastCleanEpoch: Version;
    /**
     * Whether this node (in its consumer capacity) is dirty.
     *
     * Only live consumers become dirty, when receiving a change notification from a dependency
     * producer.
     */
    dirty: boolean;
    /**
     * Producers which are dependencies of this consumer.
     *
     * Uses the same indices as the `producerLastReadVersion` and `producerIndexOfThis` arrays.
     */
    producerNode: ReactiveNode[] | undefined;
    /**
     * `Version` of the value last read by a given producer.
     *
     * Uses the same indices as the `producerNode` and `producerIndexOfThis` arrays.
     */
    producerLastReadVersion: Version[] | undefined;
    /**
     * Index of `this` (consumer) in each producer's `liveConsumers` array.
     *
     * This value is only meaningful if this node is live (`liveConsumers.length > 0`). Otherwise
     * these indices are stale.
     *
     * Uses the same indices as the `producerNode` and `producerLastReadVersion` arrays.
     */
    producerIndexOfThis: number[] | undefined;
    /**
     * Index into the producer arrays that the next dependency of this node as a consumer will use.
     *
     * This index is zeroed before this node as a consumer begins executing. When a producer is read,
     * it gets inserted into the producers arrays at this index. There may be an existing dependency
     * in this location which may or may not match the incoming producer, depending on whether the
     * same producers were read in the same order as the last computation.
     */
    nextProducerIndex: number;
    /**
     * Array of consumers of this producer that are "live" (they require push notifications).
     *
     * `liveConsumerNode.length` is effectively our reference count for this node.
     */
    liveConsumerNode: ReactiveNode[] | undefined;
    /**
     * Index of `this` (producer) in each consumer's `producerNode` array.
     *
     * Uses the same indices as the `liveConsumerNode` array.
     */
    liveConsumerIndexOfThis: number[] | undefined;
    /**
     * Whether writes to signals are allowed when this consumer is the `activeConsumer`.
     *
     * This is used to enforce guardrails such as preventing writes to writable signals in the
     * computation function of computed signals, which is supposed to be pure.
     */
    consumerAllowSignalWrites: boolean;
    readonly consumerIsAlwaysLive: boolean;
    /**
     * Tracks whether producers need to recompute their value independently of the reactive graph (for
     * example, if no initial value has been computed).
     */
    producerMustRecompute(node: unknown): boolean;
    producerRecomputeValue(node: unknown): void;
    consumerMarkedDirty(node: unknown): void;
    /**
     * Called when a signal is read within this consumer.
     */
    consumerOnSignalRead(node: unknown): void;
    /**
     * A debug name for the reactive node. Used in Angular DevTools to identify the node.
     */
    debugName?: string;
    /**
     * Kind of node. Example: 'signal', 'computed', 'input', 'effect'.
     *
     * ReactiveNode has this as 'unknown' by default, but derived node types should override this to
     * make available the kind of signal that particular instance of a ReactiveNode represents.
     *
     * Used in Angular DevTools to identify the kind of signal.
     */
    kind: string;
}

export declare function runPostSignalSetFn(): void;

export declare function setActiveConsumer(consumer: ReactiveNode | null): ReactiveNode | null;


export declare function setAlternateWeakRefImpl(impl: unknown): void;

export declare function setPostSignalSetFn(fn: (() => void) | null): (() => void) | null;

export declare function setThrowInvalidWriteToSignalError(fn: <T>(node: SignalNode<T>) => never): void;

/**
 * Symbol used to tell `Signal`s apart from other functions.
 *
 * This can be used to auto-unwrap signals in various cases, or to auto-wrap non-signal values.
 */
export declare const SIGNAL: unique symbol;

export declare const SIGNAL_NODE: SignalNode<unknown>;

declare type SignalBaseGetter<T> = (() => T) & {
    readonly [SIGNAL]: unknown;
};

export declare interface SignalGetter<T> extends SignalBaseGetter<T> {
    readonly [SIGNAL]: SignalNode<T>;
}

export declare interface SignalNode<T> extends ReactiveNode {
    value: T;
    equal: ValueEqualityFn<T>;
}

export declare function signalSetFn<T>(node: SignalNode<T>, newValue: T): void;

export declare function signalUpdateFn<T>(node: SignalNode<T>, updater: (value: T) => T): void;


/**
 * A comparison function which can determine if two values are equal.
 */
export declare type ValueEqualityFn<T> = (a: T, b: T) => boolean;


declare type Version = number & {
    __brand: 'Version';
};

export declare interface Watch {
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

/**
 * A cleanup function that can be optionally registered from the watch logic. If registered, the
 * cleanup logic runs before the next watch execution.
 */
export declare type WatchCleanupFn = () => void;

/**
 * A callback passed to the watch function that makes it possible to register cleanup logic.
 */
export declare type WatchCleanupRegisterFn = (cleanupFn: WatchCleanupFn) => void;

declare interface WatchNode extends ReactiveNode {
    hasRun: boolean;
    fn: ((onCleanup: WatchCleanupRegisterFn) => void) | null;
    schedule: ((watch: Watch) => void) | null;
    cleanupFn: WatchCleanupFn;
    ref: Watch;
}

export { }
