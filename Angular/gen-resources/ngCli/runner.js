"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const path = require("path");
rerouteModulesToProject("@angular/cli");
const projectLocation = process.argv[2];
const scriptToRun = process.argv[3];
process.argv.splice(1, 2);
process.chdir(projectLocation);
require(scriptToRun);
function rerouteModulesToProject(...modulePrefixes) {
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
