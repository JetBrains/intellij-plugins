// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import type ts from "tsc-ide-plugin/tsserverlibrary.shim";
import {decorateIdeLanguageServiceExtensions} from "./decorateLanguageService"

const init: ts.server.PluginModuleFactory = () => ({
  create: (info) => {
    decorateIdeLanguageServiceExtensions(
      info.project.__vue__.language,
      info.languageService,
    )

    return info.languageService
  }
})

export = init
