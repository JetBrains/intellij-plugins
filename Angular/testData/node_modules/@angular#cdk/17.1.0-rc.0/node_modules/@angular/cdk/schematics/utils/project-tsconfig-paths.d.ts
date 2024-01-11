/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { workspaces } from '@angular-devkit/core';
import { Tree } from '@angular-devkit/schematics';
import { WorkspacePath } from '../update-tool/file-system';
/** Gets the tsconfig path from the given target within the specified project. */
export declare function getTargetTsconfigPath(project: workspaces.ProjectDefinition, targetName: string): WorkspacePath | null;
/** Resolve the workspace configuration of the specified tree gracefully. */
export declare function getWorkspaceConfigGracefully(tree: Tree): Promise<workspaces.WorkspaceDefinition | null>;
