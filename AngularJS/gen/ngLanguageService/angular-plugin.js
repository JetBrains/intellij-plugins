"use strict";
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var AngularLanguagePluginFactory = (function () {
    function AngularLanguagePluginFactory() {
    }
    AngularLanguagePluginFactory.prototype.create = function (state) {
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
                var pluginEntryPoint = requiredObject;
                if (typeof requiredObject == "function") {
                    pluginEntryPoint = requiredObject();
                }
                console.error("Kinds: " + JSON.stringify(ts_impl.SyntaxKind));
                console.error("text:" + JSON.stringify(pluginEntryPoint));
                var PluginClass = pluginEntryPoint.default;
                extendEx(ts_impl, "createLanguageService", function (oldFunction, args) {
                    var languageService = oldFunction.apply(_this, args);
                    var host = args[0];
                    var documentRegistry = args[1];
                    languageService["angular-plugin"] = new PluginClass({
                        ts: ts_impl,
                        host: host,
                        service: languageService,
                        registry: documentRegistry
                    });
                    return languageService;
                });
                var AngularSession = (function (_super) {
                    __extends(AngularSession, _super);
                    function AngularSession() {
                        _super.apply(this, arguments);
                    }
                    AngularSession.prototype.appendPluginDiagnostics = function (project, diags, normalizedFileName) {
                        var languageService = project != null ? this.getLanguageService(project) : null;
                        if (!languageService) {
                            return diags;
                        }
                        var plugin = languageService["angular-plugin"];
                        if (!plugin) {
                            //error
                            return diags;
                        }
                        if (!diags) {
                            diags = [];
                        }
                        try {
                            var semanticDiagnosticsFilter = plugin.getSemanticDiagnosticsFilter(normalizedFileName, diags);
                            return semanticDiagnosticsFilter;
                        }
                        catch (err) {
                            console.log('Error processing angular templates ' + err.message + '\n' + err.stack);
                            diags.push({
                                file: null,
                                code: -1,
                                messageText: "Angular Language Service internal error: " + err.message,
                                start: 0,
                                length: 0,
                                category: ts_impl.DiagnosticCategory.Error
                            });
                        }
                        return diags;
                    };
                    return AngularSession;
                }(sessionClass));
                return getSession(ts_impl, loggerImpl, commonDefaultOptions, mainFile, projectEmittedWithAllFiles, AngularSession);
            };
            return AngularLanguagePlugin;
        }(TypeScriptLanguagePluginImpl));
        var typeScriptLanguagePlugin = new AngularLanguagePlugin(state);
        return {
            languagePlugin: typeScriptLanguagePlugin,
            readyMessage: typeScriptLanguagePlugin.readyMessage
        };
    };
    return AngularLanguagePluginFactory;
}());
function extendEx(ObjectToExtend, name, func) {
    var oldFunction = ObjectToExtend[name];
    ObjectToExtend[name] = function () {
        return func.apply(this, [oldFunction, arguments]);
    };
}
exports.extendEx = extendEx;
exports.factory = new AngularLanguagePluginFactory();
