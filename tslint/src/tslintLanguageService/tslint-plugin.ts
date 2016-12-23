// import * as tslint from 'tslint';


namespace TsLintCommands {
    export let GetErrors: string = "GetErrors";
}

const enum TsLintVersion {
    VERSION_3_AND_BEFORE,
    VERSION_4_AND_HIGHER
}
type LinerOptions = {linter?: any, versionKind?: TsLintVersion, linterConfiguration: any, version?: string}

export class TSLintPlugin implements LanguagePlugin {

    private readonly linterOptions: LinerOptions;
    private readonly additionalRulesDirectory?: string;

    constructor(state: PluginState) {
        this.linterOptions = resolveTsLint(state);
        this.additionalRulesDirectory = state.additionalRootDirectory;
    }

    onMessage(p: string, writer: MessageWriter): void {
        const request: TsLintRequest = JSON.parse(p);
        let result = this.process(request);
        if (result) {
            let output = (<any>result).output;
            let version = this.linterOptions.version;
            let command = request.command;
            let seq = request.seq;
            let resultJson = `{"body":${output},"version":"${version}",` +
                `"command":"${command}","request_seq":${seq}}`;

            writer.write(resultJson);
        }
    }

    process(parsedObject: TsLintRequest): Object | null {
        switch (parsedObject.command) {
            case TsLintCommands.GetErrors: {
                let result = this.getErrors(parsedObject.arguments);
                return result;
            }
        }

        return null;
    }

    getErrors(toProcess: GetErrorsArguments): {} {
        let options = {
            formatter: "json",
            fix: false,
            rulesDirectory: this.additionalRulesDirectory,
            formattersDirectory: undefined
        }

        let linterOptions = this.linterOptions;
        let linter: any = this.linterOptions.linter;
        let result = {}

        let configuration = this.getConfiguration(toProcess, linter);
        if (linterOptions.versionKind == TsLintVersion.VERSION_4_AND_HIGHER) {
            let tslint = new linter(options);
            tslint.lint(toProcess.fileName, toProcess.content, configuration);
            result = tslint.getResult();
        }
        else {
            (<any>options).configuration = configuration;
            let tslint = new (<any>linter)(toProcess.fileName, toProcess.content, options);
            result = tslint.lint();
        }

        return result;
    }

    getConfiguration(toProcess: GetErrorsArguments, linter: any) {

        let linterConfiguration = this.linterOptions.linterConfiguration;
        ;
        let versionKind = this.linterOptions.versionKind;
        if (versionKind == TsLintVersion.VERSION_4_AND_HIGHER) {
            let configurationResult: any = linterConfiguration.findConfiguration(toProcess.configPath, toProcess.fileName);
            if (!configurationResult) {
                throw new Error("Cannot find configuration " + toProcess.configPath);
            }
            if (configurationResult && configurationResult.error) {
                throw configurationResult.error;
            }

            return configurationResult.results;
        } else {
            return linter.findConfiguration(toProcess.configPath, toProcess.fileName);
        }
    }
}


function resolveTsLint(options: PluginState): LinerOptions {
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