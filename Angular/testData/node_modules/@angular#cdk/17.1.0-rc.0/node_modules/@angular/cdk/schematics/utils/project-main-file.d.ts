/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { Path, workspaces } from '@angular-devkit/core';
/** Looks for the main TypeScript file in the given project and returns its path. */
export declare function getProjectMainFile(project: workspaces.ProjectDefinition): Path;
