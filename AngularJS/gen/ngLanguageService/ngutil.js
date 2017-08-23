"use strict";
exports.__esModule = true;
exports.IDEGetHtmlErrors = "IDEGetHtmlErrors";
exports.IDENgCompletions = "IDENgCompletions";
exports.IDEGetProjectHtmlErr = "IDEGetProjectHtmlErr";
function getServiceDiags(ts_impl, ngLanguageService, ngHost, normalizedFileName, sourceFile, languageService) {
    var diags = [];
    try {
        var errors = ngLanguageService.getDiagnostics(normalizedFileName);
        if (errors && errors.length) {
            var file = sourceFile != null ? sourceFile : ngHost.getSourceFile(normalizedFileName);
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
}
exports.getServiceDiags = getServiceDiags;
