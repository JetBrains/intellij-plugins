import {TSLintPlugin} from "./tslint-plugin";

class TSLintPluginFactory implements LanguagePluginFactory {
    create(state: PluginState): {languagePlugin: LanguagePlugin} {
        return {languagePlugin: new TSLintPlugin(state)};
    }
}

let factory = new TSLintPluginFactory();

export {factory}