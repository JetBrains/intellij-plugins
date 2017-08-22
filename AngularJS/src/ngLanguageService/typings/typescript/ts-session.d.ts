import {IDETypeScriptSession} from "./util";
import {DefaultOptionsHolder} from "./ts-default-options";

export declare type ProjectResult = {
    succeeded: boolean;
    projectOptions?: ts.server.ProjectOptions;
    error?: ts.server.ProjectOpenResult;
    errors?: any;
};
export declare function createSessionClass(ts_impl: any, defaultOptionsHolder: DefaultOptionsHolder): typeof IDETypeScriptSession;
