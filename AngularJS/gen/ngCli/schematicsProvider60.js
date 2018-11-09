"use strict";
var schematicsUtils = require('@angular/cli/utilities/schematics');
var defaultCollectionName;
try {
    defaultCollectionName = require('@angular/cli/utilities/config').getDefaultSchematicCollection();
}
catch (e) {
    defaultCollectionName = require('@angular/cli/models/config').CliConfig.getValue('defaults.schematics.collection');
}
var schematicsProvider = {
    getCollection: schematicsUtils.getCollection,
    getEngineHost: schematicsUtils.getEngineHost,
    getSchematic: schematicsUtils.getSchematic,
    getDefaultSchematicCollection: function () { return defaultCollectionName; }
};
module.exports = schematicsProvider;
