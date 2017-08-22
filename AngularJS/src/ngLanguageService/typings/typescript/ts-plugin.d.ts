import {DefaultOptionsHolder} from "./ts-default-options";

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
    protected getSession(ts_impl: typeof ts, loggerImpl: any, defaultOptionsHolder: DefaultOptionsHolder): ts.server.Session;
    private instantiateSession(ts_impl, loggerImpl, defaultOptionsHolder, sessionClass);
    protected createSessionClass(ts_impl: typeof ts, defaultOptionsHolder: DefaultOptionsHolder): any;
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
