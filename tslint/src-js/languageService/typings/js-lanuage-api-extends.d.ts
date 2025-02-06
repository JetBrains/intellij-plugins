/**
 * Plugin state for starting the service
 *
 * see also js-language-service-api.d.ts
 */
interface PluginState {
    /**
     * TSLint nodejs package path
     */
    readonly tslintPackagePath: string;

    /**
     * Path to package.json with tslint dependency (e.g. defined for Yarn PnP)
     */
    readonly packageJsonPath?: string;

    /**
     * TSLint configuration 'additional root' parameter
     */
    readonly additionalRootDirectory?: string;
}

interface TsLintRequest {
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
    readonly arguments?: any;
}

interface CommandArguments {
    /**
     * Absolute path for the file to check
     */
    readonly filePath: string;

    /**
     * Absolute config path
     */
    readonly configPath: string;
}

interface GetErrorsArguments extends CommandArguments {
    /**
     * Content of the file
     */
    readonly content: string;
}

interface FixErrorsArguments extends CommandArguments {
}