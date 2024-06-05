"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.decorateLanguageService = void 0;
const language_core_1 = require("@volar/language-core");
const dedupe_1 = require("./dedupe");
const transform_1 = require("./transform");
const utils_1 = require("./utils");
const windowsPathReg = /\\/g;
function decorateLanguageService(language, languageService, caseSensitiveFileNames) {
    // ignored methods
    const { getNavigationTree, getOutliningSpans, } = languageService;
    languageService.getNavigationTree = filePath => {
        const fileName = filePath.replace(windowsPathReg, '/');
        const [serviceScript, sourceScript] = (0, utils_1.getServiceScript)(language, fileName);
        if (serviceScript || sourceScript?.associatedOnly) {
            const tree = getNavigationTree(sourceScript.id);
            tree.childItems = undefined;
            return tree;
        }
        else {
            return getNavigationTree(fileName);
        }
    };
    languageService.getOutliningSpans = filePath => {
        const fileName = filePath.replace(windowsPathReg, '/');
        const [serviceScript, sourceScript] = (0, utils_1.getServiceScript)(language, fileName);
        if (serviceScript || sourceScript?.associatedOnly) {
            return [];
        }
        else {
            return getOutliningSpans(fileName);
        }
    };
    // methods
    const { findReferences, findRenameLocations, getCompletionEntryDetails, getCompletionsAtPosition, getDefinitionAndBoundSpan, getDefinitionAtPosition, getFileReferences, getFormattingEditsForDocument, getFormattingEditsForRange, getFormattingEditsAfterKeystroke, getImplementationAtPosition, getLinkedEditingRangeAtPosition, getQuickInfoAtPosition, getSignatureHelpItems, getReferencesAtPosition, getSemanticDiagnostics, getSyntacticDiagnostics, getSuggestionDiagnostics, getTypeDefinitionAtPosition, getEncodedSemanticClassifications, getDocumentHighlights, getApplicableRefactors, getEditsForFileRename, getEditsForRefactor, getRenameInfo, getCodeFixesAtPosition, prepareCallHierarchy, provideCallHierarchyIncomingCalls, provideCallHierarchyOutgoingCalls, provideInlayHints, organizeImports, } = languageService;
    languageService.getFormattingEditsForDocument = (filePath, options) => {
        const fileName = filePath.replace(windowsPathReg, '/');
        const [serviceScript, sourceScript, map] = (0, utils_1.getServiceScript)(language, fileName);
        if (sourceScript?.associatedOnly) {
            return [];
        }
        if (serviceScript) {
            if (!map.mappings.some(mapping => (0, language_core_1.isFormattingEnabled)(mapping.data))) {
                return [];
            }
            const edits = getFormattingEditsForDocument(sourceScript.id, options);
            return edits
                .map(edit => takeIfSameName(fileName, (0, transform_1.transformTextChange)(serviceScript, sourceScript, map, edit, language_core_1.isFormattingEnabled)))
                .filter(utils_1.notEmpty);
        }
        else {
            return getFormattingEditsForDocument(fileName, options);
        }
    };
    languageService.getFormattingEditsForRange = (filePath, start, end, options) => {
        const fileName = filePath.replace(windowsPathReg, '/');
        const [serviceScript, sourceScript, map] = (0, utils_1.getServiceScript)(language, fileName);
        if (sourceScript?.associatedOnly) {
            return [];
        }
        if (serviceScript) {
            const generateStart = (0, transform_1.toGeneratedOffset)(serviceScript, sourceScript, map, start, language_core_1.isFormattingEnabled);
            const generateEnd = (0, transform_1.toGeneratedOffset)(serviceScript, sourceScript, map, end, language_core_1.isFormattingEnabled);
            if (generateStart !== undefined && generateEnd !== undefined) {
                const edits = getFormattingEditsForRange(sourceScript.id, generateStart, generateEnd, options);
                return edits
                    .map(edit => takeIfSameName(fileName, (0, transform_1.transformTextChange)(serviceScript, sourceScript, map, edit, language_core_1.isFormattingEnabled)))
                    .filter(utils_1.notEmpty);
            }
            return [];
        }
        else {
            return getFormattingEditsForRange(fileName, start, end, options);
        }
    };
    languageService.getFormattingEditsAfterKeystroke = (filePath, position, key, options) => {
        const fileName = filePath.replace(windowsPathReg, '/');
        const [serviceScript, sourceScript, map] = (0, utils_1.getServiceScript)(language, fileName);
        if (sourceScript?.associatedOnly) {
            return [];
        }
        if (serviceScript) {
            const generatePosition = (0, transform_1.toGeneratedOffset)(serviceScript, sourceScript, map, position, language_core_1.isFormattingEnabled);
            if (generatePosition !== undefined) {
                const edits = getFormattingEditsAfterKeystroke(sourceScript.id, generatePosition, key, options);
                return edits
                    .map(edit => takeIfSameName(fileName, (0, transform_1.transformTextChange)(serviceScript, sourceScript, map, edit, language_core_1.isFormattingEnabled)))
                    .filter(utils_1.notEmpty);
            }
            return [];
        }
        else {
            return getFormattingEditsAfterKeystroke(fileName, position, key, options);
        }
    };
    languageService.getEditsForFileRename = (oldFilePath, newFilePath, formatOptions, preferences) => {
        const edits = getEditsForFileRename(oldFilePath, newFilePath, formatOptions, preferences);
        return (0, transform_1.transformFileTextChanges)(language, edits, language_core_1.isRenameEnabled);
    };
    languageService.getLinkedEditingRangeAtPosition = (filePath, position) => {
        const fileName = filePath.replace(windowsPathReg, '/');
        const [serviceScript, sourceScript, map] = (0, utils_1.getServiceScript)(language, fileName);
        if (sourceScript?.associatedOnly) {
            return undefined;
        }
        if (serviceScript) {
            const generatePosition = (0, transform_1.toGeneratedOffset)(serviceScript, sourceScript, map, position, language_core_1.isLinkedEditingEnabled);
            if (generatePosition !== undefined) {
                const info = getLinkedEditingRangeAtPosition(sourceScript.id, generatePosition);
                if (info) {
                    return {
                        ranges: info.ranges
                            .map(span => takeIfSameName(fileName, (0, transform_1.transformTextSpan)(serviceScript, sourceScript, map, span, language_core_1.isLinkedEditingEnabled)))
                            .filter(utils_1.notEmpty),
                        wordPattern: info.wordPattern,
                    };
                }
            }
        }
        else {
            return getLinkedEditingRangeAtPosition(fileName, position);
        }
    };
    languageService.prepareCallHierarchy = (filePath, position) => {
        const fileName = filePath.replace(windowsPathReg, '/');
        const [serviceScript, sourceScript, map] = (0, utils_1.getServiceScript)(language, fileName);
        if (sourceScript?.associatedOnly) {
            return undefined;
        }
        if (serviceScript) {
            const generatePosition = (0, transform_1.toGeneratedOffset)(serviceScript, sourceScript, map, position, language_core_1.isCallHierarchyEnabled);
            if (generatePosition !== undefined) {
                const item = prepareCallHierarchy(sourceScript.id, generatePosition);
                if (Array.isArray(item)) {
                    return item.map(item => (0, transform_1.transformCallHierarchyItem)(language, item, language_core_1.isCallHierarchyEnabled));
                }
                else if (item) {
                    return (0, transform_1.transformCallHierarchyItem)(language, item, language_core_1.isCallHierarchyEnabled);
                }
            }
        }
        else {
            return prepareCallHierarchy(fileName, position);
        }
    };
    languageService.provideCallHierarchyIncomingCalls = (filePath, position) => {
        let calls = [];
        const fileName = filePath.replace(windowsPathReg, '/');
        const [serviceScript, sourceScript, map] = (0, utils_1.getServiceScript)(language, fileName);
        if (sourceScript?.associatedOnly) {
            return [];
        }
        if (serviceScript) {
            const generatePosition = (0, transform_1.toGeneratedOffset)(serviceScript, sourceScript, map, position, language_core_1.isCallHierarchyEnabled);
            if (generatePosition !== undefined) {
                calls = provideCallHierarchyIncomingCalls(sourceScript.id, generatePosition);
            }
        }
        else {
            calls = provideCallHierarchyIncomingCalls(fileName, position);
        }
        return calls
            .map(call => {
            const from = (0, transform_1.transformCallHierarchyItem)(language, call.from, language_core_1.isCallHierarchyEnabled);
            const fromSpans = call.fromSpans
                .map(span => (0, transform_1.transformSpan)(language, call.from.file, span, language_core_1.isCallHierarchyEnabled)?.textSpan)
                .filter(utils_1.notEmpty);
            return {
                from,
                fromSpans,
            };
        });
    };
    languageService.provideCallHierarchyOutgoingCalls = (filePath, position) => {
        let calls = [];
        const fileName = filePath.replace(windowsPathReg, '/');
        const [serviceScript, sourceScript, map] = (0, utils_1.getServiceScript)(language, fileName);
        if (sourceScript?.associatedOnly) {
            return [];
        }
        if (serviceScript) {
            const generatePosition = (0, transform_1.toGeneratedOffset)(serviceScript, sourceScript, map, position, language_core_1.isCallHierarchyEnabled);
            if (generatePosition !== undefined) {
                calls = provideCallHierarchyOutgoingCalls(fileName, generatePosition);
            }
        }
        else {
            calls = provideCallHierarchyOutgoingCalls(fileName, position);
        }
        return calls
            .map(call => {
            const to = (0, transform_1.transformCallHierarchyItem)(language, call.to, language_core_1.isCallHierarchyEnabled);
            const fromSpans = call.fromSpans
                .map(span => serviceScript
                ? takeIfSameName(fileName, (0, transform_1.transformTextSpan)(serviceScript, sourceScript, map, span, language_core_1.isCallHierarchyEnabled))
                : span)
                .filter(utils_1.notEmpty);
            return {
                to,
                fromSpans,
            };
        });
    };
    languageService.organizeImports = (args, formatOptions, preferences) => {
        const unresolved = organizeImports(args, formatOptions, preferences);
        return (0, transform_1.transformFileTextChanges)(language, unresolved, language_core_1.isCodeActionsEnabled);
    };
    languageService.getQuickInfoAtPosition = (filePath, position) => {
        const fileName = filePath.replace(windowsPathReg, '/');
        const [serviceScript, sourceScript, map] = (0, utils_1.getServiceScript)(language, fileName);
        if (sourceScript?.associatedOnly) {
            return undefined;
        }
        if (serviceScript) {
            const infos = [];
            for (const [generatePosition, mapping] of (0, transform_1.toGeneratedOffsets)(serviceScript, sourceScript, map, position)) {
                if (!(0, language_core_1.isHoverEnabled)(mapping.data)) {
                    continue;
                }
                const info = getQuickInfoAtPosition(sourceScript.id, generatePosition);
                if (info) {
                    const textSpan = takeIfSameName(fileName, (0, transform_1.transformTextSpan)(serviceScript, sourceScript, map, info.textSpan, language_core_1.isHoverEnabled));
                    if (textSpan) {
                        infos.push({
                            ...info,
                            textSpan: textSpan,
                        });
                    }
                }
            }
            if (infos.length === 1) {
                return infos[0];
            }
            else if (infos.length >= 2) {
                const combine = { ...infos[0] };
                combine.displayParts = combine.displayParts?.slice();
                combine.documentation = combine.documentation?.slice();
                combine.tags = combine.tags?.slice();
                const displayPartsStrs = new Set([displayPartsToString(infos[0].displayParts)]);
                const documentationStrs = new Set([displayPartsToString(infos[0].documentation)]);
                const tagsStrs = new Set();
                for (const tag of infos[0].tags ?? []) {
                    tagsStrs.add(tag.name + '__volar__' + displayPartsToString(tag.text));
                }
                for (let i = 1; i < infos.length; i++) {
                    const { displayParts, documentation, tags } = infos[i];
                    if (displayParts?.length && !displayPartsStrs.has(displayPartsToString(displayParts))) {
                        displayPartsStrs.add(displayPartsToString(displayParts));
                        combine.displayParts ??= [];
                        combine.displayParts.push({ ...displayParts[0], text: '\n\n' + displayParts[0].text });
                        combine.displayParts.push(...displayParts.slice(1));
                    }
                    if (documentation?.length && !documentationStrs.has(displayPartsToString(documentation))) {
                        documentationStrs.add(displayPartsToString(documentation));
                        combine.documentation ??= [];
                        combine.documentation.push({ ...documentation[0], text: '\n\n' + documentation[0].text });
                        combine.documentation.push(...documentation.slice(1));
                    }
                    for (const tag of tags ?? []) {
                        if (!tagsStrs.has(tag.name + '__volar__' + displayPartsToString(tag.text))) {
                            tagsStrs.add(tag.name + '__volar__' + displayPartsToString(tag.text));
                            combine.tags ??= [];
                            combine.tags.push(tag);
                        }
                    }
                }
                return combine;
            }
        }
        else {
            return getQuickInfoAtPosition(fileName, position);
        }
    };
    languageService.getSignatureHelpItems = (filePath, position, options) => {
        const fileName = filePath.replace(windowsPathReg, '/');
        const [serviceScript, sourceScript, map] = (0, utils_1.getServiceScript)(language, fileName);
        if (sourceScript?.associatedOnly) {
            return undefined;
        }
        if (serviceScript) {
            const generatePosition = (0, transform_1.toGeneratedOffset)(serviceScript, sourceScript, map, position, language_core_1.isSignatureHelpEnabled);
            if (generatePosition !== undefined) {
                const result = getSignatureHelpItems(sourceScript.id, generatePosition, options);
                if (result) {
                    const applicableSpan = takeIfSameName(fileName, (0, transform_1.transformTextSpan)(serviceScript, sourceScript, map, result.applicableSpan, language_core_1.isSignatureHelpEnabled));
                    if (applicableSpan) {
                        return {
                            ...result,
                            applicableSpan,
                        };
                    }
                }
            }
        }
        else {
            return getSignatureHelpItems(fileName, position, options);
        }
    };
    languageService.getDocumentHighlights = (filePath, position, filesToSearch) => {
        const fileName = filePath.replace(windowsPathReg, '/');
        const unresolved = linkedCodeFeatureWorker(fileName, position, language_core_1.isHighlightEnabled, (fileName, position) => getDocumentHighlights(fileName, position, filesToSearch), function* (result) {
            for (const ref of result) {
                for (const reference of ref.highlightSpans) {
                    yield [reference.fileName ?? ref.fileName, reference.textSpan.start];
                }
            }
        });
        const resolved = unresolved
            .flat()
            .map(highlights => {
            return {
                ...highlights,
                highlightSpans: highlights.highlightSpans
                    .map(span => {
                    const { fileName: spanFileName, textSpan } = (0, transform_1.transformSpan)(language, span.fileName ?? highlights.fileName, span.textSpan, language_core_1.isHighlightEnabled) ?? {};
                    if (textSpan && sameName(spanFileName, fileName)) {
                        return {
                            ...span,
                            contextSpan: (0, transform_1.transformSpan)(language, span.fileName ?? highlights.fileName, span.contextSpan, language_core_1.isHighlightEnabled)?.textSpan,
                            textSpan,
                        };
                    }
                })
                    .filter(utils_1.notEmpty),
            };
        });
        return resolved;
    };
    languageService.getApplicableRefactors = (filePath, positionOrRange, preferences, triggerReason, kind, includeInteractiveActions) => {
        const fileName = filePath.replace(windowsPathReg, '/');
        const [serviceScript, sourceScript, map] = (0, utils_1.getServiceScript)(language, fileName);
        if (sourceScript?.associatedOnly) {
            return [];
        }
        if (serviceScript) {
            const generatePosition = (0, transform_1.toGeneratedOffset)(serviceScript, sourceScript, map, typeof positionOrRange === 'number' ? positionOrRange : positionOrRange.pos, language_core_1.isCodeActionsEnabled);
            if (generatePosition !== undefined) {
                const por = typeof positionOrRange === 'number'
                    ? generatePosition
                    : {
                        pos: generatePosition,
                        end: generatePosition + positionOrRange.end - positionOrRange.pos,
                    };
                return getApplicableRefactors(sourceScript.id, por, preferences, triggerReason, kind, includeInteractiveActions);
            }
            return [];
        }
        else {
            return getApplicableRefactors(fileName, positionOrRange, preferences, triggerReason, kind, includeInteractiveActions);
        }
    };
    languageService.getEditsForRefactor = (filePath, formatOptions, positionOrRange, refactorName, actionName, preferences) => {
        let edits;
        const fileName = filePath.replace(windowsPathReg, '/');
        const [serviceScript, sourceScript, map] = (0, utils_1.getServiceScript)(language, fileName);
        if (sourceScript?.associatedOnly) {
            return undefined;
        }
        if (serviceScript) {
            const generatePosition = (0, transform_1.toGeneratedOffset)(serviceScript, sourceScript, map, typeof positionOrRange === 'number'
                ? positionOrRange
                : positionOrRange.pos, language_core_1.isCodeActionsEnabled);
            if (generatePosition !== undefined) {
                const por = typeof positionOrRange === 'number'
                    ? generatePosition
                    : {
                        pos: generatePosition,
                        end: generatePosition + positionOrRange.end - positionOrRange.pos,
                    };
                edits = getEditsForRefactor(sourceScript.id, formatOptions, por, refactorName, actionName, preferences);
            }
        }
        else {
            edits = getEditsForRefactor(fileName, formatOptions, positionOrRange, refactorName, actionName, preferences);
        }
        if (edits) {
            edits.edits = (0, transform_1.transformFileTextChanges)(language, edits.edits, language_core_1.isCodeActionsEnabled);
            return edits;
        }
    };
    languageService.getRenameInfo = (filePath, position, options) => {
        const fileName = filePath.replace(windowsPathReg, '/');
        const [serviceScript, sourceScript, map] = (0, utils_1.getServiceScript)(language, fileName);
        if (sourceScript?.associatedOnly) {
            return {
                canRename: false,
                localizedErrorMessage: "Cannot rename"
            };
        }
        if (serviceScript) {
            let failed;
            for (const [generateOffset, mapping] of (0, transform_1.toGeneratedOffsets)(serviceScript, sourceScript, map, position)) {
                if (!(0, language_core_1.isRenameEnabled)(mapping.data)) {
                    continue;
                }
                const info = getRenameInfo(sourceScript.id, generateOffset, options);
                if (info.canRename) {
                    const span = takeIfSameName(fileName, (0, transform_1.transformTextSpan)(serviceScript, sourceScript, map, info.triggerSpan, language_core_1.isRenameEnabled));
                    if (span) {
                        info.triggerSpan = span;
                        return info;
                    }
                }
                else {
                    failed = info;
                }
            }
            if (failed) {
                return failed;
            }
            return {
                canRename: false,
                localizedErrorMessage: 'Failed to get rename locations',
            };
        }
        else {
            return getRenameInfo(fileName, position, options);
        }
    };
    languageService.getCodeFixesAtPosition = (filePath, start, end, errorCodes, formatOptions, preferences) => {
        let fixes = [];
        const fileName = filePath.replace(windowsPathReg, '/');
        const [serviceScript, sourceScript, map] = (0, utils_1.getServiceScript)(language, fileName);
        if (sourceScript?.associatedOnly) {
            return [];
        }
        if (serviceScript) {
            const generateStart = (0, transform_1.toGeneratedOffset)(serviceScript, sourceScript, map, start, language_core_1.isCodeActionsEnabled);
            const generateEnd = (0, transform_1.toGeneratedOffset)(serviceScript, sourceScript, map, end, language_core_1.isCodeActionsEnabled);
            if (generateStart !== undefined && generateEnd !== undefined) {
                fixes = getCodeFixesAtPosition(sourceScript.id, generateStart, generateEnd, errorCodes, formatOptions, preferences);
            }
        }
        else {
            fixes = getCodeFixesAtPosition(fileName, start, end, errorCodes, formatOptions, preferences);
        }
        fixes = fixes.map(fix => {
            fix.changes = (0, transform_1.transformFileTextChanges)(language, fix.changes, language_core_1.isCodeActionsEnabled);
            return fix;
        });
        return fixes;
    };
    languageService.getEncodedSemanticClassifications = (filePath, span, format) => {
        const fileName = filePath.replace(windowsPathReg, '/');
        const [serviceScript, sourceScript, map] = (0, utils_1.getServiceScript)(language, fileName);
        if (sourceScript?.associatedOnly) {
            return {
                spans: [],
                endOfLineState: 0
            };
        }
        if (serviceScript) {
            let start;
            let end;
            for (const mapping of map.mappings) {
                // TODO reuse the logic from language service
                if ((0, language_core_1.isSemanticTokensEnabled)(mapping.data) && mapping.sourceOffsets[0] >= span.start && mapping.sourceOffsets[0] <= span.start + span.length) {
                    start ??= mapping.generatedOffsets[0];
                    end ??= mapping.generatedOffsets[mapping.generatedOffsets.length - 1] + (mapping.generatedLengths ?? mapping.lengths)[mapping.lengths.length - 1];
                    start = Math.min(start, mapping.generatedOffsets[0]);
                    end = Math.max(end, mapping.generatedOffsets[mapping.generatedOffsets.length - 1] + (mapping.generatedLengths ?? mapping.lengths)[mapping.lengths.length - 1]);
                }
            }
            start ??= 0;
            end ??= sourceScript.snapshot.getLength();
            const mappingOffset = (0, transform_1.getMappingOffset)(serviceScript, sourceScript);
            start += mappingOffset;
            end += mappingOffset;
            const result = getEncodedSemanticClassifications(sourceScript.id, { start, length: end - start }, format);
            const spans = [];
            for (let i = 0; i < result.spans.length; i += 3) {
                const sourceStart = takeIfSameName(fileName, (0, transform_1.toSourceOffset)(serviceScript, sourceScript, map, result.spans[i], language_core_1.isSemanticTokensEnabled));
                const sourceEnd = takeIfSameName(fileName, (0, transform_1.toSourceOffset)(serviceScript, sourceScript, map, result.spans[i] + result.spans[i + 1], language_core_1.isSemanticTokensEnabled));
                if (sourceStart !== undefined && sourceEnd !== undefined) {
                    spans.push(sourceStart, sourceEnd - sourceStart, result.spans[i + 2]);
                }
            }
            result.spans = spans;
            return result;
        }
        else {
            return getEncodedSemanticClassifications(fileName, span, format);
        }
    };
    languageService.getSyntacticDiagnostics = filePath => {
        const fileName = filePath.replace(windowsPathReg, '/');
        const [_serviceScript, sourceScript] = (0, utils_1.getServiceScript)(language, fileName);
        if (sourceScript?.associatedOnly) {
            return [];
        }
        return (0, transform_1.transformAndFilterDiagnostics)(getSyntacticDiagnostics(sourceScript?.id ?? fileName), language, fileName, languageService.getProgram(), false);
    };
    languageService.getSemanticDiagnostics = filePath => {
        const fileName = filePath.replace(windowsPathReg, '/');
        const [_serviceScript, sourceScript] = (0, utils_1.getServiceScript)(language, fileName);
        if (sourceScript?.associatedOnly) {
            return [];
        }
        return (0, transform_1.transformAndFilterDiagnostics)(getSemanticDiagnostics(sourceScript?.id ?? fileName), language, fileName, languageService.getProgram(), false);
    };
    languageService.getSuggestionDiagnostics = filePath => {
        const fileName = filePath.replace(windowsPathReg, '/');
        const [_serviceScript, sourceScript] = (0, utils_1.getServiceScript)(language, fileName);
        if (sourceScript?.associatedOnly) {
            return [];
        }
        return (0, transform_1.transformAndFilterDiagnostics)(getSuggestionDiagnostics(sourceScript?.id ?? fileName), language, fileName, languageService.getProgram(), false);
    };
    languageService.getDefinitionAndBoundSpan = (filePath, position) => {
        const fileName = filePath.replace(windowsPathReg, '/');
        const unresolved = linkedCodeFeatureWorker(fileName, position, language_core_1.isDefinitionEnabled, (fileName, position) => getDefinitionAndBoundSpan(fileName, position), function* (result) {
            for (const ref of result.definitions ?? []) {
                yield [ref.fileName, ref.textSpan.start];
            }
        });
        const textSpan = unresolved
            .map(s => (0, transform_1.transformSpan)(language, fileName, s.textSpan, language_core_1.isDefinitionEnabled)?.textSpan)
            .filter(utils_1.notEmpty)[0];
        if (!textSpan) {
            return;
        }
        const definitions = unresolved
            .map(s => s.definitions
            ?.map(s => (0, transform_1.transformDocumentSpan)(language, s, language_core_1.isDefinitionEnabled, s.fileName !== fileName))
            .filter(utils_1.notEmpty))
            .filter(utils_1.notEmpty)
            .flat();
        return {
            textSpan,
            definitions: (0, dedupe_1.dedupeDocumentSpans)(definitions),
        };
    };
    languageService.findReferences = (filePath, position) => {
        const fileName = filePath.replace(windowsPathReg, '/');
        const unresolved = linkedCodeFeatureWorker(fileName, position, language_core_1.isReferencesEnabled, (fileName, position) => findReferences(fileName, position), function* (result) {
            for (const ref of result) {
                for (const reference of ref.references) {
                    yield [reference.fileName, reference.textSpan.start];
                }
            }
        });
        const resolved = unresolved
            .flat()
            .map(symbol => {
            const definition = (0, transform_1.transformDocumentSpan)(language, symbol.definition, language_core_1.isDefinitionEnabled, true);
            return {
                definition,
                references: symbol.references
                    .map(r => (0, transform_1.transformDocumentSpan)(language, r, language_core_1.isReferencesEnabled))
                    .filter(utils_1.notEmpty),
            };
        });
        return resolved;
    };
    languageService.getDefinitionAtPosition = (filePath, position) => {
        const fileName = filePath.replace(windowsPathReg, '/');
        const unresolved = linkedCodeFeatureWorker(fileName, position, language_core_1.isDefinitionEnabled, (fileName, position) => getDefinitionAtPosition(fileName, position), function* (result) {
            for (const ref of result) {
                yield [ref.fileName, ref.textSpan.start];
            }
        });
        const resolved = unresolved
            .flat()
            .map(s => (0, transform_1.transformDocumentSpan)(language, s, language_core_1.isDefinitionEnabled, s.fileName !== fileName))
            .filter(utils_1.notEmpty);
        return (0, dedupe_1.dedupeDocumentSpans)(resolved);
    };
    languageService.getTypeDefinitionAtPosition = (filePath, position) => {
        const fileName = filePath.replace(windowsPathReg, '/');
        const unresolved = linkedCodeFeatureWorker(fileName, position, language_core_1.isTypeDefinitionEnabled, (fileName, position) => getTypeDefinitionAtPosition(fileName, position), function* (result) {
            for (const ref of result) {
                yield [ref.fileName, ref.textSpan.start];
            }
        });
        const resolved = unresolved
            .flat()
            .map(s => (0, transform_1.transformDocumentSpan)(language, s, language_core_1.isTypeDefinitionEnabled))
            .filter(utils_1.notEmpty);
        return (0, dedupe_1.dedupeDocumentSpans)(resolved);
    };
    languageService.getImplementationAtPosition = (filePath, position) => {
        const fileName = filePath.replace(windowsPathReg, '/');
        const unresolved = linkedCodeFeatureWorker(fileName, position, language_core_1.isImplementationEnabled, (fileName, position) => getImplementationAtPosition(fileName, position), function* (result) {
            for (const ref of result) {
                yield [ref.fileName, ref.textSpan.start];
            }
        });
        const resolved = unresolved
            .flat()
            .map(s => (0, transform_1.transformDocumentSpan)(language, s, language_core_1.isImplementationEnabled))
            .filter(utils_1.notEmpty);
        return (0, dedupe_1.dedupeDocumentSpans)(resolved);
    };
    languageService.findRenameLocations = (filePath, position, findInStrings, findInComments, preferences) => {
        const fileName = filePath.replace(windowsPathReg, '/');
        const unresolved = linkedCodeFeatureWorker(fileName, position, language_core_1.isRenameEnabled, (fileName, position) => findRenameLocations(fileName, position, findInStrings, findInComments, preferences), function* (result) {
            for (const ref of result) {
                yield [ref.fileName, ref.textSpan.start];
            }
        });
        const resolved = unresolved
            .flat()
            .map(s => (0, transform_1.transformDocumentSpan)(language, s, language_core_1.isRenameEnabled))
            .filter(utils_1.notEmpty);
        return (0, dedupe_1.dedupeDocumentSpans)(resolved);
    };
    languageService.getReferencesAtPosition = (filePath, position) => {
        const fileName = filePath.replace(windowsPathReg, '/');
        const unresolved = linkedCodeFeatureWorker(fileName, position, language_core_1.isReferencesEnabled, (fileName, position) => getReferencesAtPosition(fileName, position), function* (result) {
            for (const ref of result) {
                yield [ref.fileName, ref.textSpan.start];
            }
        });
        const resolved = unresolved
            .flat()
            .map(s => (0, transform_1.transformDocumentSpan)(language, s, language_core_1.isReferencesEnabled))
            .filter(utils_1.notEmpty);
        return (0, dedupe_1.dedupeDocumentSpans)(resolved);
    };
    languageService.getCompletionsAtPosition = (filePath, position, options, formattingSettings) => {
        const fileName = filePath.replace(windowsPathReg, '/');
        const [serviceScript, sourceScript, map] = (0, utils_1.getServiceScript)(language, fileName);
        if (sourceScript?.associatedOnly) {
            return undefined;
        }
        if (serviceScript) {
            const results = [];
            for (const [generatedOffset, mapping] of (0, transform_1.toGeneratedOffsets)(serviceScript, sourceScript, map, position)) {
                if (!(0, language_core_1.isCompletionEnabled)(mapping.data)) {
                    continue;
                }
                const result = getCompletionsAtPosition(sourceScript.id, generatedOffset, options, formattingSettings);
                if (!result) {
                    continue;
                }
                if (typeof mapping.data.completion === 'object' && mapping.data.completion.onlyImport) {
                    result.entries = result.entries.filter(entry => !!entry.sourceDisplay);
                }
                for (const entry of result.entries) {
                    entry.replacementSpan = entry.replacementSpan && takeIfSameName(fileName, (0, transform_1.transformTextSpan)(serviceScript, sourceScript, map, entry.replacementSpan, language_core_1.isCompletionEnabled));
                }
                result.optionalReplacementSpan = result.optionalReplacementSpan
                    && takeIfSameName(fileName, (0, transform_1.transformTextSpan)(serviceScript, sourceScript, map, result.optionalReplacementSpan, language_core_1.isCompletionEnabled));
                const isAdditional = typeof mapping.data.completion === 'object' && mapping.data.completion.isAdditional;
                if (isAdditional) {
                    results.push(result);
                }
                else {
                    results.unshift(result);
                }
            }
            if (results.length) {
                return {
                    ...results[0],
                    entries: results
                        .map(additionalResult => additionalResult.entries)
                        .flat(),
                };
            }
        }
        else {
            return getCompletionsAtPosition(fileName, position, options, formattingSettings);
        }
    };
    languageService.getCompletionEntryDetails = (filePath, position, entryName, formatOptions, source, preferences, data) => {
        let details;
        const fileName = filePath.replace(windowsPathReg, '/');
        const [serviceScript, sourceScript, map] = (0, utils_1.getServiceScript)(language, fileName);
        if (sourceScript?.associatedOnly) {
            return undefined;
        }
        if (serviceScript) {
            const generatePosition = (0, transform_1.toGeneratedOffset)(serviceScript, sourceScript, map, position, language_core_1.isCompletionEnabled);
            if (generatePosition !== undefined) {
                details = getCompletionEntryDetails(sourceScript.id, generatePosition, entryName, formatOptions, source, preferences, data);
            }
        }
        else {
            return getCompletionEntryDetails(fileName, position, entryName, formatOptions, source, preferences, data);
        }
        if (details?.codeActions) {
            for (const codeAction of details.codeActions) {
                codeAction.changes = (0, transform_1.transformFileTextChanges)(language, codeAction.changes, language_core_1.isCompletionEnabled);
            }
        }
        return details;
    };
    languageService.provideInlayHints = (filePath, span, preferences) => {
        const fileName = filePath.replace(windowsPathReg, '/');
        const [serviceScript, sourceScript, map] = (0, utils_1.getServiceScript)(language, fileName);
        if (sourceScript?.associatedOnly) {
            return [];
        }
        if (serviceScript) {
            let start;
            let end;
            for (const mapping of map.mappings) {
                if ((0, language_core_1.isInlayHintsEnabled)(mapping.data) && mapping.sourceOffsets[0] >= span.start && mapping.sourceOffsets[0] <= span.start + span.length) {
                    start ??= mapping.generatedOffsets[0];
                    end ??= mapping.generatedOffsets[mapping.generatedOffsets.length - 1];
                    start = Math.min(start, mapping.generatedOffsets[0]);
                    end = Math.max(end, mapping.generatedOffsets[mapping.generatedOffsets.length - 1]);
                }
            }
            if (start === undefined || end === undefined) {
                start = 0;
                end = 0;
            }
            const mappingOffset = (0, transform_1.getMappingOffset)(serviceScript, sourceScript);
            start += mappingOffset;
            end += mappingOffset;
            const result = provideInlayHints(sourceScript.id, { start, length: end - start }, preferences);
            const hints = [];
            for (const hint of result) {
                const sourcePosition = takeIfSameName(fileName, (0, transform_1.toSourceOffset)(serviceScript, sourceScript, map, hint.position, language_core_1.isInlayHintsEnabled));
                if (sourcePosition !== undefined) {
                    hints.push({
                        ...hint,
                        position: sourcePosition,
                    });
                }
            }
            return hints;
        }
        else {
            return provideInlayHints(fileName, span, preferences);
        }
    };
    languageService.getFileReferences = filePath => {
        const fileName = filePath.replace(windowsPathReg, '/');
        const unresolved = getFileReferences(fileName);
        const resolved = unresolved
            .map(s => (0, transform_1.transformDocumentSpan)(language, s, language_core_1.isReferencesEnabled))
            .filter(utils_1.notEmpty);
        return (0, dedupe_1.dedupeDocumentSpans)(resolved);
    };
    function linkedCodeFeatureWorker(fileName, position, filter, worker, getLinkedCodes) {
        const results = [];
        const processedFilePositions = new Set();
        const [serviceScript, sourceScript, map] = (0, utils_1.getServiceScript)(language, fileName);
        if (serviceScript) {
            for (const [generatedOffset, mapping] of map.getGeneratedOffsets(position)) {
                if (filter(mapping.data)) {
                    process(sourceScript.id, generatedOffset + (0, transform_1.getMappingOffset)(serviceScript, sourceScript));
                }
            }
        }
        else {
            process(fileName, position);
        }
        return results;
        function process(fileName, position) {
            if (processedFilePositions.has(fileName + ':' + position)) {
                return;
            }
            processedFilePositions.add(fileName + ':' + position);
            const result = worker(fileName, position);
            if (!result) {
                return;
            }
            results.push(result);
            for (const ref of getLinkedCodes(result)) {
                processedFilePositions.add(ref[0] + ':' + ref[1]);
                const [serviceScript, sourceScript] = (0, utils_1.getServiceScript)(language, ref[0]);
                if (!serviceScript) {
                    continue;
                }
                const linkedCodeMap = language.linkedCodeMaps.get(serviceScript.code);
                if (!linkedCodeMap) {
                    continue;
                }
                const mappingOffset = (0, transform_1.getMappingOffset)(serviceScript, sourceScript);
                for (const linkedCodeOffset of linkedCodeMap.getLinkedOffsets(ref[1] - mappingOffset)) {
                    process(ref[0], linkedCodeOffset + mappingOffset);
                }
            }
        }
    }
    function normalizeId(id) {
        return caseSensitiveFileNames ? id : id.toLowerCase();
    }
    function sameName(name1, name2) {
        return !!name1 && !!name2 && normalizeId(name1) === normalizeId(name2);
    }
    function takeIfSameName(name, value) {
        if (value && sameName(name, value[0])) {
            return value[1];
        }
        return undefined;
    }
}
exports.decorateLanguageService = decorateLanguageService;
function displayPartsToString(displayParts) {
    if (displayParts) {
        return displayParts.map(displayPart => displayPart.text).join('');
    }
    return '';
}
//# sourceMappingURL=decorateLanguageService.js.map