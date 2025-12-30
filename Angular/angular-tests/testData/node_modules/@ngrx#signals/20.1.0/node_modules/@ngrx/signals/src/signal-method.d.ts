import { EffectRef, Injector, Signal } from '@angular/core';
export type SignalMethod<Input> = ((input: Input | Signal<Input>, config?: {
    injector?: Injector;
}) => EffectRef) & EffectRef;
export declare function signalMethod<Input>(processingFn: (value: Input) => void, config?: {
    injector?: Injector;
}): SignalMethod<Input>;
