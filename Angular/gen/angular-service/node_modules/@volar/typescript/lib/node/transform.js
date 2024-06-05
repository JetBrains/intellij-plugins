"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.getMappingOffset = exports.toGeneratedOffsets = exports.toGeneratedOffset = exports.toSourceOffset = exports.transformTextSpan = exports.transformTextChange = exports.transformSpan = exports.transformDocumentSpan = exports.transformFileTextChanges = exports.fillSourceFileText = exports.transformDiagnostic = exports.transformAndFilterDiagnostics = exports.transformCallHierarchyItem = void 0;
const language_core_1 = require("@volar/language-core");
const utils_1 = require("./utils");
const transformedDiagnostics = new WeakMap();
const transformedSourceFile = new WeakSet();
function transformCallHierarchyItem(language, item, filter) {
    const span = transformSpan(language, item.file, item.span, filter);
    const selectionSpan = transformSpan(language, item.file, item.selectionSpan, filter);
    return {
        ...item,
        file: span?.fileName ?? item.file,
        span: span?.textSpan ?? { start: 0, length: 0 },
        selectionSpan: selectionSpan?.textSpan ?? { start: 0, length: 0 },
    };
}
exports.transformCallHierarchyItem = transformCallHierarchyItem;
function transformAndFilterDiagnostics(diagnostics, language, fileName, program, isTsc) {
    return diagnostics.map(d => transformDiagnostic(language, d, program, isTsc))
        .filter(d => d?.file?.fileName == fileName)
        .filter(utils_1.notEmpty);
}
exports.transformAndFilterDiagnostics = transformAndFilterDiagnostics;
function transformDiagnostic(language, diagnostic, program, isTsc) {
    if (!transformedDiagnostics.has(diagnostic)) {
        transformedDiagnostics.set(diagnostic, undefined);
        const { relatedInformation } = diagnostic;
        if (relatedInformation) {
            diagnostic.relatedInformation = relatedInformation
                .map(d => transformDiagnostic(language, d, program, isTsc))
                .filter(utils_1.notEmpty);
        }
        if (diagnostic.file !== undefined
            && diagnostic.start !== undefined
            && diagnostic.length !== undefined) {
            const [serviceScript, sourceScript, map] = (0, utils_1.getServiceScript)(language, diagnostic.file.fileName);
            if (serviceScript) {
                const [sourceSpanFileName, sourceSpan] = transformTextSpan(serviceScript, sourceScript, map, {
                    start: diagnostic.start,
                    length: diagnostic.length
                }, language_core_1.shouldReportDiagnostics) ?? [];
                const actualDiagnosticFile = sourceSpanFileName
                    ? diagnostic.file.fileName === sourceSpanFileName
                        ? diagnostic.file
                        : program?.getSourceFile(sourceSpanFileName)
                    : undefined;
                if (sourceSpan && actualDiagnosticFile) {
                    if (isTsc) {
                        fillSourceFileText(language, diagnostic.file);
                    }
                    transformedDiagnostics.set(diagnostic, {
                        ...diagnostic,
                        file: actualDiagnosticFile,
                        start: sourceSpan.start,
                        length: sourceSpan.length,
                    });
                }
            }
            else {
                transformedDiagnostics.set(diagnostic, diagnostic);
            }
        }
        else {
            transformedDiagnostics.set(diagnostic, diagnostic);
        }
    }
    return transformedDiagnostics.get(diagnostic);
}
exports.transformDiagnostic = transformDiagnostic;
// fix https://github.com/vuejs/language-tools/issues/4099 without `incremental`
function fillSourceFileText(language, sourceFile) {
    if (transformedSourceFile.has(sourceFile)) {
        return;
    }
    transformedSourceFile.add(sourceFile);
    const [serviceScript, sourceScript] = (0, utils_1.getServiceScript)(language, sourceFile.fileName);
    if (serviceScript && !serviceScript.preventLeadingOffset) {
        sourceFile.text = sourceScript.snapshot.getText(0, sourceScript.snapshot.getLength())
            + sourceFile.text.substring(sourceScript.snapshot.getLength());
    }
}
exports.fillSourceFileText = fillSourceFileText;
function transformFileTextChanges(language, changes, filter) {
    const changesPerFile = {};
    const newFiles = new Set();
    for (const fileChanges of changes) {
        const [_, source] = (0, utils_1.getServiceScript)(language, fileChanges.fileName);
        if (source) {
            fileChanges.textChanges.forEach(c => {
                const { fileName, textSpan } = transformSpan(language, fileChanges.fileName, c.span, filter) ?? {};
                if (fileName && textSpan) {
                    (changesPerFile[fileName] ?? (changesPerFile[fileName] = [])).push({ ...c, span: textSpan });
                }
            });
        }
        else {
            const list = (changesPerFile[fileChanges.fileName] ?? (changesPerFile[fileChanges.fileName] = []));
            fileChanges.textChanges.forEach(c => {
                list.push(c);
            });
            if (fileChanges.isNewFile) {
                newFiles.add(fileChanges.fileName);
            }
        }
    }
    const result = [];
    for (const fileName in changesPerFile) {
        result.push({
            fileName,
            isNewFile: newFiles.has(fileName),
            textChanges: changesPerFile[fileName]
        });
    }
    return result;
}
exports.transformFileTextChanges = transformFileTextChanges;
function transformDocumentSpan(language, documentSpan, filter, shouldFallback) {
    let textSpan = transformSpan(language, documentSpan.fileName, documentSpan.textSpan, filter);
    if (!textSpan && shouldFallback) {
        textSpan = {
            fileName: documentSpan.fileName,
            textSpan: { start: 0, length: 0 },
        };
    }
    if (!textSpan) {
        return;
    }
    const contextSpan = transformSpan(language, documentSpan.fileName, documentSpan.contextSpan, filter);
    const originalTextSpan = transformSpan(language, documentSpan.originalFileName, documentSpan.originalTextSpan, filter);
    const originalContextSpan = transformSpan(language, documentSpan.originalFileName, documentSpan.originalContextSpan, filter);
    return {
        ...documentSpan,
        fileName: textSpan.fileName,
        textSpan: textSpan.textSpan,
        contextSpan: contextSpan?.textSpan,
        originalFileName: originalTextSpan?.fileName,
        originalTextSpan: originalTextSpan?.textSpan,
        originalContextSpan: originalContextSpan?.textSpan,
    };
}
exports.transformDocumentSpan = transformDocumentSpan;
function transformSpan(language, fileName, textSpan, filter) {
    if (!fileName || !textSpan) {
        return;
    }
    const [serviceScript, sourceScript, map] = (0, utils_1.getServiceScript)(language, fileName);
    if (sourceScript?.associatedOnly) {
        return;
    }
    else if (serviceScript) {
        const [sourceSpanFileName, sourceSpan] = transformTextSpan(serviceScript, sourceScript, map, textSpan, filter) ?? [];
        if (sourceSpan && sourceSpanFileName) {
            return {
                fileName: sourceSpanFileName,
                textSpan: sourceSpan,
            };
        }
    }
    else {
        return {
            fileName,
            textSpan,
        };
    }
}
exports.transformSpan = transformSpan;
function transformTextChange(serviceScript, sourceScript, map, textChange, filter) {
    const [sourceSpanFileName, sourceSpan] = transformTextSpan(serviceScript, sourceScript, map, textChange.span, filter) ?? [];
    if (sourceSpan && sourceSpanFileName) {
        return [sourceSpanFileName, {
                newText: textChange.newText,
                span: sourceSpan,
            }];
    }
    return undefined;
}
exports.transformTextChange = transformTextChange;
function transformTextSpan(serviceScript, sourceScript, map, textSpan, filter) {
    const start = textSpan.start;
    const end = textSpan.start + textSpan.length;
    const [idStart, sourceStart] = toSourceOffset(serviceScript, sourceScript, map, start, filter) ?? [];
    const [idEnd, sourceEnd] = toSourceOffset(serviceScript, sourceScript, map, end, filter) ?? [];
    if (idStart === idEnd && idStart !== undefined
        && sourceStart !== undefined && sourceEnd !== undefined && sourceEnd >= sourceStart) {
        return [idStart, {
                start: sourceStart,
                length: sourceEnd - sourceStart,
            }];
    }
    return undefined;
}
exports.transformTextSpan = transformTextSpan;
function toSourceOffset(serviceScript, sourceScript, map, position, filter) {
    for (const [sourceOffset, mapping] of map.getSourceOffsets(position - getMappingOffset(serviceScript, sourceScript))) {
        if (filter(mapping.data)) {
            return [mapping.source ?? sourceScript.id, sourceOffset];
        }
    }
    return undefined;
}
exports.toSourceOffset = toSourceOffset;
function toGeneratedOffset(serviceScript, sourceScript, map, position, filter) {
    for (const [generateOffset, mapping] of map.getGeneratedOffsets(position)) {
        if (filter(mapping.data)) {
            return generateOffset + getMappingOffset(serviceScript, sourceScript);
        }
    }
}
exports.toGeneratedOffset = toGeneratedOffset;
function* toGeneratedOffsets(serviceScript, sourceScript, map, position) {
    for (const [generateOffset, mapping] of map.getGeneratedOffsets(position)) {
        yield [generateOffset + getMappingOffset(serviceScript, sourceScript), mapping];
    }
}
exports.toGeneratedOffsets = toGeneratedOffsets;
function getMappingOffset(serviceScript, sourceScript) {
    return !serviceScript.preventLeadingOffset
        ? sourceScript.snapshot.getLength()
        : 0;
}
exports.getMappingOffset = getMappingOffset;
//# sourceMappingURL=transform.js.map