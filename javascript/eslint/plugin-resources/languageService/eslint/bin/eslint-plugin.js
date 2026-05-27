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
exports.ESLintPlugin = void 0;
var eslint_api_1 = require("./eslint-api");
var eslint_common_1 = require("./eslint-common");
var ESLintPlugin = /** @class */ (function () {
    function ESLintPlugin(state) {
        this.includeSourceText = state.includeSourceText;
        this.additionalRulesDirectory = state.additionalRootDirectory;
        var eslintPackagePath;
        if (state.standardPackagePath != null) {
            var standardPackagePath = state.standardPackagePath;
            this.standardLinter = (0, eslint_common_1.requireInContext)(standardPackagePath, state.packageJsonPath);
            // Standard doesn't provide API to check if file is ignored (https://github.com/standard/standard/issues/1448).
            // The only way is to use ESLint for that.
            eslintPackagePath = findESLintPackagePath(standardPackagePath, state.packageJsonPath);
        }
        else {
            eslintPackagePath = state.eslintPackagePath;
        }
        eslintPackagePath = (0, eslint_common_1.normalizePath)(eslintPackagePath);
        this.options = (0, eslint_common_1.requireInContext)(eslintPackagePath + "lib/options", state.packageJsonPath);
        this.cliEngineCtor = (0, eslint_common_1.requireInContext)(eslintPackagePath + "lib/api", state.packageJsonPath).CLIEngine;
    }
    ESLintPlugin.prototype.onMessage = function (p, writer) {
        var request = JSON.parse(p);
        var response = new eslint_api_1.ESLintResponse(request.seq, request.command);
        try {
            if (request.command === eslint_api_1.GetErrors) {
                response.body = this.filterSourceIfNeeded(this.getErrors(request.arguments));
            }
            else if (request.command === eslint_api_1.FixErrors) {
                response.body = this.filterSourceIfNeeded(this.fixErrors(request.arguments));
            }
            else {
                response.error = "Unknown command: ".concat(request.command);
            }
        }
        catch (e) {
            response.isNoConfigFile = "no-config-found" === e.messageTemplate
                || (e.message && (0, eslint_common_1.containsString)(e.message.toString(), "No ESLint configuration found"));
            response.error = e.toString() + "\n\n" + e.stack;
        }
        writer.write(JSON.stringify(response));
    };
    ESLintPlugin.prototype.filterSourceIfNeeded = function (body) {
        if (!this.includeSourceText) {
            body.results.forEach(function (value) {
                delete value.source;
                value.messages.forEach(function (msg) { return delete msg.source; });
            });
        }
        return body;
    };
    ESLintPlugin.prototype.getErrors = function (getErrorsArguments) {
        return this.invokeESLint(getErrorsArguments);
    };
    ESLintPlugin.prototype.fixErrors = function (fixErrorsArguments) {
        return this.invokeESLint(fixErrorsArguments, { fix: true });
    };
    ESLintPlugin.prototype.invokeESLint = function (requestArguments, additionalOptions) {
        if (additionalOptions === void 0) { additionalOptions = {}; }
        var parsedCommandLineOptions = translateOptions(this.options.parse(requestArguments.extraOptions || ""));
        var options = __assign(__assign({}, parsedCommandLineOptions), additionalOptions);
        options.ignorePath = requestArguments.ignoreFilePath;
        if (requestArguments.configPath != null) {
            options.configFile = requestArguments.configPath;
        }
        if (this.additionalRulesDirectory != null && this.additionalRulesDirectory.length > 0) {
            if (options.rulePaths == null) {
                options.rulePaths = [this.additionalRulesDirectory];
            }
            else {
                options.rulePaths.push(this.additionalRulesDirectory);
            }
        }
        var cliEngine = new this.cliEngineCtor(options);
        if (cliEngine.isPathIgnored(requestArguments.fileName)) {
            return createEmptyResult();
        }
        if (this.standardLinter != null) {
            var standardOptions = { filename: requestArguments.fileName };
            if (additionalOptions.fix) {
                standardOptions.fix = true;
            }
            return this.standardLinter.lintTextSync(requestArguments.content, standardOptions);
        }
        var config = cliEngine.getConfigForFile(requestArguments.fileName);
        if (!isFileKindAcceptedByConfig(config, requestArguments.fileKind)) {
            return createEmptyResult();
        }
        return cliEngine.executeOnText(requestArguments.content, requestArguments.fileName);
    };
    return ESLintPlugin;
}());
exports.ESLintPlugin = ESLintPlugin;
function isFileKindAcceptedByConfig(config, fileKind) {
    var plugins = config.plugins;
    function hasPlugin(toCheck) {
        return Array.isArray(plugins) && plugins
            .filter(function (value) { return value == toCheck || value == "eslint-plugin-" + toCheck; }).length > 0;
    }
    function hasParser(parser) {
        return (config.parser != undefined && config.parser != null && (0, eslint_common_1.containsString)((0, eslint_common_1.normalizePath)(config.parser), parser))
            || (config.parserOptions != undefined && config.parserOptions != null
                && (0, eslint_common_1.containsString)((0, eslint_common_1.normalizePath)(config.parserOptions["parser"]), parser));
    }
    if (fileKind === eslint_api_1.FileKind.ts) {
        return (
        // typescript plugin was later renamed to @typescript-eslint
        hasPlugin("typescript")
            || hasPlugin("@typescript-eslint")
            || hasParser("babel-eslint")
            || hasParser("@babel/eslint-parser")
            || hasParser("typescript-eslint-parser")
            || hasParser("@typescript-eslint/parser"));
    }
    if (fileKind === eslint_api_1.FileKind.html) {
        return hasPlugin("html");
    }
    if (fileKind === eslint_api_1.FileKind.vue) {
        return (
        //eslint-plugin-html used to process .vue files prior to v5
        hasPlugin("html") ||
            //eslint-plugin-vue in plugins used to be enough to process .vue files prior to v3
            hasPlugin("vue") ||
            hasParser("vue-eslint-parser"));
    }
    return true;
}
function findESLintPackagePath(standardPackagePath, contextPath) {
    var resolvedStandardPackagePath = (0, eslint_common_1.requireResolveInContext)(standardPackagePath, contextPath);
    var requirePath = require.resolve("eslint", { paths: [resolvedStandardPackagePath] });
    requirePath = (0, eslint_common_1.toUnixPathSeparators)(requirePath);
    var eslintPackageStr = "/eslint/";
    var ind = requirePath.lastIndexOf(eslintPackageStr);
    if (ind < 0) {
        throw Error("Cannot find eslint package for " + requirePath);
    }
    return requirePath.substring(0, ind + eslintPackageStr.length);
}
function createEmptyResult() {
    return {
        results: [],
        warningCount: 0,
        fixableWarningCount: 0,
        fixableErrorCount: 0,
        errorCount: 0,
        usedDeprecatedRules: []
    };
}
// taken from private part of eslint(lib/cli.js), we need it here
/**
 * Translates the CLI options into the options expected by the CLIEngine.
 * @param {Object} cliOptions The CLI options to translate.
 * @returns {CLIEngine.Options} The options object for the CLIEngine.
 * @private
 */
function translateOptions(cliOptions) {
    return {
        envs: cliOptions.env,
        extensions: cliOptions.ext,
        rules: cliOptions.rule,
        plugins: cliOptions.plugin,
        globals: cliOptions.global,
        ignore: cliOptions.ignore,
        ignorePath: cliOptions.ignorePath,
        ignorePattern: cliOptions.ignorePattern,
        configFile: cliOptions.config,
        rulePaths: cliOptions.rulesdir,
        useEslintrc: cliOptions.eslintrc,
        parser: cliOptions.parser,
        parserOptions: cliOptions.parserOptions,
        cache: cliOptions.cache,
        cacheFile: cliOptions.cacheFile,
        cacheLocation: cliOptions.cacheLocation,
        allowInlineConfig: cliOptions.inlineConfig,
        reportUnusedDisableDirectives: cliOptions.reportUnusedDisableDirectives,
        resolvePluginsRelativeTo: cliOptions.resolvePluginsRelativeTo
    };
}
//# sourceMappingURL=eslint-plugin.js.map