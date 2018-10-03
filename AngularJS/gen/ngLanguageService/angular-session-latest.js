"use strict";
var __extends = (this && this.__extends) || (function () {
    var extendStatics = function (d, b) {
        extendStatics = Object.setPrototypeOf ||
            ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
            function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
        return extendStatics(d, b);
    }
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
exports.__esModule = true;
var ngutil_1 = require("./ngutil");
function createAngularSessionClass(ts_impl, sessionClass, loggerImpl) {
    var AngularSessionLatest = /** @class */ (function (_super) {
        __extends(AngularSessionLatest, _super);
        function AngularSessionLatest() {
            return _super !== null && _super.apply(this, arguments) || this;
        }
        AngularSessionLatest.prototype.executeCommand = function (request) {
            var command = request.command;
            if (command == ngutil_1.IDEGetHtmlErrors) {
                if (!this.hasConfiguredProject(request))
                    return { responseRequired: true, response: [] };
                request.command = ts_impl.server.CommandNames.SemanticDiagnosticsSync;
                return _super.prototype.executeCommand.call(this, request);
            }
            if (command == ngutil_1.IDENgCompletions) {
                if (!this.hasConfiguredProject(request))
                    return { responseRequired: true, response: [] };
                request.command = ts_impl.server.CommandNames.Completions;
                return _super.prototype.executeCommand.call(this, request);
            }
            if (command == ngutil_1.IDEGetProjectHtmlErr || command == "geterrForProject") {
                var fileRequestArgs = request.arguments;
                if (this.hasConfiguredProject(request)) {
                    this.sendNgProjectDiagnostics(fileRequestArgs);
                }
                if (command == ngutil_1.IDEGetProjectHtmlErr) {
                    request.command = ts_impl.server.CommandNames.GeterrForProject;
                }
                return _super.prototype.executeCommand.call(this, request);
            }
            return _super.prototype.executeCommand.call(this, request);
        };
        AngularSessionLatest.prototype.hasConfiguredProject = function (request) {
            var fileRequestArgs = request.arguments;
            var fileName = ts_impl.normalizePath(fileRequestArgs.file);
            var defaultProject = this.projectService.getDefaultProjectForFile(fileName, false);
            if (defaultProject == null || defaultProject.projectKind == ts_impl.server.ProjectKind.Inferred) {
                return false;
            }
            return true;
        };
        AngularSessionLatest.prototype.sendNgProjectDiagnostics = function (fileRequestArgs) {
            var _this = this;
            try {
                loggerImpl.serverLogger("ngLog: Start process project diagnostics");
                var projectFileName_1 = fileRequestArgs.projectFileName;
                var project_1 = null;
                if (projectFileName_1 != null) {
                    project_1 = this.projectService.findProject(projectFileName_1);
                }
                else {
                    var fileName = ts_impl.normalizePath(fileRequestArgs.file);
                    project_1 = this.projectService.getDefaultProjectForFile(fileName, false);
                }
                if (!project_1 || project_1.projectKind == ts_impl.server.ProjectKind.Inferred) {
                    loggerImpl.serverLogger("ngLog: Cannot find project for project ng diagnostics");
                    return;
                }
                var externalFiles = project_1.getExternalFiles();
                if (!externalFiles || externalFiles.length == 0) {
                    loggerImpl.serverLogger("ngLog: No external files for project " + project_1.getProjectName());
                    return;
                }
                externalFiles.forEach(function (file) {
                    var response = _this.executeCommand({
                        command: ngutil_1.IDEGetHtmlErrors,
                        seq: 0,
                        type: "request",
                        arguments: {
                            projectFileName: projectFileName_1 ? projectFileName_1 : project_1.getProjectName(),
                            file: file
                        }
                    });
                    if (loggerImpl.isLogEnabled) {
                        loggerImpl.serverLogger("ngLog: Response: " + JSON.stringify(response));
                    }
                    var body = response.response;
                    if (body && body.length > 0) {
                        var toSend = {
                            file: file,
                            diagnostics: body
                        };
                        _this.event(toSend, 'semanticDiag');
                        if (loggerImpl.isLogEnabled) {
                            loggerImpl.serverLogger("ngLog: end sending diagnostics " + body.length);
                        }
                    }
                    else {
                        if (loggerImpl.isLogEnabled) {
                            loggerImpl.serverLogger("ngLog: no diagnostics for " + file);
                        }
                    }
                });
            }
            catch (e) {
                loggerImpl.serverLogger("ngLog: Cannot process project errors " + e.message);
                if (loggerImpl.isLogEnabled) {
                    throw e;
                }
            }
        };
        return AngularSessionLatest;
    }(sessionClass));
    return AngularSessionLatest;
}
exports.createAngularSessionClass = createAngularSessionClass;
