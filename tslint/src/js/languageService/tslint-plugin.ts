import {getVersion, Version} from "../utils";
import {IConfigurationFile} from "tslint/lib/configuration";
import {ILinterOptions, Linter, LintResult} from "tslint";
import {readFileSync} from "fs"

namespace TsLintCommands {
    export let GetErrors: string = "GetErrors";
    export let FixErrors: string = "FixErrors";
}

type LinterApi = {
    linter: typeof Linter;
    version: Version
}

class Response {
    version?: string;
    command: string;
    request_seq: number;
    body: string | null;
    error: string | null;
}

export class TSLintPlugin implements LanguagePlugin {

    private readonly linterApi: LinterApi;
    private readonly additionalRulesDirectory?: string;

    constructor(state: PluginState) {
        this.linterApi = resolveTsLint(state.tslintPackagePath, state.packageJsonPath);
        this.additionalRulesDirectory = state.additionalRootDirectory;
    }

    private process(parsedObject: TsLintRequest): Object | null {
        switch (parsedObject.command) {
            case TsLintCommands.GetErrors: {
                return this.getErrors(parsedObject.arguments);
            }
            case TsLintCommands.FixErrors: {
                return this.fixErrors(parsedObject.arguments);
            }
        }

        return null;
    }

    onMessage(p: string, writer: MessageWriter): void {
        const request: TsLintRequest = JSON.parse(p);
        // here we use object -> JSON.stringify, because we need to escape possible error's text symbols
        // and we do not want to duplicate this code
        let response: Response = new Response();
        response.version = this.linterApi.version.raw;
        response.command = request.command;
        response.request_seq = request.seq;

        let result: Object | null;
        try {
            result = this.process(request);
        } catch (e) {
            response.error = e.toString() + "\n\n" + e.stack;
            writer.write(JSON.stringify(response));
            return;
        }
        if (result) {
            response.body = (<any>result).output;
        }
        writer.write(JSON.stringify(response));
    }

    private getErrors(toProcess: GetErrorsArguments): LintResult {
        return this.processLinting(toProcess, this.getOptions(false));
    }

    private fixErrors(toProcess: FixErrorsArguments): LintResult {
        //TODO. why here?
        let contents = readFileSync(toProcess.filePath, "utf8");
        return this.processLinting({...toProcess, content: contents}, this.getOptions(true));
    }

    private getOptions(fix: boolean) {
        return {
            formatter: "json",
            fix: fix,
            rulesDirectory: this.additionalRulesDirectory
        };
    }

    private processLinting(args: CommandArguments & { content: string }, options: ILinterOptions): LintResult {
        let linter = this.linterApi.linter;
        let major = this.linterApi.version.major || 0;

        let configuration = this.getConfiguration(args.filePath, args.configPath);
        if (major >= 4) {
            let tslint = new linter(options);
            tslint.lint(args.filePath, args.content, configuration);
            return tslint.getResult();
        }
        (<any>options).configuration = configuration;
        let tslint = new (<any>linter)(args.filePath, args.content, options);
        return tslint.lint();
    }

    private getConfiguration(fileName: string, configFileName: string): IConfigurationFile {
        let majorVersion = this.linterApi.version.major;
        let configurationResult = this.linterApi.linter.findConfiguration(configFileName, fileName);
        if (majorVersion && majorVersion >= 4) {
            if (!configurationResult || !configurationResult.results) {
                throw new Error("Cannot find configuration " + configFileName);
            }
            return configurationResult.results;
        }
        return (<IConfigurationFile>configurationResult)
    }
}

function resolveTsLint(packagePath: string, packageJsonPath?: string): LinterApi {
    const tslint: any = requireInContext(packagePath, packageJsonPath);
    const version = getVersion(tslint);
    const linter = version.major && version.major >= 4 ? tslint.Linter : tslint;
    return {linter, version};
}

// Duplicate of eslint/src/eslint-common.ts

function requireInContext(modulePathToRequire: string, contextPath?: string): any {
  const contextRequire = getContextRequire(contextPath);
  return contextRequire(modulePathToRequire);
}

function getContextRequire(contextPath?: string): NodeRequire {
  if (contextPath != null) {
    const module = require('module')
    if (typeof module.createRequire === 'function') {
      // https://nodejs.org/api/module.html#module_module_createrequire_filename
      // Implemented in Yarn PnP: https://next.yarnpkg.com/advanced/pnpapi/#requiremodule
      return module.createRequire(contextPath);
    }
    // noinspection JSDeprecatedSymbols
    if (typeof module.createRequireFromPath === 'function') {
      // Use createRequireFromPath (a deprecated version of createRequire) to support Node.js 10.x
      // noinspection JSDeprecatedSymbols
      return module.createRequireFromPath(contextPath);
    }
    throw Error('Function module.createRequire is unavailable in Node.js ' + process.version +
      ', Node.js >= 12.2.0 is required')
  }
  return require;
}
