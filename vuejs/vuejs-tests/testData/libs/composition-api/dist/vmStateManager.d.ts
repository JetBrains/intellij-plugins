import {ComponentInstance, Data} from './component';

export interface VfaState {
    refs?: string[];
    rawBindings?: Data;
    slots?: string[];
}
declare function set<K extends keyof VfaState>(vm: ComponentInstance, key: K, value: VfaState[K]): void;
declare function get<K extends keyof VfaState>(vm: ComponentInstance, key: K): VfaState[K] | undefined;
declare const _default: {
    set: typeof set;
    get: typeof get;
};
export default _default;
