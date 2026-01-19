import type {CodeInformation, Language, SourceScript} from "@volar/language-core"
import type TS from "typescript/lib/tsserverlibrary"
import type {WebStormGetOptions} from "typescript/lib/tsserverlibrary"
import type {Range} from "tsc-ide-plugin/protocol"
import type {ReverseMapping} from "tsc-ide-plugin/ide-get-element-type"
import {getServiceScript} from "@volar/typescript/lib/node/utils"
import type {TypeScriptServiceScript} from "@volar/typescript"
import {toGeneratedRanges, toSourceRanges} from "@volar/typescript/lib/node/transform"
import * as console from "node:console"

declare module "@volar/language-core/lib/types" {
  interface CodeInformation {
    types: boolean
    reverseTypes: boolean
  }
}

function toSourceRange(sourceScript: SourceScript<string> | undefined, language: Language<string>, serviceScript: TypeScriptServiceScript, start: number, end: number, fallbackToAnyMatch: boolean, filter: (data: CodeInformation) => boolean): [fileName: string, start: number, end: number] | undefined {
  for (const result of toSourceRanges(sourceScript, language, serviceScript, start, end, fallbackToAnyMatch, filter)) {
    return result
  }
  return undefined
}

function toGeneratedRange(language: Language, serviceScript: TypeScriptServiceScript, sourceScript: SourceScript<string>, start: number, end: number, filter: (data: CodeInformation) => boolean): readonly [start: number, end: number] | undefined {
  for (const result of toGeneratedRanges(language, serviceScript, sourceScript, start, end, filter)) {
    return result
  }
  return undefined
}

export type UnboundReverseMapper = (
  ts: typeof TS,
  sourceFile: TS.SourceFile,
  generatedRange: Range,
) => ReverseMapping;

export function createUnboundReverseMapper(language: Language<string>, languageService: TS.LanguageService): UnboundReverseMapper {
  return function (ts: typeof TS, sourceFile: TS.SourceFile, generatedRange: Range): ReverseMapping {
    const [serviceScript, targetScript] = getServiceScript(language, sourceFile.fileName);
    if (targetScript?.associatedOnly) {
      return undefined;
    }
    const program = languageService.getProgram()
    const generatedFile = targetScript ? program?.getSourceFile(targetScript.id) : undefined;
    if (serviceScript && sourceFile && generatedFile) {
      try {
        let generatedRangePosStart = ts.getPositionOfLineAndCharacter(generatedFile, generatedRange.start.line, generatedRange.start.character)
        let generatedRangePosEnd = ts.getPositionOfLineAndCharacter(generatedFile, generatedRange.end.line, generatedRange.end.character)

        const sourceRange = toSourceRange(undefined, language, serviceScript, generatedRangePosStart, generatedRangePosEnd, true, it => it.types || it.reverseTypes);

        if (sourceRange !== undefined) {
          let targetFile = program?.getSourceFile(sourceRange[0])
          if (!targetFile) return undefined;
          return {
            fileName: sourceRange[0],
            sourceRange: {
              start: targetFile.getLineAndCharacterOfPosition(sourceRange[1]),
              end: targetFile.getLineAndCharacterOfPosition(sourceRange[2]),
            },
          }
        }
      }
      catch (e) {
        console.error(e)
      }
    }
    return undefined;
  }
}

export function decorateIdeLanguageServiceExtensions(
  language: Language<string>,
  languageService: TS.LanguageService,
  unboundReverseMapper: UnboundReverseMapper,
) {
  const {
    webStormGetElementType,
    webStormGetTypeProperties,
    webStormGetTypeProperty,
    webStormGetSymbolType,
    webStormGetCompletionSymbols,
  } = languageService

  if (
    webStormGetElementType === undefined
    || webStormGetTypeProperties === undefined
    || webStormGetTypeProperty === undefined
    || webStormGetSymbolType === undefined
    || webStormGetCompletionSymbols === undefined
  ) return

  function withReverseMapper<
    O extends WebStormGetOptions,
    R extends Record<never, never> | undefined,
  >(source: (options: O) => R): (options: O) => R {
    return (options) =>
      source({
        ...options,
        reverseMapper: unboundReverseMapper.bind(null, options.ts),
      })
  }

  languageService.webStormGetElementType = (options) => {
    const {ts, fileName, startOffset, endOffset} = options

    const [serviceScript, targetScript, sourceScript] =
      getServiceScript(language, fileName);
    if (targetScript?.associatedOnly) {
      return undefined;
    }
    const program = languageService.getProgram()
    const sourceFile = program?.getSourceFile(fileName);
    const generatedFile = targetScript ? program?.getSourceFile(targetScript.id) : undefined;

    if (serviceScript && sourceFile && generatedFile) {
      const generatedRange = toGeneratedRange(language, serviceScript, sourceScript, startOffset, endOffset, (it) => it.types);
      if (generatedRange !== undefined) {
        return webStormGetElementType({
          ...options,
          fileName: targetScript.id,
          startOffset: generatedRange[0],
          endOffset: generatedRange[1],
          reverseMapper: unboundReverseMapper.bind(null, ts),
        })
      }
      return undefined;
    }
    else {
      return webStormGetElementType(options)
    }
  }

  languageService.webStormGetCompletionSymbols = withReverseMapper(webStormGetCompletionSymbols)
  languageService.webStormGetSymbolType = withReverseMapper(webStormGetSymbolType)
  languageService.webStormGetTypeProperties = withReverseMapper(webStormGetTypeProperties)
  languageService.webStormGetTypeProperty = withReverseMapper(webStormGetTypeProperty)
}

export function decorateNgLanguageServiceExtensions(
  language: Language<string>,
  languageService: TS.LanguageService,
  unboundReverseMapper: UnboundReverseMapper,
  webStormGetElementType: TS.LanguageService["webStormGetElementType"],
) {
  languageService.webStormNgGetGeneratedElementType = (
    ts, fileName, range, forceReturnType, cancellationToken
  ) => {
    let program = languageService.getProgram();
    if (!program) {
      return undefined
    }

    const [_serviceScript, targetScript, _sourceScript] = getServiceScript(language, fileName);
    const generatedFile = targetScript ? program?.getSourceFile(targetScript.id) : undefined;
    if (!generatedFile)
      return undefined;

    let startOffset = ts.getPositionOfLineAndCharacter(generatedFile, range.start.line, range.start.character)
    let endOffset = ts.getPositionOfLineAndCharacter(generatedFile, range.end.line, range.end.character)

    return webStormGetElementType({
      ts,
      fileName,
      startOffset,
      endOffset,
      typeRequestKind: "Default",
      forceReturnType,
      cancellationToken,
      reverseMapper: unboundReverseMapper.bind(null, ts),
    })
  }
}