import { HolderContainer } from "./compile-info-holder";
import { IDETypeScriptSession } from "./util";
export declare function getSession(ts_impl: any, logger: ts.server.Logger, commonDefaultOptions: ts.CompilerOptions, mainFile: string, projectEmittedWithAllFiles: HolderContainer, sessionClass: typeof IDETypeScriptSession): ts.server.Session;
