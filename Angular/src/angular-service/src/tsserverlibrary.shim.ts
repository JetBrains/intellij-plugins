import type * as ts from "tsc-ide-plugin/tsserverlibrary.shim"
import {CodeMapping} from "@volar/language-core"

declare module "tsc-ide-plugin/tsserverlibrary.shim" {

  interface LanguageService {
    webStormNgUpdateTranspiledTemplate(
      ts: typeof import("typescript/lib/tsserverlibrary"),
      fileName: string,
      transpiledCode: string | undefined,
      sourceCodeLength: { [key: string]: string },
      mappings: ({source: string} & CodeMapping)[]
    ): void
  }
}

export = ts;