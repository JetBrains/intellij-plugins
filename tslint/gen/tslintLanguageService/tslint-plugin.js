// import * as tslint from 'tslint';
"use strict";
var TsLintCommands;
(function (TsLintCommands) {
    TsLintCommands.GetErrors = "GetErrors";
})(TsLintCommands || (TsLintCommands = {}));
var TSLintPlugin = (function () {
    function TSLintPlugin(state) {
        this.linterOptions = resolveTsLint(state);
        this.additionalRulesDirectory = state.additionalRootDirectory;
    }
    TSLintPlugin.prototype.onMessage = function (p, writer) {
        var request = JSON.parse(p);
        var result = this.process(request);
        if (result) {
            var output = result.output;
            var version = this.linterOptions.version;
            var command = request.command;
            var seq = request.seq;
            var resultJson = ("{\"body\":" + output + ",\"version\":\"" + version + "\",") +
                ("\"command\":\"" + command + "\",\"request_seq\":" + seq + "}");
            writer.write(resultJson);
        }
    };
    TSLintPlugin.prototype.process = function (parsedObject) {
        switch (parsedObject.command) {
            case TsLintCommands.GetErrors: {
                var result = this.getErrors(parsedObject.arguments);
                return result;
            }
        }
        return null;
    };
    TSLintPlugin.prototype.getErrors = function (toProcess) {
        var options = {
            formatter: "json",
            fix: false,
            rulesDirectory: this.additionalRulesDirectory,
            formattersDirectory: undefined
        };
        var linterOptions = this.linterOptions;
        var linter = this.linterOptions.linter;
        var result = {};
        var configuration = this.getConfiguration(toProcess, linter);
        if (linterOptions.versionKind == 1 /* VERSION_4_AND_HIGHER */) {
            var tslint = new linter(options);
            tslint.lint(toProcess.fileName, toProcess.content, configuration);
            result = tslint.getResult();
        }
        else {
            options.configuration = configuration;
            var tslint = new linter(toProcess.fileName, toProcess.content, options);
            result = tslint.lint();
        }
        return result;
    };
    TSLintPlugin.prototype.getConfiguration = function (toProcess, linter) {
        var linterConfiguration = this.linterOptions.linterConfiguration;
        ;
        var versionKind = this.linterOptions.versionKind;
        if (versionKind == 1 /* VERSION_4_AND_HIGHER */) {
            var configurationResult = linterConfiguration.findConfiguration(toProcess.configPath, toProcess.fileName);
            if (!configurationResult) {
                throw new Error("Cannot find configuration " + toProcess.configPath);
            }
            if (configurationResult && configurationResult.error) {
                throw configurationResult.error;
            }
            return configurationResult.results;
        }
        else {
            return linter.findConfiguration(toProcess.configPath, toProcess.fileName);
        }
    };
    return TSLintPlugin;
}());
exports.TSLintPlugin = TSLintPlugin;
function resolveTsLint(options) {
    var tslintPackagePath = options.tslintPackagePath;
    var value = require(tslintPackagePath);
    if (typeof value == "function") {
        value = value();
    }
    var versionText = getVersionText(value);
    var versionKind = getVersion(versionText);
    var linter = versionKind == 1 /* VERSION_4_AND_HIGHER */ ? value.Linter : value;
    var linterConfiguration = value.Configuration;
    return { linter: linter, linterConfiguration: linterConfiguration, versionKind: versionKind, version: versionText };
}
function getVersionText(tslint) {
    return tslint.VERSION || (tslint.Linter && tslint.Linter.VERSION);
}
;
function getVersion(version) {
    if (version == null) {
        return 0 /* VERSION_3_AND_BEFORE */;
    }
    var firstDot = version.indexOf(".");
    var majorVersion = firstDot == -1 ? version : version.substr(0, firstDot + 1);
    return majorVersion && (Number(majorVersion) > 3) ?
        1 /* VERSION_4_AND_HIGHER */ :
        0 /* VERSION_3_AND_BEFORE */;
}
