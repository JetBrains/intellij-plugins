import { StateClassInternal, SharedSelectorOptions } from './internal/internals';
import { PlainObjectOf } from '../internals/src/symbols';
import { ActionHandlerMetaData } from './actions/symbols';
interface MetaDataModel {
    name: string | null;
    actions: PlainObjectOf<ActionHandlerMetaData[]>;
    defaults: any;
    path: string | null;
    children?: StateClassInternal[];
}
interface SelectorMetaDataModel {
    originalFn: Function | null;
    containerClass: any;
    selectorName: string | null;
    getSelectorOptions: () => SharedSelectorOptions;
}
export declare function ensureStoreMetadata(target: StateClassInternal<any, any>): MetaDataModel;
export declare function getStoreMetadata(target: StateClassInternal<any, any>): MetaDataModel;
export declare function ensureSelectorMetadata(target: Function): SelectorMetaDataModel;
export declare function getSelectorMetadata(target: any): SelectorMetaDataModel;
export {};
