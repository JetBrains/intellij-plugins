import type {GetElementTypeResponse} from "tsc-ide-plugin/protocol"

declare module "typescript/lib/tsserverlibrary" {

  interface LanguageService {
    webStormNgGetGeneratedElementType(
      ts: typeof import("typescript/lib/tsserverlibrary"),
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
