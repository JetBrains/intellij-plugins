namespace TsLintCommands {
    export let GetErrors: string = "GetErrors";
    export let FixErrors: string = "FixErrors";
}

const enum TsLintVersion {
    VERSION_3_AND_BEFORE,
    VERSION_4_AND_HIGHER
}

type LinterOptions = {
    linter?: any;
    versionKind?: TsLintVersion;
    linterConfiguration: any;
    version?: string
}
class Response {
    version?: string;
    command: string;
    request_seq: number;
    body: string | null;
    error: string | null;
}
let fs = require("fs");

export class TSLintPlugin implements LanguagePlugin {

    private readonly linterOptions: LinterOptions;
    private readonly additionalRulesDirectory?: string;

    constructor(state: PluginState) {
        this.linterOptions = resolveTsLint(state);
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
        response.version = this.linterOptions.version;
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

    private getErrors(toProcess: GetErrorsArguments): {} {
        let options = this.getOptions(false);
        return this.processLinting(toProcess.fileName, toProcess.content, toProcess.configPath, options);
    }

    private fixErrors(toProcess: FixErrorsArguments): {} {
        let options = this.getOptions(true);

        let contents = fs.readFileSync(toProcess.fileName, "utf8");

        return this.processLinting(toProcess.fileName, contents, toProcess.configPath, options);
    }

    private getOptions(fix: boolean) {
        return {
            formatter: "json",
            fix: fix,
            rulesDirectory: this.additionalRulesDirectory,
            formattersDirectory: undefined
        };
    }

    private processLinting(fileName: string, content: string | null | undefined, configFileName: string, options: {}) {
        let linterOptions = this.linterOptions;
        let linter: any = this.linterOptions.linter;
        let result = {};

        let configuration = this.getConfiguration(fileName, configFileName, linter);
        if (linterOptions.versionKind == TsLintVersion.VERSION_4_AND_HIGHER) {
            let tslint = new linter(options);
            tslint.lint(fileName, content, configuration);
            result = tslint.getResult();
        }
        else {
            (<any>options).configuration = configuration;
            let tslint = new (<any>linter)(fileName, content, options);
            result = tslint.lint();
        }

        return result;
    }

    private getConfiguration(fileName: string, configFileName: string, linter: any) {

        let linterConfiguration = this.linterOptions.linterConfiguration;
        let versionKind = this.linterOptions.versionKind;
        if (versionKind == TsLintVersion.VERSION_4_AND_HIGHER) {
            let configurationResult: any = linterConfiguration.findConfiguration(configFileName, fileName);
            if (!configurationResult) {
                throw new Error("Cannot find configuration " + configFileName);
            }
            if (configurationResult && configurationResult.error) {
                throw configurationResult.error;
            }

            return configurationResult.results;
        } else {
            return linter.findConfiguration(configFileName, fileName);
        }
    }
}


function resolveTsLint(options: PluginState): LinterOptions {
    const tslintPackagePath = options.tslintPackagePath;
    let value: any = require(tslintPackagePath);

    let versionText = getVersionText(value);
    const versionKind = getVersion(versionText);

    const linter = versionKind == TsLintVersion.VERSION_4_AND_HIGHER ? value.Linter : value;
    const linterConfiguration = value.Configuration;

    return {linter, linterConfiguration, versionKind, version: versionText};
}

function getVersionText(tslint: any) {
    return tslint.VERSION || (tslint.Linter && tslint.Linter.VERSION);
};

function getVersion(version?: string): TsLintVersion {
    if (version == null) {
        return TsLintVersion.VERSION_3_AND_BEFORE;
    }


    const firstDot = version.indexOf(".");
    const majorVersion = firstDot == -1 ? version : version.substr(0, firstDot + 1);

    return majorVersion && (Number(majorVersion) > 3) ?
        TsLintVersion.VERSION_4_AND_HIGHER :
        TsLintVersion.VERSION_3_AND_BEFORE;
}