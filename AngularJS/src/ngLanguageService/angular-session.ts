import {IDETypeScriptSession} from "./typings/typescript/util";
import {LanguageService} from "./typings/types";
import LanguageServiceHost = ts.LanguageServiceHost;

let path = require('path');

export function createAngularSessionClass(ts_impl: typeof ts, sessionClass: {new(state: TypeScriptPluginState): IDETypeScriptSession}) {

    (ts_impl.server.CommandNames as any).IDEGetHtmlErrors = "IDEGetHtmlErrors";


    abstract class AngularSession extends sessionClass {

        beforeFirstMessage(): void {
            if (this.tsVersion() == "2.0.0") {
                let sessionThis: AngularSession = this;
                extendEx(ts_impl.server.Project, "updateFileMap", function (oldFunc, args) {
                    oldFunc.apply(this, args);
                    if (this.filenameToSourceFile) {
                        let languageService = sessionThis.getLanguageService(this);
                        let ngLanguageService = sessionThis.getNgLanguageService(languageService);
                        for (let template of ngLanguageService.getTemplateReferences()) {
                            let fileName = ts_impl.normalizePath(template);
                            sessionThis.logMessage("File " + fileName);
                            this.filenameToSourceFile[template] = {fileName, text: ""}
                        }
                    }
                });
            }
        }

        executeCommand(request: ts.server.protocol.Request): {response?: any; responseRequired?: boolean} {
            let command = request.command;
            if (command == ts_impl.server.CommandNames.IDEGetHtmlErrors) {
                let args = request.arguments;
                return {response: {infos: this.getHtmlDiagnosticsEx(args.files)}, responseRequired: true};
            }

            return super.executeCommand(request);
        }

        refreshStructureEx(): void {
            super.refreshStructureEx();

            if (this.projectService) {
                for (let prj of this.projectService.inferredProjects) {
                    this.updateNgProject(prj);
                }

                for (let prj of this.projectService.configuredProjects) {
                    this.updateNgProject(prj);
                }
            }
        }

        getHtmlDiagnosticsEx(fileNames: string[]): ts.server.protocol.DiagnosticEventBody[] {
            let result: ts.server.protocol.DiagnosticEventBody[] = [];
            for (let fileName of fileNames) {
                fileName = ts_impl.normalizePath(fileName);
                let projectForFileEx = this.getForceProject(fileName);
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
            if (!languageService) {
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
