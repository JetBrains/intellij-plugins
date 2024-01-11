/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import * as ts from 'typescript';
import { FileSystem, WorkspacePath } from '../file-system';
/** Class capturing a tsconfig parse error. */
export declare class TsconfigParseError extends Error {
}
/**
 * Attempts to parse the specified tsconfig file.
 *
 * @throws {TsconfigParseError} If the tsconfig could not be read or parsed.
 */
export declare function parseTsconfigFile(tsconfigPath: WorkspacePath, fileSystem: FileSystem): ts.ParsedCommandLine;
