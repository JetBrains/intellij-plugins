"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.runTsc = exports.getLanguagePlugins = void 0;
const fs = require("fs");
let getLanguagePlugins = () => [];
exports.getLanguagePlugins = getLanguagePlugins;
function runTsc(tscPath, extensions, _getLanguagePlugins) {
    exports.getLanguagePlugins = _getLanguagePlugins;
    const proxyApiPath = require.resolve('../node/proxyCreateProgram');
    const readFileSync = fs.readFileSync;
    fs.readFileSync = (...args) => {
        if (args[0] === tscPath) {
            let tsc = readFileSync(...args);
            // add allow extensions
            const extsText = extensions.map(ext => `"${ext}"`).join(', ');
            tsc = replace(tsc, /supportedTSExtensions = .*(?=;)/, s => s + `.concat([[${extsText}]])`);
            tsc = replace(tsc, /supportedJSExtensions = .*(?=;)/, s => s + `.concat([[${extsText}]])`);
            tsc = replace(tsc, /allSupportedExtensions = .*(?=;)/, s => s + `.concat([[${extsText}]])`);
            // proxy createProgram
            tsc = replace(tsc, /function createProgram\(.+\) {/, s => `var createProgram = require(${JSON.stringify(proxyApiPath)}).proxyCreateProgram(`
                + [
                    `new Proxy({}, { get(_target, p, _receiver) { return eval(p); } } )`,
                    `_createProgram`,
                    `require(${JSON.stringify(__filename)}).getLanguagePlugins`,
                ].join(', ')
                + `);\n`
                + s.replace('createProgram', '_createProgram'));
            return tsc;
        }
        return readFileSync(...args);
    };
    try {
        require(tscPath);
    }
    finally {
        fs.readFileSync = readFileSync;
        delete require.cache[tscPath];
    }
}
exports.runTsc = runTsc;
function replace(text, ...[search, replace]) {
    const before = text;
    text = text.replace(search, replace);
    const after = text;
    if (after === before) {
        throw 'Search string not found: ' + JSON.stringify(search.toString());
    }
    return after;
}
//# sourceMappingURL=runTsc.js.map