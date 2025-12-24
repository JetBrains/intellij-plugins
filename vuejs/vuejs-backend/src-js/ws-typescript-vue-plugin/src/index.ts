// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import type * as ts from "tsc-ide-plugin/tsserverlibrary.shim";
import {decorateIdeLanguageServiceExtensions} from "./decorateLanguageService"

import type {Language} from "@volar/language-core"
import {createLanguageServicePlugin} from "@volar/typescript/lib/quickstart/createLanguageServicePlugin"
import type {createPluginCallbackReturnValue} from "@volar/typescript/lib/quickstart/languageServicePluginCommon"

function loadLanguagePlugins(
  _: unknown,
  info: ts.server.PluginCreateInfo,
): createPluginCallbackReturnValue {
  return {
    languagePlugins: [],
    setup(language: Language<string>) {
      decorateIdeLanguageServiceExtensions(language, info.languageService)
    }
  }
}

const init = createLanguageServicePlugin(loadLanguagePlugins as any)
export = init
