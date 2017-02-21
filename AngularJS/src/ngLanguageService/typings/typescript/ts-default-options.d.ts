export declare class DefaultOptionsHolder {
    options: ts.CompilerOptions;
    configFileName: string | null;
    private defaultOptions;
    private updateConfigCallback;
    constructor(defaultOptions: ts.CompilerOptions, ts_impl: typeof ts);
    hasConfig(): boolean;
    watchConfig(callback: () => void, ts_impl: typeof ts): void;
    refresh(ts_impl: typeof ts): void;
    getConfigOptions(commonDefaultOptions: ts.CompilerOptions, ts_impl: typeof ts): {
        commonDefaultOptions: ts.CompilerOptions;
        configFileName?: string;
    };
}
