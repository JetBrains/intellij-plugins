import {IDETypeScriptSession} from "./typings/typescript/util";
import {TypeScriptLanguagePlugin} from "./typings/typescript/ts-plugin";
import {createAngularSessionClass} from "./angular-session";

class AngularLanguagePluginFactory implements LanguagePluginFactory {
    create(state: any): {languagePlugin: LanguagePlugin, readyMessage?: any } {
        let fixedPath = state.typescriptPluginPath;
        const TypeScriptLanguagePluginImpl: {new(state): TypeScriptLanguagePlugin} = require(fixedPath + "ts-plugin.js").TypeScriptLanguagePlugin
        const getSession = require(fixedPath + "ts-session-provider.js").getSession
        const createSessionClass = require(fixedPath + "ts-session.js").createSessionClass

        class AngularLanguagePlugin extends TypeScriptLanguagePluginImpl {

            constructor(state) {
                super(state);
            }

            protected getSession(ts_impl: typeof ts,
                                 loggerImpl: any,
                                 commonDefaultOptions: any,
                                 pathProcessor: any,
                                 mainFile: string,
                                 projectEmittedWithAllFiles: any): any {
                let sessionClass: {new(state): IDETypeScriptSession} = createSessionClass(ts_impl, loggerImpl, commonDefaultOptions, pathProcessor, projectEmittedWithAllFiles, mainFile)

                let requiredObject = require(state.ngServicePath);
                let pluginEntryPoint = requiredObject;
                if (typeof requiredObject == "function") {
                    pluginEntryPoint = requiredObject();
                }

                let PluginClass: typeof LanguageServicePlugin = pluginEntryPoint.default;

                extendEx(ts_impl, "createLanguageService", (oldFunction, args) => {
                    let languageService = oldFunction.apply(this, args);
                    let host = args[0];
                    let documentRegistry = args[1];

                    languageService["angular-plugin"] = new PluginClass({
                        ts: ts_impl,
                        host,
                        service: languageService,
                        registry: documentRegistry
                    });

                    return languageService;

                });


                let angularSession = createAngularSessionClass(ts_impl, sessionClass);

                return getSession(ts_impl, loggerImpl, commonDefaultOptions, mainFile, projectEmittedWithAllFiles, angularSession);
            }
        }

        let typeScriptLanguagePlugin: any = new AngularLanguagePlugin(state);
        return {
            languagePlugin: typeScriptLanguagePlugin,
            readyMessage: typeScriptLanguagePlugin.readyMessage
        };
    }
}

export function extendEx(ObjectToExtend: any, name: string, func: (oldFunction: any, args: any) => any) {

    let oldFunction = ObjectToExtend[name];

    ObjectToExtend[name] = function (this: any) {
        return func.apply(this, [oldFunction, arguments]);
    }
}

export const factory = new AngularLanguagePluginFactory();