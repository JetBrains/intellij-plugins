export declare type PathProcessor = {
    getExpandedPath(oldFileName: string, contentRoot: any, sourceRoot: any, onError: any): any;
};
export declare function getPathProcessor(ts_impl: any, params: {
    projectPath?: string;
    outPath?: string;
}): PathProcessor;
