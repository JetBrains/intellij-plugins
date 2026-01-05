// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import type * as ts from "tsc-ide-plugin/tsserverlibrary.shim"

import type {Language} from "@volar/language-core"
import {toGeneratedRange} from "./ranges"

export function decorateIdeLanguageServiceExtensions(
  language: Language<string>,
  languageService: ts.LanguageService,
) {

  const {
    webStormGetElementType,
    webStormGetTypeProperties,
    webStormGetTypeProperty,
    webStormGetSymbolType,
  } = languageService

  if (
    webStormGetElementType === undefined
    || webStormGetTypeProperties === undefined
    || webStormGetTypeProperty === undefined
    || webStormGetSymbolType === undefined
  ) return

  languageService.webStormGetElementType = (options) => {
    const {fileName} = options
    if (!fileName.endsWith(".vue"))
      return webStormGetElementType(options)

    const generatedOffsets = toGeneratedRange(language, fileName, options.startOffset, options.endOffset)
    if (!generatedOffsets)
      // throw error?
      return undefined

    const [startOffset, endOffset] = generatedOffsets
    return webStormGetElementType({
      ...options,
      startOffset,
      endOffset,
    })
  }

  languageService.webStormGetSymbolType = (options) => {
    return webStormGetSymbolType({
      ...options,
    })
  }

  languageService.webStormGetTypeProperties = (options) => {
    return webStormGetTypeProperties({
      ...options,
    })
  }

  languageService.webStormGetTypeProperty = (options) => {
    return webStormGetTypeProperty({
      ...options,
    })
  }
}
