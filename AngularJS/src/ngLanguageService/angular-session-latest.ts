import {SessionClass} from "./typings/ts-session-provider";
import {IDEGetHtmlErrors, IDEGetProjectHtmlErr, IDENgCompletions} from "./ngutil";
import * as ts from './typings/tsserverlibrary';
import * as logger from './typings/logger-impl'


export function createAngularSessionClass(ts_impl: typeof ts, sessionClass: SessionClass, loggerImpl: typeof logger): SessionClass {


    class AngularSessionLatest extends sessionClass {
        executeCommand(request: ts.server.protocol.Request): { response?: any; responseRequired?: boolean } {
            let command = request.command;
            if (command == IDEGetHtmlErrors) {
                request.command = ts_impl.server.CommandNames.SemanticDiagnosticsSync;

                return super.executeCommand(request)
            }
            if (command == IDENgCompletions) {

                request.command = ts_impl.server.CommandNames.Completions;
                return super.executeCommand(request)
            }

            if (command == IDEGetProjectHtmlErr || command == "geterrForProject") {
                let fileRequestArgs = <ts.server.protocol.FileRequestArgs>request.arguments;
                this.sendNgProjectDiagnostics(fileRequestArgs);

                if (command == IDEGetProjectHtmlErr) {
                    request.command = ts_impl.server.CommandNames.GeterrForProject;
                }
                return super.executeCommand(request)
            }

            return super.executeCommand(request);
        }

        private sendNgProjectDiagnostics(fileRequestArgs: ts.server.protocol.FileRequestArgs) {
            try {
                loggerImpl.serverLogger("ngLog: Start process project diagnostics");
                let projectFileName = fileRequestArgs.projectFileName;
                let project: ts.server.Project = null;
                if (projectFileName != null) {
                    project = this.projectService.findProject(projectFileName)
                } else {
                    let fileName = ts_impl.normalizePath(fileRequestArgs.file);
                    project = this.projectService.getDefaultProjectForFile(fileName, false);
                }

                if (!project || project.projectKind == ts_impl.server.ProjectKind.Inferred) {
                    loggerImpl.serverLogger("ngLog: Cannot find project for project ng diagnostics");
                    return;
                }

                let externalFiles = project.getExternalFiles();
                if (!externalFiles || externalFiles.length == 0) {
                    loggerImpl.serverLogger("ngLog: No external files for project " + project.getProjectName());
                    return;
                }

                externalFiles.forEach(file => {
                    let response = this.executeCommand({
                        command: IDEGetHtmlErrors,
                        seq: 0,
                        type: "request",
                        arguments: {
                            projectFileName: projectFileName ? projectFileName : project.getProjectName(),
                            file
                        }
                    });

                    if (loggerImpl.isLogEnabled) {
                        loggerImpl.serverLogger("ngLog: Response: " + JSON.stringify(response));
                    }

                    let body = <ts.server.protocol.Diagnostic[]>response.response;

                    if (body && body.length > 0) {
                        let toSend: ts.server.protocol.DiagnosticEventBody = {
                            file,
                            diagnostics: body
                        }
                        this.event(toSend, 'semanticDiag');

                        if (loggerImpl.isLogEnabled) {
                            loggerImpl.serverLogger("ngLog: end sending diagnostics " + body.length);
                        }
                    } else {
                        if (loggerImpl.isLogEnabled) {
                            loggerImpl.serverLogger("ngLog: no diagnostics for " + file);
                        }
                    }
                });
            } catch (e) {
                loggerImpl.serverLogger("ngLog: Cannot process project errors " + e.message);
                if (loggerImpl.isLogEnabled) {
                    throw e;
                }
            }
        }
    }


    return AngularSessionLatest;
}