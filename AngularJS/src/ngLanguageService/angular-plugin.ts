import {IDETypeScriptSession} from "./typings/typescript/util";
import {TypeScriptLanguagePlugin} from "./typings/typescript/ts-plugin";
import {createAngularSessionClass} from "./angular-session";

class AngularLanguagePluginFactory implements LanguagePluginFactory {
    create(state: AngularTypeScriptPluginState): {languagePlugin: LanguagePlugin, readyMessage?: any } {

        let angularLanguagePlugin = createPluginClass(state);

        let typeScriptLanguagePlugin: any = new angularLanguagePlugin(state);
        return {
            languagePlugin: typeScriptLanguagePlugin,
            readyMessage: typeScriptLanguagePlugin.readyMessage
        };
    }
}


function createPluginClass(state: AngularTypeScriptPluginState) {
    let fixedPath = state.typescriptPluginPath;

    const TypeScriptLanguagePluginImpl: typeof TypeScriptLanguagePlugin = require(fixedPath + "ts-plugin.js").TypeScriptLanguagePlugin
    const getSession = require(fixedPath + "ts-session-provider.js").getSession
    const createSessionClass = require(fixedPath + "ts-session.js").createSessionClass


    class AngularLanguagePlugin extends TypeScriptLanguagePluginImpl {

        constructor(state: AngularTypeScriptPluginState) {
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
                let obj: any = {}
                if (ts_impl["ide_processed"]) {
                    obj.typescript = ts_impl;
                    console.error("Passed processed ts_impl")
                }
                pluginEntryPoint = requiredObject(obj);
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

        overrideSysDefaults(ts_impl: typeof ts, state: TypeScriptPluginState, serverFile: string) {
            super.overrideSysDefaults(ts_impl, state, serverFile);

            const path = require('path');
            let tsPath = path.join(state.serverFolderPath, 'typescript.js');
            try {
                let ts2 = require(tsPath);
                for (let prop in ts2) {
                    if (ts2.hasOwnProperty(prop) && !ts_impl.hasOwnProperty(prop)) {
                        ts_impl[prop] = ts2[prop];
                    }
                }

                ts_impl["ide_processed"] = true;

                //clean resources
                let name = require.resolve(tsPath);
                delete require.cache[name];
            } catch (err) {
                //do nothing
            }


        }
    }

    return AngularLanguagePlugin;
}

export function extendEx(ObjectToExtend: any, name: string, func: (oldFunction: any, args: any) => any) {

    let oldFunction = ObjectToExtend[name];

    ObjectToExtend[name] = function (this: any) {
        return func.apply(this, [oldFunction, arguments]);
    }
}

export const factory = new AngularLanguagePluginFactory();