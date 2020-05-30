"use strict";
exports.__esModule = true;
var PrettierPlugin = /** @class */ (function () {
    function PrettierPlugin() {
    }
    PrettierPlugin.prototype.onMessage = function (p, writer) {
        var r = JSON.parse(p);
        var response;
        try {
            if (r.command == "reformat") {
                response = this.handleReformatCommand(r.arguments);
            }
            else {
                response = { error: "Unknown command: " + r.command };
            }
        }
        catch (e) {
            var msg = e instanceof String ? e : (e.stack && e.stack.length > 0 ? e.stack : e.message || e);
            response = { error: "" + msg };
        }
        response.request_seq = r.seq;
        writer.write(JSON.stringify(response));
    };
    PrettierPlugin.prototype.handleReformatCommand = function (args) {
        var prettierApi = this.requirePrettierApi(args.prettierPath, args.packageJsonPath);
        var options = { ignorePath: args.ignoreFilePath, withNodeModules: true };
        if (prettierApi.getFileInfo) {
            var fileInfo = prettierApi.getFileInfo.sync(args.path, options);
            if (fileInfo.ignored) {
                return { ignored: true };
            }
            if (fileInfo.inferredParser == null) {
                return { unsupported: true };
            }
        }
        return performFormat(prettierApi, args);
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
function performFormat(api, args) {
    if (args.flushConfigCache) {
        api.clearConfigCache();
    }
    var config = api.resolveConfig.sync(args.path, { useCache: true, editorconfig: true });
    if (config == null) {
        config = { filepath: args.path };
    }
    if (config.filepath == null) {
        config.filepath = args.path;
    }
    config.rangeStart = args.start;
    config.rangeEnd = args.end;
    return { formatted: api.format(args.content, config) };
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
