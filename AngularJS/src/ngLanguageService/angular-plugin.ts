import {IDETypeScriptSession} from "./typings/typescript/util";
import {TypeScriptLanguagePlugin} from "./typings/typescript/ts-plugin";
import {createAngularSessionClass, getServiceDiags} from "./angular-session";
import {LanguageService} from "./typings/types";

class AngularLanguagePluginFactory implements LanguagePluginFactory {
    create(state: AngularTypeScriptPluginState): { languagePlugin: LanguagePlugin, readyMessage?: any } {

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
    const instantiateSession = require(fixedPath + "ts-session-provider.js").instantiateSession;
    const createSessionClass = require(fixedPath + "ts-session.js").createSessionClass;
    const util = require(fixedPath + "util.js");

    class AngularLanguagePlugin extends TypeScriptLanguagePluginImpl {

        constructor(state: AngularTypeScriptPluginState) {
            super(state);
        }

        protected getSession(ts_impl: typeof ts,
                             loggerImpl: any,
                             defaultOptionHolder: any): any {
            let sessionClass: { new(state): IDETypeScriptSession } = createSessionClass(ts_impl, defaultOptionHolder)

            if (ts_impl["ide_processed"]) {
                let requiredObject = require(state.ngServicePath);
                let ng = requiredObject;
                if (typeof requiredObject == "function") {
                    let obj: any = {}
                    obj.typescript = ts_impl;
                    ng = requiredObject(obj);
                }


                if (!isVersionCompatible(ng, util, ts_impl)) {
                    ts_impl["ngIncompatible"] = true;
                }
                extendEx(ts_impl, "createLanguageService", (oldFunction, args) => {
                    let languageService = oldFunction.apply(this, args);
                    let host = args[0];

                    let ngHost = new ng.TypeScriptServiceHost(host, languageService);
                    let ngService: LanguageService = ng.createLanguageService(ngHost);
                    ngHost.setSite(ngService);

                    extendEx(languageService, "getSemanticDiagnostics", (getSemanticDiagnosticsOld, args) => {
                        let diags = getSemanticDiagnosticsOld.apply(ngService, args);
                        if (diags == null) {
                            diags = [];
                        }
                        let name = args[0];

                        return diags.concat(getServiceDiags(ts_impl, ngService, ngHost, name, null, languageService));
                    });

                    languageService["ngService"] = () => ngService;
                    languageService["ngHost"] = () => ngHost;

                    return languageService;

                });
            } else {
                ts_impl["skipNg"] = "Cannot start Angular Service with the bundled TypeScript. " +
                    "Please specify 'typescript' node_modules package.";
            }


            let angularSession = createAngularSessionClass(ts_impl, sessionClass);

            return instantiateSession(ts_impl, loggerImpl, defaultOptionHolder, angularSession);
        }

        overrideSysDefaults(ts_impl: typeof ts, state: TypeScriptPluginState, serverFile: string) {
            const path = require('path');
            let tsPath = path.join(state.serverFolderPath, 'typescript.js');
            try {
                let fullTypescriptVersion = require(tsPath);
                for (let prop in fullTypescriptVersion) {
                    if (fullTypescriptVersion.hasOwnProperty(prop)) {
                        ts_impl[prop] = fullTypescriptVersion[prop];
                    }
                }

                ts_impl["ide_processed"] = true;

                //clean resources
                let name = require.resolve(tsPath);
                delete require.cache[name];
            } catch (err) {
                //do nothing
            }


            super.overrideSysDefaults(ts_impl, state, serverFile);
        }
    }

    return AngularLanguagePlugin;
}

function isVersionCompatible(ng: any, util: any, ts_impl: typeof ts) {
    try {
        if (ng.VERSION && ng.VERSION.full && util.isTypeScript20(ts_impl)) {
            let versions = util.parseNumbersInVersion(ng.VERSION.full);
            return !util.isVersionMoreOrEqual(versions, 2, 4, 5);
        }
    } catch (e) {
        return true;
    }

    return true;
}

export function extendEx(ObjectToExtend: any, name: string, func: (oldFunction: any, args: any) => any) {

    let oldFunction = ObjectToExtend[name];

    ObjectToExtend[name] = function (this: any) {
        return func.apply(this, [oldFunction, arguments]);
    }
}

export const factory = new AngularLanguagePluginFactory();