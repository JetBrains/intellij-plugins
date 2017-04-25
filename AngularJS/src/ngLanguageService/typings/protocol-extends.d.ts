/**
 * Extensions for typescript server protocol
 * Provides new messages / fields
 */
declare namespace ts {
    /**
     * expose core api
     */
    export function normalizePath(path: string): string;

    export function getDirectoryPath(path: string): string;

    export function getNewLineCharacter(p: any): string;

    export function getRootLength(p: string): number;

    export function getNormalizedAbsolutePath(p: string, dir: any): string;

    export function sortAndDeduplicateDiagnostics(p:any):any;

    export namespace server {
        export enum LogLevel {
            terse,
            normal,
            requestTime,
            verbose
        }
    }

}

/**
 * see TypeScriptServiceInitialStateObject
 */
interface TypeScriptPluginState extends PluginState {
    serverFolderPath: string;

    //deprecated parameters but we need it for back-compatibility
    hasManualParams: boolean;
    outPath?: string;
    projectPath?: string;
    commandLineArguments?: string[];
    mainFilePath?: string;
    isUseSingleInferredProject?: boolean;
}

declare namespace ts.server.CommandNames {
    export let IDEChangeFiles: string;
    export let IDECompile: string;
    export let IDEGetErrors: string;
    export let IDEGetAllErrors: string;
    export let IDEGetMainFileErrors: string;
    export let IDEGetProjectErrors: string;
    export let IDECompletions: string;
}

declare namespace ts.server.protocol {

    /**
     *  Extend request by adding config file relates to the file
     */
    export interface OpenRequestArgs extends FileRequestWithConfigArgs {

    }

    export interface FileRequestWithConfigArgs extends FileRequestArgs {
        projectFileName?: string;
    }

    export interface IDEUpdateFilesRequest extends Request {
        arguments: IDEUpdateFilesContentArgs;
    }

    export interface IDEUpdateFilesContentArgs {
        /**
         * map filepath -> new content
         */
        files: {
            [name: string]: string;
        }

        filesToReloadContentFromDisk: string[];
    }

    export interface IDECompileFileRequest extends Request {

        /**
         * path to typescript file or ts config
         */
        arguments: IDECompileFileRequestArgs;
    }


    export interface IDECompileFileRequestArgs extends FileRequestWithConfigArgs {
        includeErrors: boolean;
        force?: boolean

        //deprecated parameters but we need it for back-compatibility
        contentRootForMacro?: string;
        sourceRootForMacro?: string;
    }

    export type IDEBodyDiagnostics = {
        generatedFiles: string[],
        infos: DiagnosticEventBody[]
    }

    export interface IDECompileResponse extends Response {
        body: IDEBodyDiagnostics
    }

    export interface Diagnostic {
        category?: string
    }

    export interface IDEGetErrorRequest extends Request {
        arguments: GeterrRequestArgs;
    }


    export interface IDECompletionsRequest extends FileLocationRequest {
        arguments: CompletionsRequestArgs;
    }

    export interface IDECompletionResponse extends Response {
        body?: CompletionEntryDetails[];
    }
}


declare namespace ts.server {
    interface ProjectOptions {
        compileOnSave?: boolean
    }
}