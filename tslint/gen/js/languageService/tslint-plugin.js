"use strict";
exports.__esModule = true;
var utils_1 = require("../utils");
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
var fs = require("fs");
var TSLintPlugin = /** @class */ (function () {
    function TSLintPlugin(state) {
        this.linterOptions = resolveTsLint(state);
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
        response.version = this.linterOptions.version.versionString;
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
        var options = this.getOptions(false);
        return this.processLinting(toProcess.fileName, toProcess.content, toProcess.configPath, options);
    };
    TSLintPlugin.prototype.fixErrors = function (toProcess) {
        var options = this.getOptions(true);
        var contents = fs.readFileSync(toProcess.fileName, "utf8");
        return this.processLinting(toProcess.fileName, contents, toProcess.configPath, options);
    };
    TSLintPlugin.prototype.getOptions = function (fix) {
        return {
            formatter: "json",
            fix: fix,
            rulesDirectory: this.additionalRulesDirectory,
            formattersDirectory: undefined
        };
    };
    TSLintPlugin.prototype.processLinting = function (fileName, content, configFileName, options) {
        var linterOptions = this.linterOptions;
        var linter = this.linterOptions.linter;
        var result = {};
        var configuration = this.getConfiguration(fileName, configFileName, linter);
        if (linterOptions.version.kind == 1 /* VERSION_4_AND_HIGHER */) {
            var tslint = new linter(options);
            tslint.lint(fileName, content, configuration);
            result = tslint.getResult();
        }
        else {
            options.configuration = configuration;
            var tslint = new linter(fileName, content, options);
            result = tslint.lint();
        }
        return result;
    };
    TSLintPlugin.prototype.getConfiguration = function (fileName, configFileName, linter) {
        var linterConfiguration = this.linterOptions.linterConfiguration;
        var versionKind = this.linterOptions.version.kind;
        if (versionKind == 1 /* VERSION_4_AND_HIGHER */) {
            var configurationResult = linterConfiguration.findConfiguration(configFileName, fileName);
            if (!configurationResult) {
                throw new Error("Cannot find configuration " + configFileName);
            }
            if (configurationResult && configurationResult.error) {
                throw configurationResult.error;
            }
            return configurationResult.results;
        }
        else {
            return linter.findConfiguration(configFileName, fileName);
        }
    };
    return TSLintPlugin;
}());
exports.TSLintPlugin = TSLintPlugin;
function resolveTsLint(options) {
    var tslintPackagePath = options.tslintPackagePath;
    var tslint = require(tslintPackagePath);
    var version = utils_1.getVersion(tslint);
    var linter = version.kind == 1 /* VERSION_4_AND_HIGHER */ ? tslint.Linter : tslint;
    var linterConfiguration = tslint.Configuration;
    return { linter: linter, linterConfiguration: linterConfiguration, version: version };
}
