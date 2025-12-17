import {type CodeInformation, Language, type SourceScript} from "@volar/language-core"
import type * as TS from "./tsserverlibrary.shim"
import type {GetElementTypeResponse, Range, TypeRequestKind} from "tsc-ide-plugin/protocol"
import type {ReverseMapper} from "tsc-ide-plugin/ide-get-element-type"
import {getServiceScript} from "@volar/typescript/lib/node/utils"
import {type TypeScriptServiceScript} from "@volar/typescript"
import {toGeneratedRanges, toSourceRanges} from "@volar/typescript/lib/node/transform"
import * as console from "node:console"

// Patch Volar searchExternalFiles method to search for all HTML files in the project
let volarDecorateLanguageServiceHost = require("@volar/typescript/lib/node/decorateLanguageServiceHost")
volarDecorateLanguageServiceHost.searchExternalFiles = searchExternalFilesPatched

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

export type UnboundReverseMapper = (ts: typeof TS, sourceFile: TS.SourceFile, generatedRange: Range) => {
  sourceRange: Range,
  fileName: string
} | undefined;

export function createUnboundReverseMapper(language: Language<string>, languageService: TS.LanguageService): UnboundReverseMapper {
  return function (ts: typeof TS, sourceFile: TS.SourceFile, generatedRange: Range): {
    sourceRange: Range,
    fileName: string
  } | undefined {
    const [serviceScript, targetScript, sourceScript] =
      getServiceScript(language, sourceFile.fileName);
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

export function decorateIdeLanguageServiceExtensions(language: Language<string>, languageService: TS.LanguageService, unboundReverseMapper: UnboundReverseMapper) {

  let {webStormGetElementType, webStormGetTypeProperties, webStormGetTypeProperty, webStormGetSymbolType} = languageService

  if (webStormGetElementType === undefined ||
    webStormGetTypeProperties === undefined ||
    webStormGetTypeProperty === undefined ||
    webStormGetSymbolType === undefined)
    return

  languageService.webStormGetElementType = (
    ts: typeof TS,
    fileName: string,
    startOffset: number,
    endOffset: number,
    typeRequestKind: TypeRequestKind,
    forceReturnType: boolean,
    cancellationToken: TS.CancellationToken,
    reverseMapper?: ReverseMapper,
  ): GetElementTypeResponse => {
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
        return webStormGetElementType(ts, targetScript.id, generatedRange[0], generatedRange[1], typeRequestKind, forceReturnType, cancellationToken, unboundReverseMapper.bind(null, ts))
      }
      return undefined;
    }
    else {
      return webStormGetElementType(ts, fileName, startOffset, endOffset, typeRequestKind, forceReturnType, cancellationToken, reverseMapper)
    }
  }

  languageService.webStormGetSymbolType = (
    ts,
    symbolId: number,
    cancellationToken: TS.CancellationToken,
    _reverseMapper?: ReverseMapper,
  ) => {
    return webStormGetSymbolType(ts, symbolId, cancellationToken, unboundReverseMapper.bind(null, ts))
  }

  languageService.webStormGetTypeProperties = (
    ts,
    typeId: number,
    cancellationToken: TS.CancellationToken,
    _reverseMapper?: ReverseMapper,
  ) => {
    return webStormGetTypeProperties(ts, typeId, cancellationToken, unboundReverseMapper.bind(null, ts))
  }

  languageService.webStormGetTypeProperty = (
    ts,
    typeId: number,
    propertyName: string,
    cancellationToken: TS.CancellationToken,
    _reverseMapper?: ReverseMapper,
  ) => {
    return webStormGetTypeProperty(ts, typeId, propertyName, cancellationToken, unboundReverseMapper.bind(null, ts))
  }
}

export function decorateNgLanguageServiceExtensions(
  language: Language<string>,
  languageService: TS.LanguageService,
  unboundReverseMapper: UnboundReverseMapper,
  webStormGetElementType: TS.LanguageService["webStormGetElementType"]) {
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

    return webStormGetElementType(ts, fileName, startOffset, endOffset, "Default", forceReturnType, cancellationToken, unboundReverseMapper.bind(null, ts))
  }
}

function searchExternalFilesPatched(ts: typeof TS, project: TS.server.Project, exts: string[]): string[] {
  if (project.projectKind !== ts.server.ProjectKind.Configured
    || (project.getProjectReferences()?.length ?? 0) > 0) {
    return [];
  }
  const parseHost: TS.ParseConfigHost = {
    useCaseSensitiveFileNames: project.useCaseSensitiveFileNames(),
    fileExists: project.fileExists.bind(project),
    readFile: project.readFile.bind(project),
    readDirectory: (...args) => {
      args[1] = exts;
      return project.readDirectory(...args);
    },
  };
  // We are interested in all HTML files in the project
  const parsed = ts.parseJsonConfigFileContent({}, parseHost, project.getCurrentDirectory());
  return parsed.fileNames;
}