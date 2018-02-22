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
            else if (r.command == "getSupportedFiles") {
                response = this.getSupportedFiles(r.arguments.prettierPath);
            }
            else {
                response = { error: "Unknown command: " + r.command };
            }
        }
        catch (e) {
            response = { error: e.message + " " + e.stack };
        }
        response.request_seq = r.seq;
        writer.write(JSON.stringify(response));
    };
    PrettierPlugin.prototype.handleReformatCommand = function (args) {
        var prettierApi = this.requirePrettierApi(args.prettierPath);
        try {
            return {
                formatted: performFormat(prettierApi, args.content, args.path, args.start, args.end)
            };
        }
        catch (e) {
            return { error: args.path + ": " + (e.stack && e.stack.length > 0 ? e.stack : e.message) };
        }
    };
    PrettierPlugin.prototype.getSupportedFiles = function (path) {
        var prettierApi = this.requirePrettierApi(path);
        var info = prettierApi.getSupportInfo();
        var extensions = flatten(info.languages.map(function (l) { return l.extensions; })).map(function (e) { return withoutPrefix(e, "."); });
        var fileNames = flatten(info.languages.map(function (l) { return l.filenames != null ? l.filenames : []; }));
        return {
            fileNames: fileNames,
            extensions: extensions
        };
    };
    PrettierPlugin.prototype.requirePrettierApi = function (path) {
        if (this._prettierApi != null && this._prettierApi.path == path) {
            return this._prettierApi;
        }
        var prettier = require(path);
        prettier.path = path;
        return prettier;
    };
    return PrettierPlugin;
}());
exports.PrettierPlugin = PrettierPlugin;
function withoutPrefix(e, prefix) {
    if (e == null || e.length == 0) {
        return e;
    }
    var index = e.indexOf(prefix);
    return index == 0 ? e.substr(prefix.length) : e;
}
function flatten(arr) {
    return arr.reduce(function (previousValue, currentValue) { return previousValue.concat(currentValue); });
}
function performFormat(api, text, path, rangeStart, rangeEnd) {
    var config = api.resolveConfig.sync(path, { useCache: false, editorconfig: true });
    if (config == null) {
        config = { filepath: path };
    }
    if (config.filepath == null) {
        config.filepath = path;
    }
    config.rangeStart = rangeStart;
    config.rangeEnd = rangeEnd;
    return api.format(text, config);
}
