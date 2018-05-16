/**
 * NodeJS require function
 */
declare let require: any;

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

interface GetErrorsArguments {
    /**
     * Absolute path for the file to check
     */
    readonly fileName: string;

    /**
     * Absolute config path
     */
    readonly configPath: string;

    /**
     * Content of the file
     */
    readonly content: string;
}

interface FixErrorsArguments {
    /**
     * Absolute path for the file to check
     */
    readonly fileName: string;

    /**
     * Absolute config path
     */
    readonly configPath: string;
}