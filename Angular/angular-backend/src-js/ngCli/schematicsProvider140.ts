// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Collection, Schematic} from '@angular-devkit/schematics';
import {SchematicsProvider} from "./schematicsProvider";
import {findWorkspaceFile} from "@angular/cli/src/utilities/project"
import {AngularWorkspace, getWorkspaceRaw} from "@angular/cli/src/utilities/config"
import {
  DEFAULT_SCHEMATICS_COLLECTION,
  SchematicsCommandModule
} from "@angular/cli/src/command-builder/schematics-command-module";

async function getWorkspace() {
  const workspaceFile = findWorkspaceFile();
  if (workspaceFile === null) {
    const [, localPath] = getWorkspaceRaw('local');
    if (localPath !== null) {
      throw new Error(
        `An invalid configuration file was found ['${localPath}'].` +
        ' Please delete the file before running the command.',
      );
    }
  }
  else {
    try {
      return await AngularWorkspace.load(workspaceFile);
    }
    catch (e) {
      throw new Error(`Unable to read workspace file '${workspaceFile}': ${e.message}`);
    }
  }
}

const schematicsProvider: Promise<SchematicsProvider> = (async function () {
  let workspace = await getWorkspace()
  let command = new (SchematicsCommandModule as any)(
    {
      workspace: workspace,
      currentDirectory: process.cwd(),
      root: workspace?.basePath ?? process.cwd()
    }
  );

  return {
    getCollection(collectionName: string): Collection<any, any> {
      const workflow = command.getOrCreateWorkflowForBuilder(collectionName);
      return workflow.engine.createCollection(collectionName);
    },
    listSchematics(collection): string[] {
      return collection.listSchematicNames(false);
    },
    getSchematic(collection: Collection<any, any>, schematicName: string, allowPrivate?: boolean): Schematic<any, any> {
      return collection.createSchematic(schematicName, allowPrivate);
    },
    getDefaultSchematicCollection() {
      return DEFAULT_SCHEMATICS_COLLECTION;
    }
  }
})()

export = schematicsProvider;
