import type * as ts from "tsc-ide-plugin/tsserverlibrary.shim"
import type {GetElementTypeResponse} from "tsc-ide-plugin/protocol"
import {Angular2TcbMappingInfo} from "./mappings"

declare module "tsc-ide-plugin/tsserverlibrary.shim" {

  interface LanguageService {
    webStormNgUpdateTranspiledTemplate(
      ts: typeof import("tsc-ide-plugin/tsserverlibrary.shim"),
      fileName: string,
      transpiledCode: string | undefined,
      sourceCodeLength: { [key: string]: string },
      mappings: Angular2TcbMappingInfo[]
    ): void

    webStormNgGetGeneratedElementType(
      ts: typeof import("tsc-ide-plugin/tsserverlibrary.shim"),
      fileName: string,
      startOffset: number,
      endOffset: number,
      forceReturnType: boolean,
    ): GetElementTypeResponse | undefined
  }
}

export = ts;