"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.createTypeScriptLanguage = void 0;
const language_core_1 = require("@volar/language-core");
const language_core_2 = require("@volar/language-core");
const path = require("path-browserify");
const resolveModuleName_1 = require("../resolveModuleName");
const common_1 = require("../common");
const scriptVersions = new Map();
const fsFileSnapshots = new Map();
function createTypeScriptLanguage(ts, languagePlugins, projectHost) {
    const language = (0, language_core_1.createLanguage)([
        ...languagePlugins,
        common_1.fileLanguageIdProviderPlugin,
    ], projectHost.useCaseSensitiveFileNames, scriptId => {
        const fileName = projectHost.scriptIdToFileName(scriptId);
        // opened files
        let snapshot = projectHost.getScriptSnapshot(fileName);
        if (!snapshot) {
            // fs files
            const cache = fsFileSnapshots.get(fileName);
            const modifiedTime = projectHost.getModifiedTime?.(fileName)?.valueOf();
            if (!cache || cache[0] !== modifiedTime) {
                if (projectHost.fileExists(fileName)) {
                    const text = projectHost.readFile(fileName);
                    const snapshot = text !== undefined ? ts.ScriptSnapshot.fromString(text) : undefined;
                    fsFileSnapshots.set(fileName, [modifiedTime, snapshot]);
                }
                else {
                    fsFileSnapshots.set(fileName, [modifiedTime, undefined]);
                }
            }
            snapshot = fsFileSnapshots.get(fileName)?.[1];
        }
        if (snapshot) {
            language.scripts.set(scriptId, snapshot);
        }
        else {
            language.scripts.delete(scriptId);
        }
    });
    let { languageServiceHost, getExtraScript } = createLanguageServiceHost();
    for (const language of languagePlugins) {
        if (language.typescript?.resolveLanguageServiceHost) {
            languageServiceHost = language.typescript.resolveLanguageServiceHost(languageServiceHost);
        }
    }
    if (languagePlugins.some(language => language.typescript?.extraFileExtensions.length)) {
        // TODO: can this share between monorepo packages?
        const moduleCache = ts.createModuleResolutionCache(languageServiceHost.getCurrentDirectory(), languageServiceHost.useCaseSensitiveFileNames?.() ? s => s : s => s.toLowerCase(), languageServiceHost.getCompilationSettings());
        const resolveModuleName = (0, resolveModuleName_1.createResolveModuleName)(ts, languageServiceHost, languagePlugins, fileName => language.scripts.get(projectHost.fileNameToScriptId(fileName)));
        let lastSysVersion = projectHost.getSystemVersion?.();
        languageServiceHost.resolveModuleNameLiterals = (moduleLiterals, containingFile, redirectedReference, options, sourceFile) => {
            if (projectHost.getSystemVersion && lastSysVersion !== projectHost.getSystemVersion()) {
                lastSysVersion = projectHost.getSystemVersion();
                moduleCache.clear();
            }
            return moduleLiterals.map(moduleLiteral => {
                return resolveModuleName(moduleLiteral.text, containingFile, options, moduleCache, redirectedReference, sourceFile.impliedNodeFormat);
            });
        };
        languageServiceHost.resolveModuleNames = (moduleNames, containingFile, _reusedNames, redirectedReference, options) => {
            if (projectHost.getSystemVersion && lastSysVersion !== projectHost.getSystemVersion()) {
                lastSysVersion = projectHost.getSystemVersion();
                moduleCache.clear();
            }
            return moduleNames.map(moduleName => {
                return resolveModuleName(moduleName, containingFile, options, moduleCache, redirectedReference).resolvedModule;
            });
        };
    }
    language.typescript = {
        projectHost,
        languageServiceHost,
        getExtraServiceScript: getExtraScript,
    };
    return language;
    function createLanguageServiceHost() {
        let lastProjectVersion;
        let tsProjectVersion = 0;
        let tsFileRegistry = new language_core_1.FileMap(projectHost.useCaseSensitiveFileNames);
        let extraScriptRegistry = new language_core_1.FileMap(projectHost.useCaseSensitiveFileNames);
        let lastTsVirtualFileSnapshots = new Set();
        let lastOtherVirtualFileSnapshots = new Set();
        const languageServiceHost = {
            ...projectHost,
            getCurrentDirectory: projectHost.getCurrentDirectory,
            getCompilationSettings() {
                const options = projectHost.getCompilationSettings();
                if (languagePlugins.some(language => language.typescript?.extraFileExtensions.length)) {
                    options.allowNonTsExtensions ??= true;
                    if (!options.allowNonTsExtensions) {
                        console.warn('`allowNonTsExtensions` must be `true`.');
                    }
                }
                return options;
            },
            getLocalizedDiagnosticMessages: projectHost.getLocalizedDiagnosticMessages,
            getProjectReferences: projectHost.getProjectReferences,
            getDefaultLibFileName: options => {
                try {
                    return ts.getDefaultLibFilePath(options);
                }
                catch {
                    // web
                    return `/node_modules/typescript/lib/${ts.getDefaultLibFileName(options)}`;
                }
            },
            useCaseSensitiveFileNames() {
                return projectHost.useCaseSensitiveFileNames;
            },
            getNewLine() {
                return projectHost.newLine;
            },
            getTypeRootsVersion: () => {
                return projectHost.getSystemVersion?.() ?? -1; // TODO: only update for /node_modules changes?
            },
            getDirectories(dirName) {
                return projectHost.getDirectories(dirName);
            },
            readDirectory(dirName, extensions, excludes, includes, depth) {
                const exts = new Set(extensions);
                for (const languagePlugin of languagePlugins) {
                    for (const ext of languagePlugin.typescript?.extraFileExtensions ?? []) {
                        exts.add('.' + ext.extension);
                    }
                }
                extensions = [...exts];
                return projectHost.readDirectory(dirName, extensions, excludes, includes, depth);
            },
            readFile(fileName) {
                const snapshot = getScriptSnapshot(fileName);
                if (snapshot) {
                    return snapshot.getText(0, snapshot.getLength());
                }
            },
            fileExists(fileName) {
                return getScriptVersion(fileName) !== '';
            },
            getProjectVersion() {
                sync();
                return tsProjectVersion + (projectHost.getSystemVersion ? `:${projectHost.getSystemVersion()}` : '');
            },
            getScriptFileNames() {
                sync();
                return [...tsFileRegistry.keys()];
            },
            getScriptKind(fileName) {
                sync();
                if (extraScriptRegistry.has(fileName)) {
                    return extraScriptRegistry.get(fileName).scriptKind;
                }
                const sourceScript = language.scripts.get(projectHost.fileNameToScriptId(fileName));
                if (sourceScript?.generated) {
                    const serviceScript = sourceScript.generated.languagePlugin.typescript?.getServiceScript(sourceScript.generated.root);
                    if (serviceScript) {
                        return serviceScript.scriptKind;
                    }
                }
                switch (path.extname(fileName)) {
                    case '.js':
                    case '.cjs':
                    case '.mjs':
                        return ts.ScriptKind.JS;
                    case '.jsx':
                        return ts.ScriptKind.JSX;
                    case '.ts':
                    case '.cts':
                    case '.mts':
                        return ts.ScriptKind.TS;
                    case '.tsx':
                        return ts.ScriptKind.TSX;
                    case '.json':
                        return ts.ScriptKind.JSON;
                    default:
                        return ts.ScriptKind.Unknown;
                }
            },
            getScriptVersion,
            getScriptSnapshot,
        };
        return {
            languageServiceHost,
            getExtraScript,
        };
        function getExtraScript(fileName) {
            sync();
            return extraScriptRegistry.get(fileName);
        }
        function sync() {
            const newProjectVersion = projectHost.getProjectVersion?.();
            const shouldUpdate = newProjectVersion === undefined || newProjectVersion !== lastProjectVersion;
            if (!shouldUpdate) {
                return;
            }
            lastProjectVersion = newProjectVersion;
            extraScriptRegistry.clear();
            const newTsVirtualFileSnapshots = new Set();
            const newOtherVirtualFileSnapshots = new Set();
            const tsFileNamesSet = new Set();
            for (const fileName of projectHost.getScriptFileNames()) {
                const sourceScript = language.scripts.get(projectHost.fileNameToScriptId(fileName));
                if (sourceScript?.generated) {
                    const serviceScript = sourceScript.generated.languagePlugin.typescript?.getServiceScript(sourceScript.generated.root);
                    if (serviceScript) {
                        newTsVirtualFileSnapshots.add(serviceScript.code.snapshot);
                        tsFileNamesSet.add(fileName);
                    }
                    for (const extraServiceScript of sourceScript.generated.languagePlugin.typescript?.getExtraServiceScripts?.(fileName, sourceScript.generated.root) ?? []) {
                        newTsVirtualFileSnapshots.add(extraServiceScript.code.snapshot);
                        tsFileNamesSet.add(extraServiceScript.fileName);
                        extraScriptRegistry.set(extraServiceScript.fileName, extraServiceScript);
                    }
                    for (const code of (0, language_core_2.forEachEmbeddedCode)(sourceScript.generated.root)) {
                        newOtherVirtualFileSnapshots.add(code.snapshot);
                    }
                }
                else {
                    tsFileNamesSet.add(fileName);
                }
            }
            if (!setEquals(lastTsVirtualFileSnapshots, newTsVirtualFileSnapshots)) {
                tsProjectVersion++;
            }
            else if (setEquals(lastOtherVirtualFileSnapshots, newOtherVirtualFileSnapshots)) {
                // no any meta language files update, it mean project version was update by source files this time
                tsProjectVersion++;
            }
            lastTsVirtualFileSnapshots = newTsVirtualFileSnapshots;
            lastOtherVirtualFileSnapshots = newOtherVirtualFileSnapshots;
            tsFileRegistry.clear();
            for (const fileName of tsFileNamesSet) {
                tsFileRegistry.set(fileName, true);
            }
        }
        function getScriptSnapshot(fileName) {
            sync();
            if (extraScriptRegistry.has(fileName)) {
                return extraScriptRegistry.get(fileName).code.snapshot;
            }
            const sourceScript = language.scripts.get(projectHost.fileNameToScriptId(fileName));
            if (sourceScript?.generated) {
                const serviceScript = sourceScript.generated.languagePlugin.typescript?.getServiceScript(sourceScript.generated.root);
                if (serviceScript) {
                    return serviceScript.code.snapshot;
                }
            }
            else if (sourceScript) {
                return sourceScript.snapshot;
            }
        }
        function getScriptVersion(fileName) {
            sync();
            if (!scriptVersions.has(fileName)) {
                scriptVersions.set(fileName, { lastVersion: 0, map: new WeakMap() });
            }
            const version = scriptVersions.get(fileName);
            if (extraScriptRegistry.has(fileName)) {
                const snapshot = extraScriptRegistry.get(fileName).code.snapshot;
                if (!version.map.has(snapshot)) {
                    version.map.set(snapshot, version.lastVersion++);
                }
                return version.map.get(snapshot).toString();
            }
            const sourceScript = language.scripts.get(projectHost.fileNameToScriptId(fileName));
            if (sourceScript?.generated) {
                const serviceScript = sourceScript.generated.languagePlugin.typescript?.getServiceScript(sourceScript.generated.root);
                if (serviceScript) {
                    if (!version.map.has(serviceScript.code.snapshot)) {
                        version.map.set(serviceScript.code.snapshot, version.lastVersion++);
                    }
                    return version.map.get(serviceScript.code.snapshot).toString();
                }
            }
            const isOpenedFile = !!projectHost.getScriptSnapshot(fileName);
            if (isOpenedFile) {
                const sourceScript = language.scripts.get(projectHost.fileNameToScriptId(fileName));
                if (sourceScript && !sourceScript.generated) {
                    if (!version.map.has(sourceScript.snapshot)) {
                        version.map.set(sourceScript.snapshot, version.lastVersion++);
                    }
                    return version.map.get(sourceScript.snapshot).toString();
                }
            }
            if (projectHost.fileExists(fileName)) {
                return projectHost.getModifiedTime?.(fileName)?.valueOf().toString() ?? '0';
            }
            return '';
        }
    }
}
exports.createTypeScriptLanguage = createTypeScriptLanguage;
function setEquals(a, b) {
    if (a.size !== b.size) {
        return false;
    }
    for (const item of a) {
        if (!b.has(item)) {
            return false;
        }
    }
    return true;
}
//# sourceMappingURL=createProject.js.map