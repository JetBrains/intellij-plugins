"use strict";
exports.__esModule = true;
exports.requireResolveInContext = exports.requireInContext = exports.toUnixPathSeparators = exports.normalizePath = exports.containsString = void 0;
function containsString(src, toFind) {
    return src != null && src.indexOf(toFind) >= 0;
}
exports.containsString = containsString;
function normalizePath(eslintPackagePath) {
    if (eslintPackagePath === undefined)
        return undefined;
    if (eslintPackagePath.charAt(eslintPackagePath.length - 1) !== '/' &&
        eslintPackagePath.charAt(eslintPackagePath.length - 1) !== '\\') {
        eslintPackagePath = eslintPackagePath + '/';
    }
    return toUnixPathSeparators(eslintPackagePath);
}
exports.normalizePath = normalizePath;
function toUnixPathSeparators(path) {
    return path.split("\\").join("/");
}
exports.toUnixPathSeparators = toUnixPathSeparators;
function requireInContext(modulePathToRequire, contextPath) {
    var contextRequire = getContextRequire(contextPath);
    return contextRequire(modulePathToRequire);
}
exports.requireInContext = requireInContext;
function requireResolveInContext(modulePathToRequire, contextPath) {
    var contextRequire = getContextRequire(contextPath);
    return contextRequire.resolve(modulePathToRequire);
}
exports.requireResolveInContext = requireResolveInContext;
function getContextRequire(contextPath) {
    if (contextPath != null) {
        var module = require('module');
        if (typeof module.createRequire === 'function') {
            // https://nodejs.org/api/module.html#module_module_createrequire_filename
            // Implemented in Yarn PnP: https://next.yarnpkg.com/advanced/pnpapi/#requiremodule
            return module.createRequire(contextPath);
        }
        // noinspection JSDeprecatedSymbols
        if (typeof module.createRequireFromPath === 'function') {
            // Use createRequireFromPath (a deprecated version of createRequire) to support Node.js 10.x
            // noinspection JSDeprecatedSymbols
            return module.createRequireFromPath(contextPath);
        }
        throw Error('Function module.createRequire is unavailable in Node.js ' + process.version +
            ', Node.js >= 12.2.0 is required');
    }
    return require;
}
//# sourceMappingURL=eslint-common.js.map