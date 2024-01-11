/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { Path } from '@angular-devkit/core';
import { Tree, UpdateRecorder } from '@angular-devkit/schematics';
import { DirectoryEntry, FileSystem } from '../update-tool/file-system';
/**
 * File system that leverages the virtual tree from the CLI devkit. This file
 * system is commonly used by `ng update` migrations that run as part of the
 * Angular CLI.
 */
export declare class DevkitFileSystem extends FileSystem {
    private _tree;
    private _updateRecorderCache;
    constructor(_tree: Tree);
    resolve(...segments: string[]): Path;
    edit(filePath: Path): UpdateRecorder;
    commitEdits(): void;
    fileExists(filePath: Path): boolean;
    directoryExists(dirPath: Path): boolean;
    overwrite(filePath: Path, content: string): void;
    create(filePath: Path, content: string): void;
    delete(filePath: Path): void;
    read(filePath: Path): string | null;
    readDirectory(dirPath: Path): DirectoryEntry;
}
