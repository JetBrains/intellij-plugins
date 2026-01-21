// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import type ts from "typescript/lib/tsserverlibrary"
import type {Language} from "@volar/language-core"
import {createReverseMapper, toGeneratedRange} from "./ranges"

export function decorateIdeLanguageServiceExtensions(
  language: Language<string>,
  languageService: ts.LanguageService,
) {

  const {
    webStormGetCompletionSymbols,
    webStormGetElementType,
    webStormGetTypeProperties,
    webStormGetTypeProperty,
    webStormGetSymbolType,
  } = languageService

  if (
    webStormGetCompletionSymbols === undefined
    || webStormGetElementType === undefined
    || webStormGetTypeProperties === undefined
    || webStormGetTypeProperty === undefined
    || webStormGetSymbolType === undefined
  ) return

  const reverseMapper = createReverseMapper(language)

  function withReverseMapper<
    O extends ts.WebStormGetOptions,
    R extends Record<never, never> | undefined,
  >(source: (options: O) => R): (options: O) => R {
    return (options) =>
      source({
        ...options,
        reverseMapper,
      })
  }

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
      reverseMapper,
    })
  }

  languageService.webStormGetCompletionSymbols = withReverseMapper(webStormGetCompletionSymbols)
  languageService.webStormGetSymbolType = withReverseMapper(webStormGetSymbolType)
  languageService.webStormGetTypeProperties = withReverseMapper(webStormGetTypeProperties)
  languageService.webStormGetTypeProperty = withReverseMapper(webStormGetTypeProperty)
}
