import {PrettierPlugin} from "./prettier-plugin";

class PluginFactory implements LanguagePluginFactory {
    create(state: PluginState): { languagePlugin: LanguagePlugin; readyMessage?: any } {
        return {languagePlugin: new PrettierPlugin()};
    }
}

let factory = new PluginFactory();

export {factory};