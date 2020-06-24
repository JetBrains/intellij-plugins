// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Collection, Schematic} from '@angular-devkit/schematics';
import {SchematicsProvider} from "./schematicsProvider";
import {SchematicCommand} from "@angular/cli/models/schematic-command";

let command = new (SchematicCommand as any)({}, null);

let defaultCollectionName;
try {
  defaultCollectionName = require('@angular/cli/utilities/config').getDefaultSchematicCollection();
} catch (e) {
  defaultCollectionName = require('@angular/cli/models/config').CliConfig.getValue('defaults.schematics.collection');
}
let engineHost = command.getEngineHost();

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
    return defaultCollectionName;
  }
})

export = schematicsProvider;
