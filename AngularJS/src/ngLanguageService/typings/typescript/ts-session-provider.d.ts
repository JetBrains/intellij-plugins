import {IDETypeScriptSession} from "./util";
import {DefaultOptionsHolder} from "./ts-default-options";

export declare function instantiateSession(ts_impl: any, logger: ts.server.Logger, defaultOptionsHolder: DefaultOptionsHolder, sessionClass: typeof IDETypeScriptSession): ts.server.Session;
