import { Prettify } from '@ngrx/signals';
import { EventCreator } from './event-creator';
type EventType<Source extends string, EventName extends string> = `[${Source}] ${EventName}`;
type EventCreatorGroup<Source extends string, Events extends Record<string, any>> = {
    [EventName in keyof Events]: EventName extends string ? EventCreator<EventType<Source, EventName>, Events[EventName]> : never;
};
/**
 * @experimental
 * @description
 *
 * Creates a group of event creators.
 *
 * @usageNotes
 *
 * ```ts
 * import { type } from '@ngrx/signals';
 * import { eventGroup } from '@ngrx/signals/events';
 *
 * const counterPageEvents = eventGroup({
 *   source: 'Counter Page',
 *   events: {
 *     increment: type<void>(),
 *     decrement: type<void>(),
 *     set: type<number>(),
 *   },
 * });
 * ```
 */
export declare function eventGroup<Source extends string, Events extends Record<string, unknown>>(config: {
    source: Source;
    events: Events;
}): Prettify<EventCreatorGroup<Source, Events>>;
export {};
