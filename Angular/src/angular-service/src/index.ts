// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import type * as ts from "./tsserverlibrary.shim";
import {createLanguageServicePlugin} from "@volar/typescript/lib/quickstart/createLanguageServicePlugin"
import {Language, LanguagePlugin} from "@volar/language-core"
import {AngularVirtualCode} from "./code"
import {
  createUnboundReverseMapper,
  decorateIdeLanguageServiceExtensions,
  decorateNgLanguageServiceExtensions,
} from "./decorateLanguageService"
import {CodegenContext, TypeScriptServiceScript} from "@volar/language-core/lib/types"
import {AngularSourceMap} from "./ngSourceMap"
import {Angular2TcbMappingInfo} from "./mappings"

function loadLanguagePlugins(ts: typeof import('tsc-ide-plugin/tsserverlibrary.shim'),
                             info: ts.server.PluginCreateInfo): {
  languagePlugins: [LanguagePlugin<string, AngularVirtualCode>];
  setup?: (language: Language<string>) => void;
} {
  addNgCommands(ts, info);
  let ngTcbBlocks = new Map<string, AngularVirtualCode>()
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
          let virtualCode = ngTcbBlocks.get(scriptId)
          if (!virtualCode) {
            virtualCode = new AngularVirtualCode(scriptId, ctx, ts.sys.useCaseSensitiveFileNames)
            ngTcbBlocks.set(scriptId, virtualCode)
          }
          return virtualCode.sourceFileUpdated(snapshot)
        }
        return undefined
      },
      updateVirtualCode(scriptId: string, virtualCode: AngularVirtualCode, newSnapshot: ts.IScriptSnapshot, ctx: CodegenContext<string>): AngularVirtualCode | undefined {
        return virtualCode.sourceFileUpdated(newSnapshot)
      },
      typescript: {
        extraFileExtensions: [{
          extension: "html",
          scriptKind: ts.ScriptKind.External,
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

const ngTranspiledTemplateCommand = "ngTranspiledTemplate";
const ngGetGeneratedElementTypeCommand = "ngGetGeneratedElementType";

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
  session.addProtocolHandler(ngTranspiledTemplateCommand, ngTranspiledTemplateHandler.bind(null, ts, session, projectService));
  session.addProtocolHandler(ngGetGeneratedElementTypeCommand, ngGetGeneratedElementTypeHandler.bind(null, ts, session, projectService));

  projectService.logger.info("Angular specific commands are successfully added.");
}

type TranspiledTemplateArguments = {
  file: string;
  transpiledContent: string;
  sourceCode: { [key: string]: string }
  mappings: Angular2TcbMappingInfo[];
}

type GetGeneratedElementTypeArguments = {
  file: string;
  startOffset: number,
  endOffset: number,
  forceReturnType: boolean,
}

const ngTranspiledTemplateHandler = (ts: typeof import('tsc-ide-plugin/tsserverlibrary.shim'),
                                     session: ts.server.Session,
                                     projectService: ts.server.ProjectService,
                                     request: ts.server.protocol.Request) => {

  const requestArguments = request.arguments as TranspiledTemplateArguments

  let fileName = ts.server.toNormalizedPath(requestArguments.file)
  let project = projectService.getDefaultProjectForFile(fileName, true)
  if (project) {
    project.getLanguageService().webStormNgUpdateTranspiledTemplate(
      ts, fileName, requestArguments.transpiledContent, requestArguments.sourceCode, requestArguments.mappings);

    // trigger reload
    (session as any).change(
      {
        file: fileName,
        line: 1,
        offset: 1,
        endLine: 1,
        endOffset: 1,
        insertString: ""
      })
  }
  return {
    responseRequired: true,
    response: {}
  }
};

const ngGetGeneratedElementTypeHandler = (ts: typeof import('tsc-ide-plugin/tsserverlibrary.shim'),
                                          _session: ts.server.Session,
                                          projectService: ts.server.ProjectService,
                                          request: ts.server.protocol.Request) => {

  const requestArguments = request.arguments as GetGeneratedElementTypeArguments

  let fileName = ts.server.toNormalizedPath(requestArguments.file)
  let project = projectService.getDefaultProjectForFile(fileName, true)
  return project?.getLanguageService()?.webStormNgGetGeneratedElementType(
      ts, fileName, requestArguments.startOffset, requestArguments.endOffset, requestArguments.forceReturnType)
    ?? {responseRequired: true, response: undefined}
};

const init = createLanguageServicePlugin(loadLanguagePlugins as any)
export = init
