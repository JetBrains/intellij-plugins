import {PathProcessor} from "./out-path-process";
import * as ts from '../typings/tsserverlibrary';

export declare class DefaultOptionsHolder {
    pluginState: TypeScriptPluginState;
    options: ts.CompilerOptions;
    configFileName: string | null;
    mainFile: string | null;
    pathProcessor: PathProcessor | null;
    private defaultOptions;
    private updateConfigCallback;
    isUseSingleInferredProject(): boolean;
    constructor(defaultOptions: ts.CompilerOptions, ts_impl: typeof ts, pluginState: TypeScriptPluginState);
    showParentConfigWarning(): boolean;
    hasConfig(): boolean;
    watchConfig(callback: () => void, ts_impl: typeof ts): void;
    refresh(ts_impl: typeof ts): void;
    getConfigOptions(commonDefaultOptions: ts.CompilerOptions, ts_impl: typeof ts): {
        commonDefaultOptions: ts.CompilerOptions;
        configFileName?: string;
    };
}
