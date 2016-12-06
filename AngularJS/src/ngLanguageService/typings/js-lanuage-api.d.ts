interface LanguagePluginFactory {
    create(state:PluginState): { languagePlugin: LanguagePlugin, readyMessage?:any};
}

interface LanguagePluginFactoryProvider {
    factory: LanguagePluginFactory;
}

interface LanguagePlugin {
    onMessage(p:string):any;
}

interface PluginState {
    pluginName:string
    pluginPath?:string
    sessionId:string
}