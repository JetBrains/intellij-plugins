import { PathProcessor } from "./out-path-process";
import { HolderContainer } from "./compile-info-holder";
import { DefaultOptionsHolder } from "./ts-default-options";
export declare class TypeScriptLanguagePlugin implements LanguagePlugin {
    private _session;
    readyMessage: {
        version: string;
        supportedErrorCodes?: (string | number)[];
    };
    constructor(state: TypeScriptPluginState);
    /**
     * if hasManualParams returns '{}' or parsed options
     * {} is a flag for skipping 'Cannot find parent tsconfig.json' notification
     * otherwise returns 'null' or parsed options
     */
    private getDefaultCommandLineOptions(state, ts_impl);
    overrideSysDefaults(ts_impl: typeof ts, state: TypeScriptPluginState, serverFile: string): void;
    protected getSession(ts_impl: typeof ts, loggerImpl: any, commonDefaultOptions: DefaultOptionsHolder, pathProcessor: PathProcessor, mainFile: string, projectEmittedWithAllFiles: HolderContainer): ts.server.Session;
    onMessage(p: string, writer: MessageWriter): void;
}
export declare class TypeScriptLanguagePluginFactory implements LanguagePluginFactory {
    create(state: PluginState): {
        languagePlugin: LanguagePlugin;
        readyMessage?: any;
    };
}
declare let factory: TypeScriptLanguagePluginFactory;
export { factory };
