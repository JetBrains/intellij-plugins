/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { workspaces } from '@angular-devkit/core';
/**
 * Finds the specified project configuration in the workspace. Throws an error if the project
 * couldn't be found.
 */
export declare function getProjectFromWorkspace(workspace: workspaces.WorkspaceDefinition, projectName: string | undefined): workspaces.ProjectDefinition;
