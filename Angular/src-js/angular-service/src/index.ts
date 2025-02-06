// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import type * as ts from "./tsserverlibrary.shim";
import {type TypeScriptServiceScript} from "@volar/typescript"
import {createLanguageServicePlugin} from "@volar/typescript/lib/quickstart/createLanguageServicePlugin"
import {Language, LanguagePlugin} from "@volar/language-core"
import {AngularVirtualCode} from "./code"
import {
  createUnboundReverseMapper,
  decorateIdeLanguageServiceExtensions,
  decorateNgLanguageServiceExtensions,
} from "./decorateLanguageService"
import {CodegenContext} from "@volar/language-core/lib/types"
import {AngularSourceMap} from "./ngSourceMap"
import {ngTranspiledTemplates, registerProtocolHandlers} from "./ngCommands"

function loadLanguagePlugins(ts: typeof import('tsc-ide-plugin/tsserverlibrary.shim'),
                             info: ts.server.PluginCreateInfo): {
  languagePlugins: [LanguagePlugin<string, AngularVirtualCode>];
  setup?: (language: Language<string>) => void;
} {
  addNgCommands(ts, info);
  return {
    languagePlugins: [{
      getLanguageId(scriptId: string): string | undefined {
        return scriptId.endsWith(".html") ? "html" : undefined
      },
      isAssociatedFileOnly(_scriptId: string, languageId: string): boolean {
        return languageId === "html"
      },
      createVirtualCode(scriptId: string, languageId: string, snapshot: ts.IScriptSnapshot, ctx: CodegenContext<string>): AngularVirtualCode | undefined {
        if (languageId === "typescript" && !scriptId.endsWith(".d.ts") && scriptId.indexOf("/node_modules/") < 0) {
          const normalizedScriptId = ts.server.toNormalizedPath(scriptId)
          let virtualCode = new AngularVirtualCode(normalizedScriptId)
          return virtualCode.sourceFileUpdated(ts, snapshot, ctx, ngTranspiledTemplates.get(normalizedScriptId))
        }
        return undefined
      },
      updateVirtualCode(scriptId: string, virtualCode: AngularVirtualCode, newSnapshot: ts.IScriptSnapshot, ctx: CodegenContext<string>): AngularVirtualCode | undefined {
        return virtualCode.sourceFileUpdated(ts, newSnapshot, ctx, ngTranspiledTemplates.get(ts.server.toNormalizedPath(scriptId)))
      },
      typescript: {
        extraFileExtensions: [{
          extension: "html",
          scriptKind: ts.ScriptKind.Deferred,
          isMixedContent: true,
        }],
        getServiceScript(rootVirtualCode: AngularVirtualCode): TypeScriptServiceScript | undefined {
          return {
            code: rootVirtualCode,
            extension: ".ts",
            scriptKind: ts.ScriptKind.TS,
            preventLeadingOffset: true,
          }
        }
      }
    }],
    setup(language: Language<string>) {
      language.mapperFactory = (mappings) => new AngularSourceMap(mappings)

      const unboundReverseMapper = createUnboundReverseMapper(language, info.languageService)
      let {webStormGetElementType} = info.languageService
      decorateIdeLanguageServiceExtensions(language, info.languageService, unboundReverseMapper)
      decorateNgLanguageServiceExtensions(language, info.languageService, unboundReverseMapper, webStormGetElementType)
    }
  }
}

function addNgCommands(ts: typeof import('tsc-ide-plugin/tsserverlibrary.shim'), info: ts.server.PluginCreateInfo) {
  let projectService = info.project.projectService;
  projectService.logger.info("Angular: called handler processing");

  let session = info.session;
  if (session == undefined) {
    projectService.logger.info("Angular: there is no session in info.");
    return;
  }
  if (session.addProtocolHandler == undefined) {
    // addProtocolHandler was introduced in TS 4.4 or 4.5 in 2021, see https://github.com/microsoft/TypeScript/issues/43893
    projectService.logger.info("Angular: there is no addProtocolHandler method.");
    return;
  }
  if ((session as any).webStormNgCommandsAdded) return

  (session as any).webStormNgCommandsAdded = true
  registerProtocolHandlers(session, ts, projectService)
  projectService.logger.info("Angular specific commands are successfully added.");
}

const init = createLanguageServicePlugin(loadLanguagePlugins as any)
export = init
