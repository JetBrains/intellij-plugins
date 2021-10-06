// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
"use strict";
exports.__esModule = true;
exports.factory = void 0;
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
