/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { workspaces } from '@angular-devkit/core';
import { Schema } from '@schematics/angular/component/schema';
import { Tree } from '@angular-devkit/schematics';
/**
 * Returns the default options for the `@schematics/angular:component` schematic which would
 * have been specified at project initialization (ng new or ng init).
 *
 * This is necessary because the Angular CLI only exposes the default values for the "--style",
 * "--inlineStyle", "--skipTests" and "--inlineTemplate" options to the "component" schematic.
 */
export declare function getDefaultComponentOptions(project: workspaces.ProjectDefinition): Partial<Schema>;
/** Determines whether the schematic is configured to be standalone. */
export declare function isStandaloneSchematic(host: Tree, options: Schema): Promise<boolean>;
