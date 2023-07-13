import {VueConstructor} from 'vue';
import {ComponentInstance} from './component';

declare let currentVue: VueConstructor | null;
declare let currentVM: ComponentInstance | null;
export declare function getCurrentVue(): VueConstructor;
export declare function setCurrentVue(vue: VueConstructor): void;
export declare function getCurrentVM(): ComponentInstance | null;
export declare function setCurrentVM(vm: ComponentInstance | null): void;
export { currentVue, currentVM };
