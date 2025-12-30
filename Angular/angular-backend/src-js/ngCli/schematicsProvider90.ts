// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Collection, Schematic} from '@angular-devkit/schematics';
import {SchematicsProvider} from "./schematicsProvider";
import {SchematicCommand} from "@angular/cli/models/schematic-command";
import {findWorkspaceFile} from "@angular/cli/utilities/project"
import {AngularWorkspace, getWorkspaceRaw} from "@angular/cli/utilities/config"

const getWorkspaceDetails = require("@angular/cli/utilities/project").getWorkspaceDetails

async function getWorkspace() {
  if (getWorkspaceDetails) {
    // Angular 9-10
    return await getWorkspaceDetails()
  }
  // Angular 11+
  let workspace;
  const workspaceFile = findWorkspaceFile();
  if (workspaceFile === null) {
    const [, localPath] = getWorkspaceRaw('local');
    if (localPath !== null) {
      throw new Error(
        `An invalid configuration file was found ['${localPath}'].` +
        ' Please delete the file before running the command.',
      );
    }
  } else {
    try {
      return await AngularWorkspace.load(workspaceFile);
    } catch (e) {
      throw new Error(`Unable to read workspace file '${workspaceFile}': ${e.message}`);
    }
  }
}

const schematicsProvider: Promise<SchematicsProvider> = (async function () {
  let workspace = await getWorkspace()
  let command = new (SchematicCommand as any)({
    workspace: workspace,
    currentDirectory: process.cwd(),
    root: workspace?.basePath ?? process.cwd()
  }, null, null);
  let {listSchematicNames} = (await command.createWorkflow({interactive: false})).engineHost;
  let defaultSchematicCollection = await command.getDefaultSchematicCollection()

  return {
    getCollection(collectionName: string): Collection<any, any> {
      return command.getCollection(collectionName);
    },
    listSchematics(collection): string[] {
      return listSchematicNames(collection.description)
    },
    getSchematic(collection: Collection<any, any>, schematicName: string, allowPrivate?: boolean): Schematic<any, any> {
      return command.getSchematic(collection, schematicName, allowPrivate);
    },
    getDefaultSchematicCollection() {
      return defaultSchematicCollection;
    }
  }
})()

export = schematicsProvider;
