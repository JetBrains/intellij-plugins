"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.rerouteModulesToProject = void 0;
// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
const path = require("path");
function rerouteModulesToProject(projectLocation, modulePrefixes) {
    const Module = require("module");
    const oldResolveLookupPaths = Module._resolveLookupPaths;
    Module._resolveLookupPaths = function _resolveLookupPaths(request, parent, newReturn) {
        const result = oldResolveLookupPaths(request, parent, newReturn);
        //reroute @angular/cli includes to the project location
        for (const prefix of modulePrefixes) {
            if (request.startsWith(prefix)) {
                const projectNodeModules = path.resolve(projectLocation, "node_modules");
                return newReturn || result.length > 2 || (result.length === 2 && !Array.isArray(result[1]))
                    ? [projectNodeModules]
                    : [result[0], [projectNodeModules]];
            }
        }
        return result;
    };
}
exports.rerouteModulesToProject = rerouteModulesToProject;
