"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (this && this.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g;
    return g = { next: verb(0), "throw": verb(1), "return": verb(2) }, typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (_) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
exports.__esModule = true;
exports.Standard17Plugin = void 0;
var eslint_api_1 = require("./eslint-api");
var eslint_common_1 = require("./eslint-common");
var Standard17Plugin = /** @class */ (function () {
    function Standard17Plugin(state) {
        this.includeSourceText = state.includeSourceText;
        this.standardPackagePath = state.standardPackagePath;
    }
    Standard17Plugin.prototype.onMessage = function (p, writer) {
        return __awaiter(this, void 0, void 0, function () {
            var request, response, lintResults, lintResults, e_1;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0:
                        request = JSON.parse(p);
                        response = new eslint_api_1.ESLintResponse(request.seq, request.command);
                        _a.label = 1;
                    case 1:
                        _a.trys.push([1, 7, , 8]);
                        if (!(request.command === eslint_api_1.GetErrors)) return [3 /*break*/, 3];
                        return [4 /*yield*/, this.getErrors(request.arguments)];
                    case 2:
                        lintResults = _a.sent();
                        response.body = { results: this.filterSourceIfNeeded(lintResults) };
                        return [3 /*break*/, 6];
                    case 3:
                        if (!(request.command === eslint_api_1.FixErrors)) return [3 /*break*/, 5];
                        return [4 /*yield*/, this.fixErrors(request.arguments)];
                    case 4:
                        lintResults = _a.sent();
                        response.body = { results: this.filterSourceIfNeeded(lintResults) };
                        return [3 /*break*/, 6];
                    case 5:
                        response.error = "Unknown command: ".concat(request.command);
                        _a.label = 6;
                    case 6: return [3 /*break*/, 8];
                    case 7:
                        e_1 = _a.sent();
                        response.isNoConfigFile = "no-config-found" === e_1.messageTemplate
                            || (e_1.message && (0, eslint_common_1.containsString)(e_1.message.toString(), "No ESLint configuration found"));
                        response.error = e_1.toString() + "\n\n" + e_1.stack;
                        return [3 /*break*/, 8];
                    case 8:
                        writer.write(JSON.stringify(response));
                        return [2 /*return*/];
                }
            });
        });
    };
    Standard17Plugin.prototype.filterSourceIfNeeded = function (results) {
        if (!this.includeSourceText) {
            results.forEach(function (value) {
                delete value.source;
                value.messages.forEach(function (msg) { return delete msg.source; });
            });
        }
        return results;
    };
    Standard17Plugin.prototype.getErrors = function (getErrorsArguments) {
        return __awaiter(this, void 0, void 0, function () {
            return __generator(this, function (_a) {
                return [2 /*return*/, this.invokeESLint(getErrorsArguments)];
            });
        });
    };
    Standard17Plugin.prototype.fixErrors = function (fixErrorsArguments) {
        return __awaiter(this, void 0, void 0, function () {
            return __generator(this, function (_a) {
                return [2 /*return*/, this.invokeESLint(fixErrorsArguments, { fix: true })];
            });
        });
    };
    Standard17Plugin.prototype.invokeESLint = function (requestArguments, additionalOptions) {
        if (additionalOptions === void 0) { additionalOptions = {}; }
        return __awaiter(this, void 0, void 0, function () {
            var options, path, standardEngine;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0:
                        options = additionalOptions;
                        options.filename = requestArguments.fileName;
                        path = this.standardPackagePath + "/index.js";
                        if (path.charAt(1) == ":") {
                            // Windows absolute path
                            path = "file:///" + path;
                        }
                        return [4 /*yield*/, import(path)];
                    case 1:
                        standardEngine = (_a.sent())["default"];
                        return [2 /*return*/, standardEngine.lintText(requestArguments.content, options)];
                }
            });
        });
    };
    return Standard17Plugin;
}());
exports.Standard17Plugin = Standard17Plugin;
//# sourceMappingURL=standard17-plugin.js.map