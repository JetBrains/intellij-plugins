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
    ts_impl.server.CommandNames.IDEGetProjectHtmlErr = "IDEGetProjectHtmlErr";
    var skipAngular = ts_impl["skipNg"];
    var refreshErrorCount = 0;
    var globalError = skipAngular ? skipAngular : null;
    var AngularSession = (function (_super) {
        __extends(AngularSession, _super);
        function AngularSession() {
            return _super.apply(this, arguments) || this;
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
            if (command == ts_impl.server.CommandNames.IDEGetProjectHtmlErr) {
                var args = request.arguments;
                var fileName = args.file;
                var project = this.getProjectForFileEx(fileName);
                if (project == null || !this.getProjectConfigPathEx(project)) {
                    return { response: { infos: [] }, responseRequired: true };
                }
                return { response: { infos: this.getProjectDiagnosticsEx(project) }, responseRequired: true };
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
                _super.prototype.beforeFirstMessage.call(this);
                return;
            }
            var sessionThis = this;
            var version = this.tsVersion();
            if (version == "2.0.0") {
                sessionThis.logMessage("Override updateFileMap (old)");
                extendEx(ts_impl.server.Project, "updateFileMap", function (oldFunc, args) {
                    oldFunc.apply(this, args);
                    try {
                        var projectPath = sessionThis.getProjectConfigPathEx(this);
                        if (projectPath) {
                            if (this.filenameToSourceFile) {
                                sessionThis.logMessage("Connect templates to project (old)");
                                for (var _i = 0, _a = sessionThis.getTemplatesRefs(this); _i < _a.length; _i++) {
                                    var fileName = _a[_i];
                                    this.filenameToSourceFile[fileName] = { fileName: fileName, text: "" };
                                }
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
                sessionThis.logMessage("Override updateFileMap (new)");
                extendEx(ts_impl.server.Project, "updateGraph", function (oldFunc, args) {
                    var result = oldFunc.apply(this, args);
                    try {
                        if (this.getScriptInfoLSHost) {
                            var projectPath = sessionThis.getProjectConfigPathEx(this);
                            if (projectPath) {
                                sessionThis.logMessage("Connect templates to project");
                                for (var _i = 0, _a = sessionThis.getTemplatesRefs(this); _i < _a.length; _i++) {
                                    var fileName = _a[_i];
                                    this.getScriptInfoLSHost(fileName);
                                }
                            }
                        }
                    }
                    catch (err) {
                        if (ts_impl["ngIncompatible"] && !ts_impl["ngInitErrorIncompatible"]) {
                            ts_impl["ngInitErrorIncompatible"] = "This version of Angular language service requires TypeScript 2.1 or higher.";
                        }
                        //something wrong
                        sessionThis.logError(err, "update graph ng service");
                    }
                    return result;
                });
                extendEx(ts_impl.server.ConfiguredProject, "close", function (oldFunc, args) {
                    sessionThis.logMessage("Disconnect templates from project");
                    var projectPath = sessionThis.getProjectConfigPathEx(this);
                    if (projectPath) {
                        for (var _i = 0, _a = sessionThis.getTemplatesRefs(this); _i < _a.length; _i++) {
                            var fileName = _a[_i];
                            // attach script info to project (directly)
                            var scriptInfoForNormalizedPath = sessionThis.getProjectService().getScriptInfoForNormalizedPath(fileName);
                            if (scriptInfoForNormalizedPath) {
                                scriptInfoForNormalizedPath.detachFromProject(this);
                            }
                        }
                    }
                    return oldFunc.apply(this, args);
                });
            }
            _super.prototype.beforeFirstMessage.call(this);
            sessionThis.logMessage("Complete before first message");
        };
        AngularSession.prototype.getTemplatesRefs = function (project) {
            var result = [];
            var languageService = this.getLanguageService(project, false);
            if (!languageService)
                return result;
            var ngLanguageService = this.getNgLanguageService(languageService);
            if (!ngLanguageService)
                return result;
            for (var _i = 0, _a = ngLanguageService.getTemplateReferences(); _i < _a.length; _i++) {
                var template = _a[_i];
                var fileName = ts_impl.normalizePath(template);
                result.push(fileName);
            }
            return result;
        };
        AngularSession.prototype.refreshStructureEx = function () {
            _super.prototype.refreshStructureEx.call(this);
            if (skipAngular) {
                return;
            }
            try {
                if (this.projectService) {
                    for (var _i = 0, _a = this.projectService.configuredProjects; _i < _a.length; _i++) {
                        var prj = _a[_i];
                        this.updateNgProject(prj);
                    }
                }
            }
            catch (err) {
                refreshErrorCount++;
                this.logError(err, "refresh from angular");
                if (refreshErrorCount > 1) {
                    skipAngular = true;
                    this.logMessage("ERROR angular integration will be disable", true);
                }
            }
        };
        AngularSession.prototype.getHtmlDiagnosticsEx = function (fileNames) {
            var result = [];
            if (!skipAngular) {
                this.appendHtmlDiagnostics(null, fileNames, result);
            }
            return this.appendGlobalNgErrors(result);
        };
        AngularSession.prototype.appendHtmlDiagnostics = function (project, fileNames, result) {
            var _this = this;
            var _loop_1 = function (fileName) {
                fileName = ts_impl.normalizePath(fileName);
                project = project == null ? this_1.getForceProject(fileName) : project;
                try {
                    if (project) {
                        var htmlDiagnostics = this_1.getNgDiagnostics(project, fileName, null);
                        if (!htmlDiagnostics || htmlDiagnostics.length == 0) {
                            return "continue";
                        }
                        var mappedDiagnostics = htmlDiagnostics.map(function (el) { return _this.formatDiagnostic(fileName, project, el); });
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
                    var angularErr = [this_1.formatDiagnostic(fileName, project, {
                            file: null,
                            code: -1,
                            messageText: "Angular Language Service internal globalError: " + err.message + err.stack,
                            start: 0,
                            length: 0,
                            category: ts_impl.DiagnosticCategory.Error
                        })];
                    this_1.logError(err, "HtmlDiagnostics");
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
            var result = this.getNgDiagnostics(project, normalizedFileName, null);
            if (!result || result.length == 0) {
                return diags;
            }
            if (!diags) {
                diags = [];
            }
            return diags.concat(result);
        };
        AngularSession.prototype.getNgDiagnostics = function (project, normalizedFileName, sourceFile) {
            var languageService = project != null && this.getProjectConfigPathEx(project) ? this.getLanguageService(project, false) : null;
            if (!languageService || skipAngular) {
                return [];
            }
            var ngLanguageService = this.getNgLanguageService(languageService);
            if (!ngLanguageService) {
                //globalError
                return [];
            }
            var diags = [];
            try {
                var errors = ngLanguageService.getDiagnostics(normalizedFileName);
                if (errors && errors.length) {
                    var file = sourceFile != null ? sourceFile : this.getNgHost(languageService).getSourceFile(normalizedFileName);
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
                this.logError(err, "ng diagnostics");
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
        AngularSession.prototype.appendPluginProjectDiagnostics = function (project, program, diags) {
            var _this = this;
            var result = _super.prototype.appendPluginProjectDiagnostics.call(this, project, program, diags);
            if (!project || !program || this.tsVersion() == "2.0.0") {
                return result;
            }
            if (result == null) {
                result = [];
            }
            var _loop_2 = function (file) {
                var fileName = file.fileName;
                var ngDiagnostics = this_2.getNgDiagnostics(project, fileName, file);
                if (!ngDiagnostics || ngDiagnostics.length == 0) {
                    return "continue";
                }
                var mappedDiags = ngDiagnostics.map(function (el) { return _this.formatDiagnostic(fileName, project, el); });
                result.push({
                    file: fileName,
                    diagnostics: mappedDiags
                });
            };
            var this_2 = this;
            for (var _i = 0, _a = program.getSourceFiles(); _i < _a.length; _i++) {
                var file = _a[_i];
                _loop_2(file);
            }
            if (this.getProjectConfigPathEx(project)) {
                var templatesRefs = this.getTemplatesRefs(project);
                if (templatesRefs && templatesRefs.length > 0) {
                    this.appendHtmlDiagnostics(project, templatesRefs, result);
                }
            }
            return result;
        };
        AngularSession.prototype.appendGlobalErrors = function (result, processedProjects, empty) {
            var appendProjectErrors = _super.prototype.appendGlobalErrors.call(this, result, processedProjects, empty);
            appendProjectErrors = this.appendGlobalNgErrors(appendProjectErrors);
            return appendProjectErrors;
        };
        AngularSession.prototype.appendGlobalNgErrors = function (appendProjectErrors) {
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
                            text: "For better performance please use TypeScript version 2.0.3 or higher. Angular project errors are disabled"
                        }]
                });
            }
            else if (ts_impl["ngInitErrorIncompatible"]) {
                if (appendProjectErrors == null) {
                    appendProjectErrors = [];
                }
                appendProjectErrors.push({
                    file: null,
                    diagnostics: [{
                            category: "warning",
                            end: null,
                            start: null,
                            text: ts_impl["ngInitErrorIncompatible"]
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
            if (!project || !this.getProjectConfigPathEx(project)) {
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
