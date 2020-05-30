// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Collection, Schematic} from '@angular-devkit/schematics';
import {SchematicsProvider} from "./schematicsProvider";
import {SchematicCommand} from "@angular/cli/models/schematic-command";
import {getWorkspace} from "@angular/cli/utilities/config"

const schematicsProvider: Promise<SchematicsProvider> = (async function () {
  let workspace = await getWorkspace('local');
  let command = new (SchematicCommand as any)({workspace: workspace}, null, null);
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
