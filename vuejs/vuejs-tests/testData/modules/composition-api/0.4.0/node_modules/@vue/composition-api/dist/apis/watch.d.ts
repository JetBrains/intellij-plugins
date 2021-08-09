import {Ref} from '../reactivity';

declare type CleanupRegistrator = (invalidate: () => void) => void;
declare type SimpleEffect = (onCleanup: CleanupRegistrator) => void;
declare type StopHandle = () => void;
declare type WatcherCallBack<T> = (newVal: T, oldVal: T, onCleanup: CleanupRegistrator) => void;
declare type WatcherSource<T> = Ref<T> | (() => T);
declare type MapSources<T> = {
    [K in keyof T]: T[K] extends WatcherSource<infer V> ? V : never;
};
declare type FlushMode = 'pre' | 'post' | 'sync';
interface WatcherOption {
    lazy: boolean;
    deep: boolean;
    flush: FlushMode;
}
export interface VueWatcher {
    lazy: boolean;
    get(): any;
    teardown(): void;
}
export declare function watch<T = any>(source: SimpleEffect, options?: Omit<Partial<WatcherOption>, 'lazy'>): StopHandle;
export declare function watch<T = any>(source: WatcherSource<T>, cb: WatcherCallBack<T>, options?: Partial<WatcherOption>): StopHandle;
export declare function watch<T extends WatcherSource<unknown>[]>(sources: T, cb: (newValues: MapSources<T>, oldValues: MapSources<T>, onCleanup: CleanupRegistrator) => any, options?: Partial<WatcherOption>): StopHandle;
export {};
