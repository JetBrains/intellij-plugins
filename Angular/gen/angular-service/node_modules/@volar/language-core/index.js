"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __exportStar = (this && this.__exportStar) || function(m, exports) {
    for (var p in m) if (p !== "default" && !Object.prototype.hasOwnProperty.call(exports, p)) __createBinding(exports, m, p);
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.forEachEmbeddedCode = exports.createLanguage = void 0;
__exportStar(require("@volar/source-map"), exports);
__exportStar(require("./lib/editorFeatures"), exports);
__exportStar(require("./lib/linkedCodeMap"), exports);
__exportStar(require("./lib/types"), exports);
__exportStar(require("./lib/utils"), exports);
const source_map_1 = require("@volar/source-map");
const linkedCodeMap_1 = require("./lib/linkedCodeMap");
function createLanguage(plugins, scriptRegistry, sync) {
    const virtualCodeToSourceScriptMap = new WeakMap();
    const virtualCodeToSourceMap = new WeakMap();
    const virtualCodeToLinkedCodeMap = new WeakMap();
    return {
        plugins,
        scripts: {
            fromVirtualCode(virtualCode) {
                return virtualCodeToSourceScriptMap.get(virtualCode);
            },
            get(id) {
                sync(id);
                const result = scriptRegistry.get(id);
                if (result?.isAssociationDirty) {
                    this.set(id, result.snapshot, result.languageId);
                }
                return scriptRegistry.get(id);
            },
            set(id, snapshot, languageId, _plugins = plugins) {
                if (!languageId) {
                    for (const plugin of plugins) {
                        languageId = plugin.getLanguageId?.(id);
                        if (languageId) {
                            break;
                        }
                    }
                }
                if (!languageId) {
                    console.warn(`languageId not found for ${id}`);
                    return;
                }
                let associatedOnly = false;
                for (const plugin of plugins) {
                    if (plugin.isAssociatedFileOnly?.(id, languageId)) {
                        associatedOnly = true;
                        break;
                    }
                }
                if (scriptRegistry.has(id)) {
                    const sourceScript = scriptRegistry.get(id);
                    if (sourceScript.languageId !== languageId || sourceScript.associatedOnly !== associatedOnly) {
                        this.delete(id);
                        return this.set(id, snapshot, languageId);
                    }
                    else if (associatedOnly) {
                        sourceScript.snapshot = snapshot;
                    }
                    else if (sourceScript.isAssociationDirty || sourceScript.snapshot !== snapshot) {
                        // snapshot updated
                        sourceScript.snapshot = snapshot;
                        const codegenCtx = prepareCreateVirtualCode(sourceScript);
                        if (sourceScript.generated) {
                            const { updateVirtualCode, createVirtualCode } = sourceScript.generated.languagePlugin;
                            const newVirtualCode = updateVirtualCode
                                ? updateVirtualCode(id, sourceScript.generated.root, snapshot, codegenCtx)
                                : createVirtualCode?.(id, languageId, snapshot, codegenCtx);
                            if (newVirtualCode) {
                                sourceScript.generated.root = newVirtualCode;
                                sourceScript.generated.embeddedCodes.clear();
                                for (const code of forEachEmbeddedCode(sourceScript.generated.root)) {
                                    virtualCodeToSourceScriptMap.set(code, sourceScript);
                                    sourceScript.generated.embeddedCodes.set(code.id, code);
                                }
                                return sourceScript;
                            }
                            else {
                                this.delete(id);
                                return;
                            }
                        }
                        triggerTargetsDirty(sourceScript);
                    }
                    else {
                        // not changed
                        return sourceScript;
                    }
                }
                else {
                    // created
                    const sourceScript = {
                        id: id,
                        languageId,
                        snapshot,
                        associatedIds: new Set(),
                        targetIds: new Set(),
                        associatedOnly
                    };
                    scriptRegistry.set(id, sourceScript);
                    if (associatedOnly) {
                        return sourceScript;
                    }
                    for (const languagePlugin of _plugins) {
                        const virtualCode = languagePlugin.createVirtualCode?.(id, languageId, snapshot, prepareCreateVirtualCode(sourceScript));
                        if (virtualCode) {
                            sourceScript.generated = {
                                root: virtualCode,
                                languagePlugin,
                                embeddedCodes: new Map(),
                            };
                            for (const code of forEachEmbeddedCode(virtualCode)) {
                                virtualCodeToSourceScriptMap.set(code, sourceScript);
                                sourceScript.generated.embeddedCodes.set(code.id, code);
                            }
                            break;
                        }
                    }
                    return sourceScript;
                }
            },
            delete(id) {
                const sourceScript = scriptRegistry.get(id);
                if (sourceScript) {
                    sourceScript.generated?.languagePlugin.disposeVirtualCode?.(id, sourceScript.generated.root);
                    scriptRegistry.delete(id);
                    triggerTargetsDirty(sourceScript);
                }
            },
        },
        maps: {
            get(virtualCode) {
                for (const map of this.forEach(virtualCode)) {
                    return map[2];
                }
                throw `no map found for ${virtualCode.id}`;
            },
            *forEach(virtualCode) {
                let mapCache = virtualCodeToSourceMap.get(virtualCode.snapshot);
                if (!mapCache) {
                    virtualCodeToSourceMap.set(virtualCode.snapshot, mapCache = new WeakMap());
                }
                const sourceScript = virtualCodeToSourceScriptMap.get(virtualCode);
                if (!mapCache.has(sourceScript.snapshot)) {
                    const allMappings = [];
                    virtualCode.mappings.forEach(mapping => allMappings.push({
                        ...mapping,
                        source: sourceScript.id
                    }));
                    if (virtualCode.associatedScriptMappings) {
                        for (const [relatedScriptId, relatedMappings] of virtualCode.associatedScriptMappings) {
                            relatedMappings.forEach(mapping => allMappings.push({
                                ...mapping,
                                source: relatedScriptId
                            }));
                        }
                    }
                    mapCache.set(sourceScript.snapshot, new source_map_1.SourceMap(allMappings, sourceScript.id));
                }
                yield [sourceScript.id, sourceScript.snapshot, mapCache.get(sourceScript.snapshot)];
                if (virtualCode.associatedScriptMappings) {
                    for (const [relatedScriptId, relatedMappings] of virtualCode.associatedScriptMappings) {
                        const relatedSourceScript = scriptRegistry.get(relatedScriptId);
                        if (relatedSourceScript) {
                            if (!mapCache.has(relatedSourceScript.snapshot)) {
                                mapCache.set(relatedSourceScript.snapshot, new source_map_1.SourceMap(relatedMappings));
                            }
                            yield [relatedSourceScript.id, relatedSourceScript.snapshot, mapCache.get(relatedSourceScript.snapshot)];
                        }
                    }
                }
            },
        },
        linkedCodeMaps: {
            get(virtualCode) {
                const sourceScript = virtualCodeToSourceScriptMap.get(virtualCode);
                let mapCache = virtualCodeToLinkedCodeMap.get(virtualCode.snapshot);
                if (mapCache?.[0] !== sourceScript.snapshot) {
                    virtualCodeToLinkedCodeMap.set(virtualCode.snapshot, mapCache = [
                        sourceScript.snapshot,
                        virtualCode.linkedCodeMappings
                            ? new linkedCodeMap_1.LinkedCodeMap(virtualCode.linkedCodeMappings)
                            : undefined,
                    ]);
                }
                return mapCache[1];
            },
        },
    };
    function triggerTargetsDirty(sourceScript) {
        sourceScript.targetIds.forEach(id => {
            const sourceScript = scriptRegistry.get(id);
            if (sourceScript) {
                sourceScript.isAssociationDirty = true;
            }
        });
    }
    function prepareCreateVirtualCode(sourceScript) {
        for (const id of sourceScript.associatedIds) {
            scriptRegistry.get(id)?.targetIds.delete(sourceScript.id);
        }
        sourceScript.associatedIds.clear();
        sourceScript.isAssociationDirty = false;
        return {
            getAssociatedScript(id) {
                sync(id);
                const relatedSourceScript = scriptRegistry.get(id);
                if (relatedSourceScript) {
                    relatedSourceScript.targetIds.add(sourceScript.id);
                    sourceScript.associatedIds.add(relatedSourceScript.id);
                }
                return relatedSourceScript;
            },
        };
    }
}
exports.createLanguage = createLanguage;
function* forEachEmbeddedCode(virtualCode) {
    yield virtualCode;
    if (virtualCode.embeddedCodes) {
        for (const embeddedCode of virtualCode.embeddedCodes) {
            yield* forEachEmbeddedCode(embeddedCode);
        }
    }
}
exports.forEachEmbeddedCode = forEachEmbeddedCode;
//# sourceMappingURL=index.js.map