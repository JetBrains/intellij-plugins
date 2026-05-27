import {ESLint} from 'eslint'
import {EslintPluginState, ESLintRequest, ESLintResponse, FileKind, FixErrors, GetErrors, RequestArguments} from "./eslint-api"
import {containsString, normalizePath, requireInContext, requireResolveInContext} from "./eslint-common"

export class ESLint8Plugin implements LanguagePlugin {
  private readonly includeSourceText: boolean | null;
  private readonly additionalRulesDirectory?: string;
  private readonly LegacyESLint: any;
  private readonly FlatESLint: any;
  private readonly libOptions: any;

  constructor(state: EslintPluginState) {
    this.includeSourceText = state.includeSourceText;
    this.additionalRulesDirectory = state.additionalRootDirectory;

    this.libOptions = null;
    this.FlatESLint = null;
    this.LegacyESLint = null;

    let isESLint8 = state.linterPackageVersion.substring(0, 2) == "8."
    let eslintPackagePath = normalizePath(state.eslintPackagePath)!;
    let defaultESLint = requireInContext(eslintPackagePath, state.packageJsonPath).ESLint

    if (isESLint8) {
      this.LegacyESLint = defaultESLint;
    }
    else {
      this.FlatESLint = defaultESLint;
    }

    try {
      const apiJsPath = requireResolveInContext(eslintPackagePath, state.packageJsonPath);

      try {
        this.libOptions = requireInContext("../lib/options" /* path relative to eslint/lib/api.js */, apiJsPath);
      }
      catch (e) {
        this.libOptions = null;
      }

      if (isESLint8) {
        try {
          this.FlatESLint = requireInContext("../lib/unsupported-api", apiJsPath).FlatESLint;
        }
        catch (e) {
          this.FlatESLint = null;
        }
      }
      else {
        try {
          this.LegacyESLint = requireInContext("../lib/unsupported-api", apiJsPath).LegacyESLint;
        }
        catch (e) {
          this.LegacyESLint = null;
        }
      }
    }
    catch (e) {
      // failed to resolve apiJsPath
    }
  }

  async onMessage(p: string, writer: MessageWriter) {
    const request: ESLintRequest = JSON.parse(p);
    let response: ESLintResponse = new ESLintResponse(request.seq, request.command);
    try {
      if (request.command === GetErrors) {
        let lintResults: ESLint.LintResult[] = await this.getErrors(request.arguments);
        response.body = {results: this.filterSourceIfNeeded(lintResults)};
      }
      else if (request.command === FixErrors) {
        let lintResults: ESLint.LintResult[] = await this.fixErrors(request.arguments);
        response.body = {results: this.filterSourceIfNeeded(lintResults)};
      }
      else {
        response.error = `Unknown command: ${request.command}`
      }
    }
    catch (e) {
      response.isNoConfigFile = "no-config-found" === e.messageTemplate
        || (e.message && containsString(e.message.toString(), "No ESLint configuration found"));
      response.error = e.toString() + "\n\n" + e.stack;
    }
    writer.write(JSON.stringify(response));
  }

  private filterSourceIfNeeded(results: ESLint.LintResult[]): ESLint.LintResult[] {
    if (!this.includeSourceText) {
      results.forEach(value => {
        delete value.source
        value.messages.forEach(msg => delete msg.source)
      })
    }
    return results
  }

  private async getErrors(getErrorsArguments: RequestArguments): Promise<ESLint.LintResult[]> {
    return this.invokeESLint(getErrorsArguments)
  }

  private async fixErrors(fixErrorsArguments: RequestArguments): Promise<ESLint.LintResult[]> {
    return this.invokeESLint(fixErrorsArguments, {fix: true})
  }

  private async invokeESLint(requestArguments: RequestArguments, additionalOptions: ESLint.Options = {}): Promise<ESLint.LintResult[]> {
    const usingFlatConfig = requestArguments.flatConfig && this.FlatESLint instanceof Function;

    const CLIOptions =
      this.libOptions instanceof Function
        ? this.libOptions(usingFlatConfig) // eslint 8.23+
        : this.libOptions;

    const parsedCommandLineOptions =
      CLIOptions != null && CLIOptions.parse instanceof Function
        ? translateOptions(CLIOptions.parse(requestArguments.extraOptions || ""), usingFlatConfig ? "flat" : "eslintrc")
        : {};

    const options: ESLint.Options & {
      ignorePath?: ESLint.LegacyOptions["ignorePath"];
      rulePaths?: ESLint.LegacyOptions["rulePaths"];
    } = { ...parsedCommandLineOptions, ...additionalOptions };

    if (!usingFlatConfig) {
      options.ignorePath = requestArguments.ignoreFilePath;
    }

    if (requestArguments.configPath != null) {
      options.overrideConfigFile = requestArguments.configPath;
    }

    if (this.additionalRulesDirectory != null && this.additionalRulesDirectory.length > 0) {
      if (options.rulePaths == null) {
        options.rulePaths = [this.additionalRulesDirectory]
      }
      else {
        options.rulePaths.push(this.additionalRulesDirectory);
      }
    }

    const eslint = usingFlatConfig ? new this.FlatESLint(options) : new this.LegacyESLint(options);

    if (requestArguments.fileKind === FileKind.html) {
      const config: any = await eslint.calculateConfigForFile(requestArguments.fileName);
      if (config == null) {
        return [];
      }

      const plugins: Record<string, unknown> | string[] | null | undefined = config.plugins;

      if (!plugins) {
        return [];
      }

      const hasHtmlPlugin = Array.isArray(plugins)
        ? plugins.includes("html")
        : typeof plugins === "object" && Object.keys(plugins).some(plugin => plugin.toLowerCase().includes("html"));;

      if (!hasHtmlPlugin) {
        return [];
      }
    }

    if (await eslint.isPathIgnored(requestArguments.fileName)) {
      return [];
    }

    return eslint.lintText(requestArguments.content, {filePath: requestArguments.fileName});
  }
}


// See https://github.com/eslint/eslint/blob/0dd9704c4751e1cd02039f7d6485fee09bbccbf6/lib/cli.js#L69
/**
 * Translates the CLI options into the options expected by the ESLint constructor.
 * @param {ParsedCLIOptions} cliOptions The CLI options to translate.
 * @param {"flat"|"eslintrc"} [configType="eslintrc"] The format of the
 *      config to generate.
 * @returns {Promise<ESLintOptions>} The options object for the ESLint constructor.
 * @private
 */
/*async*/ function translateOptions({
                                      cache,
                                      cacheFile,
                                      cacheLocation,
                                      cacheStrategy,
                                      config,
                                      configLookup,
                                      env,
                                      errorOnUnmatchedPattern,
                                      eslintrc,
                                      ext,
                                      fix,
                                      fixDryRun,
                                      fixType,
                                      global,
                                      ignore,
                                      ignorePath,
                                      ignorePattern,
                                      inlineConfig,
                                      parser,
                                      parserOptions,
                                      flag,
                                      plugin,
                                      quiet,
                                      reportUnusedDisableDirectives,
                                      reportUnusedDisableDirectivesSeverity,
                                      resolvePluginsRelativeTo,
                                      rule,
                                      rulesdir,
                                      warnIgnored
                                }, configType) {

  let overrideConfig, overrideConfigFile;
  /*
  const importer = new ModuleImporter();
  */

  if (configType === "flat") {
    overrideConfigFile = (typeof config === "string") ? config : !configLookup;
    if (overrideConfigFile === false) {
      overrideConfigFile = void 0;
    }

    let globals = {};

    if (global) {
      globals = global.reduce((obj, name) => {
        if (name.endsWith(":true")) {
          obj[name.slice(0, -5)] = "writable";
        } else {
          obj[name] = "readonly";
        }
        return obj;
      }, globals);
    }

    overrideConfig = [{
      languageOptions: {
        globals,
        parserOptions: parserOptions || {}
      },
      rules: rule ? rule : {}
    }];

    if (reportUnusedDisableDirectives || reportUnusedDisableDirectivesSeverity !== void 0) {
      overrideConfig[0].linterOptions = {
        reportUnusedDisableDirectives: reportUnusedDisableDirectives
          ? "error"
          : normalizeSeverityToString(reportUnusedDisableDirectivesSeverity)
      };
    }

    /*
    if (parser) {
      overrideConfig[0].languageOptions.parser = await importer.import(parser);
    }

    if (plugin) {
      const plugins = {};

      for (const pluginName of plugin) {

        const shortName = naming.getShorthandName(pluginName, "eslint-plugin");
        const longName = naming.normalizePackageName(pluginName, "eslint-plugin");

        plugins[shortName] = await importer.import(longName);
      }

      overrideConfig[0].plugins = plugins;
    }
    */

  } else {
    overrideConfigFile = config;

    overrideConfig = {
      env: env && env.reduce((obj, name) => {
        obj[name] = true;
        return obj;
      }, {}),
      globals: global && global.reduce((obj, name) => {
        if (name.endsWith(":true")) {
          obj[name.slice(0, -5)] = "writable";
        } else {
          obj[name] = "readonly";
        }
        return obj;
      }, {}),
      ignorePatterns: ignorePattern,
      parser,
      parserOptions,
      plugins: plugin,
      rules: rule
    };
  }

  const options = {
    allowInlineConfig: inlineConfig,
    cache,
    cacheLocation: cacheLocation || cacheFile,
    cacheStrategy,
    errorOnUnmatchedPattern,
    /*
    fix: (fix || fixDryRun) && (quiet ? quietFixPredicate : true),
    */
    fixTypes: fixType,
    ignore,
    overrideConfig,
    overrideConfigFile
  };

  if (configType === "flat") {
    options.ignorePatterns = ignorePattern;
    if (flag) {
      options.flags = flag;
    }
    // options.warnIgnored = warnIgnored; --- not needed because the IDE doesn't lint ignored files; backward compatibility gets broken if uncommented
  } else {
    options.resolvePluginsRelativeTo = resolvePluginsRelativeTo;
    options.rulePaths = rulesdir;
    options.useEslintrc = eslintrc;
    options.extensions = ext;
    options.ignorePath = ignorePath;
    if (reportUnusedDisableDirectives || reportUnusedDisableDirectivesSeverity !== void 0) {
      options.reportUnusedDisableDirectives = reportUnusedDisableDirectives
        ? "error"
        : normalizeSeverityToString(reportUnusedDisableDirectivesSeverity);
    }
  }

  return options;
}

// See https://github.com/eslint/eslint/blob/0dd9704c4751e1cd02039f7d6485fee09bbccbf6/lib/shared/severity.js#L14
/**
 * Convert severity value of different types to a string.
 * @param {string|number} severity severity value
 * @throws error if severity is invalid
 * @returns {string} severity string
 */
function normalizeSeverityToString(severity) {
  if ([2, "2", "error"].includes(severity)) {
    return "error";
  }
  if ([1, "1", "warn"].includes(severity)) {
    return "warn";
  }
  if ([0, "0", "off"].includes(severity)) {
    return "off";
  }
  throw new Error(`Invalid severity value: ${severity}`);
}
