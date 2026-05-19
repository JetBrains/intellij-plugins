import {EslintPluginState, ESLintRequest, ESLintResponse, RequestArguments, FixErrors, GetErrors} from "./eslint-api"
import {containsString} from "./eslint-common"

export class Standard17Plugin implements LanguagePlugin {
  private readonly includeSourceText: boolean | null;
  private readonly standardPackagePath: string;

  constructor(state: EslintPluginState) {
    this.includeSourceText = state.includeSourceText;
    this.standardPackagePath = state.standardPackagePath;
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

  private async invokeESLint(requestArguments: RequestArguments, additionalOptions = {}): Promise<ESLint.LintResult[]> {
    const options = additionalOptions;
    options.filename = requestArguments.fileName;

    let path = this.standardPackagePath + "/index.js";
    if (path.charAt(1) == ":") {
      // Windows absolute path
      path = "file:///" + path;
    }
    const standardEngine = (await import(path)).default;

    return standardEngine.lintText(requestArguments.content, options);
  }
}
