import type {CLIEngine, Linter} from './typings/eslint-7'
import {EslintPluginState, ESLintRequest, ESLintResponse, FileKind, RequestArguments, FixErrors, GetErrors} from "./eslint-api"
import {containsString, normalizePath, requireInContext, requireResolveInContext, toUnixPathSeparators} from "./eslint-common"

export class ESLintPlugin implements LanguagePlugin {
    private readonly includeSourceText: boolean | null;
    private readonly additionalRulesDirectory?: string;
    private readonly standardLinter: any;
    private readonly options: any;
    private readonly cliEngineCtor: any;

    constructor(state: EslintPluginState) {
        this.includeSourceText = state.includeSourceText;
        this.additionalRulesDirectory = state.additionalRootDirectory;
        let eslintPackagePath;
        if (state.standardPackagePath != null) {
            const standardPackagePath = state.standardPackagePath;
            this.standardLinter = requireInContext(standardPackagePath, state.packageJsonPath);
            // Standard doesn't provide API to check if file is ignored (https://github.com/standard/standard/issues/1448).
            // The only way is to use ESLint for that.
            eslintPackagePath = findESLintPackagePath(standardPackagePath, state.packageJsonPath);
        }
        else {
            eslintPackagePath = state.eslintPackagePath;
        }
        eslintPackagePath = normalizePath(eslintPackagePath);
        this.options = requireInContext(eslintPackagePath + "lib/options", state.packageJsonPath);
        this.cliEngineCtor = requireInContext(eslintPackagePath + "lib/api", state.packageJsonPath).CLIEngine;
    }

    onMessage(p: string, writer: MessageWriter): void {
        const request: ESLintRequest = JSON.parse(p);
        let response: ESLintResponse = new ESLintResponse(request.seq, request.command);
        try {
            if (request.command === GetErrors) {
                response.body = this.filterSourceIfNeeded(this.getErrors(request.arguments));
            } else if (request.command === FixErrors) {
                response.body = this.filterSourceIfNeeded(this.fixErrors(request.arguments))
            } else {
                response.error = `Unknown command: ${request.command}`
            }
        } catch (e: unknown) {
            response.isNoConfigFile = "no-config-found" === e.messageTemplate
              || (e.message && containsString(e.message.toString(), "No ESLint configuration found"));
            response.error = e.toString() + "\n\n" + e.stack;
        }
        writer.write(JSON.stringify(response));
    }

    private filterSourceIfNeeded(body: CLIEngine.LintReport): CLIEngine.LintReport {
        if (!this.includeSourceText) {
            body.results.forEach(value => {
                delete value.source
                value.messages.forEach(msg => delete msg.source)
            })
        }
        return body
    }

    private getErrors(getErrorsArguments: RequestArguments): CLIEngine.LintReport {
        return this.invokeESLint(getErrorsArguments)
    }

    private fixErrors(fixErrorsArguments: RequestArguments): CLIEngine.LintReport {
        return this.invokeESLint(fixErrorsArguments, {fix: true})
    }

    private invokeESLint(requestArguments: RequestArguments, additionalOptions: CLIEngine.Options = {}): CLIEngine.LintReport {
        const parsedCommandLineOptions = translateOptions(this.options.parse(requestArguments.extraOptions || ""));
        const options: CLIEngine.Options = {...parsedCommandLineOptions, ...additionalOptions};
        options.ignorePath = requestArguments.ignoreFilePath;
        if (requestArguments.configPath != null) {
            options.configFile = requestArguments.configPath;
        }
        if (this.additionalRulesDirectory != null && this.additionalRulesDirectory.length > 0) {
            if (options.rulePaths == null) {
                options.rulePaths = [this.additionalRulesDirectory]
            } else {
                options.rulePaths.push(this.additionalRulesDirectory);
            }
        }
        const cliEngine: CLIEngine = new this.cliEngineCtor(options);
        if (cliEngine.isPathIgnored(requestArguments.fileName)) {
            return createEmptyResult();
        }
        if (this.standardLinter != null) {
            const standardOptions : any = {filename: requestArguments.fileName};
            if (additionalOptions.fix) {
                standardOptions.fix = true;
            }
            return this.standardLinter.lintTextSync(requestArguments.content, standardOptions);
        }
        const config = cliEngine.getConfigForFile(requestArguments.fileName);
        if (!isFileKindAcceptedByConfig(config, requestArguments.fileKind)) {
            return createEmptyResult();
        }
        return cliEngine.executeOnText(requestArguments.content, requestArguments.fileName);
    }
}

function isFileKindAcceptedByConfig(config: Linter.Config, fileKind: FileKind): boolean {
    const plugins: string[] | null | undefined = (<any>config).plugins;

    function hasPlugin(toCheck: string): boolean {
        return Array.isArray(plugins) && plugins
            .filter(value => value == toCheck || value == "eslint-plugin-" + toCheck).length > 0;
    }

    function hasParser(parser: string): boolean {
        return (config.parser != undefined && config.parser != null && containsString(normalizePath(config.parser), parser))
          || (config.parserOptions != undefined && config.parserOptions != null
            && containsString(normalizePath(config.parserOptions["parser"]), parser))
    }

    if (fileKind === FileKind.ts) {
        return (
            // typescript plugin was later renamed to @typescript-eslint
            hasPlugin("typescript")
            || hasPlugin("@typescript-eslint")
            || hasParser("babel-eslint")
            || hasParser("@babel/eslint-parser")
            || hasParser("typescript-eslint-parser")
            || hasParser("@typescript-eslint/parser"))
    }
    if (fileKind === FileKind.html) {
        return hasPlugin("html")
    }
    if (fileKind === FileKind.vue) {
        return (
            //eslint-plugin-html used to process .vue files prior to v5
            hasPlugin("html") ||
            //eslint-plugin-vue in plugins used to be enough to process .vue files prior to v3
            hasPlugin("vue") ||
            hasParser("vue-eslint-parser")
        )
    }
    return true;
}

function findESLintPackagePath(standardPackagePath: string, contextPath?: string): string {
    const resolvedStandardPackagePath = requireResolveInContext(standardPackagePath, contextPath);
    let requirePath = require.resolve("eslint", {paths: [resolvedStandardPackagePath]});
    requirePath = toUnixPathSeparators(requirePath);
    const eslintPackageStr = "/eslint/";
    const ind = requirePath.lastIndexOf(eslintPackageStr);
    if (ind < 0) {
        throw Error("Cannot find eslint package for " + requirePath);
    }
    return requirePath.substring(0, ind + eslintPackageStr.length);
}

function createEmptyResult(): CLIEngine.LintReport {
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
function translateOptions(cliOptions: any): CLIEngine.Options {
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
