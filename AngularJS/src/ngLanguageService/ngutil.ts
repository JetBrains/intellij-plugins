import * as ts from './typings/tsserverlibrary';
import {LanguageService as ngLanguageService} from "./typings/types";

export const IDEGetHtmlErrors = "IDEGetHtmlErrors";
export const IDENgCompletions = "IDENgCompletions";
export const IDEGetProjectHtmlErr = "IDEGetProjectHtmlErr";


export function getServiceDiags(ts_impl, ngLanguageService: ngLanguageService, ngHost: ts.LanguageServiceHost, normalizedFileName: string, sourceFile: ts.SourceFile | null, languageService: ts.LanguageService) {
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