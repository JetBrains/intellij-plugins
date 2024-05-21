"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.getServiceScript = exports.notEmpty = void 0;
function notEmpty(value) {
    return value !== null && value !== undefined;
}
exports.notEmpty = notEmpty;
function getServiceScript(language, fileName) {
    const sourceScript = language.scripts.get(fileName);
    if (sourceScript?.generated) {
        const serviceScript = sourceScript.generated.languagePlugin.typescript?.getServiceScript(sourceScript.generated.root);
        if (serviceScript) {
            const map = language.maps.get(serviceScript.code, sourceScript.id);
            if (map) {
                return [serviceScript, sourceScript, map];
            }
        }
    }
    return [undefined, undefined, undefined];
}
exports.getServiceScript = getServiceScript;
//# sourceMappingURL=utils.js.map