import * as i0 from '@angular/core';
import { OnDestroy } from '@angular/core';

type UniqueSelectionDispatcherListener = (id: string, name: string) => void;
/**
 * Class to coordinate unique selection based on name.
 * Intended to be consumed as an Angular service.
 * This service is needed because native radio change events are only fired on the item currently
 * being selected, and we still need to uncheck the previous selection.
 *
 * This service does not *store* any IDs and names because they may change at any time, so it is
 * less error-prone if they are simply passed through when the events occur.
 */
declare class UniqueSelectionDispatcher implements OnDestroy {
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

export { UniqueSelectionDispatcher };
export type { UniqueSelectionDispatcherListener };
