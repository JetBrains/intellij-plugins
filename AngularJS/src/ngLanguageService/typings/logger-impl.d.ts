export declare const isLogEnabled: any;
import * as ts from '../typings/tsserverlibrary';

export declare class LoggerImpl implements ts.server.Logger {
    logFilename: string;
    level: string;
    private ts_impl;
    hasLevel(level: ts.server.LogLevel): boolean;
    getLogFileName(): string;
    fd: number;
    seq: number;
    inGroup: boolean;
    firstInGroup: boolean;
    constructor(logFilename: string, level: string, ts_impl: typeof ts);
    static padStringRight(str: string, padding: string): string;
    close(): void;
    group(logGroupEntries: (log: (msg: string) => void) => void): void;
    perftrc(s: string): void;
    info(s: string): void;
    err(s: string): void;
    startGroup(): void;
    endGroup(): void;
    loggingEnabled(): boolean;
    isVerbose(): boolean;
    msg(s: string, type?: string): void;
}
export declare function createLoggerFromEnv(ts_impl: typeof ts): LoggerImpl;
export declare function serverLogger(message: string, force?: boolean): void;
