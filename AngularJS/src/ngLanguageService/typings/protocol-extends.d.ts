


declare module './tsserverlibrary' {

    /**
     * expose core api
     */
    export function normalizePath(path: string): server.NormalizedPath;

    export function getDirectoryPath(path: string): string;

    export function getNewLineCharacter(p: any): string;

    export function getRootLength(p: string): number;

    export function getNormalizedAbsolutePath(p: string, dir: any): string;

    export function sortAndDeduplicateDiagnostics(p: any): any;


    /**
     * Extensions for typescript server protocol
     * Provides new messages / fields
     */


    export namespace server.CommandNames {
        export let IDEChangeFiles: string;
        export let IDECompile: string;
        export let IDEGetErrors: string;
        export let IDEGetAllErrors: string;
        export let IDEGetMainFileErrors: string;
        export let IDEGetProjectErrors: string;
        export let IDECompletions: string;
        export let IDEComposite: string;
        export let IDEEmpty: string;
    }

    export namespace server.protocol {

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

        export interface IDECompositeRequest extends Request {
            arguments: { nestedRequests: Request[] }
        }


        export interface IDECompileFileRequestArgs extends server.protocol.FileRequestArgs {
            includeErrors: boolean;
            force?: boolean

            //deprecated parameters but we need it for back-compatibility
            contentRootForMacro?: string;
            sourceRootForMacro?: string;
        }

        export type IDEBodyDiagnostics = {
            generatedFiles?: string[],
            processedFiles?: string[],
            infos?: server.protocol.DiagnosticEventBody[]
        }

        export interface IDECompileResponse extends server.protocol.Response {
            body: IDEBodyDiagnostics
        }
        
        export interface IDEGetErrorRequest extends server.protocol.Request {
            arguments: server.protocol.GeterrRequestArgs;
        }

        

        export interface IDECompletionsRequest extends server.protocol.FileLocationRequest {
            arguments: server.protocol.CompletionsRequestArgs;
        }

        export interface IDECompletionResponse extends Response {
            body?: server.protocol.CompletionEntryDetails[];
        }
    }


    export namespace server {
        interface ProjectOptions {
            compileOnSave?: boolean
        }
    }
}

export {}