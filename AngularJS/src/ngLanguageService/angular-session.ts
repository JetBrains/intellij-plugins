import {IDETypeScriptSession} from "./typings/typescript/util";
import {Completions, LanguageService} from "./typings/types";
import {SessionClass} from "./typings/typescript/ts-session-provider";
import LanguageServiceHost = ts.LanguageServiceHost;

let path = require('path');

export function createAngularSessionClass(ts_impl: typeof ts, sessionClass: { new (...args: any[]): IDETypeScriptSession }): SessionClass {

    (ts_impl.server.CommandNames as any).IDEGetHtmlErrors = "IDEGetHtmlErrors";
    (ts_impl.server.CommandNames as any).IDENgCompletions = "IDENgCompletions";
    (ts_impl.server.CommandNames as any).IDEGetProjectHtmlErr = "IDEGetProjectHtmlErr";


    let skipAngular = ts_impl["skipNg"];
    let refreshErrorCount = 0;
    let globalError = skipAngular ? skipAngular : null;

    abstract class AngularSession extends sessionClass {

        executeCommand(request: ts.server.protocol.Request): { response?: any; responseRequired?: boolean } {
            let command = request.command;
            if (command == ts_impl.server.CommandNames.IDEGetHtmlErrors) {
                let args = request.arguments;
                return {response: {infos: this.getHtmlDiagnosticsEx(args.files)}, responseRequired: true};
            }
            if (command == ts_impl.server.CommandNames.IDENgCompletions) {
                const args = <ts.server.protocol.CompletionsRequestArgs>request.arguments;

                return this.getNgCompletion(args);
            }

            if (command == ts_impl.server.CommandNames.IDEGetProjectHtmlErr || command == "geterrForProject") {
                let args: ts.server.protocol.FileRequestArgs = request.arguments;
                let fileName = args.file;
                let project = this.getProjectForFileEx(fileName);
                if (project != null && this.getProjectConfigPathEx(project)) {
                    try {
                        let pluginProjectDiagnostics = this.getPluginProjectDiagnostics(project);
                        pluginProjectDiagnostics.forEach(el => {
                            this.event(el, 'semanticDiag')
                        })
                    } catch (e) {
                        this.logError(e, "Internal angular service error");
                    }
                }

                return command == ts_impl.server.CommandNames.IDEGetProjectHtmlErr ?
                    (<any>this).processOldProjectErrors(request) :
                    super.executeCommand(request)
            }

            if (skipAngular) {
                return super.executeCommand(request);
            }

            if (command == ts_impl.server.CommandNames.Open) {
                if (this.tsVersion() == "2.0.5") {
                    const openArgs = <ts.server.protocol.OpenRequestArgs>request.arguments;
                    let file = openArgs.file;
                    let normalizePath = ts_impl.normalizePath(file);
                    (this.projectService as any).getOrCreateScriptInfoForNormalizedPath(normalizePath, true, openArgs.fileContent);
                }
            }

            return super.executeCommand(request);
        }

        beforeFirstMessage(): void {
            if (skipAngular) {
                super.beforeFirstMessage();
                return;
            }

            let sessionThis: AngularSession = this;
            let version = this.tsVersion();
            if (version == "2.0.0") {
                sessionThis.logMessage("Override updateFileMap (old)")
                extendEx(ts_impl.server.Project, "updateFileMap", function (oldFunc, args) {
                    oldFunc.apply(this, args);
                    try {
                        let projectPath = sessionThis.getProjectConfigPathEx(this);
                        if (projectPath) {
                            if (this.filenameToSourceFile) {
                                sessionThis.logMessage("Connect templates to project (old)")
                                for (let fileName of sessionThis.getTemplatesRefs(this)) {
                                    this.filenameToSourceFile[fileName] = {fileName, text: ""}
                                }
                            }
                        }
                    } catch (err) {
                        //something wrong
                        sessionThis.logError(err, "update graph ng service");
                    }
                });
            } else if (version == "2.0.5") {
                sessionThis.logMessage("Override updateFileMap (new)")
                extendEx((ts_impl.server as any).Project, "updateGraph", function (this: ts.server.Project, oldFunc, args) {
                    let result = oldFunc.apply(this, args);
                    try {
                        if ((<any>this).getScriptInfoLSHost) {
                            let projectPath = sessionThis.getProjectConfigPathEx(this);
                            if (projectPath) {
                                sessionThis.logMessage("Connect templates to project");
                                for (let fileName of sessionThis.getTemplatesRefs(this)) {
                                    (<any>this).getScriptInfoLSHost(fileName);
                                }
                            }
                        }

                    } catch (err) {
                        if (ts_impl["ngIncompatible"] && !ts_impl["ngInitErrorIncompatible"]) {
                            ts_impl["ngInitErrorIncompatible"] = "This version of Angular language service requires TypeScript 2.1 or higher.";
                        }

                        //something wrong
                        sessionThis.logError(err, "update graph ng service");
                    }

                    return result;
                });

                extendEx((ts_impl.server as any).ConfiguredProject, "close", function (oldFunc, args) {
                    sessionThis.logMessage("Disconnect templates from project");
                    let projectPath = sessionThis.getProjectConfigPathEx(this);
                    if (projectPath) {
                        for (let fileName of sessionThis.getTemplatesRefs(this)) {
                            // attach script info to project (directly)
                            let scriptInfoForNormalizedPath = (sessionThis as any).getProjectService().getScriptInfoForNormalizedPath(fileName);
                            if (scriptInfoForNormalizedPath) {
                                scriptInfoForNormalizedPath.detachFromProject(this);
                            }
                        }
                    }

                    return oldFunc.apply(this, args);
                });
            }
            super.beforeFirstMessage();
            sessionThis.logMessage("Complete before first message");
        }

        getTemplatesRefs(project: ts.server.Project): string[] {
            let result = []
            let languageService = this.getLanguageService(project, false);
            if (!languageService) return result;
            let ngLanguageService = this.getNgLanguageService(languageService);
            if (!ngLanguageService) return result;
            for (let template of ngLanguageService.getTemplateReferences()) {
                let fileName = ts_impl.normalizePath(template);
                result.push(fileName)
            }

            return result;
        }

        refreshStructureEx(): void {
            super.refreshStructureEx();

            if (skipAngular) {
                return;
            }

            try {
                if (this.projectService) {
                    for (let prj of this.projectService.configuredProjects) {
                        this.updateNgProject(prj);
                    }
                }
            } catch (err) {
                refreshErrorCount++;
                this.logError(err, "refresh from angular");
                if (refreshErrorCount > 1) {
                    skipAngular = true;
                    this.logMessage("ERROR angular integration will be disable", true);
                }
            }
        }

        getHtmlDiagnosticsEx(fileNames: string[]): ts.server.protocol.DiagnosticEventBody[] {
            let result: ts.server.protocol.DiagnosticEventBody[] = [];
            if (!skipAngular) {
                this.appendHtmlDiagnostics(null, fileNames, result);
            }

            return this.appendGlobalNgErrors(result);
        }

        private appendHtmlDiagnostics(project: ts.server.Project
            | null, fileNames: string[], result: ts.server.protocol.DiagnosticEventBody[]) {
            for (let fileName of fileNames) {
                fileName = ts_impl.normalizePath(fileName);
                project = project == null ? this.getForceProject(fileName) : project;
                try {
                    if (project) {
                        let htmlDiagnostics = this.getNgDiagnostics(project, fileName, null);
                        if (!htmlDiagnostics || htmlDiagnostics.length == 0) {
                            continue;
                        }

                        let mappedDiagnostics: ts.server.protocol.Diagnostic[] = htmlDiagnostics.map((el: ts.Diagnostic) => this.formatDiagnostic(fileName, project, el));

                        result.push({
                            file: fileName,
                            diagnostics: mappedDiagnostics
                        });
                    } else {
                        this.logMessage("Cannot find parent config for html file " + fileName);
                    }
                } catch (err) {
                    let angularErr = [this.formatDiagnostic(fileName, project, {
                        file: null,
                        code: -1,
                        messageText: "Angular Language Service internal globalError: " + err.message + err.stack,
                        start: 0,
                        length: 0,
                        category: ts_impl.DiagnosticCategory.Error
                    })];
                    this.logError(err, "HtmlDiagnostics");

                    result.push({
                        file: fileName,
                        diagnostics: angularErr
                    });

                }
            }
        }

        updateNgProject(project: ts.server.Project) {
            let languageService = this.getLanguageService(project, false);
            let ngHost: any = this.getNgHost(languageService);
            if (ngHost.updateAnalyzedModules) {
                ngHost.updateAnalyzedModules();
            }
        }

        getNgLanguageService(languageService: ts.LanguageService): LanguageService {
            let ngService = languageService["ngService"];
            return ngService ? ngService() : null;
        }

        getNgHost(languageService: ts.LanguageService): LanguageServiceHost {
            let ngHost = languageService["ngHost"];
            return ngHost ? ngHost() : ngHost;
        }

        private getNgDiagnostics(project: ts.server.Project, normalizedFileName: string, sourceFile: ts.SourceFile): ts.Diagnostic[] {

            let languageService = project != null && this.getProjectConfigPathEx(project) ? this.getLanguageService(project, false) : null;
            if (!languageService || skipAngular) {
                return [];
            }

            let ngLanguageService = this.getNgLanguageService(languageService);

            if (!ngLanguageService) {
                //globalError

                return [];
            }

            return getServiceDiags(ts_impl, ngLanguageService, this.getNgHost(languageService), normalizedFileName, sourceFile, languageService);
        }


        getPluginProjectDiagnostics(project: ts.server.Project): ts.server.protocol.DiagnosticEventBody[] | null {
            let program: ts.Program = this.getLanguageService(project).getProgram();


            if (!project || !program || this.tsVersion() == "2.0.0") {
                return [];
            }

            let result = []

            for (let file of program.getSourceFiles()) {
                let fileName = file.fileName;

                let ngDiagnostics: ts.Diagnostic[] = this.getNgDiagnostics(project, fileName, file);
                if (!ngDiagnostics || ngDiagnostics.length == 0) {
                    continue;
                }

                let mappedDiags = ngDiagnostics.map(el => this.formatDiagnostic(fileName, project, el));

                result.push({
                    file: fileName,
                    diagnostics: mappedDiags
                });
            }

            if (this.getProjectConfigPathEx(project)) {
                let templatesRefs = this.getTemplatesRefs(project);

                if (templatesRefs && templatesRefs.length > 0) {
                    this.appendHtmlDiagnostics(project, templatesRefs, result)
                }
            }

            return result;
        }

        appendGlobalErrors(result: ts.server.protocol.DiagnosticEventBody[], processedProjects: { [p: string]: ts.server.Project }, empty: boolean): ts.server.protocol.DiagnosticEventBody[] {
            let appendProjectErrors = super.appendGlobalErrors(result, processedProjects, empty);
            appendProjectErrors = this.appendGlobalNgErrors(appendProjectErrors);
            return appendProjectErrors;
        }

        private appendGlobalNgErrors(appendProjectErrors: ts.server.protocol.DiagnosticEventBody[]) {
            if (skipAngular && globalError) {
                appendProjectErrors.push({
                    file: null,
                    diagnostics: [{
                        category: "warning",
                        end: null,
                        start: null,
                        text: globalError
                    }]
                })
            } else if (this.tsVersion() == "2.0.0") {
                if (appendProjectErrors == null) {
                    appendProjectErrors = []
                }
                appendProjectErrors.push({
                    file: null,
                    diagnostics: [{
                        category: "warning",
                        end: null,
                        start: null,
                        text: "For better performance please use TypeScript version 2.0.3 or higher. Angular project errors are disabled"
                    }]
                })
            } else if (ts_impl["ngInitErrorIncompatible"]) {
                if (appendProjectErrors == null) {
                    appendProjectErrors = []
                }
                appendProjectErrors.push({
                    file: null,
                    diagnostics: [{
                        category: "warning",
                        end: null,
                        start: null,
                        text: ts_impl["ngInitErrorIncompatible"]
                    }]
                })
            }
            return appendProjectErrors;
        }

        getNgCompletion(args: ts.server.protocol.CompletionsRequestArgs) {
            if (skipAngular) {
                return {
                    response: [],
                    responseRequired: true
                };
            }
            let file = args.file;
            file = ts_impl.normalizePath(file);
            let project = this.getForceProject(file);
            if (!project || !this.getProjectConfigPathEx(project)) {
                return {
                    response: [],
                    responseRequired: true
                };
            }

            let offset = this.lineOffsetToPosition(project, file, args.line, args.offset);
            let ngLanguageService = this.getNgLanguageService(this.getLanguageService(project, false));

            let completionsAt: Completions = ngLanguageService.getCompletionsAt(file, offset);

            return {
                response: completionsAt == null ? [] : completionsAt,
                responseRequired: true
            };
        }
    }

    return <any>AngularSession;
}


export function extendEx(ObjectToExtend: typeof ts.server.Project, name: string, func: (oldFunction: any, args: any) => any) {
    let proto: any = ObjectToExtend.prototype;

    let oldFunction = proto[name];

    proto[name] = function (this: ts.server.Project) {
        return func.apply(this, [oldFunction, arguments]);
    }
}

export function getServiceDiags(ts_impl, ngLanguageService: LanguageService, ngHost: LanguageServiceHost, normalizedFileName: string, sourceFile: ts.SourceFile | null, languageService: ts.LanguageService) {
    let diags = [];
    try {
        let errors = ngLanguageService.getDiagnostics(normalizedFileName);
        if (errors && errors.length) {
            let file = sourceFile != null ? sourceFile : (ngHost as any).getSourceFile(normalizedFileName);
            for (const error of errors) {
                diags.push({
                    file,
                    start: error.span.start,
                    length: error.span.end - error.span.start,
                    messageText: "Angular: " + error.message,
                    category: ts_impl.DiagnosticCategory.Error,
                    code: 0
                });
            }
        }
    } catch (err) {
        diags.push({
            file: null,
            code: -1,
            messageText: "Angular Language Service internal globalError: " + err.message,
            start: 0,
            length: 0,
            category: ts_impl.DiagnosticCategory.Warning
        })
    }
    return diags;
}