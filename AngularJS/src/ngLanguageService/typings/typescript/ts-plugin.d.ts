import {PathProcessor} from "./out-path-process";
import {HolderContainer} from "./compile-info-holder";
export declare class TypeScriptLanguagePlugin implements LanguagePlugin {
    private _session;
    readyMessage: {
        version: string;
        supportedErrorCodes?: (string | number)[];
    };

    constructor(state: TypeScriptPluginState);

    protected getSession(ts_impl: typeof ts, loggerImpl: any, commonDefaultOptions: ts.CompilerOptions, pathProcessor: PathProcessor, mainFile: string, projectEmittedWithAllFiles: HolderContainer): ts.server.Session;

    onMessage(p: string): void;

    overrideSysDefaults(ts_impl: typeof ts, state: TypeScriptPluginState, serverFile: string);
}
export declare class TypeScriptLanguagePluginFactory implements LanguagePluginFactory {
    create(state: PluginState): {
        languagePlugin: LanguagePlugin;
        readyMessage?: any;
    };
}
declare let factory: TypeScriptLanguagePluginFactory;
export {factory};
