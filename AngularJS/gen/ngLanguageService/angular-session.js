"use strict";
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var path = require('path');
function createAngularSessionClass(ts_impl, sessionClass) {
    ts_impl.server.CommandNames.IDEGetHtmlErrors = "IDEGetHtmlErrors";
    ts_impl.server.CommandNames.IDENgCompletions = "IDENgCompletions";
    var skipAngular = ts_impl["skipNg"];
    var globalError = skipAngular ? "Cannot start Angular Service using bundled TypeScript version. " +
        "Please specify typescript node_modules package instead in the TypeScript settings" : null;
    var AngularSession = (function (_super) {
        __extends(AngularSession, _super);
        function AngularSession() {
            _super.apply(this, arguments);
        }
        AngularSession.prototype.executeCommand = function (request) {
            var command = request.command;
            if (command == ts_impl.server.CommandNames.IDEGetHtmlErrors) {
                var args = request.arguments;
                return { response: { infos: this.getHtmlDiagnosticsEx(args.files) }, responseRequired: true };
            }
            if (command == ts_impl.server.CommandNames.IDENgCompletions) {
                var args = request.arguments;
                return this.getNgCompletion(args);
            }
            if (skipAngular) {
                return _super.prototype.executeCommand.call(this, request);
            }
            if (command == ts_impl.server.CommandNames.Open) {
                if (this.tsVersion() == "2.0.5") {
                    var openArgs = request.arguments;
                    var file = openArgs.file;
                    var normalizePath = ts_impl.normalizePath(file);
                    this.projectService.getOrCreateScriptInfoForNormalizedPath(normalizePath, true, openArgs.fileContent);
                }
            }
            return _super.prototype.executeCommand.call(this, request);
        };
        AngularSession.prototype.beforeFirstMessage = function () {
            if (skipAngular) {
                return;
            }
            var sessionThis = this;
            var version = this.tsVersion();
            if (version == "2.0.0") {
                extendEx(ts_impl.server.Project, "updateFileMap", function (oldFunc, args) {
                    oldFunc.apply(this, args);
                    try {
                        if (this.filenameToSourceFile) {
                            sessionThis.logMessage("Connect templates to project");
                            var languageService = sessionThis.getLanguageService(this, false);
                            var ngLanguageService = sessionThis.getNgLanguageService(languageService);
                            for (var _i = 0, _a = ngLanguageService.getTemplateReferences(); _i < _a.length; _i++) {
                                var template = _a[_i];
                                var fileName = ts_impl.normalizePath(template);
                                this.filenameToSourceFile[template] = { fileName: fileName, text: "" };
                            }
                        }
                    }
                    catch (err) {
                        //something wrong
                        sessionThis.logError(err, "update graph ng service");
                    }
                });
            }
            else if (version == "2.0.5") {
                extendEx(ts_impl.server.Project, "updateGraph", function (oldFunc, args) {
                    oldFunc.apply(this, args);
                    try {
                        if (this.getScriptInfoLSHost) {
                            sessionThis.logMessage("Connect templates to project");
                            var languageService = sessionThis.getLanguageService(this, false);
                            var ngLanguageService = sessionThis.getNgLanguageService(languageService);
                            for (var _i = 0, _a = ngLanguageService.getTemplateReferences(); _i < _a.length; _i++) {
                                var template = _a[_i];
                                var fileName = ts_impl.normalizePath(template);
                                // attach script info to project (directly)
                                this.getScriptInfoLSHost(fileName);
                            }
                        }
                    }
                    catch (err) {
                        //something wrong
                        sessionThis.logError(err, "update graph ng service");
                    }
                });
                extendEx(ts_impl.server.ConfiguredProject, "close", function (oldFunc, args) {
                    sessionThis.logMessage("Disconnect templates from project");
                    var languageService = sessionThis.getLanguageService(this, false);
                    var ngLanguageService = sessionThis.getNgLanguageService(languageService);
                    for (var _i = 0, _a = ngLanguageService.getTemplateReferences(); _i < _a.length; _i++) {
                        var template = _a[_i];
                        var fileName = ts_impl.normalizePath(template);
                        // attach script info to project (directly)
                        var scriptInfoForNormalizedPath = sessionThis.getProjectService().getScriptInfoForNormalizedPath(fileName);
                        if (scriptInfoForNormalizedPath) {
                            scriptInfoForNormalizedPath.detachFromProject(this);
                        }
                    }
                    oldFunc.apply(this, args);
                });
            }
        };
        AngularSession.prototype.refreshStructureEx = function () {
            _super.prototype.refreshStructureEx.call(this);
            if (skipAngular) {
                return;
            }
            try {
                if (this.projectService) {
                    for (var _i = 0, _a = this.projectService.inferredProjects; _i < _a.length; _i++) {
                        var prj = _a[_i];
                        this.updateNgProject(prj);
                    }
                    for (var _b = 0, _c = this.projectService.configuredProjects; _b < _c.length; _b++) {
                        var prj = _c[_b];
                        this.updateNgProject(prj);
                    }
                }
            }
            catch (err) {
                this.logError(err, "refresh from angular");
                skipAngular = true;
                this.logMessage("ERROR angular integration will be disable", true);
            }
        };
        AngularSession.prototype.getHtmlDiagnosticsEx = function (fileNames) {
            var _this = this;
            var result = [];
            if (!skipAngular) {
                var _loop_1 = function(fileName) {
                    fileName = ts_impl.normalizePath(fileName);
                    var projectForFileEx = this_1.getForceProject(fileName);
                    try {
                        if (projectForFileEx) {
                            var htmlDiagnostics = this_1.appendPluginDiagnostics(projectForFileEx, [], fileName);
                            var mappedDiagnostics = htmlDiagnostics.map(function (el) { return _this.formatDiagnostic(fileName, projectForFileEx, el); });
                            result.push({
                                file: fileName,
                                diagnostics: mappedDiagnostics
                            });
                        }
                        else {
                            this_1.logMessage("Cannot find parent config for html file " + fileName);
                        }
                    }
                    catch (err) {
                        var angularErr = [this_1.formatDiagnostic(fileName, projectForFileEx, {
                                file: null,
                                code: -1,
                                messageText: "Angular Language Service internal globalError: " + err.message,
                                start: 0,
                                length: 0,
                                category: ts_impl.DiagnosticCategory.Error
                            })];
                        result.push({
                            file: fileName,
                            diagnostics: angularErr
                        });
                    }
                };
                var this_1 = this;
                for (var _i = 0, fileNames_1 = fileNames; _i < fileNames_1.length; _i++) {
                    var fileName = fileNames_1[_i];
                    _loop_1(fileName);
                }
            }
            return this.appendGlobalErrors(result);
        };
        AngularSession.prototype.updateNgProject = function (project) {
            var languageService = this.getLanguageService(project, false);
            var ngHost = this.getNgHost(languageService);
            if (ngHost.updateAnalyzedModules) {
                ngHost.updateAnalyzedModules();
            }
        };
        AngularSession.prototype.getNgLanguageService = function (languageService) {
            return languageService["ngService"];
        };
        AngularSession.prototype.getNgHost = function (languageService) {
            return languageService["ngHost"];
        };
        AngularSession.prototype.appendPluginDiagnostics = function (project, diags, normalizedFileName) {
            var languageService = project != null ? this.getLanguageService(project, false) : null;
            if (!languageService || skipAngular) {
                return diags;
            }
            var ngLanguageService = this.getNgLanguageService(languageService);
            if (!ngLanguageService) {
                //globalError
                return diags;
            }
            try {
                if (!diags) {
                    diags = [];
                }
                var errors = ngLanguageService.getDiagnostics(normalizedFileName);
                if (errors && errors.length) {
                    var file = this.getNgHost(languageService).getSourceFile(normalizedFileName);
                    for (var _i = 0, errors_1 = errors; _i < errors_1.length; _i++) {
                        var error = errors_1[_i];
                        diags.push({
                            file: file,
                            start: error.span.start,
                            length: error.span.end - error.span.start,
                            messageText: "Angular: " + error.message,
                            category: ts_impl.DiagnosticCategory.Error,
                            code: 0
                        });
                    }
                }
            }
            catch (err) {
                console.log('Error processing angular templates ' + err.message + '\n' + err.stack);
                diags.push({
                    file: null,
                    code: -1,
                    messageText: "Angular Language Service internal globalError: " + err.message,
                    start: 0,
                    length: 0,
                    category: ts_impl.DiagnosticCategory.Warning
                });
            }
            return diags;
        };
        AngularSession.prototype.appendProjectErrors = function (result, processedProjects, empty) {
            var appendProjectErrors = _super.prototype.appendProjectErrors.call(this, result, processedProjects, empty);
            appendProjectErrors = this.appendGlobalErrors(appendProjectErrors);
            return appendProjectErrors;
        };
        AngularSession.prototype.appendGlobalErrors = function (appendProjectErrors) {
            if (skipAngular && globalError) {
                appendProjectErrors.push({
                    file: null,
                    diagnostics: [{
                            category: "warning",
                            end: null,
                            start: null,
                            text: globalError
                        }]
                });
            }
            else if (this.tsVersion() == "2.0.0") {
                if (appendProjectErrors == null) {
                    appendProjectErrors = [];
                }
                appendProjectErrors.push({
                    file: null,
                    diagnostics: [{
                            category: "warning",
                            end: null,
                            start: null,
                            text: "For better performance please use TypeScript version 2.0.3 or higher"
                        }]
                });
            }
            return appendProjectErrors;
        };
        AngularSession.prototype.getNgCompletion = function (args) {
            if (skipAngular) {
                return {
                    response: [],
                    responseRequired: true
                };
            }
            var file = args.file;
            file = ts_impl.normalizePath(file);
            var project = this.getForceProject(file);
            if (!project) {
                return {
                    response: [],
                    responseRequired: true
                };
            }
            var offset = this.lineOffsetToPosition(project, file, args.line, args.offset);
            var ngLanguageService = this.getNgLanguageService(this.getLanguageService(project, false));
            var completionsAt = ngLanguageService.getCompletionsAt(file, offset);
            return {
                response: completionsAt == null ? [] : completionsAt,
                responseRequired: true
            };
        };
        return AngularSession;
    }(sessionClass));
    return AngularSession;
}
exports.createAngularSessionClass = createAngularSessionClass;
function extendEx(ObjectToExtend, name, func) {
    var proto = ObjectToExtend.prototype;
    var oldFunction = proto[name];
    proto[name] = function () {
        return func.apply(this, [oldFunction, arguments]);
    };
}
exports.extendEx = extendEx;
