import { Observable } from 'rxjs';
import { EmptyFeatureResult, Prettify, SignalStoreFeature, SignalStoreFeatureResult, StateSignals, WritableStateSource } from '@ngrx/signals';
/**
 * @experimental
 * @description
 *
 * SignalStore feature for defining side effects.
 *
 * @usageNotes
 *
 * ```ts
 * import { signalStore, withState } from '@ngrx/signals';
 * import { event, Events, withEffects } from '@ngrx/signals/events';
 *
 * const increment = event('[Counter Page] Increment');
 * const decrement = event('[Counter Page] Decrement');
 *
 * const CounterStore = signalStore(
 *   withState({ count: 0 }),
 *   withEffects(({ count }, events = inject(Events)) => ({
 *     logCount$: events.on(increment, decrement).pipe(
 *       tap(({ type }) => console.log(type, count())),
 *     ),
 *   })),
 * );
 * ```
 */
export declare function withEffects<Input extends SignalStoreFeatureResult>(effectsFactory: (store: Prettify<StateSignals<Input['state']> & Input['props'] & Input['methods'] & WritableStateSource<Prettify<Input['state']>>>) => Record<string, Observable<unknown>>): SignalStoreFeature<Input, EmptyFeatureResult>;
