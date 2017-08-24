import {TypeScriptLanguagePlugin} from "./typings/ts-plugin";
import {createAngularSessionClassTs20} from "./angular-session-20";
import {DefaultOptionsHolder} from "./typings/ts-default-options";
import {SessionClass} from "./typings/ts-session-provider";
import * as ts from './typings/tsserverlibrary'
import * as utilObj from './typings/util';
import {createAngularSessionClass} from "./angular-session-latest";

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
    const util: typeof utilObj = require(fixedPath + "util.js");

    class AngularLanguagePlugin extends TypeScriptLanguagePluginImpl {

        constructor(state: AngularTypeScriptPluginState) {
            super(state);
        }

        protected createSessionClass(ts_impl, defaultOptionsHolder: DefaultOptionsHolder): SessionClass {
            let sessionClass: SessionClass = super.createSessionClass(ts_impl, defaultOptionsHolder);

            if (ts_impl["ide_processed"]) {
                let requiredObject = require(state.ngServicePath);
                let ng = requiredObject;
                if (typeof requiredObject == "function") {
                    let obj: any = {}
                    obj.typescript = ts_impl;
                    ng = requiredObject(obj);
                }

                ts_impl["ng_service"] = ng;


                if (!isVersionCompatible(ng, util, ts_impl)) {
                    ts_impl["ngIncompatible"] = true;
                }
            } else {
                ts_impl["skipNg"] = "Cannot start Angular Service with the bundled TypeScript. " +
                    "Please specify 'typescript' node_modules package.";
            }

            let version = ts_impl.version;

            let versionNumbers = util.parseNumbersInVersion(version);

            return util.isVersionMoreOrEqual(versionNumbers, 2, 3, 0) ? createAngularSessionClass(ts_impl, sessionClass) : createAngularSessionClassTs20(ts_impl, <any>sessionClass);
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


export const factory = new AngularLanguagePluginFactory();