import { Injector, Signal } from '@angular/core';
import { Observable } from 'rxjs';
type RxMethodRef = {
    destroy: () => void;
};
export type RxMethod<Input> = ((input: Input | Signal<Input> | Observable<Input>, config?: {
    injector?: Injector;
}) => RxMethodRef) & RxMethodRef;
export declare function rxMethod<Input>(generator: (source$: Observable<Input>) => Observable<unknown>, config?: {
    injector?: Injector;
}): RxMethod<Input>;
export {};
