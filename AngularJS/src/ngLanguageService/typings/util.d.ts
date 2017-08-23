import {DefaultOptionsHolder} from "./ts-default-options";
import * as protocol from '../typings/protocol';
import * as ts from '../typings/tsserverlibrary';
import ProjectService = ts.server.ProjectService;
export declare function initCommandNames(TypeScriptCommandNames: any): void;
export declare const sessionClasstemp: typeof ts.server.Session;
export declare abstract class IDETypeScriptSession extends sessionClasstemp implements ts.server.Session {
    getChangeSeq(): number;
    processOldProjectErrors(request: protocol.Request): {
        responseRequired: boolean;
    };
    updateProjectStructureEx(): void;
    refreshStructureEx(): void;
    logMessage(text: string, force?: boolean): void;
    abstract closeClientFileEx(normalizedFileName: string): void;
    updateFilesEx(args: any): Response;
    abstract changeFileEx(fileName: string, content: string, tsconfig?: string): void;
    getTime(): number;
    abstract getProjectForFileEx(fileName: string, projectFile?: string): ts.server.Project;
    compileFileEx(req: any): Response;
    getCompletionEx(request: protocol.Request): {
        response: any;
        responseRequired: boolean;
    };
    abstract lineOffsetToPosition(project: ts.server.Project, fileName: string, line: number, offset: number): number;
    abstract getLanguageService(project: ts.server.Project, sync?: boolean): ts.LanguageService;
    getDiagnosticsEx(fileNames: string[], commonProject?: ts.server.Project, reqOpen?: boolean): ts.server.protocol.DiagnosticEventBody[];
    getForceProject(fileName: string): ts.server.Project;
    abstract containsFileEx(project: ts.server.Project, file: string, reqOpen: boolean): boolean;
    abstract getProjectName(project: ts.server.Project): string | undefined | null;
    abstract getProjectConfigPathEx(project: ts.server.Project): string | null;
    abstract positionToLineOffset(project: ts.server.Project, fileName: string, position: number): ts.server.ILineInfo;
    formatDiagnostic(fileName: string, project: ts.server.Project, diagnostic: ts.Diagnostic): ts.server.protocol.Diagnostic;
    getMainFileDiagnosticsForFileEx(fileName: string): ts.server.protocol.DiagnosticEventBody[];
    getProjectDiagnosticsForFileEx(fileName: string): ts.server.protocol.DiagnosticEventBody[];
    getProjectDiagnosticsEx(project: ts.server.Project): ts.server.protocol.DiagnosticEventBody[];
    abstract afterCompileProcess(project: ts.server.Project, requestedFile: string, wasOpened: boolean | undefined): void;
    abstract needRecompile(project: ts.server.Project): boolean;
    abstract reloadFileFromDisk(info: ts.server.ScriptInfo): void;
    abstract getProjectForCompileRequest(req: any, normalizedRequestedFile: string): {
        project: ts.server.Project;
        wasOpened?: boolean;
    };
    abstract setNewLine(project: ts.server.Project, options: ts.CompilerOptions): void;
    abstract getCompileOptionsEx(project: ts.server.Project): ts.CompilerOptions;
    beforeFirstMessage(): void;
    abstract tsVersion(): string;
    abstract getScriptInfo(projectService: ts.server.ProjectService, fileName: string): ts.server.ScriptInfo;
    appendGlobalErrors(result: ts.server.protocol.DiagnosticEventBody[], processedProjects: {
        [p: string]: ts.server.Project;
    }, empty: boolean): ts.server.protocol.DiagnosticEventBody[];
}
export declare const DETAILED_COMPLETION_COUNT: number;
export declare const DETAILED_MAX_TIME: number;
export declare function isTypeScript15(ts_impl: typeof ts): boolean;
export declare function isTypeScript16(ts_impl: typeof ts): boolean;
export declare function isTypeScript17(ts_impl: typeof ts): boolean;
export declare function isTypeScript20(ts_impl: typeof ts): boolean;
/**
 * Default tsserver implementation doesn't return response in most cases ("open", "close", etc.)
 * we want to override the behaviour and send empty-response holder
 */
export declare const doneRequest: Response;
export declare type Response = {
    response?: any;
    responseRequired?: boolean;
};
export declare class DiagnosticsContainer {
    value: {
        [p: string]: ts.server.protocol.Diagnostic[];
    };
    reset(): void;
}
export declare function updateInferredProjectSettings(ts_impl: typeof ts, defaultOptionsHolder: DefaultOptionsHolder, projectService: ProjectService): void;
export declare function setDefaultOptions(serviceDefaults: any, defaultOptionsHolder: DefaultOptionsHolder, projectService: ProjectService): void;
export declare function copyPropertiesInto(fromObject: any, toObject: any): void;
export declare function extendEx(ObjectToExtend: typeof ts.server.ProjectService, name: string, func: (oldFunction: any, args: any) => any): void;
export declare function parseNumbersInVersion(version: string): number[];
export declare function isVersionMoreOrEqual(version: number[], ...expected: number[]): boolean;
export declare function isFunctionKind(kind: string): boolean;
export declare function getDefaultSessionClass(ts_impl: typeof ts, host: any, defaultOptionsHolder: DefaultOptionsHolder): DefaultSessionClass;
export interface DefaultSession extends ts.server.Session {
    beforeFirstMessage(): void;
    getFileWrite(projectFilename: string, outFiles: string[], realWriteFile: any, contentRoot?: string, sourceRoot?: string): (fileName: string, data?: any, writeByteOrderMark?: any, onError?: any, sourceFiles?: any[]) => void;
    getCompileInfo(req: ts.server.protocol.IDECompileFileRequestArgs): CompileInfo;
    compileFileEx(req: ts.server.protocol.IDECompileFileRequestArgs): Response;
}
export declare type DefaultSessionClass = {
    new (...args: any[]): DefaultSession;
};
export interface CompileInfo {
    project?: ts.server.Project | null;
    projectName?: string;
    projectPath?: string;
    isCompilingRequired?: boolean;
    projectUsesOutFile?: boolean;
    getSourceFiles?(): ts.SourceFile[];
    emit?(file?: ts.SourceFile): ts.server.protocol.DiagnosticEventBody[] | null;
    getDiagnostics?(): ts.server.protocol.DiagnosticEventBody[];
    postProcess?(): void;
    getOutFiles?(): string[];
}
