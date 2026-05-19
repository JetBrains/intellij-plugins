export const GetErrors = "GetErrors";
export const FixErrors = "FixErrors";

export interface EslintPluginState extends PluginState {
  readonly eslintPackagePath: string;
  readonly standardPackagePath: string;
  readonly linterPackageVersion: string;
  readonly packageJsonPath?: string;
  readonly additionalRootDirectory?: string;
  readonly includeSourceText: boolean | null;
}

export interface ESLintRequest {
  /**
   * Unique id of the message
   */
  readonly seq: number;

  /**
   * Message type (usually it is "request")
   */
  readonly type: string;

  /**
   * Id of the command
   */
  readonly command: string;

  /**
   * Additional arguments
   */
  readonly arguments: RequestArguments;
}

export interface RequestArguments {
  /**
   * .eslintignore file path
   */
  readonly ignoreFilePath: string;
  /**
   * Absolute path for the file to check
   */
  readonly fileName: string;

  /**
   * Absolute config path
   */
  readonly configPath: string | null;
  readonly content: string;
  readonly extraOptions: string | null;
  readonly fileKind: FileKind;
  readonly flatConfig: boolean;
}

export class ESLintResponse {
  constructor(public request_seq: number, public command: string) {
  }

  body?: any;
  error?: string;
  isNoConfigFile?: boolean
}

/**
 * See com.intellij.lang.javascript.linter.eslint.EslintUtil.FileKind
 */
export enum FileKind {
  ts = "ts",
  html = "html",
  vue = "vue",
  jsAndOther = "js_and_other",
}

