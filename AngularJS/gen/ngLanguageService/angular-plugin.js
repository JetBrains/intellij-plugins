"use strict";
var __extends = (this && this.__extends) || (function () {
    var extendStatics = Object.setPrototypeOf ||
        ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
        function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
exports.__esModule = true;
var angular_session_20_1 = require("./angular-session-20");
var angular_session_latest_1 = require("./angular-session-latest");
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
    var instantiateSession = require(fixedPath + "ts-session-provider.js").instantiateSession;
    var loggerImpl = require(fixedPath + "logger-impl.js");
    var util = require(fixedPath + "util.js");
    var AngularLanguagePlugin = (function (_super) {
        __extends(AngularLanguagePlugin, _super);
        function AngularLanguagePlugin(state) {
            return _super.call(this, state) || this;
        }
        AngularLanguagePlugin.prototype.createSessionClass = function (ts_impl, defaultOptionsHolder) {
            var sessionClass = _super.prototype.createSessionClass.call(this, ts_impl, defaultOptionsHolder);
            if (ts_impl["ide_processed"]) {
                var requiredObject = require(state.ngServicePath);
                var ng = requiredObject;
                if (typeof requiredObject == "function") {
                    var obj = {};
                    obj.typescript = ts_impl;
                    ng = requiredObject(obj);
                }
                ts_impl["ng_service"] = ng;
                ts_impl["ideUtil"] = util;
                if (!isVersionCompatible(ng, util, ts_impl)) {
                    ts_impl["ngIncompatible"] = true;
                }
            }
            else {
                ts_impl["skipNg"] = "Cannot start Angular Service with the bundled TypeScript. " +
                    "Please specify 'typescript' node_modules package.";
            }
            var version = ts_impl.version;
            var versionNumbers = util.parseNumbersInVersion(version);
            var is240OrMore = util.isVersionMoreOrEqual(versionNumbers, 2, 4, 0);
            return is240OrMore ? angular_session_latest_1.createAngularSessionClass(ts_impl, sessionClass, loggerImpl) : angular_session_20_1.createAngularSessionClassTs20(ts_impl, sessionClass);
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
                //do nothing
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
exports.factory = new AngularLanguagePluginFactory();
