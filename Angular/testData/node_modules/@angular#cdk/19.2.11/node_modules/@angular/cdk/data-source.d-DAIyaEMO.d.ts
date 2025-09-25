import { Observable } from 'rxjs';

/** Represents a range of numbers with a specified start and end. */
type ListRange = {
    start: number;
    end: number;
};
/**
 * Interface for any component that provides a view of some data collection and wants to provide
 * information regarding the view and any changes made.
 */
interface CollectionViewer {
    /**
     * A stream that emits whenever the `CollectionViewer` starts looking at a new portion of the
     * data. The `start` index is inclusive, while the `end` is exclusive.
     */
    viewChange: Observable<ListRange>;
}

declare abstract class DataSource<T> {
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
/** Checks whether an object is a data source. */
declare function isDataSource(value: any): value is DataSource<any>;

export { DataSource, isDataSource };
export type { CollectionViewer, ListRange };
