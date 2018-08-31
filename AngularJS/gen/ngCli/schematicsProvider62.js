"use strict";
var schematic_command_1 = require("@angular/cli/models/schematic-command");
var command = new schematic_command_1.SchematicCommand(null, null);
var schematicsProvider = {
    getCollection: function (collectionName) {
        return command.getCollection(collectionName);
    },
    getEngineHost: function () {
        return command.getEngineHost();
    },
    getSchematic: function (collection, schematicName, allowPrivate) {
        return command.getSchematic(collection, schematicName, allowPrivate);
    }
};
module.exports = schematicsProvider;
