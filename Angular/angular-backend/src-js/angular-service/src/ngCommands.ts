import type ts from "typescript/lib/tsserverlibrary"
import type {Range} from "tsc-ide-plugin/protocol"
import {Angular2TcbMappingInfo} from "./mappings"
import {AngularTranspiledTemplate, buildAngularTranspiledTemplate} from "./code"

type TypeScript = typeof ts

const customHandlers: {
  [K: string]: (TS: typeof ts,
                session: ts.server.Session,
                projectService: ts.server.ProjectService,
                request: ts.server.protocol.Request) => ts.server.HandlerResponse
} = {
  "ngTranspiledTemplate": ngTranspiledTemplateHandler,
  "ngGetGeneratedElementType": ngGetGeneratedElementTypeHandler,
}

export function registerProtocolHandlers(
  session: ts.server.Session,
  ts: TypeScript,
  projectService: ts.server.ProjectService,
) {
  for (let command in customHandlers) {
    session.addProtocolHandler(command, (request: ts.server.protocol.Request) => {
      try {
        return customHandlers[command](ts, session, projectService, request)
      }
      catch (e) {
        let ideErrorKind = (e as Error).ideKind
        if (ideErrorKind) {
          switch (ideErrorKind) {
            case "OperationCancelledException":
              return {
                responseRequired: true,
                response: {
                  cancelled: true
                }
              }
            default:
              return {
                responseRequired: true,
                response: {
                  error: ideErrorKind
                }
              }
          }
        }
        else {
          throw e
        }
      }
    });
  }
}


type TranspiledTemplateArguments = {
  file: string;
  transpiledContent: string;
  sourceCode: { [key: string]: string }
  mappings: Angular2TcbMappingInfo[];
}

type GetGeneratedElementTypeArguments = {
  file: string;
  projectFileName?: string;
  range: Range,
  forceReturnType: boolean,
}


export let ngTranspiledTemplates = new Map<string, AngularTranspiledTemplate>()

// The TS server resyncs a project's root files from its tsconfig on every reload
// (see ProjectService.updateNonInferredProjectFiles), which drops any root added via
// project.addRoot() that isn't part of the tsconfig's file list - including the
// template ".html" roots added below. This is called again from a patched
// Project.prototype.updateGraph (see patchProjectUpdateGraphToRestoreNgRoots) to
// re-add those roots after such a reload.
function restoreNgTranspiledTemplateRoots(ts: TypeScript, project: ts.server.Project): boolean {
  let restoredAny = false
  ngTranspiledTemplates.forEach((transpiledTemplate, fileName) => {
    if (!project.containsFile(ts.server.toNormalizedPath(fileName))) return
    transpiledTemplate.mappings?.forEach(mapping => {
      let mappingFileName = ts.server.toNormalizedPath(mapping.fileName)
      if (mappingFileName.endsWith(".html") && !project.containsFile(mappingFileName)) {
        let mappingScriptInfo = project.projectService.getOrCreateScriptInfoForNormalizedPath(
          mappingFileName,
          true, undefined, ts.ScriptKind.Unknown,
          false, undefined
        )
        if (mappingScriptInfo && !project.containsFile(mappingFileName)) {
          project.addRoot(mappingScriptInfo, mappingFileName)
          restoredAny = true
        }
      }
    });
  });
  return restoredAny
}

export function patchProjectUpdateGraphToRestoreNgRoots(ts: TypeScript) {
  const projectPrototype = ts.server.Project.prototype as any
  if (projectPrototype.webStormNgRootsRestorePatched) return
  projectPrototype.webStormNgRootsRestorePatched = true

  const originalUpdateGraph = projectPrototype.updateGraph
  projectPrototype.updateGraph = function (this: ts.server.Project, ...args: any[]) {
    const result = originalUpdateGraph.apply(this, args)
    if (restoreNgTranspiledTemplateRoots(ts, this)) {
      // addRoot() only marks the project dirty; fold the restored roots into the
      // program right away instead of waiting for the next unrelated update.
      originalUpdateGraph.apply(this, args)
    }
    return result
  }
}

function ngTranspiledTemplateHandler(ts: TypeScript,
                                     session: ts.server.Session,
                                     projectService: ts.server.ProjectService,
                                     request: ts.server.protocol.Request) {

  const requestArguments = request.arguments as TranspiledTemplateArguments

  let fileName = ts.server.toNormalizedPath(requestArguments.file)
  const transpiledTemplate = buildAngularTranspiledTemplate(ts, requestArguments.transpiledContent, requestArguments.sourceCode, requestArguments.mappings)
  if (transpiledTemplate)
    ngTranspiledTemplates.set(fileName, transpiledTemplate);
  else
    ngTranspiledTemplates.delete(fileName);

  let scriptInfo = projectService.getScriptInfo(fileName)
  if (scriptInfo) {
    scriptInfo.containingProjects?.forEach(project => restoreNgTranspiledTemplateRoots(ts, project));
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
    responseRequired: false,
  }
};

function ngGetGeneratedElementTypeHandler(ts: TypeScript,
                                          _session: ts.server.Session,
                                          projectService: ts.server.ProjectService,
                                          request: ts.server.protocol.Request) {

  const requestArguments = request.arguments as GetGeneratedElementTypeArguments

  let fileName = ts.server.toNormalizedPath(requestArguments.file)
  let projectFileName = requestArguments.projectFileName
    ? ts.server.toNormalizedPath(requestArguments.projectFileName)
    : undefined
  let project = (projectFileName ? projectService.findProject(projectFileName) : undefined)
    ?? projectService.getDefaultProjectForFile(fileName, true)
  return project?.getLanguageService()?.webStormNgGetGeneratedElementType(
    ts, fileName, requestArguments.range, requestArguments.forceReturnType, projectService.cancellationToken as any
  ) ?? {responseRequired: true, response: {}}
};