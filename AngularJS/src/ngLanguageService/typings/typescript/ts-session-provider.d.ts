import { HolderContainer } from "./compile-info-holder";
import { IDETypeScriptSession } from "./util";
import { DefaultOptionsHolder } from "./ts-default-options";
export declare function getSession(ts_impl: any, logger: ts.server.Logger, commonDefaultOptions: DefaultOptionsHolder, mainFile: string, projectEmittedWithAllFiles: HolderContainer, sessionClass: typeof IDETypeScriptSession): ts.server.Session;
