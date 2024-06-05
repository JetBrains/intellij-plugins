"use strict";
// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
const createLanguageServicePlugin_1 = require("@volar/typescript/lib/quickstart/createLanguageServicePlugin");
const code_1 = require("./code");
const decorateLanguageService_1 = require("./decorateLanguageService");
const utils_1 = require("./utils");
(0, decorateLanguageService_1.patchVolarAndDecorateLanguageService)();
function loadLanguagePlugins(ts, info) {
    addNgCommands(ts, info);
    let ngTcbBlocks = new Map();
    return [{
            getLanguageId(scriptId) {
                return scriptId.endsWith(".html") ? "html" : undefined;
            },
            isAssociatedFileOnly(_scriptId, languageId) {
                return languageId === "html";
            },
            createVirtualCode(scriptId, languageId, snapshot, ctx) {
                var _a;
                if (languageId === "typescript" && (0, utils_1.hasComponentDecorator)(ts, scriptId, snapshot, (_a = info.project.getCompilerOptions().target) !== null && _a !== void 0 ? _a : ts.ScriptTarget.Latest)) {
                    let virtualCode = ngTcbBlocks.get(scriptId);
                    if (!virtualCode) {
                        virtualCode = new code_1.AngularVirtualCode(scriptId, ctx, ts.sys.useCaseSensitiveFileNames);
                        ngTcbBlocks.set(scriptId, virtualCode);
                    }
                    return virtualCode.sourceFileUpdated(snapshot);
                }
                return undefined;
            },
            updateVirtualCode(scriptId, virtualCode, newSnapshot, ctx) {
                var _a;
                if ((0, utils_1.hasComponentDecorator)(ts, scriptId, newSnapshot, (_a = info.project.getCompilerOptions().target) !== null && _a !== void 0 ? _a : ts.ScriptTarget.Latest)) {
                    return virtualCode.sourceFileUpdated(newSnapshot);
                }
                else {
                    ngTcbBlocks.delete(scriptId);
                    return undefined;
                }
            },
            typescript: {
                extraFileExtensions: [{
                        extension: "html",
                        scriptKind: ts.ScriptKind.External,
                        isMixedContent: true,
                    }],
                getServiceScript(rootVirtualCode) {
                    return {
                        code: rootVirtualCode,
                        extension: ".ts",
                        scriptKind: ts.ScriptKind.TS,
                        preventLeadingOffset: true,
                    };
                }
            }
        }];
}
const ngTranspiledTemplateCommand = "ngTranspiledTemplate";
function addNgCommands(ts, info) {
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
    if (session.webStormNgCommandsAdded)
        return;
    session.webStormNgCommandsAdded = true;
    session.addProtocolHandler(ngTranspiledTemplateCommand, ngTranspiledTemplateHandler.bind(null, ts, session, projectService));
    projectService.logger.info("Angular specific commands are successfully added.");
}
const ngTranspiledTemplateHandler = (ts, session, projectService, request) => {
    const requestArguments = request.arguments;
    let fileName = ts.server.toNormalizedPath(requestArguments.file);
    let project = projectService.getDefaultProjectForFile(fileName, true);
    if (project) {
        project.getLanguageService().webStormNgUpdateTranspiledTemplate(ts, fileName, requestArguments.transpiledContent, requestArguments.sourceCode, requestArguments.mappings);
        // trigger reload
        session.change({
            file: fileName,
            line: 1,
            offset: 1,
            endLine: 1,
            endOffset: 1,
            insertString: ""
        });
    }
    return {
        responseRequired: true,
        response: {}
    };
};
const init = (0, createLanguageServicePlugin_1.createLanguageServicePlugin)(loadLanguagePlugins);
module.exports = init;
