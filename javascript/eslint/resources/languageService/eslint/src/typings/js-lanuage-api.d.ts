interface LanguagePluginFactory {
    create(state: PluginState): {languagePlugin: LanguagePlugin, readyMessage?: any};
}

interface LanguagePluginFactoryProvider {
    factory: LanguagePluginFactory;
}

interface LanguagePlugin {
    onMessage(p: string, writer: MessageWriter): void;
}

interface PluginState {
    pluginName: string
    pluginPath?: string
    sessionId: string
}

interface MessageWriter {
    write(answer: string): void
}