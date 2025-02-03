"use strict";
// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
Object.defineProperty(exports, "__esModule", { value: true });
const rerouteModulesToProject_1 = require("./rerouteModulesToProject");
const projectLocation = process.argv[2];
process.argv.splice(2, 1);
rerouteModulesToProject_1.rerouteModulesToProject(projectLocation, ["@angular/cli", "@angular-devkit/core", "@angular-devkit/schematics", "rxjs"]);
require("./generateVirtual");
