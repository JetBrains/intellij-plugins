"use strict";
const schematic_command_1 = require("@angular/cli/models/schematic-command");
const config_1 = require("@angular/cli/utilities/config");
let workspace = config_1.getWorkspace();
if (!workspace || !workspace.root)
    throw new Error("Try 9.0 provider");
let command = new schematic_command_1.SchematicCommand({ workspace }, null, null);
let engineHost = command.createWorkflow({ interactive: false }).engineHost;
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
        return command.getDefaultSchematicCollection();
    }
});
module.exports = schematicsProvider;
