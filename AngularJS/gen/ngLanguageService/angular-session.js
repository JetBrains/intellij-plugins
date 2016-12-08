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
        AngularSession.prototype.refreshStructureEx = function () {
            _super.prototype.refreshStructureEx.call(this);
            if (this.projectService) {
                for (var _i = 0, _a = this.projectService.inferredProjects; _i < _a.length; _i++) {
                    var prj = _a[_i];
                    this.updateNgProject(prj);
                }
                for (var _b = 0, _c = this.projectService.configuredProjects; _b < _c.length; _b++) {
                    var prj = _c[_b];
                    this.updateNgProject(prj);
                }
            }
        };
        AngularSession.prototype.updateNgProject = function (project) {
            var languageService = this.getLanguageService(project);
            var ngHost = this.getNgHost(languageService);
            if (ngHost.updateAnalyzedModules) {
                ngHost.updateAnalyzedModules();
                this.logMessage("Updated ng project");
            }
        };
        AngularSession.prototype.getNgLanguageService = function (languageService) {
            return languageService["ngService"];
        };
        AngularSession.prototype.getNgHost = function (languageService) {
            return languageService["ngHost"];
        };
        AngularSession.prototype.appendPluginDiagnostics = function (project, diags, normalizedFileName) {
            var languageService = project != null ? this.getLanguageService(project) : null;
            if (!languageService) {
                return diags;
            }
            var ngLanguageService = this.getNgLanguageService(languageService);
            if (!ngLanguageService) {
                //error
                return diags;
            }
            try {
                if (!diags) {
                    diags = [];
                }
                var errors = ngLanguageService.getDiagnostics(normalizedFileName);
                if (errors && errors.length) {
                    var file = this.getNgHost(languageService).getSourceFile(normalizedFileName);
                    for (var _i = 0, errors_1 = errors; _i < errors_1.length; _i++) {
                        var error = errors_1[_i];
                        diags.push({
                            file: file,
                            start: error.span.start,
                            length: error.span.end - error.span.start,
                            messageText: error.message,
                            category: 0,
                            code: 0
                        });
                    }
                }
            }
            catch (err) {
                console.log('Error processing angular templates ' + err.message + '\n' + err.stack);
                diags.push({
                    file: null,
                    code: -1,
                    messageText: "Angular Language Service internal error: " + err.message,
                    start: 0,
                    length: 0,
                    category: 0
                });
            }
            return diags;
        };
        return AngularSession;
    }(sessionClass));
    return AngularSession;
}
exports.createAngularSessionClass = createAngularSessionClass;
