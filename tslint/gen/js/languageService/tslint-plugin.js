"use strict";
var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
exports.__esModule = true;
var utils_1 = require("../utils");
var fs_1 = require("fs");
var TsLintCommands;
(function (TsLintCommands) {
    TsLintCommands.GetErrors = "GetErrors";
    TsLintCommands.FixErrors = "FixErrors";
})(TsLintCommands || (TsLintCommands = {}));
var Response = /** @class */ (function () {
    function Response() {
    }
    return Response;
}());
var TSLintPlugin = /** @class */ (function () {
    function TSLintPlugin(state) {
        this.linterApi = resolveTsLint(state.tslintPackagePath);
        this.additionalRulesDirectory = state.additionalRootDirectory;
    }
    TSLintPlugin.prototype.process = function (parsedObject) {
        switch (parsedObject.command) {
            case TsLintCommands.GetErrors: {
                return this.getErrors(parsedObject.arguments);
            }
            case TsLintCommands.FixErrors: {
                return this.fixErrors(parsedObject.arguments);
            }
        }
        return null;
    };
    TSLintPlugin.prototype.onMessage = function (p, writer) {
        var request = JSON.parse(p);
        // here we use object -> JSON.stringify, because we need to escape possible error's text symbols
        // and we do not want to duplicate this code
        var response = new Response();
        response.version = this.linterApi.version.raw;
        response.command = request.command;
        response.request_seq = request.seq;
        var result;
        try {
            result = this.process(request);
        }
        catch (e) {
            response.error = e.toString() + "\n\n" + e.stack;
            writer.write(JSON.stringify(response));
            return;
        }
        if (result) {
            response.body = result.output;
        }
        writer.write(JSON.stringify(response));
    };
    TSLintPlugin.prototype.getErrors = function (toProcess) {
        return this.processLinting(toProcess, this.getOptions(false));
    };
    TSLintPlugin.prototype.fixErrors = function (toProcess) {
        //TODO. why here?
        var contents = fs_1.readFileSync(toProcess.filePath, "utf8");
        return this.processLinting(__assign({}, toProcess, { content: contents }), this.getOptions(true));
    };
    TSLintPlugin.prototype.getOptions = function (fix) {
        return {
            formatter: "json",
            fix: fix,
            rulesDirectory: this.additionalRulesDirectory
        };
    };
    TSLintPlugin.prototype.processLinting = function (args, options) {
        var linter = this.linterApi.linter;
        var major = this.linterApi.version.major || 0;
        var configuration = this.getConfiguration(args.filePath, args.configPath);
        if (major >= 4) {
            var tslint_1 = new linter(options);
            tslint_1.lint(args.filePath, args.content, configuration);
            return tslint_1.getResult();
        }
        options.configuration = configuration;
        var tslint = new linter(args.filePath, args.content, options);
        return tslint.lint();
    };
    TSLintPlugin.prototype.getConfiguration = function (fileName, configFileName) {
        var majorVersion = this.linterApi.version.major;
        var configurationResult = this.linterApi.linter.findConfiguration(configFileName, fileName);
        if (majorVersion && majorVersion >= 4) {
            if (!configurationResult || !configurationResult.results) {
                throw new Error("Cannot find configuration " + configFileName);
            }
            return configurationResult.results;
        }
        return configurationResult;
    };
    TSLintPlugin.prototype.hasJSRules = function (config) {
        return this.linterApi.version.major && this.linterApi.version.major >= 5
            ? config.jsRules.size > 0
            : Object.keys(config.jsRules).length > 0;
    };
    return TSLintPlugin;
}());
exports.TSLintPlugin = TSLintPlugin;
function resolveTsLint(packagePath) {
    var tslint = require(packagePath);
    var version = utils_1.getVersion(tslint);
    var linter = version.major && version.major >= 4 ? tslint.Linter : tslint;
    return { linter: linter, version: version };
}
