import { HolderContainer } from "./compile-info-holder";
import { PathProcessor } from "./out-path-process";
import { IDETypeScriptSession } from "./util";
import { DefaultOptionsHolder } from "./ts-default-options";
export declare type ProjectResult = {
    succeeded: boolean;
    projectOptions?: ts.server.ProjectOptions;
    error?: ts.server.ProjectOpenResult;
    errors?: any;
};
export declare function createSessionClass(ts_impl: any, logger: ts.server.Logger, commonDefaultOptions?: DefaultOptionsHolder, pathProcessor?: PathProcessor, projectEmittedWithAllFiles?: HolderContainer, mainFile?: string): typeof IDETypeScriptSession;
