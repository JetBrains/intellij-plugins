// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import * as path from "path";

export function rerouteModulesToProject(projectLocation, modulePrefixes: string[]) {
    const Module = require("module")
    const oldResolveLookupPaths = (Module as any)._resolveLookupPaths;
    (Module as any)._resolveLookupPaths = function _resolveLookupPaths(request, parent, newReturn) {
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