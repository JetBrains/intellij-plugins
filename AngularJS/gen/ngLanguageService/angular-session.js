"use strict";
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
function createAngularSessionClass(ts_impl, sessionClass) {
    var AngularSession = (function (_super) {
        __extends(AngularSession, _super);
        function AngularSession() {
            _super.apply(this, arguments);
        }
        AngularSession.prototype.appendPluginDiagnostics = function (project, diags, normalizedFileName) {
            var languageService = project != null ? this.getLanguageService(project) : null;
            if (!languageService) {
                return diags;
            }
            var plugin = languageService["angular-plugin"];
            if (!plugin) {
                //error
                return diags;
            }
            if (!diags) {
                diags = [];
            }
            try {
                var semanticDiagnosticsFilter = plugin.getSemanticDiagnosticsFilter(normalizedFileName, diags);
                return semanticDiagnosticsFilter;
            }
            catch (err) {
                console.log('Error processing angular templates ' + err.message + '\n' + err.stack);
                diags.push({
                    file: null,
                    code: -1,
                    messageText: "Angular Language Service internal error: " + err.message,
                    start: 0,
                    length: 0,
                    category: ts_impl.DiagnosticCategory.Warning
                });
            }
            return diags;
        };
        return AngularSession;
    }(sessionClass));
    return AngularSession;
}
exports.createAngularSessionClass = createAngularSessionClass;
