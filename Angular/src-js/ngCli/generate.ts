// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import {rerouteModulesToProject} from "./rerouteModulesToProject";

const projectLocation = process.argv[2];
process.argv.splice(2, 1);

rerouteModulesToProject(projectLocation, ["@angular/cli", "@angular-devkit/core", "@angular-devkit/schematics", "rxjs"]);


require("./generateVirtual")