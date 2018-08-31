// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {SchematicsProvider} from "./schematicsProvider";

let schematicsUtils = require('@angular/cli/utilities/schematics');

const schematicsProvider: SchematicsProvider = {
    getCollection: schematicsUtils.getCollection,
    getEngineHost: schematicsUtils.getEngineHost,
    getSchematic: schematicsUtils.getSchematic
}

export = schematicsProvider;