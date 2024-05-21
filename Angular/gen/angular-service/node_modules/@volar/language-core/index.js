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
exports.forEachEmbeddedCode = exports.updateVirtualCodeMapOfMap = exports.createLanguage = void 0;
__exportStar(require("@volar/source-map"), exports);
__exportStar(require("./lib/editorFeatures"), exports);
__exportStar(require("./lib/linkedCodeMap"), exports);
__exportStar(require("./lib/types"), exports);
__exportStar(require("./lib/utils"), exports);
const source_map_1 = require("@volar/source-map");
const linkedCodeMap_1 = require("./lib/linkedCodeMap");
const utils_1 = require("./lib/utils");
function createLanguage(plugins, caseSensitive, sync) {
    const sourceScripts = new utils_1.FileMap(caseSensitive);
    const virtualCodeToSourceFileMap = new WeakMap();
    const virtualCodeToMaps = new WeakMap();
    const virtualCodeToLinkedCodeMap = new WeakMap();
    return {
        plugins,
        scripts: {
            get(id) {
                sync(id);
                return sourceScripts.get(id);
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
                if (sourceScripts.has(id)) {
                    const sourceScript = sourceScripts.get(id);
                    if (sourceScript.languageId !== languageId) {
                        // languageId changed
                        this.delete(id);
                        return this.set(id, snapshot, languageId);
                    }
                    else if (sourceScript.snapshot !== snapshot) {
                        // snapshot updated
                        sourceScript.snapshot = snapshot;
                        if (sourceScript.generated) {
                            const newVirtualCode = sourceScript.generated.languagePlugin.updateVirtualCode?.(id, sourceScript.generated.root, snapshot);
                            if (newVirtualCode) {
                                sourceScript.generated.root = newVirtualCode;
                                sourceScript.generated.embeddedCodes.clear();
                                for (const code of forEachEmbeddedCode(sourceScript.generated.root)) {
                                    virtualCodeToSourceFileMap.set(code, sourceScript);
                                    sourceScript.generated.embeddedCodes.set(code.id, code);
                                }
                                return sourceScript;
                            }
                            else {
                                this.delete(id);
                                return;
                            }
                        }
                    }
                    else {
                        // not changed
                        return sourceScript;
                    }
                }
                else {
                    // created
                    const sourceScript = { id, languageId, snapshot };
                    sourceScripts.set(id, sourceScript);
                    for (const languagePlugin of _plugins) {
                        const virtualCode = languagePlugin.createVirtualCode?.(id, languageId, snapshot);
                        if (virtualCode) {
                            sourceScript.generated = {
                                root: virtualCode,
                                languagePlugin,
                                embeddedCodes: new Map(),
                            };
                            for (const code of forEachEmbeddedCode(virtualCode)) {
                                virtualCodeToSourceFileMap.set(code, sourceScript);
                                sourceScript.generated.embeddedCodes.set(code.id, code);
                            }
                            break;
                        }
                    }
                    return sourceScript;
                }
            },
            delete(id) {
                const value = sourceScripts.get(id);
                if (value) {
                    if (value.generated) {
                        value.generated.languagePlugin.disposeVirtualCode?.(id, value.generated.root);
                    }
                    sourceScripts.delete(id);
                }
            },
        },
        maps: {
            get(virtualCode, scriptId) {
                if (!scriptId) {
                    const sourceScript = virtualCodeToSourceFileMap.get(virtualCode);
                    if (!sourceScript) {
                        return;
                    }
                    scriptId = sourceScript.id;
                }
                for (const [id, [_snapshot, map]] of this.forEach(virtualCode)) {
                    if (id === scriptId) {
                        return map;
                    }
                }
            },
            forEach(virtualCode) {
                let map = virtualCodeToMaps.get(virtualCode.snapshot);
                if (!map) {
                    map = new Map();
                    virtualCodeToMaps.set(virtualCode.snapshot, map);
                }
                updateVirtualCodeMapOfMap(virtualCode, map, id => {
                    if (id) {
                        const sourceScript = sourceScripts.get(id);
                        return [id, sourceScript.snapshot];
                    }
                    else {
                        const sourceScript = virtualCodeToSourceFileMap.get(virtualCode);
                        return [sourceScript.id, sourceScript.snapshot];
                    }
                });
                return map;
            },
        },
        linkedCodeMaps: {
            get(virtualCode) {
                if (!virtualCodeToLinkedCodeMap.has(virtualCode.snapshot)) {
                    virtualCodeToLinkedCodeMap.set(virtualCode.snapshot, virtualCode.linkedCodeMappings
                        ? new linkedCodeMap_1.LinkedCodeMap(virtualCode.linkedCodeMappings)
                        : undefined);
                }
                return virtualCodeToLinkedCodeMap.get(virtualCode.snapshot);
            },
        },
    };
}
exports.createLanguage = createLanguage;
function updateVirtualCodeMapOfMap(virtualCode, mapOfMap, getSourceSnapshot) {
    const sources = new Set();
    if (!virtualCode.mappings.length) {
        const source = getSourceSnapshot(undefined);
        if (source) {
            mapOfMap.set(source[0], [source[1], new source_map_1.SourceMap([])]);
        }
    }
    for (const mapping of virtualCode.mappings) {
        if (sources.has(mapping.source)) {
            continue;
        }
        sources.add(mapping.source);
        const source = getSourceSnapshot(mapping.source);
        if (!source) {
            continue;
        }
        if (!mapOfMap.has(source[0]) || mapOfMap.get(source[0])[0] !== source[1]) {
            mapOfMap.set(source[0], [source[1], new source_map_1.SourceMap(virtualCode.mappings.filter(mapping2 => mapping2.source === mapping.source))]);
        }
    }
}
exports.updateVirtualCodeMapOfMap = updateVirtualCodeMapOfMap;
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