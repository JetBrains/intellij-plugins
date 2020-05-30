// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {SchematicsProvider} from "./schematicsProvider";

let schematicsUtils = require('@angular/cli/utilities/schematics');

let defaultCollectionName;
try {
  defaultCollectionName = require('@angular/cli/utilities/config').getDefaultSchematicCollection();
} catch (e) {
  defaultCollectionName = require('@angular/cli/models/config').CliConfig.getValue('defaults.schematics.collection');
}
let engineHost = schematicsUtils.getEngineHost()

const schematicsProvider: Promise<SchematicsProvider> = Promise.resolve({
  getCollection: schematicsUtils.getCollection,
  getSchematic: schematicsUtils.getSchematic,
  listSchematics(collection): string[] {
    return engineHost.listSchematics(collection)
  },
  getDefaultSchematicCollection: () => defaultCollectionName
})

export = schematicsProvider;
