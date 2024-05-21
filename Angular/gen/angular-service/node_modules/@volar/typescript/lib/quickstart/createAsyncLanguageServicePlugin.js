"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.createAsyncLanguageServicePlugin = void 0;
const decorateLanguageService_1 = require("../node/decorateLanguageService");
const decorateLanguageServiceHost_1 = require("../node/decorateLanguageServiceHost");
const language_core_1 = require("@volar/language-core");
const createLanguageServicePlugin_1 = require("./createLanguageServicePlugin");
const common_1 = require("../common");
const externalFiles = new WeakMap();
const decoratedLanguageServices = new WeakSet();
const decoratedLanguageServiceHosts = new WeakSet();
function createAsyncLanguageServicePlugin(extensions, scriptKind, loadLanguagePlugins) {
    return modules => {
        const { typescript: ts } = modules;
        const pluginModule = {
            create(info) {
                if (!decoratedLanguageServices.has(info.languageService)
                    && !decoratedLanguageServiceHosts.has(info.languageServiceHost)) {
                    decoratedLanguageServices.add(info.languageService);
                    decoratedLanguageServiceHosts.add(info.languageServiceHost);
                    const emptySnapshot = ts.ScriptSnapshot.fromString('');
                    const getScriptSnapshot = info.languageServiceHost.getScriptSnapshot.bind(info.languageServiceHost);
                    const getScriptVersion = info.languageServiceHost.getScriptVersion.bind(info.languageServiceHost);
                    const getScriptKind = info.languageServiceHost.getScriptKind?.bind(info.languageServiceHost);
                    const getProjectVersion = info.languageServiceHost.getProjectVersion?.bind(info.languageServiceHost);
                    let initialized = false;
                    info.languageServiceHost.getScriptSnapshot = fileName => {
                        if (!initialized && extensions.some(ext => fileName.endsWith(ext))) {
                            return emptySnapshot;
                        }
                        return getScriptSnapshot(fileName);
                    };
                    info.languageServiceHost.getScriptVersion = fileName => {
                        if (!initialized && extensions.some(ext => fileName.endsWith(ext))) {
                            return 'initializing...';
                        }
                        return getScriptVersion(fileName);
                    };
                    if (getScriptKind) {
                        info.languageServiceHost.getScriptKind = fileName => {
                            if (!initialized && extensions.some(ext => fileName.endsWith(ext))) {
                                return scriptKind; // TODO: bypass upstream bug
                            }
                            return getScriptKind(fileName);
                        };
                    }
                    if (getProjectVersion) {
                        info.languageServiceHost.getProjectVersion = () => {
                            if (!initialized) {
                                return getProjectVersion() + ',initializing...';
                            }
                            return getProjectVersion();
                        };
                    }
                    loadLanguagePlugins(ts, info).then(languagePlugins => {
                        const syncedScriptVersions = new language_core_1.FileMap(ts.sys.useCaseSensitiveFileNames);
                        const language = (0, language_core_1.createLanguage)([
                            ...languagePlugins,
                            common_1.fileLanguageIdProviderPlugin,
                        ], ts.sys.useCaseSensitiveFileNames, fileName => {
                            const version = getScriptVersion(fileName);
                            if (syncedScriptVersions.get(fileName) === version) {
                                return;
                            }
                            syncedScriptVersions.set(fileName, version);
                            const snapshot = getScriptSnapshot(fileName);
                            if (snapshot) {
                                language.scripts.set(fileName, snapshot);
                            }
                            else {
                                language.scripts.delete(fileName);
                            }
                        });
                        (0, decorateLanguageService_1.decorateLanguageService)(language, info.languageService);
                        (0, decorateLanguageServiceHost_1.decorateLanguageServiceHost)(ts, language, info.languageServiceHost);
                        info.project.markAsDirty();
                        initialized = true;
                    });
                }
                return info.languageService;
            },
            getExternalFiles(project, updateLevel = 0) {
                if (updateLevel >= (1)
                    || !externalFiles.has(project)) {
                    const oldFiles = externalFiles.get(project);
                    const newFiles = (0, decorateLanguageServiceHost_1.searchExternalFiles)(ts, project, extensions);
                    externalFiles.set(project, newFiles);
                    if (oldFiles && !(0, createLanguageServicePlugin_1.arrayItemsEqual)(oldFiles, newFiles)) {
                        project.refreshDiagnostics();
                    }
                }
                return externalFiles.get(project);
            },
        };
        return pluginModule;
    };
}
exports.createAsyncLanguageServicePlugin = createAsyncLanguageServicePlugin;
//# sourceMappingURL=createAsyncLanguageServicePlugin.js.map