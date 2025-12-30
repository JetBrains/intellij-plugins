import { Observable } from 'rxjs';
import { DataSource } from '../data-source.d-DAIyaEMO.js';
export { CollectionViewer, ListRange, isDataSource } from '../data-source.d-DAIyaEMO.js';
import { IterableChanges, ViewContainerRef } from '@angular/core';
import { _ViewRepeaterItemContext, _ViewRepeater, _ViewRepeaterItemContextFactory, _ViewRepeaterItemValueResolver, _ViewRepeaterItemChanged } from '../view-repeater.d-CJ9e48MX.js';
export { _VIEW_REPEATER_STRATEGY, _ViewRepeaterItemChange, _ViewRepeaterItemInsertArgs, _ViewRepeaterOperation } from '../view-repeater.d-CJ9e48MX.js';
import { SelectionModel } from '../selection-model.d-mtbiEbzs.js';
export { SelectionChange, getMultipleValuesInSingleSelectionError } from '../selection-model.d-mtbiEbzs.js';
export { UniqueSelectionDispatcher, UniqueSelectionDispatcherListener } from '../unique-selection-dispatcher.d-BgWACqWn.js';

/** DataSource wrapper for a native array. */
declare class ArrayDataSource<T> extends DataSource<T> {
    private _data;
    constructor(_data: readonly T[] | Observable<readonly T[]>);
    connect(): Observable<readonly T[]>;
    disconnect(): void;
}

/**
 * A repeater that destroys views when they are removed from a
 * {@link ViewContainerRef}. When new items are inserted into the container,
 * the repeater will always construct a new embedded view for each item.
 *
 * @template T The type for the embedded view's $implicit property.
 * @template R The type for the item in each IterableDiffer change record.
 * @template C The type for the context passed to each embedded view.
 */
declare class _DisposeViewRepeaterStrategy<T, R, C extends _ViewRepeaterItemContext<T>> implements _ViewRepeater<T, R, C> {
    applyChanges(changes: IterableChanges<R>, viewContainerRef: ViewContainerRef, itemContextFactory: _ViewRepeaterItemContextFactory<T, R, C>, itemValueResolver: _ViewRepeaterItemValueResolver<T, R>, itemViewChanged?: _ViewRepeaterItemChanged<R, C>): void;
    detach(): void;
}

/**
 * A repeater that caches views when they are removed from a
 * {@link ViewContainerRef}. When new items are inserted into the container,
 * the repeater will reuse one of the cached views instead of creating a new
 * embedded view. Recycling cached views reduces the quantity of expensive DOM
 * inserts.
 *
 * @template T The type for the embedded view's $implicit property.
 * @template R The type for the item in each IterableDiffer change record.
 * @template C The type for the context passed to each embedded view.
 */
declare class _RecycleViewRepeaterStrategy<T, R, C extends _ViewRepeaterItemContext<T>> implements _ViewRepeater<T, R, C> {
    /**
     * The size of the cache used to store unused views.
     * Setting the cache size to `0` will disable caching. Defaults to 20 views.
     */
    viewCacheSize: number;
    /**
     * View cache that stores embedded view instances that have been previously stamped out,
     * but don't are not currently rendered. The view repeater will reuse these views rather than
     * creating brand new ones.
     *
     * TODO(michaeljamesparsons) Investigate whether using a linked list would improve performance.
     */
    private _viewCache;
    /** Apply changes to the DOM. */
    applyChanges(changes: IterableChanges<R>, viewContainerRef: ViewContainerRef, itemContextFactory: _ViewRepeaterItemContextFactory<T, R, C>, itemValueResolver: _ViewRepeaterItemValueResolver<T, R>, itemViewChanged?: _ViewRepeaterItemChanged<R, C>): void;
    detach(): void;
    /**
     * Inserts a view for a new item, either from the cache or by creating a new
     * one. Returns `undefined` if the item was inserted into a cached view.
     */
    private _insertView;
    /** Detaches the view at the given index and inserts into the view cache. */
    private _detachAndCacheView;
    /** Moves view at the previous index to the current index. */
    private _moveView;
    /**
     * Cache the given detached view. If the cache is full, the view will be
     * destroyed.
     */
    private _maybeCacheView;
    /** Inserts a recycled view from the cache at the given index. */
    private _insertViewFromCache;
}

/**
 * Interface for a class that can flatten hierarchical structured data and re-expand the flattened
 * data back into its original structure. Should be used in conjunction with the cdk-tree.
 */
interface TreeDataNodeFlattener<T> {
    /** Transforms a set of hierarchical structured data into a flattened data array. */
    flattenNodes(structuredData: any[]): T[];
    /**
     * Expands a flattened array of data into its hierarchical form using the provided expansion
     * model.
     */
    expandFlattenedNodes(nodes: T[], expansionModel: SelectionModel<T>): T[];
    /**
     * Put node descendants of node in array.
     * If `onlyExpandable` is true, then only process expandable descendants.
     */
    nodeDescendents(node: T, nodes: T[], onlyExpandable: boolean): void;
}

export { ArrayDataSource, DataSource, SelectionModel, _DisposeViewRepeaterStrategy, _RecycleViewRepeaterStrategy, _ViewRepeater, _ViewRepeaterItemChanged, _ViewRepeaterItemContext, _ViewRepeaterItemContextFactory, _ViewRepeaterItemValueResolver };
export type { TreeDataNodeFlattener };
