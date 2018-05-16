"use strict";
exports.__esModule = true;
var tslint_plugin_1 = require("./tslint-plugin");
var TSLintPluginFactory = /** @class */ (function () {
    function TSLintPluginFactory() {
    }
    TSLintPluginFactory.prototype.create = function (state) {
        return { languagePlugin: new tslint_plugin_1.TSLintPlugin(state) };
    };
    return TSLintPluginFactory;
}());
var factory = new TSLintPluginFactory();
exports.factory = factory;
