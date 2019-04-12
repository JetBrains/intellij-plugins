"use strict";
var schematic_command_1 = require("@angular/cli/models/schematic-command");
var config_1 = require("@angular/cli/utilities/config");
var command = new schematic_command_1.SchematicCommand({ workspace: config_1.getWorkspace() }, null, null);
var schematicsProvider = {
    getCollection: function (collectionName) {
        return command.getCollection(collectionName);
    },
    getEngineHost: function () {
        return command.createWorkflow({ interactive: false }).engineHost;
    },
    getSchematic: function (collection, schematicName, allowPrivate) {
        return command.getSchematic(collection, schematicName, allowPrivate);
    },
    getDefaultSchematicCollection: function () {
        return command.getDefaultSchematicCollection();
    }
};
module.exports = schematicsProvider;
