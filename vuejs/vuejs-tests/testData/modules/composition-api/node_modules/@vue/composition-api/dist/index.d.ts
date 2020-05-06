import Vue, {VueConstructor} from 'vue';
import {Data, SetupContext, SetupFunction} from './component';

declare module 'vue/types/options' {
    interface ComponentOptions<V extends Vue> {
        setup?: SetupFunction<Data, Data>;
    }
}
declare const plugin: {
    install: (Vue: VueConstructor<Vue>) => void;
};
export default plugin;
export { default as createElement } from './createElement';
export { SetupContext };
export { createComponent, defineComponent, ComponentRenderProxy, PropType, PropOptions, } from './component';
export { getCurrentVM as getCurrentInstance } from './runtimeContext';
export * from './apis/state';
export * from './apis/lifecycle';
export * from './apis/watch';
export * from './apis/computed';
export * from './apis/inject';
