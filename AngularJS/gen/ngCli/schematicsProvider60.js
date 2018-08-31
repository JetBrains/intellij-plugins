"use strict";
var schematicsUtils = require('@angular/cli/utilities/schematics');
var schematicsProvider = {
    getCollection: schematicsUtils.getCollection,
    getEngineHost: schematicsUtils.getEngineHost,
    getSchematic: schematicsUtils.getSchematic
};
module.exports = schematicsProvider;
