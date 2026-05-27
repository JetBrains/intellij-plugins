"use strict";
exports.__esModule = true;
exports.FileKind = exports.ESLintResponse = exports.FixErrors = exports.GetErrors = void 0;
exports.GetErrors = "GetErrors";
exports.FixErrors = "FixErrors";
var ESLintResponse = /** @class */ (function () {
    function ESLintResponse(request_seq, command) {
        this.request_seq = request_seq;
        this.command = command;
    }
    return ESLintResponse;
}());
exports.ESLintResponse = ESLintResponse;
/**
 * See com.intellij.lang.javascript.linter.eslint.EslintUtil.FileKind
 */
var FileKind;
(function (FileKind) {
    FileKind["ts"] = "ts";
    FileKind["html"] = "html";
    FileKind["vue"] = "vue";
    FileKind["jsAndOther"] = "js_and_other";
})(FileKind = exports.FileKind || (exports.FileKind = {}));
//# sourceMappingURL=eslint-api.js.map