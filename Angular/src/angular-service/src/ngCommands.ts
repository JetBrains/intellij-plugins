import type * as ts from "./tsserverlibrary.shim";
import {Angular2TcbMappingInfo} from "./mappings"
import {AngularTranspiledTemplate, buildAngularTranspiledTemplate} from "./code"

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
  ts: typeof import("./tsserverlibrary.shim"),
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
  startOffset: number,
  endOffset: number,
  forceReturnType: boolean,
}


export let ngTranspiledTemplates = new Map<string, AngularTranspiledTemplate>()

function ngTranspiledTemplateHandler(ts: typeof import('tsc-ide-plugin/tsserverlibrary.shim'),
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

  if (projectService.getScriptInfo(fileName)) {
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

function ngGetGeneratedElementTypeHandler(ts: typeof import('tsc-ide-plugin/tsserverlibrary.shim'),
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
    ts, fileName, requestArguments.startOffset, requestArguments.endOffset, requestArguments.forceReturnType, projectService.cancellationToken as any
  ) ?? {responseRequired: true, response: {}}
};