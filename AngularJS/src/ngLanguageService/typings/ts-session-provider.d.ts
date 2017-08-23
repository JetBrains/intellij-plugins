import {DefaultOptionsHolder} from "./ts-default-options";
import * as ts from '../typings/tsserverlibrary';

export declare function instantiateSession(ts_impl: typeof ts, logger: ts.server.Logger, defaultOptionsHolder: DefaultOptionsHolder, sessionClass: SessionClass): ts.server.Session;
export declare type SessionClass = {
    new (...args: any[]): ts.server.Session;
};
export declare function createSessionClass(ts_impl: typeof ts, defaultOptionsHolder: DefaultOptionsHolder): SessionClass;
