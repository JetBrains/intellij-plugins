import { HolderContainer } from "./compile-info-holder";
import { PathProcessor } from "./out-path-process";
import { IDETypeScriptSession } from "./util";
export declare type ProjectResult = {
    succeeded: boolean;
    projectOptions?: ts.server.ProjectOptions;
    error?: ts.server.ProjectOpenResult;
    errors?: any;
};
export declare function createSessionClass(ts_impl: any, logger: ts.server.Logger, commonDefaultOptions?: ts.CompilerOptions, pathProcessor?: PathProcessor, projectEmittedWithAllFiles?: HolderContainer, mainFile?: string): typeof IDETypeScriptSession;
