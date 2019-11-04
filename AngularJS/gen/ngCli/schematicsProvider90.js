"use strict";
const schematic_command_1 = require("@angular/cli/models/schematic-command");
const config_1 = require("@angular/cli/utilities/config");
const schematicsProvider = (async function () {
    let workspace = await config_1.getWorkspace('local');
    let command = new schematic_command_1.SchematicCommand({ workspace: workspace }, null, null);
    let engineHost = (await command.createWorkflow({ interactive: false })).engineHost;
    let defaultSchematicCollection = await command.getDefaultSchematicCollection();
    return {
        getCollection(collectionName) {
            return command.getCollection(collectionName);
        },
        listSchematics(collection) {
            return engineHost.listSchematicNames(collection.description);
        },
        getSchematic(collection, schematicName, allowPrivate) {
            return command.getSchematic(collection, schematicName, allowPrivate);
        },
        getDefaultSchematicCollection() {
            return defaultSchematicCollection;
        }
    };
})();
module.exports = schematicsProvider;
