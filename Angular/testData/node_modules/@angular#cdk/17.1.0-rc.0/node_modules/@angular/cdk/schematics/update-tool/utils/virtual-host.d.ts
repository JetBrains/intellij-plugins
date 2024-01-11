/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import * as ts from 'typescript';
import { FileSystem } from '../file-system';
declare module 'typescript' {
    interface FileSystemEntries {
        readonly files: readonly string[];
        readonly directories: readonly string[];
    }
    const matchFiles: undefined | ((path: string, extensions: readonly string[] | undefined, excludes: readonly string[] | undefined, includes: readonly string[] | undefined, useCaseSensitiveFileNames: boolean, currentDirectory: string, depth: number | undefined, getFileSystemEntries: (path: string) => FileSystemEntries, realpath: (path: string) => string, directoryExists: (path: string) => boolean) => string[]);
}
/**
 * Implementation of a TypeScript parse config host that relies fully on
 * a given virtual file system.
 */
export declare class FileSystemHost implements ts.ParseConfigHost {
    private _fileSystem;
    useCaseSensitiveFileNames: boolean;
    constructor(_fileSystem: FileSystem);
    fileExists(path: string): boolean;
    readFile(path: string): string | undefined;
    readDirectory(rootDir: string, extensions: string[], excludes: string[] | undefined, includes: string[], depth?: number): string[];
    private _getFileSystemEntries;
}
/**
 * Creates a TypeScript compiler host that fully relies fully on the given
 * virtual file system. i.e. no interactions with the working directory.
 */
export declare function createFileSystemCompilerHost(options: ts.CompilerOptions, fileSystem: FileSystem): ts.CompilerHost;
/** Creates a format diagnostic host that works with the given file system. */
export declare function createFormatDiagnosticHost(fileSystem: FileSystem): ts.FormatDiagnosticsHost;
