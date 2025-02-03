// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Collection, Schematic} from '@angular-devkit/schematics';
import {SchematicsProvider} from "./schematicsProvider";
import {SchematicCommand} from "@angular/cli/models/schematic-command";
import {getWorkspace} from "@angular/cli/utilities/config"

let workspace = getWorkspace() as any
if (!workspace || !workspace.root) throw new Error("Try 9.0 provider")

let command = new (SchematicCommand as any)({workspace}, null, null);
let engineHost = command.createWorkflow({interactive: false}).engineHost;

const schematicsProvider: Promise<SchematicsProvider> = Promise.resolve({
  getCollection(collectionName: string): Collection<any, any> {
    return command.getCollection(collectionName);
  },
  listSchematics(collection): string[] {
    return engineHost.listSchematics(collection)
  },
  getSchematic(collection: Collection<any, any>, schematicName: string, allowPrivate?: boolean): Schematic<any, any> {
    return command.getSchematic(collection, schematicName, allowPrivate);
  },
  getDefaultSchematicCollection() {
    return command.getDefaultSchematicCollection();
  }
})

export = schematicsProvider;
