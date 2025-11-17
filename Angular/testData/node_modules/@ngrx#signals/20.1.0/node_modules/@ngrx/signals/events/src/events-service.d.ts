import { Observable } from 'rxjs';
import { EventInstance } from './event-instance';
import { EventCreator } from './event-creator';
import * as i0 from "@angular/core";
export declare const EVENTS: unique symbol;
export declare const SOURCE_TYPE: unique symbol;
declare abstract class BaseEvents {
    on(): Observable<EventInstance<string, unknown>>;
    on<EventCreators extends EventCreator<string, any>[]>(...events: [...EventCreators]): Observable<{
        [K in keyof EventCreators]: ReturnType<EventCreators[K]>;
    }[number]>;
}
/**
 * @experimental
 * @description
 *
 * Globally provided service for listening to dispatched events.
 *
 * @usageNotes
 *
 * ```ts
 * import { event, Events } from '@ngrx/signals/events';
 *
 * const increment = event('[Counter Page] Increment');
 *
 * \@Component({ \/* ... *\/ })
 * class Counter {
 *   readonly #events = inject(Events);
 *
 *   constructor() {
 *     this.#events
 *       .on(increment)
 *       .pipe(takeUntilDestroyed())
 *       .subscribe(() => \/* handle increment event *\/);
 *   }
 * }
 * ```
 */
export declare class Events extends BaseEvents {
    static ɵfac: i0.ɵɵFactoryDeclaration<Events, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<Events>;
}
export declare class ReducerEvents extends BaseEvents {
    static ɵfac: i0.ɵɵFactoryDeclaration<ReducerEvents, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<ReducerEvents>;
}
export {};
