import {IDETypeScriptSession} from "./typings/typescript/util";
import {LanguageService} from "./typings/types";
import LanguageServiceHost = ts.LanguageServiceHost;

let path = require('path');

export function createAngularSessionClass(ts_impl: typeof ts, sessionClass: {new(state: TypeScriptPluginState): IDETypeScriptSession}) {

    (ts_impl.server.CommandNames as any).IDEGetHtmlErrors = "IDEGetHtmlErrors";


    let skipAngular = false;
    abstract class AngularSession extends sessionClass {

        beforeFirstMessage(): void {
            if (skipAngular) {
                return;
            }

            let sessionThis: AngularSession = this;
            let version = this.tsVersion();
            if (version == "2.0.0") {
                extendEx(ts_impl.server.Project, "updateFileMap", function (oldFunc, args) {
                    oldFunc.apply(this, args);
                    try {
                        if (this.filenameToSourceFile) {
                            let languageService = sessionThis.getLanguageService(this, false);
                            let ngLanguageService = sessionThis.getNgLanguageService(languageService);
                            for (let template of ngLanguageService.getTemplateReferences()) {
                                let fileName = ts_impl.normalizePath(template);
                                this.filenameToSourceFile[template] = {fileName, text: ""}
                            }
                        }
                    } catch (err) {
                        //something wrong
                        sessionThis.logError(err, "initialization");
                        skipAngular = true;
                    }
                });
            } else if (version == "2.0.5") {
                extendEx((ts_impl.server as any).Project, "updateGraph", function (oldFunc, args) {

                    try {
                        if (this.getScriptInfoLSHost) {
                            let languageService = sessionThis.getLanguageService(this, false);
                            let ngLanguageService = sessionThis.getNgLanguageService(languageService);
                            for (let template of ngLanguageService.getTemplateReferences()) {
                                let fileName = ts_impl.normalizePath(template);
                                // attach script info to project (directly)
                                this.getScriptInfoLSHost(fileName);
                            }
                        }

                    } catch (err) {
                        //something wrong
                        sessionThis.logError(err, "initialization");
                        skipAngular = true;
                    }

                    oldFunc.apply(this, args);
                });
            }
        }

        executeCommand(request: ts.server.protocol.Request): {response?: any; responseRequired?: boolean} {
            if (skipAngular) {
                return super.executeCommand(request);
            }

            let command = request.command;
            if (command == ts_impl.server.CommandNames.IDEGetHtmlErrors) {
                let args = request.arguments;
                return {response: {infos: this.getHtmlDiagnosticsEx(args.files)}, responseRequired: true};
            }

            return super.executeCommand(request);
        }

        refreshStructureEx(): void {
            super.refreshStructureEx();

            if (skipAngular) {
                return;
            }

            try {
                if (this.projectService) {
                    for (let prj of this.projectService.inferredProjects) {
                        this.updateNgProject(prj);
                    }

                    for (let prj of this.projectService.configuredProjects) {
                        this.updateNgProject(prj);
                    }
                }
            } catch (err) {
                this.logError(err, "refresh from angular");
                skipAngular = true;
                this.logMessage("ERROR angular integration will be disable", true);
            }
        }

        getHtmlDiagnosticsEx(fileNames: string[]): ts.server.protocol.DiagnosticEventBody[] {
            let result: ts.server.protocol.DiagnosticEventBody[] = [];
            for (let fileName of fileNames) {
                fileName = ts_impl.normalizePath(fileName);
                let projectForFileEx = this.getForceProject(fileName);
                try {
                    if (projectForFileEx) {
                        let htmlDiagnostics = this.appendPluginDiagnostics(projectForFileEx, [], fileName);
                        let mappedDiagnostics: ts.server.protocol.Diagnostic[] = htmlDiagnostics.map((el: ts.Diagnostic) => this.formatDiagnostic(fileName, projectForFileEx, el));

                        result.push({
                            file: fileName,
                            diagnostics: mappedDiagnostics
                        });
                    } else {
                        this.logMessage("Cannot find parent config for html file " + fileName);
                    }
                } catch (err) {
                    let angularErr = [this.formatDiagnostic(fileName, projectForFileEx, {
                        file: null,
                        code: -1,
                        messageText: "Angular Language Service internal error: " + err.message,
                        start: 0,
                        length: 0,
                        category: 0
                    })];
                    result.push({
                        file: fileName,
                        diagnostics: angularErr
                    });

                }
            }

            return result;
        }

        updateNgProject(project: ts.server.Project) {
            let languageService = this.getLanguageService(project);
            let ngHost: any = this.getNgHost(languageService);
            if (ngHost.updateAnalyzedModules) {
                ngHost.updateAnalyzedModules();
            }
        }

        getNgLanguageService(languageService: ts.LanguageService): LanguageService {
            return languageService["ngService"];
        }

        getNgHost(languageService: ts.LanguageService): LanguageServiceHost {
            return languageService["ngHost"];
        }

        appendPluginDiagnostics(project: ts.server.Project, diags: ts.Diagnostic[], normalizedFileName: string): ts.Diagnostic[] {
            let languageService = project != null ? this.getLanguageService(project) : null;
            if (!languageService || skipAngular) {
                return diags;
            }

            let ngLanguageService = this.getNgLanguageService(languageService);

            if (!ngLanguageService) {
                //error

                return diags;
            }

            try {
                if (!diags) {
                    diags = [];
                }

                let errors = ngLanguageService.getDiagnostics(normalizedFileName);
                if (errors && errors.length) {
                    let file = (this.getNgHost(languageService) as any).getSourceFile(normalizedFileName);
                    for (const error of errors) {
                        diags.push({
                            file,
                            start: error.span.start,
                            length: error.span.end - error.span.start,
                            messageText: "Angular: " + error.message,
                            category: 0,
                            code: 0
                        });
                    }
                }

            } catch (err) {
                console.log('Error processing angular templates ' + err.message + '\n' + err.stack);
                diags.push({
                    file: null,
                    code: -1,
                    messageText: "Angular Language Service internal error: " + err.message,
                    start: 0,
                    length: 0,
                    category: 0
                })
            }

            return diags;
        }
    }

    return AngularSession;
}


export function extendEx(ObjectToExtend: typeof ts.server.Project, name: string, func: (oldFunction: any, args: any) => any) {
    let proto: any = ObjectToExtend.prototype;

    let oldFunction = proto[name];

    proto[name] = function (this: ts.server.Project) {
        return func.apply(this, [oldFunction, arguments]);
    }
}
