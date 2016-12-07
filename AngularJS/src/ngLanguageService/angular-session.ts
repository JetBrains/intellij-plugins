import {IDETypeScriptSession} from "./typings/typescript/util";

export function createAngularSessionClass(ts_impl: typeof ts, sessionClass: {new(state): IDETypeScriptSession}) {

    abstract class AngularSession extends sessionClass {

        appendPluginDiagnostics(project: ts.server.Project, diags: ts.Diagnostic[], normalizedFileName: string): ts.Diagnostic[] {
            let languageService = project != null ? this.getLanguageService(project) : null;
            if (!languageService) {
                return diags;
            }
            let plugin: LanguageServicePlugin = languageService["angular-plugin"];
            if (!plugin) {
                //error

                return diags;
            }

            if (!diags) {
                diags = [];
            }

            try {
                let semanticDiagnosticsFilter = plugin.getSemanticDiagnosticsFilter(normalizedFileName, diags);
                return semanticDiagnosticsFilter;
            } catch (err) {
                console.log('Error processing angular templates ' + err.message + '\n' + err.stack);
                diags.push({
                    file: null,
                    code: -1,
                    messageText: "Angular Language Service internal error: " + err.message,
                    start: 0,
                    length: 0,
                    category: ts_impl.DiagnosticCategory.Warning
                })
            }

            return diags;
        }
    }

    return AngularSession;
}
