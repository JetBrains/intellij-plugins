import { IDETypeScriptSession } from "./util";
import { HolderContainer } from "./compile-info-holder";
export declare function getSessionOld(TypeScriptSession: typeof IDETypeScriptSession, TypeScriptProjectService: typeof ts.server.ProjectService, TypeScriptCommandNames: typeof ts.server.CommandNames, logger: ts.server.Logger, host: ts.server.ServerHost, ts_impl: typeof ts, commonDefaultOptions: any, mainFile: any, projectEmittedWithAllFiles: HolderContainer): ts.server.Session;
