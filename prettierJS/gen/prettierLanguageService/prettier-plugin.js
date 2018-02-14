"use strict";
exports.__esModule = true;
var PrettierPlugin = /** @class */ (function () {
    function PrettierPlugin() {
    }
    PrettierPlugin.prototype.onMessage = function (p, writer) {
        var r = JSON.parse(p);
        var response = { request_seq: r.seq };
        if (r.command != "reformat") {
            response.error = "Unknown command: " + r.command;
            writer.write(JSON.stringify(response));
            return;
        }
        try {
            var args = r.arguments;
            var prettierApi = this.requirePrettierApi(args.prettierPath);
            try {
                response.formatted = performFormat(prettierApi, args.content, args.path, args.start, args.end);
            }
            catch (e) {
                response.error = args.path + ": " + (e.stack && e.stack.left > 0 ? e.stack : e.message);
            }
        }
        catch (e) {
            response.error = e.message + " " + e.stack;
        }
        writer.write(JSON.stringify(response));
    };
    PrettierPlugin.prototype.requirePrettierApi = function (path) {
        if (this._prettierApi != null && this._prettierApi.path == path) {
            return this._prettierApi;
        }
        var prettier = require(path);
        return this._prettierApi = {
            path: path, formatFn: prettier.format,
            resolveConfigFn: prettier.resolveConfig.sync,
            clearCache: prettier.clearConfigCache
        };
    };
    return PrettierPlugin;
}());
exports.PrettierPlugin = PrettierPlugin;
function performFormat(api, text, path, rangeStart, rangeEnd) {
    var config = api.resolveConfigFn(path, { useCache: false, editorconfig: true });
    if (config == null) {
        config = { filepath: path };
    }
    if (config.filepath == null) {
        config.filepath = path;
    }
    config.rangeStart = rangeStart;
    config.rangeEnd = rangeEnd;
    return api.formatFn(text, config);
}
