import { Subject } from 'rxjs';

/**
 * Class to be used to power selecting one or more options from a list.
 */
declare class SelectionModel<T> {
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
 * Event emitted when the value of a MatSelectionModel has changed.
 * @docs-private
 */
interface SelectionChange<T> {
    /** Model that dispatched the event. */
    source: SelectionModel<T>;
    /** Options that were added to the model. */
    added: T[];
    /** Options that were removed from the model. */
    removed: T[];
}
/**
 * Returns an error that reports that multiple values are passed into a selection model
 * with a single value.
 * @docs-private
 */
declare function getMultipleValuesInSingleSelectionError(): Error;

export { SelectionModel, getMultipleValuesInSingleSelectionError };
export type { SelectionChange };
