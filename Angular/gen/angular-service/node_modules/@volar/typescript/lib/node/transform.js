"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.toGeneratedOffsets = exports.toGeneratedOffset = exports.toSourceOffset = exports.transformTextSpan = exports.transformTextChange = exports.transformSpan = exports.transformDocumentSpan = exports.transformFileTextChanges = exports.fillSourceFileText = exports.transformDiagnostic = exports.transformCallHierarchyItem = void 0;
const language_core_1 = require("@volar/language-core");
const utils_1 = require("./utils");
const transformedDiagnostics = new WeakMap();
const transformedSourceFile = new WeakSet();
function transformCallHierarchyItem(language, item, filter) {
    const span = transformSpan(language, item.file, item.span, filter);
    const selectionSpan = transformSpan(language, item.file, item.selectionSpan, filter);
    return {
        ...item,
        span: span?.textSpan ?? { start: 0, length: 0 },
        selectionSpan: selectionSpan?.textSpan ?? { start: 0, length: 0 },
    };
}
exports.transformCallHierarchyItem = transformCallHierarchyItem;
function transformDiagnostic(language, diagnostic, isTsc) {
    if (!transformedDiagnostics.has(diagnostic)) {
        transformedDiagnostics.set(diagnostic, undefined);
        const { relatedInformation } = diagnostic;
        if (relatedInformation) {
            diagnostic.relatedInformation = relatedInformation
                .map(d => transformDiagnostic(language, d, isTsc))
                .filter(utils_1.notEmpty);
        }
        if (diagnostic.file !== undefined
            && diagnostic.start !== undefined
            && diagnostic.length !== undefined) {
            const [serviceScript, sourceScript, map] = (0, utils_1.getServiceScript)(language, diagnostic.file.fileName);
            if (serviceScript) {
                const sourceSpan = transformTextSpan(sourceScript, map, { start: diagnostic.start, length: diagnostic.length }, language_core_1.shouldReportDiagnostics);
                if (sourceSpan) {
                    if (isTsc) {
                        fillSourceFileText(language, diagnostic.file);
                    }
                    transformedDiagnostics.set(diagnostic, {
                        ...diagnostic,
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
    if (serviceScript) {
        sourceFile.text = sourceScript.snapshot.getText(0, sourceScript.snapshot.getLength())
            + sourceFile.text.substring(sourceScript.snapshot.getLength());
    }
}
exports.fillSourceFileText = fillSourceFileText;
function transformFileTextChanges(language, changes, filter) {
    const [_, source] = (0, utils_1.getServiceScript)(language, changes.fileName);
    if (source) {
        return {
            ...changes,
            textChanges: changes.textChanges.map(c => {
                const span = transformSpan(language, changes.fileName, c.span, filter);
                if (span) {
                    return {
                        ...c,
                        span: span.textSpan,
                    };
                }
            }).filter(utils_1.notEmpty),
        };
    }
    else {
        return changes;
    }
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
    const [virtualFile, sourceScript, map] = (0, utils_1.getServiceScript)(language, fileName);
    if (virtualFile) {
        const sourceSpan = transformTextSpan(sourceScript, map, textSpan, filter);
        if (sourceSpan) {
            return {
                fileName,
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
function transformTextChange(sourceScript, map, textChange, filter) {
    const sourceSpan = transformTextSpan(sourceScript, map, textChange.span, filter);
    if (sourceSpan) {
        return {
            newText: textChange.newText,
            span: sourceSpan,
        };
    }
}
exports.transformTextChange = transformTextChange;
function transformTextSpan(sourceScript, map, textSpan, filter) {
    const start = textSpan.start;
    const end = textSpan.start + textSpan.length;
    const sourceStart = toSourceOffset(sourceScript, map, start, filter);
    const sourceEnd = toSourceOffset(sourceScript, map, end, filter);
    if (sourceStart !== undefined && sourceEnd !== undefined && sourceEnd >= sourceStart) {
        return {
            start: sourceStart,
            length: sourceEnd - sourceStart,
        };
    }
}
exports.transformTextSpan = transformTextSpan;
function toSourceOffset(sourceScript, map, position, filter) {
    for (const [sourceOffset, mapping] of map.getSourceOffsets(position - sourceScript.snapshot.getLength())) {
        if (filter(mapping.data)) {
            return sourceOffset;
        }
    }
}
exports.toSourceOffset = toSourceOffset;
function toGeneratedOffset(sourceScript, map, position, filter) {
    for (const [generateOffset, mapping] of map.getGeneratedOffsets(position)) {
        if (filter(mapping.data)) {
            return generateOffset + sourceScript.snapshot.getLength();
        }
    }
}
exports.toGeneratedOffset = toGeneratedOffset;
function* toGeneratedOffsets(sourceScript, map, position) {
    for (const [generateOffset, mapping] of map.getGeneratedOffsets(position)) {
        yield [generateOffset + sourceScript.snapshot.getLength(), mapping];
    }
}
exports.toGeneratedOffsets = toGeneratedOffsets;
//# sourceMappingURL=transform.js.map