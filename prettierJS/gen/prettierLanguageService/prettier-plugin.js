// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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
exports.PrettierPlugin = void 0;
var PrettierPlugin = /** @class */ (function () {
    function PrettierPlugin() {
    }
    PrettierPlugin.prototype.onMessage = function (p, writer) {
        return __awaiter(this, void 0, void 0, function () {
            var r, response, e_1, msg;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0:
                        r = JSON.parse(p);
                        _a.label = 1;
                    case 1:
                        _a.trys.push([1, 5, , 6]);
                        if (!(r.command == "reformat")) return [3 /*break*/, 3];
                        return [4 /*yield*/, this.handleReformatCommand(r.arguments)];
                    case 2:
                        response = _a.sent();
                        return [3 /*break*/, 4];
                    case 3:
                        response = { error: "Unknown command: " + r.command };
                        _a.label = 4;
                    case 4: return [3 /*break*/, 6];
                    case 5:
                        e_1 = _a.sent();
                        msg = e_1 instanceof String ? e_1 : (e_1.stack && e_1.stack.length > 0 ? e_1.stack : e_1.message || e_1);
                        response = { error: "".concat(msg) };
                        return [3 /*break*/, 6];
                    case 6:
                        response.request_seq = r.seq;
                        writer.write(JSON.stringify(response));
                        return [2 /*return*/];
                }
            });
        });
    };
    PrettierPlugin.prototype.handleReformatCommand = function (args) {
        return __awaiter(this, void 0, void 0, function () {
            var prettierApi, config, options, fileInfo;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0:
                        prettierApi = this.requirePrettierApi(args.prettierPath, args.packageJsonPath);
                        return [4 /*yield*/, this.resolveConfig(prettierApi, args)];
                    case 1:
                        config = _a.sent();
                        options = { ignorePath: args.ignoreFilePath, withNodeModules: true, plugins: config.plugins };
                        if (!prettierApi.getFileInfo) return [3 /*break*/, 3];
                        return [4 /*yield*/, prettierApi.getFileInfo(args.path, options)];
                    case 2:
                        fileInfo = _a.sent();
                        if (fileInfo.ignored) {
                            return [2 /*return*/, { ignored: true }];
                        }
                        if (fileInfo.inferredParser == null) {
                            return [2 /*return*/, { unsupported: true }];
                        }
                        _a.label = 3;
                    case 3: return [2 /*return*/, performFormat(prettierApi, config, args)];
                }
            });
        });
    };
    PrettierPlugin.prototype.resolveConfig = function (prettierApi, args) {
        return __awaiter(this, void 0, void 0, function () {
            var config;
            return __generator(this, function (_a) {
                switch (_a.label) {
                    case 0: return [4 /*yield*/, prettierApi.resolveConfig(args.path, { useCache: true, editorconfig: true })];
                    case 1:
                        config = _a.sent();
                        if (config == null) {
                            config = { filepath: args.path };
                        }
                        if (config.filepath == null) {
                            config.filepath = args.path;
                        }
                        config.rangeStart = args.start;
                        config.rangeEnd = args.end;
                        return [2 /*return*/, config];
                }
            });
        });
    };
    PrettierPlugin.prototype.requirePrettierApi = function (prettierPath, packageJsonPath) {
        if (this._prettierApi != null
            && this._prettierApi.prettierPath == prettierPath
            && this._prettierApi.packageJsonPath == packageJsonPath) {
            return this._prettierApi;
        }
        var prettier = requireInContext(prettierPath, packageJsonPath);
        prettier.prettierPath = prettierPath;
        prettier.packageJsonPath = packageJsonPath;
        this._prettierApi = prettier;
        return prettier;
    };
    return PrettierPlugin;
}());
exports.PrettierPlugin = PrettierPlugin;
function performFormat(api, config, args) {
    return __awaiter(this, void 0, void 0, function () {
        var _a;
        return __generator(this, function (_b) {
            switch (_b.label) {
                case 0:
                    if (args.flushConfigCache) {
                        api.clearConfigCache();
                    }
                    _a = {};
                    return [4 /*yield*/, api.format(args.content, config)];
                case 1: return [2 /*return*/, (_a.formatted = _b.sent(), _a)];
            }
        });
    });
}
function requireInContext(modulePathToRequire, contextPath) {
    var contextRequire = getContextRequire(modulePathToRequire, contextPath);
    return contextRequire(modulePathToRequire);
}
function getContextRequire(modulePathToRequire, contextPath) {
    if (contextPath != null) {
        var m = require('module');
        if (typeof m.createRequire === 'function') {
            // https://nodejs.org/api/modules.html#modules_module_createrequire_filename
            // Also, implemented for Yarn Pnp: https://next.yarnpkg.com/advanced/pnpapi/#requiremodule
            return m.createRequire(contextPath);
        }
    }
    return require;
}
