import type * as ts from "tsc-ide-plugin/tsserverlibrary.shim"
import {Angular2TcbMappingInfo} from "./mappings"

declare module "tsc-ide-plugin/tsserverlibrary.shim" {

  interface LanguageService {
    webStormNgUpdateTranspiledTemplate(
      ts: typeof import("typescript/lib/tsserverlibrary"),
      fileName: string,
      transpiledCode: string | undefined,
      sourceCodeLength: { [key: string]: string },
      mappings: Angular2TcbMappingInfo[]
    ): void
  }
}

export = ts;