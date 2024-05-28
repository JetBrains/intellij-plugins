import type * as ts from "tsc-ide-plugin/languageService"
import {AngularVirtualCode} from "./code"
import {CodeMapping} from "@volar/language-core"

declare module "tsc-ide-plugin/languageService" {

  interface LanguageService {

    // @ts-ignore
    webStormNgTcbBlocks: Map<string, AngularVirtualCode>

    webStormNgUpdateTranspiledTemplate(
      ts: typeof import("typescript/lib/tsserverlibrary"),
      fileName: string,
      transpiledCode: string | undefined,
      sourceCode: { [key: string]: string },
      mappings: CodeMapping[]
    ): void

  }
}

export = ts;