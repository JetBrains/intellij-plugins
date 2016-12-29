export declare class HolderContainer {
    value: {
        [p: string]: CompileInfoHolder;
    };
    reset(): void;
}
/**
 * Emulating incremental compilation.
 * If file content wasn't changes we don't need recompile the file
 */
export declare class CompileInfoHolder {
    private _lastCompilerResult;
    private ts_impl;
    constructor(ts_impl: any);
    checkUpdateAndAddToCache(file: ts.SourceFile): boolean;
    resetForFile(fileName: string): void;
    reset(): void;
}
