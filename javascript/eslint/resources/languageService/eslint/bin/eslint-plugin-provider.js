"use strict";
exports.__esModule = true;
exports.factory = void 0;
var ESLintPluginFactory = /** @class */ (function () {
    function ESLintPluginFactory() {
    }
    ESLintPluginFactory.prototype.create = function (state) {
        if (state.standardPackagePath != null) {
            var dotIndex = state.linterPackageVersion.indexOf(".");
            var majorVersion = dotIndex > 0 ? state.linterPackageVersion.substring(0, dotIndex) : "";
            if (+majorVersion >= 17) {
                var Standard17Plugin = require('./standard17-plugin').Standard17Plugin;
                return { languagePlugin: new Standard17Plugin(state) };
            }
        }
        else {
            var dotIndex = state.linterPackageVersion.indexOf(".");
            var majorVersion = dotIndex > 0 ? state.linterPackageVersion.substring(0, dotIndex) : "";
            if (+majorVersion >= 8) {
                var ESLint8Plugin = require('./eslint8-plugin').ESLint8Plugin;
                return { languagePlugin: new ESLint8Plugin(state) };
            }
        }
        var ESLintPlugin = require('./eslint-plugin').ESLintPlugin;
        return { languagePlugin: new ESLintPlugin(state) };
    };
    return ESLintPluginFactory;
}());
var factory = new ESLintPluginFactory();
exports.factory = factory;
//# sourceMappingURL=eslint-plugin-provider.js.map