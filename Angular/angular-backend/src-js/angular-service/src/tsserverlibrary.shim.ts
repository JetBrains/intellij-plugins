import type ts from "tsc-ide-plugin/tsserverlibrary.shim"
import type {GetElementTypeResponse} from "tsc-ide-plugin/protocol"

declare module "tsc-ide-plugin/tsserverlibrary.shim" {

  interface LanguageService {
    webStormNgGetGeneratedElementType(
      ts: typeof import("tsc-ide-plugin/tsserverlibrary.shim"),
      fileName: string,
      range: {
        start: LineAndCharacter;
        end: LineAndCharacter;
      },
      forceReturnType: boolean,
      cancellationToken: CancellationToken,
    ): GetElementTypeResponse | undefined
  }
}

export = ts;