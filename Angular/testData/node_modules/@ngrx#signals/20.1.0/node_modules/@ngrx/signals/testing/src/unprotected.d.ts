import { Prettify, StateSource, WritableStateSource } from '@ngrx/signals';
type UnprotectedSource<Source extends StateSource<object>> = Source extends StateSource<infer State> ? Prettify<Omit<Source, keyof StateSource<State>> & WritableStateSource<State>> : never;
export declare function unprotected<Source extends StateSource<object>>(source: Source): UnprotectedSource<Source>;
export {};
