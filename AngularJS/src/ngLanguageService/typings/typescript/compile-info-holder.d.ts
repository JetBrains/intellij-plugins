export declare class HolderContainer {
    static readonly InferredProjectName: string;
    value: {
        [p: string]: CompileInfoHolder;
    };
    reset(): void;
    private getName(projectUniqueName, projectConfigName);
    getCompileInfoHolder(projectUniqueName: string, projectConfigName: string): CompileInfoHolder | null;
    getOrCreateCompileInfoHolder(projectUniqueName: string, projectConfigName: string): CompileInfoHolder;
    resetProject(projectUniqueName: string): void;
}
/**
 * Emulating incremental compilation.
 * If file content wasn't changes we don't need recompile the file
 */
export declare class CompileInfoHolder {
    private _lastCompilerResult;
    checkUpdateAndAddToCache(file: ts.SourceFile, ts_impl: typeof ts): boolean;
    resetForFile(fileName: string): void;
    reset(): void;
}
export declare const projectEmittedWithAllFiles: HolderContainer;
