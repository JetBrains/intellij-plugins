import {IDETypeScriptSession} from "./typings/typescript/util";
import {LanguageService} from "./typings/types";
import LanguageServiceHost = ts.LanguageServiceHost;

export function createAngularSessionClass(ts_impl: typeof ts, sessionClass: {new(state: TypeScriptPluginState): IDETypeScriptSession}) {

    abstract class AngularSession extends sessionClass {


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

        updateNgProject(project: ts.server.Project) {
            let languageService = this.getLanguageService(project);
            let ngHost: any = this.getNgHost(languageService);
            if (ngHost.updateAnalyzedModules) {
                ngHost.updateAnalyzedModules();
                this.logMessage("Updated ng project");
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
                            messageText: error.message,
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
