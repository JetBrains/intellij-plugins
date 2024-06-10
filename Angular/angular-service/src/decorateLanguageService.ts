import {Language} from "@volar/language-core"
import type * as TS from "./languageService"
import type {GetElementTypeResponse, Range} from "tsc-ide-plugin/protocol"
import type {ReverseMapper} from "tsc-ide-plugin/ide-get-element-type"
import {getServiceScript} from "@volar/typescript/lib/node/utils"
import {toGeneratedOffset} from "@volar/typescript/lib/node/transform"
import {AngularVirtualCode} from "./code"

export function decorateIdeLanguageServiceExtensions(language: Language<string>, languageService: TS.LanguageService) {

  let {webStormGetElementType, webStormGetTypeProperties, webStormGetSymbolType} = languageService

  if (webStormGetElementType === undefined || webStormGetElementType === undefined || webStormGetSymbolType === undefined)
    return

  languageService.webStormGetElementType = (
    ts: typeof TS,
    fileName: string,
    range: Range,
    forceReturnType: boolean,
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
      try {
        let originalRangePosStart = ts.getPositionOfLineAndCharacter(sourceFile, range.start.line, range.start.character)
        let originalRangePosEnd = ts.getPositionOfLineAndCharacter(sourceFile, range.end.line, range.end.character)

        const generatedRangePosStart = toGeneratedOffset(language, serviceScript, sourceScript, originalRangePosStart, () => true);
        const generatedRangePosEnd = toGeneratedOffset(language, serviceScript, sourceScript, originalRangePosEnd, () => true);

        if (generatedRangePosStart !== undefined && generatedRangePosEnd !== undefined) {
          const start = ts.getLineAndCharacterOfPosition(generatedFile, generatedRangePosStart)
          const end = ts.getLineAndCharacterOfPosition(generatedFile, generatedRangePosEnd)

          //TODO support reverseMapper
          return webStormGetElementType(ts as any, targetScript.id, {start, end}, forceReturnType)
        }
      }
      catch (e) {
        // ignore
      }
      return undefined;
    }
    else {
      return webStormGetElementType(ts as any, fileName, range, forceReturnType, reverseMapper)
    }
  }

  languageService.webStormGetSymbolType = (
    ts,
    symbolId: number,
    reverseMapper?: ReverseMapper,
  ) => {
    //TODO support reverseMapper
    return webStormGetSymbolType(ts, symbolId, reverseMapper)
  }

  languageService.webStormGetTypeProperties = (
    ts,
    typeId: number,
    reverseMapper?: ReverseMapper,
  ) => {
    //TODO support reverseMapper
    return webStormGetTypeProperties(ts, typeId, reverseMapper)
  }

}

export function decorateNgLanguageServiceExtensions(language: Language<string>, languageService: TS.LanguageService) {
  languageService.webStormNgUpdateTranspiledTemplate = (
    _ts, fileName,transpiledCode,sourceCode: { [key: string]: string }, mappings
  ) => {
    const sourceScript = language.scripts.get(fileName)
    const virtualCode = sourceScript?.generated?.root
    if (sourceScript && virtualCode instanceof AngularVirtualCode) {
      virtualCode.transpiledTemplateUpdated(transpiledCode, sourceCode, mappings);
    }
  }
}