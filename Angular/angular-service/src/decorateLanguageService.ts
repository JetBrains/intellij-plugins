import * as decorateLanguageServiceModule from "@volar/typescript/lib/node/decorateLanguageService"
import {Language} from "@volar/language-core"
import type * as ts from "typescript/lib/tsserverlibrary"
import type {GetElementTypeResponse, Range} from "../../../../plugins/JavaScriptLanguage/src/tscplugin/protocol"
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
    (language: Language, languageService: ts.LanguageService) => {
      decorateLanguageService(language, languageService)
      decorateIdeLanguageServiceExtensions(language, languageService)
    }
}

function decorateIdeLanguageServiceExtensions(language: Language, languageService: ts.LanguageService) {

  let {getElementType} = languageService

  if (getElementType === undefined)
    return

  languageService.getElementType =
    (ts: typeof import("typescript/lib/tsserverlibrary"),
     fileName: string,
     range: Range,
     forceReturnType: boolean
    ): GetElementTypeResponse => {
      const [serviceScript, sourceScript, map] =
        getServiceScript(language, fileName);
      const program = languageService.getProgram()
      const sourceFile = program?.getSourceFile(fileName);

      if (serviceScript && sourceFile) {
        let angularCode = serviceScript.code as AngularVirtualCode
        let originalFile = {
          text: angularCode.sourceCode,
          getLineAndCharacterOfPosition(position: number): ts.LineAndCharacter {
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
            return getElementType(ts as any, fileName, {start, end}, forceReturnType)
          }
        }
        catch (e) {
          // ignore
        }
        return undefined;
      }
      else {
        return getElementType(ts as any, fileName, range, forceReturnType)
      }
    }

}

// TODO try to share this

declare module "typescript/lib/tsserverlibrary" {
  interface LanguageService {
    getElementType(
      ts: typeof import("typescript/lib/tsserverlibrary"),
      fileName: string,
      range: Range,
      forceReturnType: boolean
    ): GetElementTypeResponse
  }
}