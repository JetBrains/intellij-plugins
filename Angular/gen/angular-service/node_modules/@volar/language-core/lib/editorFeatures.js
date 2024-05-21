"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.resolveRenameEditText = exports.resolveRenameNewName = exports.shouldReportDiagnostics = exports.isSignatureHelpEnabled = exports.isAutoInsertEnabled = exports.isCompletionEnabled = exports.isFormattingEnabled = exports.isCodeActionsEnabled = exports.isDiagnosticsEnabled = exports.isDocumentLinkEnabled = exports.isColorEnabled = exports.isLinkedEditingEnabled = exports.isSelectionRangesEnabled = exports.isFoldingRangesEnabled = exports.isSymbolsEnabled = exports.isHighlightEnabled = exports.isImplementationEnabled = exports.isReferencesEnabled = exports.isTypeDefinitionEnabled = exports.isDefinitionEnabled = exports.isRenameEnabled = exports.isCallHierarchyEnabled = exports.isSemanticTokensEnabled = exports.isCodeLensEnabled = exports.isInlayHintsEnabled = exports.isHoverEnabled = void 0;
function isHoverEnabled(info) {
    return !!info.semantic;
}
exports.isHoverEnabled = isHoverEnabled;
function isInlayHintsEnabled(info) {
    return !!info.semantic;
}
exports.isInlayHintsEnabled = isInlayHintsEnabled;
function isCodeLensEnabled(info) {
    return !!info.semantic;
}
exports.isCodeLensEnabled = isCodeLensEnabled;
function isSemanticTokensEnabled(info) {
    return typeof info.semantic === 'object'
        ? info.semantic.shouldHighlight?.() ?? true
        : !!info.semantic;
}
exports.isSemanticTokensEnabled = isSemanticTokensEnabled;
function isCallHierarchyEnabled(info) {
    return !!info.navigation;
}
exports.isCallHierarchyEnabled = isCallHierarchyEnabled;
function isRenameEnabled(info) {
    return typeof info.navigation === 'object'
        ? info.navigation.shouldRename?.() ?? true
        : !!info.navigation;
}
exports.isRenameEnabled = isRenameEnabled;
function isDefinitionEnabled(info) {
    return !!info.navigation;
}
exports.isDefinitionEnabled = isDefinitionEnabled;
function isTypeDefinitionEnabled(info) {
    return !!info.navigation;
}
exports.isTypeDefinitionEnabled = isTypeDefinitionEnabled;
function isReferencesEnabled(info) {
    return !!info.navigation;
}
exports.isReferencesEnabled = isReferencesEnabled;
function isImplementationEnabled(info) {
    return !!info.navigation;
}
exports.isImplementationEnabled = isImplementationEnabled;
function isHighlightEnabled(info) {
    return !!info.navigation;
}
exports.isHighlightEnabled = isHighlightEnabled;
function isSymbolsEnabled(info) {
    return !!info.structure;
}
exports.isSymbolsEnabled = isSymbolsEnabled;
function isFoldingRangesEnabled(info) {
    return !!info.structure;
}
exports.isFoldingRangesEnabled = isFoldingRangesEnabled;
function isSelectionRangesEnabled(info) {
    return !!info.structure;
}
exports.isSelectionRangesEnabled = isSelectionRangesEnabled;
function isLinkedEditingEnabled(info) {
    return !!info.structure;
}
exports.isLinkedEditingEnabled = isLinkedEditingEnabled;
function isColorEnabled(info) {
    return !!info.structure;
}
exports.isColorEnabled = isColorEnabled;
function isDocumentLinkEnabled(info) {
    return !!info.structure;
}
exports.isDocumentLinkEnabled = isDocumentLinkEnabled;
function isDiagnosticsEnabled(info) {
    return !!info.verification;
}
exports.isDiagnosticsEnabled = isDiagnosticsEnabled;
function isCodeActionsEnabled(info) {
    return !!info.verification;
}
exports.isCodeActionsEnabled = isCodeActionsEnabled;
function isFormattingEnabled(info) {
    return !!info.format;
}
exports.isFormattingEnabled = isFormattingEnabled;
function isCompletionEnabled(info) {
    return !!info.completion;
}
exports.isCompletionEnabled = isCompletionEnabled;
function isAutoInsertEnabled(info) {
    return !!info.completion;
}
exports.isAutoInsertEnabled = isAutoInsertEnabled;
function isSignatureHelpEnabled(info) {
    return !!info.completion;
}
exports.isSignatureHelpEnabled = isSignatureHelpEnabled;
// should...
function shouldReportDiagnostics(info) {
    return typeof info.verification === 'object'
        ? info.verification.shouldReport?.() ?? true
        : !!info.verification;
}
exports.shouldReportDiagnostics = shouldReportDiagnostics;
//  resolve...
function resolveRenameNewName(newName, info) {
    return typeof info.navigation === 'object'
        ? info.navigation.resolveRenameNewName?.(newName) ?? newName
        : newName;
}
exports.resolveRenameNewName = resolveRenameNewName;
function resolveRenameEditText(text, info) {
    return typeof info.navigation === 'object'
        ? info.navigation.resolveRenameEditText?.(text) ?? text
        : text;
}
exports.resolveRenameEditText = resolveRenameEditText;
//# sourceMappingURL=editorFeatures.js.map