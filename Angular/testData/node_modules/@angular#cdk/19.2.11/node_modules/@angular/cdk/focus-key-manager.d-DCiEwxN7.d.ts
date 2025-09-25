import { ListKeyManagerOption, ListKeyManager } from './list-key-manager.d-CylnKWfo.js';
import { FocusOrigin } from './focus-monitor.d-BBkiOKUH.js';

/**
 * This is the interface for focusable items (used by the FocusKeyManager).
 * Each item must know how to focus itself, whether or not it is currently disabled
 * and be able to supply its label.
 */
interface FocusableOption extends ListKeyManagerOption {
    /** Focuses the `FocusableOption`. */
    focus(origin?: FocusOrigin): void;
}
declare class FocusKeyManager<T> extends ListKeyManager<FocusableOption & T> {
    private _origin;
    /**
     * Sets the focus origin that will be passed in to the items for any subsequent `focus` calls.
     * @param origin Focus origin to be used when focusing items.
     */
    setFocusOrigin(origin: FocusOrigin): this;
    /**
     * Sets the active item to the item at the specified
     * index and focuses the newly active item.
     * @param index Index of the item to be set as active.
     */
    setActiveItem(index: number): void;
    /**
     * Sets the active item to the item that is specified and focuses it.
     * @param item Item to be set as active.
     */
    setActiveItem(item: T): void;
}

export { FocusKeyManager };
export type { FocusableOption };
