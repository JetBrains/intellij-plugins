import { DeepSignal } from './deep-signal';
export declare function deepComputed<T extends object>(computation: () => T): DeepSignal<T>;
