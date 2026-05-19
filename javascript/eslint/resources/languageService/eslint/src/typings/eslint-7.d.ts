export namespace Linter {
  type Severity = 0 | 1 | 2;

  type RuleLevel = Severity | "off" | "warn" | "error";
  type RuleLevelAndOptions<Options extends any[] = any[]> = Prepend<Partial<Options>, RuleLevel>;

  type RuleEntry<Options extends any[] = any[]> = RuleLevel | RuleLevelAndOptions<Options>;

  interface RulesRecord {
    [rule: string]: RuleEntry;
  }

  interface HasRules<Rules extends RulesRecord = RulesRecord> {
    rules?: Partial<Rules> | undefined;
  }

  interface BaseConfig<Rules extends RulesRecord = RulesRecord> extends HasRules<Rules> {
    $schema?: string | undefined;
    env?: { [name: string]: boolean } | undefined;
    extends?: string | string[] | undefined;
    globals?: { [name: string]: boolean | "readonly" | "readable" | "writable" | "writeable" } | undefined;
    noInlineConfig?: boolean | undefined;
    overrides?: ConfigOverride[] | undefined;
    parser?: string | undefined;
    parserOptions?: ParserOptions | undefined;
    plugins?: string[] | undefined;
    processor?: string | undefined;
    reportUnusedDisableDirectives?: boolean | undefined;
    settings?: { [name: string]: any } | undefined;
  }

  interface ConfigOverride<Rules extends RulesRecord = RulesRecord> extends BaseConfig<Rules> {
    excludedFiles?: string | string[] | undefined;
    files: string | string[];
  }

  interface Config<Rules extends RulesRecord = RulesRecord> extends BaseConfig<Rules> {
    ignorePatterns?: string | string[] | undefined;
    root?: boolean | undefined;
  }

  interface ParserOptions {
    ecmaVersion?: 3 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 2015 | 2016 | 2017 | 2018 | 2019 | 2020 | 2021 | undefined;
    sourceType?: "script" | "module" | undefined;
    ecmaFeatures?: {
      globalReturn?: boolean | undefined;
      impliedStrict?: boolean | undefined;
      jsx?: boolean | undefined;
      experimentalObjectRestSpread?: boolean | undefined;
      [key: string]: any;
    } | undefined;
    [key: string]: any;
  }

  interface LintOptions {
    filename?: string | undefined;
    preprocess?: ((code: string) => string[]) | undefined;
    postprocess?: ((problemLists: LintMessage[][]) => LintMessage[]) | undefined;
    filterCodeBlock?: boolean | undefined;
    disableFixes?: boolean | undefined;
    allowInlineConfig?: boolean | undefined;
    reportUnusedDisableDirectives?: boolean | undefined;
  }

  interface LintSuggestion {
    desc: string;
    messageId?: string | undefined;
  }

  interface LintMessage {
    column: number;
    line: number;
    endColumn?: number | undefined;
    endLine?: number | undefined;
    ruleId: string | null;
    message: string;
    messageId?: string | undefined;
    nodeType?: string | undefined;
    fatal?: true | undefined;
    severity: Severity;
    /** @deprecated Use `linter.getSourceCode()` */
    source?: string | null | undefined;
    suggestions?: LintSuggestion[] | undefined;
  }

  interface FixOptions extends LintOptions {
    fix?: boolean | undefined;
  }

  interface FixReport {
    fixed: boolean;
    output: string;
    messages: LintMessage[];
  }

  interface ProcessorFile {
    text: string;
    filename: string;
  }

  interface Processor<T extends string | ProcessorFile = string | ProcessorFile> {
    supportsAutofix?: boolean | undefined;
    preprocess?(text: string, filename: string): T[];
    postprocess?(messages: LintMessage[][], filename: string): LintMessage[];
  }
}

export namespace ESLint {
  interface Options {
    // File enumeration
    cwd?: string | undefined;
    errorOnUnmatchedPattern?: boolean | undefined;
    extensions?: string[] | undefined;
    globInputPaths?: boolean | undefined;
    ignore?: boolean | undefined;
    ignorePath?: string | undefined;

    // Linting
    allowInlineConfig?: boolean | undefined;
    baseConfig?: Linter.Config | undefined;
    overrideConfig?: Linter.Config | undefined;
    overrideConfigFile?: string | undefined;
    plugins?: Record<string, any> | undefined;
    reportUnusedDisableDirectives?: Linter.RuleLevel | undefined;
    resolvePluginsRelativeTo?: string | undefined;
    rulePaths?: string[] | undefined;
    useEslintrc?: boolean | undefined;

    // Autofix
    fix?: boolean | ((message: Linter.LintMessage) => boolean) | undefined;

    // Cache-related
    cache?: boolean | undefined;
    cacheLocation?: string | undefined;
    cacheStrategy?: "content" | "metadata" | undefined;
  }

  interface LintResult {
    filePath: string;
    messages: Linter.LintMessage[];
    errorCount: number;
    warningCount: number;
    fixableErrorCount: number;
    fixableWarningCount: number;
    output?: string | undefined;
    source?: string | undefined;
    usedDeprecatedRules: DeprecatedRuleUse[];
  }

  interface DeprecatedRuleUse {
    ruleId: string;
    replacedBy: string[];
  }
}

export class CLIEngine {
  static version: string;

  constructor(options: CLIEngine.Options);

  getConfigForFile(filePath: string): Linter.Config;

  executeOnText(text: string, filename?: string): CLIEngine.LintReport;

  isPathIgnored(filePath: string): boolean;
}

export namespace CLIEngine {
  class Options {
    allowInlineConfig?: boolean | undefined;
    baseConfig?: false | { [name: string]: any } | undefined;
    cache?: boolean | undefined;
    cacheFile?: string | undefined;
    cacheLocation?: string | undefined;
    cacheStrategy?: "content" | "metadata" | undefined;
    configFile?: string | undefined;
    cwd?: string | undefined;
    envs?: string[] | undefined;
    errorOnUnmatchedPattern?: boolean | undefined;
    extensions?: string[] | undefined;
    fix?: boolean | undefined;
    globals?: string[] | undefined;
    ignore?: boolean | undefined;
    ignorePath?: string | undefined;
    ignorePattern?: string | string[] | undefined;
    useEslintrc?: boolean | undefined;
    parser?: string | undefined;
    parserOptions?: Linter.ParserOptions | undefined;
    plugins?: string[] | undefined;
    resolvePluginsRelativeTo?: string | undefined;
    rules?: {
      [name: string]: Linter.RuleLevel | Linter.RuleLevelAndOptions;
    } | undefined;
    rulePaths?: string[] | undefined;
    reportUnusedDisableDirectives?: boolean | undefined;
  }

  type LintResult = ESLint.LintResult;

  interface LintReport {
    results: LintResult[];
    errorCount: number;
    warningCount: number;
    fixableErrorCount: number;
    fixableWarningCount: number;
    usedDeprecatedRules: unknown[];
  }
}

type Prepend<Tuple extends any[], Addend> = ((_: Addend, ..._1: Tuple) => any) extends (..._: infer Result) => any
  ? Result
  : never;
