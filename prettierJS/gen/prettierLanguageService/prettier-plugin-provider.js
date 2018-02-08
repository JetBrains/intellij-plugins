"use strict";
exports.__esModule = true;
var prettier_plugin_1 = require("./prettier-plugin");
var PluginFactory = /** @class */ (function () {
    function PluginFactory() {
    }
    PluginFactory.prototype.create = function (state) {
        return { languagePlugin: new prettier_plugin_1.PrettierPlugin() };
    };
    return PluginFactory;
}());
var factory = new PluginFactory();
exports.factory = factory;
