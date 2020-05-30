"use strict";
let schematicsUtils = require('@angular/cli/utilities/schematics');
let defaultCollectionName;
try {
    defaultCollectionName = require('@angular/cli/utilities/config').getDefaultSchematicCollection();
}
catch (e) {
    defaultCollectionName = require('@angular/cli/models/config').CliConfig.getValue('defaults.schematics.collection');
}
let engineHost = schematicsUtils.getEngineHost();
const schematicsProvider = Promise.resolve({
    getCollection: schematicsUtils.getCollection,
    getSchematic: schematicsUtils.getSchematic,
    listSchematics(collection) {
        return engineHost.listSchematics(collection);
    },
    getDefaultSchematicCollection: () => defaultCollectionName
});
module.exports = schematicsProvider;
