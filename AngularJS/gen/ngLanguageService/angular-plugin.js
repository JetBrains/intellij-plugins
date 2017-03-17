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
    var util = require(fixedPath + "util.js");
    var AngularLanguagePlugin = (function (_super) {
        __extends(AngularLanguagePlugin, _super);
        function AngularLanguagePlugin(state) {
            return _super.call(this, state) || this;
        }
        AngularLanguagePlugin.prototype.getSession = function (ts_impl, loggerImpl, defaultOptionHolder, pathProcessor, mainFile, projectEmittedWithAllFiles) {
            var _this = this;
            var sessionClass = createSessionClass(ts_impl, loggerImpl, defaultOptionHolder, pathProcessor, projectEmittedWithAllFiles, mainFile);
            if (ts_impl["ide_processed"]) {
                var requiredObject = require(state.ngServicePath);
                var ng_1 = requiredObject;
                if (typeof requiredObject == "function") {
                    var obj = {};
                    obj.typescript = ts_impl;
                    ng_1 = requiredObject(obj);
                }
                if (!isVersionCompatible(ng_1, util, ts_impl)) {
                    ts_impl["ngIncompatible"] = true;
                }
                extendEx(ts_impl, "createLanguageService", function (oldFunction, args) {
                    var languageService = oldFunction.apply(_this, args);
                    var host = args[0];
                    var ngHost = new ng_1.TypeScriptServiceHost(host, languageService);
                    var ngService = ng_1.createLanguageService(ngHost);
                    ngHost.setSite(ngService);
                    languageService["ngService"] = ngService;
                    languageService["ngHost"] = ngHost;
                    return languageService;
                });
            }
            else {
                ts_impl["skipNg"] = "Cannot start Angular Service with the bundled TypeScript. " +
                    "Please specify 'typescript' node_modules package.";
            }
            var angularSession = angular_session_1.createAngularSessionClass(ts_impl, sessionClass);
            return getSession(ts_impl, loggerImpl, defaultOptionHolder, mainFile, projectEmittedWithAllFiles, angularSession);
        };
        AngularLanguagePlugin.prototype.overrideSysDefaults = function (ts_impl, state, serverFile) {
            var path = require('path');
            var tsPath = path.join(state.serverFolderPath, 'typescript.js');
            try {
                var fullTypescriptVersion = require(tsPath);
                for (var prop in fullTypescriptVersion) {
                    if (fullTypescriptVersion.hasOwnProperty(prop)) {
                        ts_impl[prop] = fullTypescriptVersion[prop];
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
function isVersionCompatible(ng, util, ts_impl) {
    try {
        if (ng.VERSION && ng.VERSION.full && util.isTypeScript20(ts_impl)) {
            var versions = util.parseNumbersInVersion(ng.VERSION.full);
            return !util.isVersionMoreOrEqual(versions, 2, 4, 5);
        }
    }
    catch (e) {
        return true;
    }
    return true;
}
function extendEx(ObjectToExtend, name, func) {
    var oldFunction = ObjectToExtend[name];
    ObjectToExtend[name] = function () {
        return func.apply(this, [oldFunction, arguments]);
    };
}
exports.extendEx = extendEx;
exports.factory = new AngularLanguagePluginFactory();
