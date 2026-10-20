// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import type ts from "typescript/lib/tsserverlibrary"
import type {Language} from "@volar/language-core"
import {createReverseMapper, toGeneratedRange} from "./ranges"
import {isVueFile} from "./vue"

export function decorateIdeLanguageServiceExtensions(
  language: Language<string>,
  languageService: ts.LanguageService,
) {

  const {
    webStormGetCompletionSymbols,
    webStormGetElementType,
    webStormGetSymbolType,
    webStormGetTypeProperties,
    webStormGetTypeProperty,
    webStormGetResolvedSignature,
  } = languageService

  if (
    webStormGetCompletionSymbols === undefined
    || webStormGetElementType === undefined
    || webStormGetSymbolType === undefined
    || webStormGetTypeProperties === undefined
    || webStormGetTypeProperty === undefined
    || webStormGetResolvedSignature === undefined
  ) return

  function withReverseMapper<
    O extends ts.WebStormGetOptions,
    R extends Record<never, never> | undefined,
  >(source: (options: O) => R): (options: O) => R {
    return (options) =>
      source({
        ...options,
        reverseMapper: createReverseMapper(options.ts, language),
      })
  }

  function withMapper<
    O extends ts.WebStormGetWithOffsetsOptions,
    R extends Record<never, never>,
  >(source: (options: O) => R | undefined): (options: O) => R | undefined {
    return (options) => {
      const {fileName} = options
      if (!isVueFile(fileName))
        return source(options)

      const generatedOffsets = toGeneratedRange(language, fileName, options.startOffset, options.endOffset)
      if (!generatedOffsets)
        // throw error?
        return undefined

      const [startOffset, endOffset] = generatedOffsets
      return source({
        ...options,
        startOffset,
        endOffset,
        reverseMapper: createReverseMapper(options.ts, language),
      })
    }
  }

  languageService.webStormGetElementType = withMapper(webStormGetElementType)
  languageService.webStormGetCompletionSymbols = withReverseMapper(webStormGetCompletionSymbols)
  languageService.webStormGetSymbolType = withReverseMapper(webStormGetSymbolType)
  languageService.webStormGetTypeProperties = withReverseMapper(webStormGetTypeProperties)
  languageService.webStormGetTypeProperty = withReverseMapper(webStormGetTypeProperty)
  languageService.webStormGetResolvedSignature = withMapper(webStormGetResolvedSignature)
}
