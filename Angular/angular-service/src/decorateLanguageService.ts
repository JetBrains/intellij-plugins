import * as decorateLanguageServiceModule from "@volar/typescript/lib/node/decorateLanguageService"
import {CodeMapping, Language} from "@volar/language-core"
import type * as TS from "./languageService"
import type {GetElementTypeResponse, Range} from "tsc-ide-plugin/protocol"
import type {ReverseMapper} from "tsc-ide-plugin/ide-get-element-type"
import {getServiceScript} from "@volar/typescript/lib/node/utils"
import {toGeneratedOffset} from "@volar/typescript/lib/node/transform"
import {AngularVirtualCode} from "./code"

let decorated = false

export function patchVolarAndDecorateLanguageService() {
  if (decorated) return
  decorated = true
  let {decorateLanguageService} = decorateLanguageServiceModule

  // @ts-ignore TS2540
  decorateLanguageServiceModule.decorateLanguageService =
    (language: Language, languageService: TS.LanguageService) => {
      decorateLanguageService(language, languageService as any /* due to difference in TS version we need to cast to any */)
      decorateIdeLanguageServiceExtensions(language, languageService)
      decorateNgLanguageServiceExtensions(language, languageService)
    }
}

function decorateIdeLanguageServiceExtensions(language: Language, languageService: TS.LanguageService) {

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
    const [serviceScript, sourceScript, map] =
      getServiceScript(language, fileName);
    const program = languageService.getProgram()
    const sourceFile = program?.getSourceFile(fileName);
    const generatedFile = sourceScript ? program?.getSourceFile(sourceScript.id) : undefined;

    if (serviceScript && sourceFile && generatedFile) {
      try {
        let originalRangePosStart = ts.getPositionOfLineAndCharacter(sourceFile, range.start.line, range.start.character)
        let originalRangePosEnd = ts.getPositionOfLineAndCharacter(sourceFile, range.end.line, range.end.character)

        const generatedRangePosStart = toGeneratedOffset(sourceScript, map, originalRangePosStart, () => true);
        const generatedRangePosEnd = toGeneratedOffset(sourceScript, map, originalRangePosEnd, () => true);

        if (generatedRangePosStart !== undefined && generatedRangePosEnd !== undefined) {
          const start = ts.getLineAndCharacterOfPosition(generatedFile, generatedRangePosStart)
          const end = ts.getLineAndCharacterOfPosition(generatedFile, generatedRangePosEnd)

          //TODO support reverseMapper
          return webStormGetElementType(ts as any, sourceScript.id, {start, end}, forceReturnType)
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

function decorateNgLanguageServiceExtensions(language: Language, languageService: TS.LanguageService) {

  languageService.webStormNgTcbBlocks = new Map()

  languageService.webStormNgUpdateTranspiledTemplate = (
    _ts, fileName,transpiledCode,sourceCode: { [key: string]: string }, mappings
  ) => {
    let virtualCode: AngularVirtualCode | undefined = languageService.webStormNgTcbBlocks.get(fileName)
    if (virtualCode === undefined) {
      virtualCode = new AngularVirtualCode(fileName);
      languageService.webStormNgTcbBlocks.set(fileName, virtualCode);
    }

    virtualCode.transpiledTemplateUpdated(transpiledCode, sourceCode, mappings);
  }


}