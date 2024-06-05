"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.getServiceScript = exports.notEmpty = void 0;
function notEmpty(value) {
    return value !== null && value !== undefined;
}
exports.notEmpty = notEmpty;
function getServiceScript(language, fileName) {
    let sourceScript = language.scripts.get(fileName);
    if (sourceScript?.targetIds && sourceScript?.targetIds.size > 0) {
        const sourceId = sourceScript.id;
        for (const targetId of sourceScript.targetIds) {
            sourceScript = language.scripts.get(targetId);
            if (sourceScript?.generated) {
                const serviceScript = sourceScript.generated.languagePlugin.typescript?.getServiceScript(sourceScript.generated.root);
                if (serviceScript) {
                    for (const [id, _snapshot, map] of language.maps.forEach(serviceScript.code)) {
                        if (id === sourceId) {
                            return [serviceScript, sourceScript, map];
                        }
                    }
                    break;
                }
            }
        }
    }
    if (sourceScript?.associatedOnly) {
        return [undefined, sourceScript, undefined];
    }
    if (sourceScript?.generated) {
        const serviceScript = sourceScript.generated.languagePlugin.typescript?.getServiceScript(sourceScript.generated.root);
        if (serviceScript) {
            const map = language.maps.get(serviceScript.code);
            if (map) {
                return [serviceScript, sourceScript, map];
            }
        }
    }
    return [undefined, undefined, undefined];
}
exports.getServiceScript = getServiceScript;
//# sourceMappingURL=utils.js.map