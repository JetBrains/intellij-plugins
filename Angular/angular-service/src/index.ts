// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import type * as ts from "typescript/lib/tsserverlibrary";
import {createLanguageServicePlugin} from "@volar/typescript/lib/quickstart/createLanguageServicePlugin"
import {CodeMapping, LanguagePlugin, ServiceScript} from "@volar/language-core"
import {AngularVirtualCode} from "./code"
import {patchVolarToDecorateLanguageService} from "./decorateLanguageService"

const tcbBlocks = new Map<string, AngularVirtualCode>()

interface AngularLanguagePlugin extends LanguagePlugin<AngularVirtualCode> {
}

patchVolarToDecorateLanguageService()

function loadLanguagePlugins(ts: typeof import('typescript'),
                             info: ts.server.PluginCreateInfo): AngularLanguagePlugin[] {
  if (!addedCommands) {
    addNewCommands(ts, info)
    addedCommands = true
  }
  return [{
    getLanguageId(scriptId: string): string | undefined {
      return scriptId.endsWith(".html") ? "ng-html" : undefined
    },
    createVirtualCode(scriptId: string, languageId: string, snapshot: ts.IScriptSnapshot): AngularVirtualCode | undefined {
      if (languageId !== "ng-html") return undefined
      let virtualCode = tcbBlocks.get(scriptId)
      if (!virtualCode) {
        virtualCode = new AngularVirtualCode("ng-html")
        tcbBlocks.set(scriptId, virtualCode)
      }
      return virtualCode // tcbBlocks.get(scriptId)?.checkUpdate(snapshot, languageId)
    },
    updateVirtualCode(_scriptId: string, virtualCode: AngularVirtualCode, newSnapshot: ts.IScriptSnapshot): AngularVirtualCode | undefined {
      return virtualCode//?.checkUpdate(newSnapshot)
    },
    typescript: {
      extraFileExtensions: [],
      getServiceScript(rootVirtualCode: AngularVirtualCode): ServiceScript | undefined {
        return {
          code: rootVirtualCode,
          extension: ".ts",
          scriptKind: ts.ScriptKind.TS
        }
      }
    }
  }]
}

const ngTranspiledTemplateCommand = "ngTranspiledTemplate";

let addedCommands = false;

function addNewCommands(ts: typeof import('typescript'), info: ts.server.PluginCreateInfo) {
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

  session.addProtocolHandler(ngTranspiledTemplateCommand, ngTranspiledTemplateHandler.bind(null, ts, projectService));

  projectService.logger.info("Angular specific commands are successfully added.");
}

type TranspiledTemplateArguments = {
  file: string;
  content?: string;
  transpiledContent?: string;
  mappings: CodeMapping[];
}

const ngTranspiledTemplateHandler = (_ts: typeof import('typescript'),
                                     _projectService: ts.server.ProjectService,
                                     request: ts.server.protocol.Request) => {

  const requestArguments = request.arguments as TranspiledTemplateArguments

  const fileName = requestArguments.file

  let virtualCode: AngularVirtualCode | undefined = tcbBlocks.get(fileName)
  if (virtualCode === undefined) {
    virtualCode = new AngularVirtualCode("ng-html")
    tcbBlocks.set(fileName, virtualCode);
  }

  virtualCode.update(requestArguments.content, requestArguments.transpiledContent, requestArguments.mappings);

  return {
    responseRequired: true,
    response: {}
  }
};

const init = createLanguageServicePlugin(loadLanguagePlugins)
export = init
