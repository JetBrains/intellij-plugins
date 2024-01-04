import * as i0 from '@angular/core';
import { InjectionToken } from '@angular/core';
import { IterableChangeRecord } from '@angular/core';
import { IterableChanges } from '@angular/core';
import { Observable } from 'rxjs';
import { OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { TemplateRef } from '@angular/core';
import { ViewContainerRef } from '@angular/core';

/** DataSource wrapper for a native array. */
export declare class ArrayDataSource<T> extends DataSource<T> {
    private _data;
    constructor(_data: readonly T[] | Observable<readonly T[]>);
    connect(): Observable<readonly T[]>;
    disconnect(): void;
}

/**
 * Interface for any component that provides a view of some data collection and wants to provide
 * information regarding the view and any changes made.
 */
export declare interface CollectionViewer {
    /**
     * A stream that emits whenever the `CollectionViewer` starts looking at a new portion of the
     * data. The `start` index is inclusive, while the `end` is exclusive.
     */
    viewChange: Observable<ListRange>;
}

export declare abstract class DataSource<T> {
    /**
     * Connects a collection viewer (such as a data-table) to this data source. Note that
     * the stream provided will be accessed during change detection and should not directly change
     * values that are bound in template views.
     * @param collectionViewer The component that exposes a view over the data provided by this
     *     data source.
     * @returns Observable that emits a new value when the data changes.
     */
    abstract connect(collectionViewer: CollectionViewer): Observable<readonly T[]>;
    /**
     * Disconnects a collection viewer (such as a data-table) from this data source. Can be used
     * to perform any clean-up or tear-down operations when a view is being destroyed.
     *
     * @param collectionViewer The component that exposes a view over the data provided by this
     *     data source.
     */
    abstract disconnect(collectionViewer: CollectionViewer): void;
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
export declare class _DisposeViewRepeaterStrategy<T, R, C extends _ViewRepeaterItemContext<T>> implements _ViewRepeater<T, R, C> {
    applyChanges(changes: IterableChanges<R>, viewContainerRef: ViewContainerRef, itemContextFactory: _ViewRepeaterItemContextFactory<T, R, C>, itemValueResolver: _ViewRepeaterItemValueResolver<T, R>, itemViewChanged?: _ViewRepeaterItemChanged<R, C>): void;
    detach(): void;
}

/**
 * Returns an error that reports that multiple values are passed into a selection model
 * with a single value.
 * @docs-private
 */
export declare function getMultipleValuesInSingleSelectionError(): Error;

/** Checks whether an object is a data source. */
export declare function isDataSource(value: any): value is DataSource<any>;

/** Represents a range of numbers with a specified start and end. */
export declare type ListRange = {
    start: number;
    end: number;
};

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
export declare class _RecycleViewRepeaterStrategy<T, R, C extends _ViewRepeaterItemContext<T>> implements _ViewRepeater<T, R, C> {
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
 * Event emitted when the value of a MatSelectionModel has changed.
 * @docs-private
 */
export declare interface SelectionChange<T> {
    /** Model that dispatched the event. */
    source: SelectionModel<T>;
    /** Options that were added to the model. */
    added: T[];
    /** Options that were removed from the model. */
    removed: T[];
}

/**
 * Class to be used to power selecting one or more options from a list.
 */
export declare class SelectionModel<T> {
    private _multiple;
    private _emitChanges;
    compareWith?: ((o1: T, o2: T) => boolean) | undefined;
    /** Currently-selected values. */
    private _selection;
    /** Keeps track of the deselected options that haven't been emitted by the change event. */
    private _deselectedToEmit;
    /** Keeps track of the selected options that haven't been emitted by the change event. */
    private _selectedToEmit;
    /** Cache for the array value of the selected items. */
    private _selected;
    /** Selected values. */
    get selected(): T[];
    /** Event emitted when the value has changed. */
    readonly changed: Subject<SelectionChange<T>>;
    constructor(_multiple?: boolean, initiallySelectedValues?: T[], _emitChanges?: boolean, compareWith?: ((o1: T, o2: T) => boolean) | undefined);
    /**
     * Selects a value or an array of values.
     * @param values The values to select
     * @return Whether the selection changed as a result of this call
     * @breaking-change 16.0.0 make return type boolean
     */
    select(...values: T[]): boolean | void;
    /**
     * Deselects a value or an array of values.
     * @param values The values to deselect
     * @return Whether the selection changed as a result of this call
     * @breaking-change 16.0.0 make return type boolean
     */
    deselect(...values: T[]): boolean | void;
    /**
     * Sets the selected values
     * @param values The new selected values
     * @return Whether the selection changed as a result of this call
     * @breaking-change 16.0.0 make return type boolean
     */
    setSelection(...values: T[]): boolean | void;
    /**
     * Toggles a value between selected and deselected.
     * @param value The value to toggle
     * @return Whether the selection changed as a result of this call
     * @breaking-change 16.0.0 make return type boolean
     */
    toggle(value: T): boolean | void;
    /**
     * Clears all of the selected values.
     * @param flushEvent Whether to flush the changes in an event.
     *   If false, the changes to the selection will be flushed along with the next event.
     * @return Whether the selection changed as a result of this call
     * @breaking-change 16.0.0 make return type boolean
     */
    clear(flushEvent?: boolean): boolean | void;
    /**
     * Determines whether a value is selected.
     */
    isSelected(value: T): boolean;
    /**
     * Determines whether the model does not have a value.
     */
    isEmpty(): boolean;
    /**
     * Determines whether the model has a value.
     */
    hasValue(): boolean;
    /**
     * Sorts the selected values based on a predicate function.
     */
    sort(predicate?: (a: T, b: T) => number): void;
    /**
     * Gets whether multiple values can be selected.
     */
    isMultipleSelection(): boolean;
    /** Emits a change event and clears the records of selected and deselected values. */
    private _emitChangeEvent;
    /** Selects a value. */
    private _markSelected;
    /** Deselects a value. */
    private _unmarkSelected;
    /** Clears out the selected values. */
    private _unmarkAll;
    /**
     * Verifies the value assignment and throws an error if the specified value array is
     * including multiple values while the selection model is not supporting multiple values.
     */
    private _verifyValueAssignment;
    /** Whether there are queued up change to be emitted. */
    private _hasQueuedChanges;
    /** Returns a value that is comparable to inputValue by applying compareWith function, returns the same inputValue otherwise. */
    private _getConcreteValue;
}

/**
 * Interface for a class that can flatten hierarchical structured data and re-expand the flattened
 * data back into its original structure. Should be used in conjunction with the cdk-tree.
 */
export declare interface TreeDataNodeFlattener<T> {
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

/**
 * Class to coordinate unique selection based on name.
 * Intended to be consumed as an Angular service.
 * This service is needed because native radio change events are only fired on the item currently
 * being selected, and we still need to uncheck the previous selection.
 *
 * This service does not *store* any IDs and names because they may change at any time, so it is
 * less error-prone if they are simply passed through when the events occur.
 */
export declare class UniqueSelectionDispatcher implements OnDestroy {
    private _listeners;
    /**
     * Notify other items that selection for the given name has been set.
     * @param id ID of the item.
     * @param name Name of the item.
     */
    notify(id: string, name: string): void;
    /**
     * Listen for future changes to item selection.
     * @return Function used to deregister listener
     */
    listen(listener: UniqueSelectionDispatcherListener): () => void;
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<UniqueSelectionDispatcher, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<UniqueSelectionDispatcher>;
}

export declare type UniqueSelectionDispatcherListener = (id: string, name: string) => void;

/**
 * Injection token for {@link _ViewRepeater}. This token is for use by Angular Material only.
 * @docs-private
 */
export declare const _VIEW_REPEATER_STRATEGY: InjectionToken<_ViewRepeater<unknown, unknown, _ViewRepeaterItemContext<unknown>>>;

/**
 * Describes a strategy for rendering items in a {@link ViewContainerRef}.
 *
 * @template T The type for the embedded view's $implicit property.
 * @template R The type for the item in each IterableDiffer change record.
 * @template C The type for the context passed to each embedded view.
 */
export declare interface _ViewRepeater<T, R, C extends _ViewRepeaterItemContext<T>> {
    applyChanges(changes: IterableChanges<R>, viewContainerRef: ViewContainerRef, itemContextFactory: _ViewRepeaterItemContextFactory<T, R, C>, itemValueResolver: _ViewRepeaterItemValueResolver<T, R>, itemViewChanged?: _ViewRepeaterItemChanged<R, C>): void;
    detach(): void;
}

/**
 * Meta data describing the state of a view after it was updated by a
 * {@link _ViewRepeater}.
 *
 * @template R The type for the item in each IterableDiffer change record.
 * @template C The type for the context passed to each embedded view.
 */
export declare interface _ViewRepeaterItemChange<R, C> {
    /** The view's context after it was changed. */
    context?: C;
    /** Indicates how the view was changed. */
    operation: _ViewRepeaterOperation;
    /** The view's corresponding change record. */
    record: IterableChangeRecord<R>;
}

/**
 * Type for a callback to be executed after a view has changed.
 *
 * @template R The type for the item in each IterableDiffer change record.
 * @template C The type for the context passed to each embedded view.
 */
export declare type _ViewRepeaterItemChanged<R, C> = (change: _ViewRepeaterItemChange<R, C>) => void;

/**
 * The context for an embedded view in the repeater's view container.
 *
 * @template T The type for the embedded view's $implicit property.
 */
export declare interface _ViewRepeaterItemContext<T> {
    $implicit?: T;
}

/**
 * A factory that derives the embedded view context for an item in a view
 * container.
 *
 * @template T The type for the embedded view's $implicit property.
 * @template R The type for the item in each IterableDiffer change record.
 * @template C The type for the context passed to each embedded view.
 */
export declare type _ViewRepeaterItemContextFactory<T, R, C extends _ViewRepeaterItemContext<T>> = (record: IterableChangeRecord<R>, adjustedPreviousIndex: number | null, currentIndex: number | null) => _ViewRepeaterItemInsertArgs<C>;

/**
 * The arguments needed to construct an embedded view for an item in a view
 * container.
 *
 * @template C The type for the context passed to each embedded view.
 */
export declare interface _ViewRepeaterItemInsertArgs<C> {
    templateRef: TemplateRef<C>;
    context?: C;
    index?: number;
}

/**
 * Extracts the value of an item from an {@link IterableChangeRecord}.
 *
 * @template T The type for the embedded view's $implicit property.
 * @template R The type for the item in each IterableDiffer change record.
 */
export declare type _ViewRepeaterItemValueResolver<T, R> = (record: IterableChangeRecord<R>) => T;

/** Indicates how a view was changed by a {@link _ViewRepeater}. */
export declare enum _ViewRepeaterOperation {
    /** The content of an existing view was replaced with another item. */
    REPLACED = 0,
    /** A new view was created with `createEmbeddedView`. */
    INSERTED = 1,
    /** The position of a view changed, but the content remains the same. */
    MOVED = 2,
    /** A view was detached from the view container. */
    REMOVED = 3
}

export { }
