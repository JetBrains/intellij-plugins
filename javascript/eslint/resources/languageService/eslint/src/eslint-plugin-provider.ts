import {EslintPluginState} from "./eslint-api"

class ESLintPluginFactory implements LanguagePluginFactory {
  create(state: EslintPluginState): { languagePlugin: LanguagePlugin; readyMessage?: any } {
    if (state.standardPackagePath != null) {
      let dotIndex = state.linterPackageVersion.indexOf(".")
      let majorVersion = dotIndex > 0 ? state.linterPackageVersion.substring(0, dotIndex) : "";
      if (+majorVersion >= 17) {
        const Standard17Plugin = require('./standard17-plugin').Standard17Plugin;
        return {languagePlugin: new Standard17Plugin(state)};
      }
    }
    else {
      let dotIndex = state.linterPackageVersion.indexOf(".")
      let majorVersion = dotIndex > 0 ? state.linterPackageVersion.substring(0, dotIndex) : "";
      if (+majorVersion >= 8) {
        const ESLint8Plugin = require('./eslint8-plugin').ESLint8Plugin;
        return {languagePlugin: new ESLint8Plugin(state)};
      }
    }

    const ESLintPlugin = require('./eslint-plugin').ESLintPlugin;
    return {languagePlugin: new ESLintPlugin(state)};
  }
}

let factory = new ESLintPluginFactory();

export {factory};