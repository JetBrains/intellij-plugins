"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
var path = require("path");
rerouteModulesToProject("@angular/cli");
var projectLocation = process.argv[2];
var scriptToRun = process.argv[3];
process.argv.splice(1, 2);
process.chdir(projectLocation);
require(scriptToRun);
function rerouteModulesToProject() {
    var modulePrefixes = [];
    for (var _i = 0; _i < arguments.length; _i++) {
        modulePrefixes[_i] = arguments[_i];
    }
    var Module = require("module");
    var oldResolveLookupPaths = Module._resolveLookupPaths;
    Module._resolveLookupPaths = function _resolveLookupPaths(request, parent, newReturn) {
        var result = oldResolveLookupPaths(request, parent, newReturn);
        //reroute @angular/cli includes to the project location
        for (var _i = 0, modulePrefixes_1 = modulePrefixes; _i < modulePrefixes_1.length; _i++) {
            var prefix = modulePrefixes_1[_i];
            if (request.startsWith(prefix)) {
                var projectNodeModules = path.resolve(projectLocation, "node_modules");
                return newReturn || result.length > 2 || (result.length === 2 && !Array.isArray(result[1]))
                    ? [projectNodeModules]
                    : [result[0], [projectNodeModules]];
            }
        }
        return result;
    };
}
