import { EventInstance } from './event-instance';
import { Events, ReducerEvents } from './events-service';
import * as i0 from "@angular/core";
/**
 * @experimental
 * @description
 *
 * Globally provided service for dispatching events.
 *
 * @usageNotes
 *
 * ```ts
 * import { Dispatcher, event } from '@ngrx/signals/events';
 *
 * const increment = event('[Counter Page] Increment');
 *
 * \@Component({ \/* ... *\/ })
 * class Counter {
 *   readonly #dispatcher = inject(Dispatcher);
 *
 *   increment(): void {
 *     this.#dispatcher.dispatch(increment());
 *   }
 * }
 * ```
 */
export declare class Dispatcher {
    protected readonly reducerEvents: ReducerEvents;
    protected readonly events: Events;
    dispatch(event: EventInstance<string, unknown>): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<Dispatcher, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<Dispatcher>;
}
