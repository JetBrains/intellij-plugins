import { App } from 'vue';
declare type ComponentType = any;
export interface NUiInstance {
    version: string;
    componentPrefix: string;
    install: (app: App) => void;
}
interface NUiCreateOptions {
    components?: ComponentType[];
    componentPrefix?: string;
}
declare function create({ componentPrefix, components }?: NUiCreateOptions): NUiInstance;
export default create;
