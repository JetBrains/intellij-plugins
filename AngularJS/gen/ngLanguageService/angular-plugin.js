"use strict";
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var angular_session_1 = require("./angular-session");
var AngularLanguagePluginFactory = (function () {
    function AngularLanguagePluginFactory() {
    }
    AngularLanguagePluginFactory.prototype.create = function (state) {
        var angularLanguagePlugin = createPluginClass(state);
        var typeScriptLanguagePlugin = new angularLanguagePlugin(state);
        return {
            languagePlugin: typeScriptLanguagePlugin,
            readyMessage: typeScriptLanguagePlugin.readyMessage
        };
    };
    return AngularLanguagePluginFactory;
}());
function createPluginClass(state) {
    var fixedPath = state.typescriptPluginPath;
    var TypeScriptLanguagePluginImpl = require(fixedPath + "ts-plugin.js").TypeScriptLanguagePlugin;
    var getSession = require(fixedPath + "ts-session-provider.js").getSession;
    var createSessionClass = require(fixedPath + "ts-session.js").createSessionClass;
    var AngularLanguagePlugin = (function (_super) {
        __extends(AngularLanguagePlugin, _super);
        function AngularLanguagePlugin(state) {
            _super.call(this, state);
        }
        AngularLanguagePlugin.prototype.getSession = function (ts_impl, loggerImpl, commonDefaultOptions, pathProcessor, mainFile, projectEmittedWithAllFiles) {
            var _this = this;
            var sessionClass = createSessionClass(ts_impl, loggerImpl, commonDefaultOptions, pathProcessor, projectEmittedWithAllFiles, mainFile);
            var requiredObject = require(state.ngServicePath);
            var ng = requiredObject;
            if (typeof requiredObject == "function") {
                var obj = {};
                if (ts_impl["ide_processed"]) {
                    obj.typescript = ts_impl;
                    console.error("Passed processed ts_impl");
                }
                ng = requiredObject(obj);
            }
            extendEx(ts_impl, "createLanguageService", function (oldFunction, args) {
                var languageService = oldFunction.apply(_this, args);
                var host = args[0];
                var documentRegistry = args[1];
                var ngHost = new ng.TypeScriptServiceHost(host, languageService);
                var ngService = ng.createLanguageService(ngHost);
                ngHost.setSite(ngService);
                languageService["ngService"] = ngService;
                languageService["ngHost"] = ngHost;
                return languageService;
            });
            var angularSession = angular_session_1.createAngularSessionClass(ts_impl, sessionClass);
            return getSession(ts_impl, loggerImpl, commonDefaultOptions, mainFile, projectEmittedWithAllFiles, angularSession);
        };
        AngularLanguagePlugin.prototype.overrideSysDefaults = function (ts_impl, state, serverFile) {
            var path = require('path');
            var tsPath = path.join(state.serverFolderPath, 'typescript.js');
            try {
                var tsWithConstants = require(tsPath);
                for (var prop in tsWithConstants) {
                    if (tsWithConstants.hasOwnProperty(prop)) {
                        ts_impl[prop] = tsWithConstants[prop];
                    }
                }
                ts_impl["ide_processed"] = true;
                //clean resources
                var name_1 = require.resolve(tsPath);
                delete require.cache[name_1];
            }
            catch (err) {
            }
            _super.prototype.overrideSysDefaults.call(this, ts_impl, state, serverFile);
        };
        return AngularLanguagePlugin;
    }(TypeScriptLanguagePluginImpl));
    return AngularLanguagePlugin;
}
function extendEx(ObjectToExtend, name, func) {
    var oldFunction = ObjectToExtend[name];
    ObjectToExtend[name] = function () {
        return func.apply(this, [oldFunction, arguments]);
    };
}
exports.extendEx = extendEx;
exports.factory = new AngularLanguagePluginFactory();
