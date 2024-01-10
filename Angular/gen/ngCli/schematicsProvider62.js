"use strict";
const schematic_command_1 = require("@angular/cli/models/schematic-command");
let command = new schematic_command_1.SchematicCommand({}, null);
let defaultCollectionName;
try {
    defaultCollectionName = require('@angular/cli/utilities/config').getDefaultSchematicCollection();
}
catch (e) {
    defaultCollectionName = require('@angular/cli/models/config').CliConfig.getValue('defaults.schematics.collection');
}
let engineHost = command.getEngineHost();
const schematicsProvider = Promise.resolve({
    getCollection(collectionName) {
        return command.getCollection(collectionName);
    },
    listSchematics(collection) {
        return engineHost.listSchematics(collection);
    },
    getSchematic(collection, schematicName, allowPrivate) {
        return command.getSchematic(collection, schematicName, allowPrivate);
    },
    getDefaultSchematicCollection() {
        return defaultCollectionName;
    }
});
module.exports = schematicsProvider;
