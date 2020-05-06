import {Ref} from '../reactivity';

interface Option<T> {
    get: () => T;
    set: (value: T) => void;
}
export declare function computed<T>(getter: Option<T>['get']): Readonly<Ref<Readonly<T>>>;
export declare function computed<T>(options: Option<T>): Ref<Readonly<T>>;
export {};
