import * as decorateLanguageServiceModule from "@volar/typescript/lib/node/decorateLanguageService"
import {Language} from "@volar/language-core"
import type * as TS from "tsc-ide-plugin/languageService"
import type {GetElementTypeResponse, Range} from "tsc-ide-plugin/protocol"
import type {ReverseMapper} from "tsc-ide-plugin/ide-get-element-type"
import {getServiceScript} from "@volar/typescript/lib/node/utils"
import {toGeneratedOffset} from "@volar/typescript/lib/node/transform"
import {AngularVirtualCode} from "./code"

let decorated = false

export function patchVolarToDecorateLanguageService() {
  if (decorated) return
  decorated = true
  let {decorateLanguageService} = decorateLanguageServiceModule

  // @ts-ignore TS2540
  decorateLanguageServiceModule.decorateLanguageService =
    (language: Language, languageService: TS.LanguageService) => {
      decorateLanguageService(language, languageService as any /* due to difference in TS version we need to cast to any */)
      decorateIdeLanguageServiceExtensions(language, languageService)
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

    if (serviceScript && sourceFile) {
      let angularCode = serviceScript.code as AngularVirtualCode
      let originalFile = {
        text: angularCode.sourceCode,
        getLineAndCharacterOfPosition(position: number): TS.LineAndCharacter {
          return ts.getLineAndCharacterOfPosition(this, position);
        }
      }
      try {
        let originalRangePosStart = ts.getPositionOfLineAndCharacter(originalFile, range.start.line, range.start.character)
        let originalRangePosEnd = ts.getPositionOfLineAndCharacter(originalFile, range.end.line, range.end.character)

        const generatedRangePosStart = toGeneratedOffset(sourceScript, map, originalRangePosStart, () => true);
        const generatedRangePosEnd = toGeneratedOffset(sourceScript, map, originalRangePosEnd, () => true);

        if (generatedRangePosStart !== undefined && generatedRangePosEnd !== undefined) {
          const start = ts.getLineAndCharacterOfPosition(sourceFile, generatedRangePosStart)
          const end = ts.getLineAndCharacterOfPosition(sourceFile, generatedRangePosEnd)

          //TODO support reverseMapper
          return webStormGetElementType(ts as any, fileName, {start, end}, forceReturnType)
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