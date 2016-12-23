interface PluginState {
    tslintPackagePath: string;
    additionalRootDirectory?: string;
}

declare let require: any;


interface TsLintRequest {
    seq: number;

    type: string;

    command: string;

    arguments?: any;
}

interface GetErrorsArguments {
    fileName: string;
    configPath: string;
    content: string;
}